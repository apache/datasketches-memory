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

# This assumes it is being executed from the project root directory.
# From the project directory run: scripts/assembleSignVerify.sh
# This is generic in that it does not assume a POM file and does not use Maven.


echo
echo "================Bash Deploy to Dist================"
echo " This script must be run from the base directory of the project."
echo " This script obtains the 'project.artifactId' and the 'project.version' from the \$1 and \$2 input parameters "
echo "   For example:  $ scripts/bashDeployToDist.sh datasketches-memory 0.12.3-incubator"
echo
echo "===========The Dist Deployment Structure==========="
echo "The DataSketche base directories in https://dist.apache.org/repos/dist/ are:"
echo " - dev/incubator/datasketches/"
echo " - release/incubator/datasketches/"
echo
echo " Each of these two directories contain a KEYS file, which is where you must have your GPG public key stored."
echo
echo "After graduation the base directories will be the same but without the incubator level."
echo
echo "For both the dev and the release branches, the root contains sub-directories for each of "
echo "the datasketches-X repositories as follows:"
echo " - characterization/"
echo " - core/"
echo " - cpp/"
echo " - hive/"
echo " - memory/"
echo " - pig/"
echo " - postgresql/"
echo " - vector/"
echo
echo "Below these sub=directories is a level of leaf directories that define a particular release or release candidate."
echo
echo "Finally the leaf directories contain the zip and signature files for a release or release candidate."
echo "The full tree will look something like this example:"
echo "  dist/"
echo "    dev/"
echo "      incubator/"
echo "        datasketches/"
echo "          KEYS"
echo "          memory/"
echo "            0.12.3-incubating-RC1/"
echo "              apache-datasketches-memory-0.12.3-incubating-src.zip"
echo "              apache-datasketches-memory-0.12.3-incubating-src.zip.asc"
echo "              apache-datasketches-memory-0.12.3-incubating-src.zip.sha512"
echo "          etc."
echo
echo "===================Check List======================"
echo "1. Verify that you can successfully read and write to the dist directories using SVN."
echo
echo "2. Verify that your GPG keys have been created and stored in:"
echo " - https://dist.apache.org/repos/dist/dev/incubator/datasketches/KEYS  AND"
echo " - https://dist.apache.org/repos/dist/release/incubator/datasketches/KEYS"
echo
echo "3. Verify the POM project.version format is one of:"
echo "     X.Y.Z"
echo "     X.Y.Z-SNAPSHOT"
echo "     X.Y.Z-incubator"
echo "     X.Y.Z-incubator-SNAPSHOT"
echo
echo "   'SNAPSHOT', if relevant, is always at the end of the version string and capitalized."
echo "   Note: SNAPSHOT deployments are never relevant for the 'release' branch."
echo
echo "4. Verify that your local GitHub repository current and the git status is clean."
echo
echo "5. Locally checkout the branch or tag that you want to deploy."
echo
echo "Proceed? [y|N]"; read confirm; if [[ $confirm != "y" ]]; then echo "Please rerun this script when ready."; exit; fi

TMP=$(git status --porcelain)
if [[ -n $TMP ]];
then
  echo "git status --porcelain:  $TMP"
  echo "ERROR!!! Your GitHub repo is not clean!"
  echo
  exit 1
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
ProjectVersion=$2

# Determine the type of release / deployment we are dealing with
#  and adjust the target directories and file version to be used in the file name accordingly.

Release=false
Snapshot=false
ReleaseCandidate=false
RCNUM=
ReleaseType=
FileVersion=
LeafDir=

if [[ $ProjectVersion =~ .*-SNAPSHOT  ]]; 
then
  echo
  echo "This version is a SNAPSHOT. Do you still want to deploy?"
  read confirm
  if [[ $confirm != "y" ]]; 
  then
    echo "Please correct the version string and rerun this script"
    echo
    exit
  fi
  Snapshot=true
  ReleaseType="SNAPSHOT"
  FileVersion=${ProjectVersion%-SNAPSHOT}-$TIME # Remove SNAPSHOT, add date-time
  LeafDir=$ProjectVersion
  #continue
else # NOT a SNAPSHOT
  Snapshot=false
  echo
  echo "Is this a Release Candidate? [y|N]"
  read confirm
  if [[ $confirm != "y" ]]; 
  then # NOT ReleaseCandidate, could be Final Release
    ReleaseCandidate=false
    echo "Please confirm that this the Final Release of $ProjectArtifactId : $ProjectVersion ? [y|N]"
    read confirm
    if [[ $confirm != "y" ]];
    then # NOT Final Release either, bail out
      Release=false
      echo "Please correct the input and rerun this script"
      echo
      exit
      
    else # Final Release
      Release=true
      ReleaseType="Release"
      FileVersion="$ProjectVersion"
      LeafDir=$ProjectVersion
      #continue
    fi
  else # ReleaseCandidate
    ReleaseCandidate=true
    echo "What is the Release Candidate Number? NNN" 
    read RCNUM   
    ReleaseType="Release Candidate: RC$RCNUM"
    FileVersion="$ProjectVersion"
    LeafDir="$ProjectVersion-RC$RCNUM"
    #continue
  fi
fi



# extract the SubDir name, e.g., "memory" from the artifactId
SubDir=$(expr "$ProjectArtifactId" : 'datasketches-\([a-z]*\)')

# Are we still incubating?
if [[ $ProjectVersion =~ .*-incubating.* ]]
then
  Incubating=true
  Incubator="incubator/"
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
echo "Project Version      : $ProjectVersion"
echo "Release Type         : $ReleaseType"
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
echo "Please confirm if the above is correct: [y|N]"
read confirm
if [[ $confirm != "y" ]]; 
then
  echo "Please correct the input and rerun this script"
  echo
  exit
fi

# move to base path and checkout
mkdir -p $LocalSvnBasePath

cd $LocalSvnBasePath
svn co $RemoteSvnBasePath .
cd $BASE

if [ -d "$LocalFilesPath" && ! $Snapshot ]; 
then
  echo
  echo "ERROR!!! $LocalFilesPath already exists."
  echo
  exit 1
fi

echo
echo "Is the SVN Checkout without conflicts? [y|N]"
read confirm
if [[ $confirm != "y" ]]; 
then
  echo
  echo "Please correct the input and rerun this script"
  echo
  exit
fi

#make the leaf directories
mkdir -p $LocalFilesPath

echo
echo "## Zip:"

scripts/createZip.sh $ZipName $LocalFilesPath

echo
echo "Is the Zip file correct? [y|N]"
read confirm
if [[ $confirm != "y" ]]; 
then
  echo "Please correct the input and rerun this script"
  echo
  exit
fi

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
cd $LocalSvnBasePath
svn add --force .
svn ci -m "Deploy $FileVersion to DIST"

echo
echo "Is the dist directory structure and content OK? [y|N]"
read confirm
if [[ $confirm != "y" ]]; 
then
  echo "Please correct the input and rerun this script"
  echo "You may have to manually remove some contents from Dist"
  echo
  exit
fi
echo
echo "# SUCCESS"
echo

