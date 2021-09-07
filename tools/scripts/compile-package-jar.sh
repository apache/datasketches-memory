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

# This is a general bash script to build a JDK version-specific
# datasketches-memory-X.jar without multi-release functionality.
# This is intended to be used for developers compiling from source
# who do not wish to install several versions of the JDK on their
# machine.
# The script does not assume a POM file and does not use Maven.
# It does use git and also uses the script getGitProperties.sh.

#  Required Input Parameters:
#  \$1 = absolute path of JDK home directory
#  \$2 = Git Version Tag for this deployment
#       Example tag for SNAPSHOT         : 1.0.0-SNAPSHOT
#       Example tag for Release Candidate: 1.0.0-RC1
#       Example tag for Release          : 1.0.0
#  \$3 = absolute path of project.basedir
#  For example:  $ <this script>.sh $JAVA_HOME 2.0.0-RC1 .

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
MemoryMapFile=$ScriptsDir/LoremIpsum.txt

#### Initialise path dependent variables ####
OutputDir=target
OutputJar=${OutputDir}/org.apache.datasketches.memory-${GitTag}.jar
OutputDocsJar=${OutputDir}/org.apache.datasketches.memory-${GitTag}-javadoc.jar

PackageDir=${OutputDir}/package
PackageSrc=${PackageDir}/src
PackageTests=${PackageDir}/test-classes
PackageContents=${PackageDir}/contents
PackageDocs=${PackageDir}/javadoc
PackageMeta=${PackageContents}/META-INF
PackageDocsMeta=${PackageDocs}/META-INF
PackageManifest=${PackageMeta}/MANIFEST.MF

MemoryJava8Src=datasketches-memory-java8/src/main/java
MemoryJava9Src=datasketches-memory-java9/src/main/java
MemoryJava11Src=datasketches-memory-java11/src/main/java

#### Move to project directory ####
cd ${ProjectBaseDir}

#### Use JAVA_HOME to set required executables ####
if [[ -n "$JDKHome" ]] && [[ -x "${JDKHome}/bin/java" ]];    then Java_="${JDKHome}/bin/java";       else echo "No java version could be found.";    exit 1; fi
if [[ -n "$JDKHome" ]] && [[ -x "${JDKHome}/bin/javac" ]];   then Javac_="${JDKHome}/bin/javac";     else echo "No javac version could be found.";   exit 1; fi
if [[ -n "$JDKHome" ]] && [[ -x "${JDKHome}/bin/jar" ]];     then Jar_="${JDKHome}/bin/jar";         else echo "No jar version could be found.";     exit 1; fi
if [[ -n "$JDKHome" ]] && [[ -x "${JDKHome}/bin/javadoc" ]]; then Javadoc_="${JDKHome}/bin/javadoc"; else echo "No javadoc version could be found."; exit 1; fi

#### Parse java -version into major version number ####
if [[ "$Java_" ]]; then
  # This expression extracts the correct major version of the Java runtime.
  # For older runtime versions, such as 1.8, the leading '1.' is removed.
  # Adapted from this answer on StackOverflow:
  # https://stackoverflow.com/questions/7334754/correct-way-to-check-java-version-from-bash-script/56243046#56243046
  JavaVersion=$("$Java_" -version 2>&1 | head -1 | cut -d'"' -f2 | sed '/^1\./s///' | cut -d'.' -f1)
else
  echo "No version information could be determined from installed JDK."; exit 1;
fi

# Exit if Java version too low (< 8) or too high (> 13)
if [[ $JavaVersion -lt 8 || $JavaVersion -gt 13 ]]; then
  echo "Java version not supported: " $JavaVersion; exit 1;
fi

#### Cleanup and setup output directories ####
echo
if [ -d "$OutputDir" ]; then rm -r $OutputDir; fi
mkdir -p $OutputDir
mkdir -p $PackageSrc
mkdir -p $PackageTests
mkdir -p $PackageContents
mkdir -p $PackageMeta
mkdir -p $PackageDocsMeta

#### Copy LICENSE and NOTICE ####
cp LICENSE $PackageMeta; cp LICENSE $PackageDocsMeta;
cp NOTICE $PackageMeta; cp NOTICE $PackageDocsMeta;

#### Generate MANIFEST.MF ####
cat >> ${PackageManifest}<< EOF
Manifest-Version: 1.0
Created-By: Apache Datasketches Memory compile-package-jar.sh
Multi-Release: false
EOF

#### Generate git.properties file ####
echo "$($ScriptsDir/getGitProperties.sh $ProjectBaseDir $ProjectArtifactId $GitTag)" >> $PackageManifest
cp $PackageManifest $PackageDocsMeta

#### Copy source tree to target/src
rsync -a -I $MemoryJava8Src $PackageSrc

if [[ $JavaVersion -eq 9 || $JavaVersion -eq 10 ]]; then
  #### Copy java 9 src tree to target/src, overwriting replacements
  rsync -a -I $MemoryJava9Src $PackageSrc
elif [[ $JavaVersion -gt 10 ]]; then
  #### Copy java 9 and 11 src trees to target/src, overwriting replacements
  rsync -a -I $MemoryJava9Src $PackageSrc
  rsync -a -I $MemoryJava11Src $PackageSrc
fi

#### Compile and create docs ####
echo "--- CLEAN & COMPILE ---"
echo
echo "Compiling with JDK version $JavaVersion..."
if [[ $JavaVersion -lt 9 ]]; then
  ${Javac_} -d $PackageContents $(find $PackageSrc -name '*.java')
  ${Javadoc_} -quiet -d $PackageDocs $(find $PackageSrc -name '*.java')
else
  # Compile with JPMS exports
  ${Javac_} \
    --add-exports java.base/jdk.internal.ref=org.apache.datasketches.memory \
    --add-exports java.base/sun.nio.ch=org.apache.datasketches.memory \
    -d $PackageContents $(find $PackageSrc -name '*.java')
  # Compile Javadoc with JPMS exports
  ${Javadoc_} \
     --add-exports java.base/jdk.internal.ref=org.apache.datasketches.memory \
     --add-exports java.base/sun.nio.ch=org.apache.datasketches.memory \
     -quiet -d $PackageDocs $(find $PackageSrc -name '*.java')
fi
echo
echo "--- JARS ---"
echo
echo "Building JAR from ${PackageContents}..."
${Jar_} cf $OutputJar -C $PackageContents .
echo "Building Javadoc JAR from ${PackageDocs}..."
${Jar_} cf $OutputDocsJar -C $PackageDocs .
echo

# Uncomment this section to display JAR contents
# echo "--- JAR CONTENTS ---"
# echo
# ${Jar_} tf ${OutputJar}
# ${Jar_} tf ${OutputDocsJar}
# echo

echo "--- RUN JAR CHECKS ---"
echo
if [[ $JavaVersion -eq 8 ]]; then
  ${Javac_} -cp $OutputJar -d $PackageTests $(find $ScriptsDir -name '*.java')
  ${Java_} -cp $PackageTests:$OutputJar org.apache.datasketches.memory.tools.scripts.CheckMemoryJar $MemoryMapFile
else
  ${Javac_} \
    --add-modules org.apache.datasketches.memory \
    -p "$OutputJar" -d $PackageTests $(find $ScriptsDir -name '*.java')

  ${Java_} \
    --add-modules org.apache.datasketches.memory \
    --add-exports java.base/jdk.internal.misc=org.apache.datasketches.memory \
    --add-exports java.base/jdk.internal.ref=org.apache.datasketches.memory \
    --add-opens java.base/java.nio=org.apache.datasketches.memory \
    --add-opens java.base/sun.nio.ch=org.apache.datasketches.memory \
    -p $OutputJar -cp $PackageTests org.apache.datasketches.memory.tools.scripts.CheckMemoryJar $MemoryMapFile
fi
echo
echo "Successfully built ${OutputJar}"
echo "Successfully built ${OutputDocsJar}"
