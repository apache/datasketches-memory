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

# This is a general bash script to build a datasketches-memory-X.jar
# with multi-release functionality.  The sources, test-sources, tests and
# javadoc jars are also included in the output.
# It does use git and also uses the script getGitProperties.sh.
#
# NOTE: This script assumes that `mvn package` has been run prior to invocation.
#       By default, it is called from the maven exec-plugin.

#  Required Input Parameters:
#  \$1 = absolute path of JDK home directory
#  \$2 = Git Version Tag for this deployment
#       Example tag for SNAPSHOT         : 1.0.0-SNAPSHOT
#       Example tag for Release Candidate: 1.0.0-RC1
#       Example tag for Release          : 1.0.0
#  \$3 = absolute path of project.basedir
#  For example:  $ <this script>.sh $JAVA_HOME 2.1.0 .

if [ -z "$1" ]; then echo "Missing JDK home";            exit 1; fi
if [ -z "$2" ]; then echo "Missing Git Tag";             exit 1; fi
if [ -z "$3" ]; then echo "Missing project.basedir";     exit 1; fi

#### Extract JDKHome, Version and ProjectBaseDir from input parameters ####
JDKHome=$1
GitTag=$2
ProjectBaseDir=$3 #this must be an absolute path

#### Setup absolute directory references ####
ProjectArtifactId="memory"
ScriptsDir=${ProjectBaseDir}/tools/scripts/

#### Initialise path dependent variables ####
OutputDir=target
OutputJar=${OutputDir}/datasketches-memory-${GitTag}.jar

ArchiveDir=${OutputDir}/archive-tmp
PackageSources=${ArchiveDir}/sources
PackageTestSources=${ArchiveDir}/test-sources
PackageTests=${ArchiveDir}/tests
PackageJavaDoc=${ArchiveDir}/javadoc
PackageMrJar=${ArchiveDir}/jar

#### Move to project directory ####
cd ${ProjectBaseDir}

#### Use JAVA_HOME to set required executables ####
if [[ -n "$JDKHome" ]] && [[ -x "${JDKHome}/bin/jar" ]];     then Jar_="${JDKHome}/bin/jar";         else echo "No jar version could be found.";     exit 1; fi

MemoryJava8Classes=datasketches-memory-java8/target/classes
MemoryJava11Classes=datasketches-memory-java11/target/classes
MavenArchiver=target/maven-archiver

if ! [[ -x "${MemoryJava8Classes}" ]];    then echo "No compiled classes in ${MemoryJava8Classes}.";    exit 1; fi
if ! [[ -x "${MemoryJava11Classes}" ]];   then echo "No compiled classes in ${MemoryJava11Classes}.";   exit 1; fi
if ! [[ -x "${MavenArchiver}" ]];         then echo "No maven archiver ${MavenArchiver}.";                   exit 1; fi

#### Cleanup and setup output directories ####
echo
if [ -d "$OutputDir" ]; then rm -f $OutputDir/*.jar; fi
if [ -d "$PackageDir" ]; then rm -r $PackageDir; fi

mkdir -p $PackageSources
mkdir -p $PackageTestSources
mkdir -p $PackageTests
mkdir -p $PackageJavaDoc
mkdir -p $PackageMrJar

###########################
####    JAVADOC JAR    ####
###########################

###########################
#### MULTI-RELEASE JAR ####
###########################
PackageMrJarMeta=${PackageMrJar}/META-INF
PackageMrJarMaven=${PackageMrJarMeta}/maven/org.apache.datasketches/datasketches-memory
mkdir -p ${PackageMrJarMeta}/versions/11
mkdir -p ${PackageMrJarMaven}

#### Generate MANIFEST.MF ####
cat >> ${PackageMrJarMeta}/MANIFEST.MF<< EOF
Manifest-Version: 1.0
Created-By: Apache Datasketches Memory package-mr-jar.sh
Multi-Release: true
EOF

#### Generate DEPENDENCIES ####
cat >> ${PackageMrJarMeta}/DEPENDENCIES<< EOF
// ------------------------------------------------------------------
// Transitive dependencies of this project determined from the
// maven pom organized by organization.
// ------------------------------------------------------------------
EOF

#### Copy LICENSE and NOTICE ####
cp LICENSE $PackageMrJarMeta
cp NOTICE $PackageMrJarMeta

#### Copy pom.properties
cp ${MavenArchiver}/pom.properties $PackageMrJarMaven
cp pom.xml $PackageMrJarMaven

#### Generate git.properties file ####
echo "$($ScriptsDir/getGitProperties.sh $ProjectBaseDir $ProjectArtifactId $GitTag)" >> ${PackageMrJarMeta}/MANIFEST.MF

#### Copy java 8 compiled classes to target/jar
rsync -q -a -I --filter="- .*" ${MemoryJava8Classes}/org $PackageMrJar
#### Copy java 11 compiled classes to target/jar/META-INF/versions/11
rsync -q -a -I --filter="- .*" ${MemoryJava11Classes}/org ${PackageMrJarMeta}/versions/11
cp ${MemoryJava11Classes}/module-info.class ${PackageMrJarMeta}/versions/11

${Jar_} cf $OutputJar -C $PackageMrJar .

echo "Created multi-release jar ${OutputJar}"



