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

# This is a general bash script to test a datasketches-memory-X.jar
# with multi-release functionality.
# This is intended to be used for C/I matrix testing or for quick
# verification of the output from the assembly process.

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
MemoryMapFile=$ScriptsDir/LoremIpsum.txt

#### Initialise path dependent variables ####
OutputDir=target
OutputJar=${OutputDir}/datasketches-memory-${GitTag}.jar

PackageDir=${OutputDir}/archive-tmp
PackageChecks=${PackageDir}/checks

#### Move to project directory ####
cd ${ProjectBaseDir}

#### Use JAVA_HOME to set required executables ####
if [[ -n "$JDKHome" ]] && [[ -x "${JDKHome}/bin/java" ]];    then Java_="${JDKHome}/bin/java";       else echo "No java version could be found.";    exit 1; fi
if [[ -n "$JDKHome" ]] && [[ -x "${JDKHome}/bin/javac" ]];   then Javac_="${JDKHome}/bin/javac";     else echo "No javac version could be found.";   exit 1; fi
if [[ -n "$JDKHome" ]] && [[ -x "${JDKHome}/bin/jar" ]];     then Jar_="${JDKHome}/bin/jar";         else echo "No jar version could be found.";     exit 1; fi

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
if [ -d "$PackageChecks" ]; then rm -r $PackageChecks; fi
mkdir -p $PackageChecks

echo "--- RUN JAR CHECKS ---"
echo
if [[ $JavaVersion -eq 8 ]]; then
  ${Javac_} -cp $OutputJar -d $PackageChecks $(find $ScriptsDir -name '*.java')
  ${Java_} -cp $PackageChecks:$OutputJar org.apache.datasketches.memory.tools.scripts.CheckMemoryJar $MemoryMapFile
else
  ${Javac_} \
    --add-modules org.apache.datasketches.memory \
    -p "$OutputJar" -d $PackageChecks $(find $ScriptsDir -name '*.java')

  ${Java_} \
    --add-modules org.apache.datasketches.memory \
    --add-exports java.base/jdk.internal.misc=org.apache.datasketches.memory \
    --add-exports java.base/jdk.internal.ref=org.apache.datasketches.memory \
    --add-opens java.base/java.nio=org.apache.datasketches.memory \
    --add-opens java.base/sun.nio.ch=org.apache.datasketches.memory \
    -p $OutputJar -cp $PackageChecks org.apache.datasketches.memory.tools.scripts.CheckMemoryJar $MemoryMapFile
fi
echo
echo "Successfully checked ${OutputJar}"
