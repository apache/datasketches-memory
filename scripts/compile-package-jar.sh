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

# This is a general bash script to build a java version-specific
# datasketches-memory-X.jar without multi-release functionality.
# This is intended to be used for developers compiling from source
# who do not wish to install several versions of java on their
# machine.
# The script does not assume a POM file and does not use Maven.
# It does use git, SVN, and uses the script getGitProperties.sh

#  Required Input Parameters:
#  \$1 = absolute path of Java home directory
#  \$2 = Git Version Tag for this deployment
#       Example tag for SNAPSHOT         : 1.0.0-SNAPSHOT
#       Example tag for Release Candidate: 1.0.0-RC1
#       Example tag for Release          : 1.0.0
#  \$3 = absolute path of project.basedir
#  For example:  $ <this script>.sh $JAVA_HOME 2.0.0-RC1 .

if [ -z "$1" ]; then echo "Missing java.home";               exit 1; fi
if [ -z "$2" ]; then echo "Missing Git Version";             exit 1; fi
if [ -z "$3" ]; then echo "Missing project.basedir";         exit 1; fi

# Setup absolute directory references
ScriptsDir=$(pwd)

## Extract JavaHome and Version from input parameters:
JavaHome=$1
Version=$2
ProjectBaseDir=$3 #this must be an absolute path

####Move to project directory####
cd ${ProjectBaseDir}

#### Use JAVA_HOME to set required executables ####
if [[ -n "$JavaHome" ]] && [[ -x "${JavaHome}/bin/java" ]]; then
  Java_="${JavaHome}/bin/java"
else
  echo "No java version could be found."; exit 1;
fi

if [[ -n "$JavaHome" ]] && [[ -x "${JavaHome}/bin/javac" ]]; then
  Javac_="${JavaHome}/bin/javac"
else
  echo "No javac version could be found."; exit 1;
fi

if [[ -n "$JavaHome" ]] && [[ -x "${JavaHome}/bin/jar" ]]; then
  Jar_="${JavaHome}/bin/jar"
else
  echo "No jar version could be found."; exit 1;
fi

#### Parse java -version into major version number ####
if [[ "$Java_" ]]; then
  JavaVersion=$("$Java_" -version 2>&1 | head -1 | cut -d'"' -f2 | sed '/^1\./s///' | cut -d'.' -f1)
else
  echo "No version information could be determined from installed java."; exit 1;
fi

#### Cleanup and setup output directories ####
OutputDir=target
OutputJar=${OutputDir}/org.apache.datasketches.memory-${Version}.jar

PackageDir=${OutputDir}/package
PackageSrc=${PackageDir}/src
PackageContents=${PackageDir}/contents
PackageMeta=${PackageDir}/META-INF

MemoryJava8Src=datasketches-memory-java8/src/main/java
MemoryJava9Src=datasketches-memory-java9/src/main/java
MemoryJava11Src=datasketches-memory-java11/src/main/java

echo
echo "--- CLEAN & COMPILE ---"
rm -r $OutputDir
mkdir -p $PackageSrc
mkdir -p $PackageContents
mkdir -p $PackageMeta

#### Copy LICENSE and NOTICE ####
cp LICENSE $PackageMeta
cp NOTICE $PackageMeta

#### Copy base tree to target/src
rsync -a $MemoryJava8Src $PackageSrc

# version too low
if [[ $JavaVersion -lt 8 ]]; then
  echo "Java version not supported: " $JavaVersion; exit 1;

# version 8
elif [[ $JavaVersion -lt 9 ]]; then
  echo "Compiling with java version $JavaVersion..."
  ${Javac_} -d $PackageContents $(find $PackageSrc -name '*.java')

# version 9 or 10
elif [[ $JavaVersion -lt 11 ]]; then
  echo "Compiling with java version $JavaVersion..."
  #### Copy java 9 src tree to target/src, overwriting replacements
  rsync -a $MemoryJava9Src $PackageSrc
  # Compile with JPMS exports
  ${Javac_} \
    --add-exports java.base/jdk.internal.ref=org.apache.datasketches.memory \
    --add-exports java.base/sun.nio.ch=org.apache.datasketches.memory \
    -d $PackageContents $(find $PackageSrc -name '*.java')

# version 11, 12 or 13
elif [[ $JavaVersion -lt 14 ]]; then
  echo "Compiling with java version $JavaVersion..."
  #### Copy java 9 src tree to target/src, overwriting replacements
  rsync -a $MemoryJava9Src $PackageSrc
  #### Copy java 11 src tree to target/src, overwriting replacements
  rsync -a $MemoryJava11Src $PackageSrc
  # Compile with JPMS exports
   ${Javac_} \
   --add-exports java.base/jdk.internal.ref=org.apache.datasketches.memory \
   --add-exports java.base/sun.nio.ch=org.apache.datasketches.memory \
   -d $PackageContents $(find $PackageSrc -name '*.java')

# version too high
else
  echo "Java version not supported: " $JavaVersion; exit 1;
fi
echo
echo "--- JAR ---"
echo "Building Jar from ${PackageContents}..."
${Jar_} cf $OutputJar -C $PackageContents .
echo
echo "--- JAR CONTENTS ---"
${Jar_} tf ${OutputJar}
echo
echo "Successfully built ${OutputJar}"

