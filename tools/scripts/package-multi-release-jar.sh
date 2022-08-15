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
# It does use git and also uses the script get-git-properties.sh.
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
ProjectArtifactId="datasketches-memory"
ScriptsDir=${ProjectBaseDir}/tools/scripts/

#### Initialise path dependent variables ####
OutputDir=target
OutputMrJar=${OutputDir}/datasketches-memory-${GitTag}.jar
OutputTests=${OutputDir}/datasketches-memory-${GitTag}-tests.jar
OutputJavaDoc=${OutputDir}/datasketches-memory-${GitTag}-javadoc.jar
OutputSources=${OutputDir}/datasketches-memory-${GitTag}-sources.jar
OutputTestSources=${OutputDir}/datasketches-memory-${GitTag}-test-sources.jar

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
MemoryJava8TestClasses=datasketches-memory-java8/target/test-classes
MemoryJava8Sources=datasketches-memory-java8/src/main/java
MemoryJava8TestSources=datasketches-memory-java8/src/test/java
MemoryJava8Docs=datasketches-memory-java8/target/apidocs/
MemoryJava11Classes=datasketches-memory-java11/target/classes
MemoryJava11Sources=datasketches-memory-java11/src/main/java
MemoryJava8Docs=datasketches-memory-java8/target/apidocs/
MavenArchiver=target/maven-archiver

if ! [[ -x "${MemoryJava8Classes}" ]];        then echo "No compiled classes - run mvn package first.";        exit 1; fi
if ! [[ -x "${MemoryJava8TestClasses}" ]];    then echo "No compiled test classes - run mvn package first.";   exit 1; fi
if ! [[ -x "${MemoryJava11Classes}" ]];       then echo "No compiled classes - run mvn package first.";        exit 1; fi
if ! [[ -x "${MemoryJava8Docs}" ]];           then echo "No javadocs - run mvn package first.";                exit 1; fi
if ! [[ -x "${MavenArchiver}" ]];             then echo "No maven archiver - run mvn package first.";          exit 1; fi

#### Cleanup and setup output directories ####
echo
if [ -d "$OutputDir" ]; then rm -f $OutputDir/*.jar; fi
if [ -d "$ArchiveDir" ]; then rm -r $ArchiveDir; fi

mkdir -p $PackageSources
mkdir -p $PackageTestSources
mkdir -p $PackageTests
mkdir -p $PackageJavaDoc
mkdir -p $PackageMrJar

#### JAR Metadata function
prepare_jar () {
  JarBase=$1
  JarMeta=${JarBase}/META-INF
  JarMaven=${JarMeta}/maven/org.apache.datasketches/datasketches-memory

  mkdir -p ${JarMeta}/versions/11
  mkdir -p ${JarMaven}
  
  #### Generate DEPENDENCIES ####
 cat >> ${JarMeta}/DEPENDENCIES<< EOF
// ------------------------------------------------------------------
// Transitive dependencies of this project determined from the
// maven pom organized by organization.
// ------------------------------------------------------------------
EOF
  
  #### Copy LICENSE and NOTICE ####
  cp LICENSE $JarMeta
  cp NOTICE $JarMeta
  
  #### Copy pom.properties
  cp ${MavenArchiver}/pom.properties $JarMaven
  cp pom.xml $JarMaven
  
}

#### Generate MANIFEST.MF ####
cat >> ${ArchiveDir}/MANIFEST.MF<< EOF
Manifest-Version: 1.0
Created-By: Apache Datasketches Memory package-mr-jar.sh
Multi-Release: true
EOF
#### Generate git.properties file ####
echo "$($ScriptsDir/get-git-properties.sh $ProjectBaseDir $ProjectArtifactId $GitTag)" >> ${ArchiveDir}/MANIFEST.MF

###########################
#### MULTI-RELEASE JAR ####
###########################
prepare_jar $PackageMrJar
#### Copy java 8 compiled classes to target/jar
rsync -q -a -I --filter="- .*" ${MemoryJava8Classes}/org $PackageMrJar
#### Copy java 11 compiled classes to target/jar/META-INF/versions/11
rsync -q -a -I --filter="- .*" ${MemoryJava11Classes}/org ${PackageMrJar}/META-INF/versions/11
cp ${MemoryJava11Classes}/module-info.class ${PackageMrJar}/META-INF/versions/11

${Jar_} cfm $OutputMrJar ${ArchiveDir}/MANIFEST.MF -C $PackageMrJar .
echo "Created multi-release jar ${OutputMrJar}"

###########################
####     TESTS JAR     ####
###########################
prepare_jar $PackageTests
#### Copy java 8 compiled test classes to target/jar
rsync -q -a -I --filter="- .*" ${MemoryJava8TestClasses}/org $PackageTests

${Jar_} cfm $OutputTests ${ArchiveDir}/MANIFEST.MF -C $PackageTests .
echo "Created tests jar ${OutputTests}"

###########################
####    SOURCES JAR    ####
###########################
prepare_jar $PackageSources
#### Copy java 8 source files to target/sources
rsync -q -a -I --filter="- .*" ${MemoryJava8Sources}/org $PackageSources
#### Copy java 11 source files to target/sources/META-INF/versions/11
rsync -q -a -I --filter="- .*" ${MemoryJava11Sources}/org ${PackageSources}/META-INF/versions/11
cp ${MemoryJava11Sources}/module-info.java ${PackageSources}/META-INF/versions/11

${Jar_} cfm $OutputSources ${ArchiveDir}/MANIFEST.MF -C $PackageSources .
echo "Created sources jar ${OutputSources}"

###########################
####  TEST SOURCES JAR ####
###########################
prepare_jar $PackageTestSources
#### Copy java 8 test source files to target/test-sources
rsync -q -a -I --filter="- .*" ${MemoryJava8TestSources}/org $PackageTestSources

${Jar_} cfm $OutputTestSources ${ArchiveDir}/MANIFEST.MF -C $PackageTestSources .
echo "Created test sources jar ${OutputTestSources}"

###########################
####    JAVADOC JAR    ####
###########################
prepare_jar $PackageJavaDoc

rsync -q -a -I --filter="- .*" ${MemoryJava8Docs} $PackageJavaDoc
${Jar_} cfm $OutputJavaDoc ${ArchiveDir}/MANIFEST.MF -C $PackageJavaDoc .
echo "Created javadoc jar ${OutputJavaDoc}"

echo "$($ScriptsDir/test-jar.sh $JDKHome $GitTag $OutputMrJar $ProjectBaseDir)"
