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
echo
echo "=============Deploy using POM Metadata============="
echo " This script must be run from the base directory of the project."
echo " This script will extract the 'project.artifactId' and the 'project.version' from the pom.xml "
echo " which must be found in this directory."
echo "   For example:  $ scripts/pomDeployToDist.sh"
echo " If your project does not have a pom, then you should use the 'bashDeployToDist.sh' instead"
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
echo
echo "4. Note: this script DOES NOT MODIFY POM.  You must do that manually."
echo
echo "Proceed? [y|N]"
read confirm
if [[ $confirm != "y" ]];
then
  "Please rerun this script when ready."
  exit
fi

# extract the project.artifactId
ProjectArtifactId=$(mvn help:evaluate -Dexpression=project.artifactId -q -DforceStdout)

# extract the project.version
ProjectVersion=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

scripts/bashDeployToDist.sh $ProjectArtifactId $ProjectVersion

