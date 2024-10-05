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

# This is a general bash script to sign and deploy datasketches-memory-X.Y.Z.jars to Nexus.
# This assumes that the jar files exist with X.Y.Z suffix in /target/ 
#   and were created by "mvn clean install".
# Also the project.basedir must be set to the X.Y.X candidate at the correct git hash
# and there must be an X.Y.Z tag at that git hash.

# Even for release candidates, e.g. "-RCn", it is recommended to deploy the candidates
#   without the "-RCn". If the candidate succeeds, it is trivial just "release" them in Nexus.
# If the candidate does not succeed, you can just "drop" them in Nexus for the next round.

#  Required Input Parameters:
#  \$1 = Git Version Tag for this deployment
#       Example tag for SNAPSHOT         : 1.0.0-SNAPSHOT  //not recommended
#       Example tag for Release Candidate: 1.0.0-RC1       //not recommended
#       Example tag for Release          : 1.0.0           //always use this form
#  \$2 = absolute path of project.basedir

#  For example, from the scripts directory:  $ ./sign-deploy-jar.sh 2.1.0 ${project.basedir}

#### Extract GitTag, TestJar and ProjectBaseDir from input parameters ####
GitTag=$1
ProjectBaseDir=$2

#### Common Variables ####
GroupId="org.apache.datasketches"
ArtifactId="datasketches-memory"

#### Setup absolute directory references ####
OutputDir=${ProjectBaseDir}/target

OutputMrJar=${OutputDir}/datasketches-memory-${GitTag}.jar
OutputTests=${OutputDir}/datasketches-memory-${GitTag}-tests.jar
OutputJavaDoc=${OutputDir}/datasketches-memory-${GitTag}-javadoc.jar
OutputSources=${OutputDir}/datasketches-memory-${GitTag}-sources.jar
OutputTestSources=${OutputDir}/datasketches-memory-${GitTag}-test-sources.jar
OutputPom=${OutputDir}/datasketches-memory-${GitTag}-pom

#### Use GNU-GPG to create signature
sign_file () {
  File=$1
  gpg --verbose --personal-digest-preferences=SHA512 --detach-sign -a $File
}

### Deploy to nexus
if [[ $GitTag == *SNAPSHOT ]] 
then
  echo "Using SNAPSHOT repository."
  DistributionsUrl=https://repository.apache.org/content/repositories/snapshots/
  DistributionsId=apache.snapshots.https
else
  echo "Using RELEASES repository."
  DistributionsUrl=https://repository.apache.org/service/local/staging/deploy/maven2/
  DistributionsId=apache.releases.https
fi;

mvn org.apache.maven.plugins:maven-gpg-plugin:3.0.1:sign-and-deploy-file \
    -Durl=$DistributionsUrl\
    -DrepositoryId=$DistributionsId \
    -DgroupId=${GroupId} \
    -DartifactId=${ArtifactId} \
    -Dfile=$OutputMrJar \
    -Dsources=$OutputSources \
    -Dfiles=$OutputTests,$OutputTestSources \
    -Dtypes=jar,jar \
    -Dclassifiers=tests,test-sources \
    -Djavadoc=$OutputJavaDoc \
    -Dpackaging=jar \
    -Dversion=$GitTag \
    -DupdateReleaseInfo=true \
    -DpomFile=${ProjectBaseDir}/pom.xml

echo "Successfully signed and deployed jars"
