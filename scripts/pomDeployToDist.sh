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
echo
echo "Proceed? [y|N]"; read confirm; if [[ $confirm != "y" ]]; then echo "Please rerun this script when ready."; exit; fi

# extract the project.artifactId
ProjectArtifactId=$(mvn help:evaluate -Dexpression=project.artifactId -q -DforceStdout)

# extract the project.version
ProjectVersion=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

scripts/bashDeployToDist.sh $ProjectArtifactId $ProjectVersion

