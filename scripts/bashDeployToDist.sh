#!/bin/bash -e

# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

# This script assumes it is being run from the project root directory.
# From the project directory run: scripts/<this script>.sh
# This is generic in that it does not assume a POM file and does not use Maven.
# It does us Git.

#  Input Parameters:
#  \$1 = project.artifactId
#  \$2 = GitHub Tag 
#    For example:  $ scripts/<this script>.sh datasketches-memory 0.12.3-incubator

echo
echo "===================Check List======================"
echo "1. Verify that you can successfully read and write to the dist directories using SVN."
echo
echo "2. Verify that your GPG keys have been created and stored in:"
echo " - https://dist.apache.org/repos/dist/dev/incubator/datasketches/KEYS  AND"
echo " - https://dist.apache.org/repos/dist/release/incubator/datasketches/KEYS"
echo
echo "3. Run all Unit Tests, Strict Profiles, CheckStyle, SpotBugs, Clover, etc."
echo
echo "4. Locally create a release branch with a name of one of the following forms:"
echo "     1.0.X"
echo "     1.0.X-incubating"
echo
echo "5. Locally create a tag with a name of one of the following forms:"
echo "     1.0.0"
echo "     1.0.0-SNAPSHOTNN"
echo "     1.0.0-RCNN"
echo "     1.0.0-incubating"
echo "     1.0.0-incubating-SNAPSHOTNN"
echo "     1.0.0-incubating-RCNN"
echo
echo "   'SNAPSHOT', if relevant, is always capitalized."
echo "   Note: SNAPSHOT deployments are never relevant for the 'dist/release' branch."
echo
echo "   Commit the branch and tag back to the remote origin"
echo
echo "6. Verify that your local GitHub repository current and the git status is clean."
echo
echo "Proceed? [y|N]"; read confirm; if [[ $confirm != "y" ]]; then echo "Please rerun this script when ready."; exit 1; fi

TMP=$(git status --porcelain)
if [[ -n $TMP ]];
then
  echo "git status --porcelain:  $TMP"
  echo "WARNING!!! Your GitHub repo is not clean!"
  echo
  # exit 1  #Don't exit for now
fi


TIME=$(date -u +%Y%m%d.%H%M%S)
BASE=$(pwd)
echo
echo "DateTime: $TIME"

echo
echo "## Load GPG Agent:"
eval $(gpg-agent --daemon) > /dev/null

## Extract project.artifactId and project.version from input parameters:

ProjectArtifactId=$1
Tag=$2

# Determine the type of release / deployment we are dealing with
#  and adjust the target directories and file version to be used in the file name accordingly.

Release=false
Snapshot=false
ReleaseCandidate=false
FileVersion=
LeafDir=

### SNAPSHOT ?
if [[ $Tag =~ .*-SNAPSHOT[0-9]* ]]; 
then
  echo
  echo "This Tag is for a SNAPSHOT."
  echo "Proceed? [y|N]"; read confirm; if [[ $confirm != "y" ]]; then echo "Please rerun this script when ready."; exit 1; fi
  Snapshot=true
  FileVersion=${Tag%-SNAPSHOT}-$TIME # Remove SNAPSHOT, add date-time
  LeafDir=$Tag
else
  ### RELEASE CANDIDATE ?
  if [[ $Tag =~ .*-[rR][cC][0-9][0-9]* ]];
  then
    echo
    echo "This Tag is for a Release Candidate."
    echo "Proceed? [y|N]"; read confirm; if [[ $confirm != "y" ]]; then echo "Please rerun this script when ready."; exit 1; fi
    ReleaseCandidate=true
    RCSubStr=$(expr "$Tag" : '.*\(-[rR][cC][0-9]*\)')
    FileVersion=${Tag%$RCSubStr}
    LeafDir="$Tag"
  else
    echo "Please confirm that this the Final Release of $ProjectArtifactId : $Tag"
    echo "Proceed? [y|N]"; read confirm; if [[ $confirm != "y" ]]; then echo "Please rerun this script when ready."; exit 1; fi
    Release=true
    FileVersion="$Tag"
    LeafDir=$Tag
  fi
fi


# extract the SubDir name, e.g., "memory" from the artifactId
SubDir=$(expr "$ProjectArtifactId" : 'datasketches-\([a-z]*\)')

# Are we still incubating?
if [[ $Tag =~ .*-incubating.* ]]
then
  Incubating=true
  Incubator="incubator/" #part of file path
else
  Incubating=false
  Incubator=""
fi

# Set up the paths

FilesPath=${SubDir}/${LeafDir}
LocalPath="target/assy-tmp/dist"
RemotePath="https://dist.apache.org/repos/dist"
if ( $Release )
then
  LocalSvnBasePath="$LocalPath"/release/"$Incubator"datasketches
  RemoteSvnBasePath="$RemotePath"/release/"$Incubator"datasketches
else
  LocalSvnBasePath="$LocalPath"/dev/"$Incubator"datasketches
  RemoteSvnBasePath="$RemotePath"/dev/"$Incubator"datasketches
fi

LocalFilesPath="$LocalSvnBasePath"/"$FilesPath"

# Create target/assy-tmp dir if it doesn't exist
mkdir -p target
cd target
rm -rf assy-tmp
mkdir -p assy-tmp/dist
cd ../

ZipName=apache-${ProjectArtifactId}-${FileVersion}-src.zip

echo
echo "===========SUMMARY OF INPUT PARAMETERS============="
echo "ProjectArtifactId    : $ProjectArtifactId"
echo "Tag                  : $Tag"
echo "Incubating           : $Incubating"
echo "File Version String  : $FileVersion"
echo "Target ZIP File Name : $ZipName"
echo "Target Leaf Dir      : $LeafDir"
echo "SubDir               : $SubDir"
echo "FilesPath            : $FilesPath"
echo "LocalSvnBasePath     : $LocalSvnBasePath"
echo "RemoteSvnBasePath    : $RemoteSvnBasePath"
echo "LocalFilesPath       : $LocalFilesPath"

echo
echo "Please confirm if the above is correct."
echo "Proceed? [y|N]"; read confirm; if [[ $confirm != "y" ]]; then echo "Please rerun this script when ready."; exit 1; fi

# move to base path and checkout
mkdir -p $LocalSvnBasePath

cd $LocalSvnBasePath
svn co $RemoteSvnBasePath .
cd $BASE

if [ -d "$LocalFilesPath" ] && [ ! $Snapshot ]; 
then
  echo
  echo "ERROR!!! $LocalFilesPath already exists."
  echo
  exit 1
fi

echo
echo "Is the SVN Checkout without conflicts?"
echo "Proceed? [y|N]"; read confirm; if [[ $confirm != "y" ]]; then echo "Please rerun this script when ready."; exit 1; fi

#make the leaf directories
mkdir -p $LocalFilesPath

echo
echo "## Zip:"

# ZIP
#scripts/createZip.sh $ZipName $LocalFilesPath
git archive --output="$LocalFilesPath"/$ZipName $Tag

echo
echo "Is the Zip file correct?"
echo "Proceed? [y|N]"; read confirm; if [[ $confirm != "y" ]]; then echo "Please rerun this script when ready."; exit 1; fi

cd $LocalFilesPath #for signing

if [ ! -f "$ZipName" ]; then 
  echo
  echo " !!! ERROR: $ZipName file does not exist"
  echo
  exit 1;
fi

echo " * ZIP File = $ZipName"

echo
echo "## GPG Sign"

ASC=${ZipName}.asc
gpg -ab "$ZipName"

if [ ! -f ${ASC} ]; then 
  echo
  echo " !!! ERROR: ${ASC} file does not exist"
  exit 1;
fi
echo " * ASC File = ${ASC}"

echo
echo "## GPG Verify"
gpg --verify "$ASC" "$ZipName"

echo
echo "## SHA512 sign"

SHA512=${ZipName}.sha512
shasum -a 512 "$ZipName" >> "$SHA512"

if [ ! -f "$SHA512" ]; then 
  echo
  echo " !!! ERROR: .sha512 file does not exist"
  exit 1;
fi
echo " * SHA512 file = $SHA512"

echo
echo "## SHA512 Check:"
shasum -a 512 -c $SHA512

cd $BASE

echo 
echo "=================DEPLOY TO DIST===================="
echo
echo "Proceed? [y|N]"; read confirm; if [[ $confirm != "y" ]]; then echo "Please rerun this script when ready."; exit 1; fi

echo
cd $LocalSvnBasePath
svn add --force .
svn ci -m "Deploy $FileVersion to DIST"

TMP=$(svn status)
if [[ -n $TMP ]];
then
  echo "svn status:  $TMP"
  echo "ERROR!!! Your svn status is not clean!"
  echo
  exit 1
fi

echo
echo "Is the remote dist directory structure and content OK?"
echo "Proceed? [y|N]"; read confirm; if [[ $confirm != "y" ]]; 
then 
  echo "Please rerun this script when ready."; 
  echo "You may need to manually delete files from the dist repo"
  exit 1; 
fi

echo
echo "# DEPLOYMENT SUCCESS"
echo

