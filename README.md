<!--
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
-->

[![Build Status](https://travis-ci.org/apache/datasketches-memory.svg?branch=master)](https://travis-ci.org/apache/datasketches-memory)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.datasketches/datasketches-memory/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.apache.datasketches/datasketches-memory)
[![Coverage Status](https://coveralls.io/repos/github/apache/datasketches-memory/badge.svg?branch=master)](https://coveralls.io/github/apache/datasketches-memory?branch=master)

=================

# DataSketches Java Memory Component
This Memory component is general purpose, has no external runtime dependencies and can be used in any
application that needs to manage data structures inside or outside the Java heap.

Please visit the main [DataSketches website](https://datasketches.apache.org) for more information.

If you are interested in making contributions to this Memory component please see our
[Community](https://datasketches.apache.org/docs/Community/) page.

The goal of this component of the DataSketches library is to provide a high performance access API for accessing six different types of memory resources.  Each of the six resource types is accessed using different API methods. 

Note: *primitive* = *{byte, short, int, long, float, double}*

### Contiguous bytes on the Java Heap constructed by:

* **Heap:**
    * *Memory.wrap(primitive[])*
    * *WritableMemory.allocate(int)*
    * *WritableMemory.writableWrap(primitive[])*

* **Heap via ByteBuffer**
    * *Memory.wrap(ByteBuffer.allocate(int))*
    * *WritableMemory.writableWrap(ByteBuffer.allocate(int))*

### Contiguous bytes off the Java Heap constructed by:

* **Direct:**
    * *WritableMemory.allocateDirect(long)* method.

* **Direct via ByteBuffer** 
    *  *WritableMemory.writableWrap(ByteBuffer.allocateDirect(int))*

* **Memory-Mapped Files**  
    * *WritableMemory.writableMap(File)* method.

# Releases 3.0.0 (inclusive) to 4.0.0 (exclusive)
These are transitional releases that also supports Java 8 and 11. 
However, the goal of this set of releases is to migrate the API to what it will be in release 4.0.0.
The 4.0.0 release will require Java 17 and will utilize the Project Panama (FFM) capabilites introduced in Java 17.

Some of the capabilites of releases [2.0.0, 3.0.0) have been removed because they cannot be supported in release 4.0.0 with Panama-17. 
For example:

* The ability to directly import and export UTF-8 encoded strings has been removed.
    * The UTF-8 code in Memory 1.0.0 through 2.2.1 is out of date and needed to be removed anyway.
    * Java already has built-in UTF-8 encoding.  
* The ability to directly import and export boolean arrays has been removed.
    * The Java Language Specification does not define the byte storage format for boolean arrays, thus Panama-17 doesn't support boolean arrays.
* Other minor changes to the API is best understood by examining the Javadocs directly.

Release 3.0.0 includes two bug fixes (Issues #194, #195).

It is our expectation that this set of releases will be the last that support Java 8 and 11.

The comments in the following section for releases [2.0.0, 3.0.0) still apply.

# Releases 2.0.0 (inclusive) to 3.0.0 (exclusive)
Starting with release *datasketches-memory-2.0.0*, this Memory component supports Java 8 and 11. Providing access to the off-heap resources in Java 8 only requires reflection. However, **Java 9 introduced the Java Platform Module System (JPMS) where access to these internal classes requires starting up the JVM with special JPMS arguments.**  The actual JVM arguments required will depend on how the user intends to use the Memory API, the Java version used to run the user's application and whether the user's application is a JPMS application or not.

Also see the [usage examples](docs/usage-examples.md) for more information.

## USE AS A LIBRARY (using jars from Maven Central)
In this environment, the user is using the Jars from Maven Central as a library  dependency and not attempting to build the Memory component from the source code or run the Memory component tests.

* If you are running Java 8, no extra JVM arguments are required.
* If you are running Java 11-13 and only using the **Heap** related API, no extra JVM arguments are required.

Otherwise, if you are running Java 11-13 and ...

* If your application **is not a JPMS module** use the following table. Choose the columns that describe your use of the Memory API.  If any of the columns contain a *Yes*, then the JVM argument in the first column of the row containing a *Yes* will be required. If you are not sure the extent of the Memory API being used, there is no harm in specifying all 4 JVM arguments. Note: do not embed any spaces in the full argument.

|        JVM Arguments for non-JPMS Applications         | Direct ByteBuffer | Direct | MemoryMapped Files |
| :----------------------------------------------------: | :---------------: | :----: | :----------------: |
| --add-exports java.base/jdk.internal.misc= ALL-UNNAMED |                   |  Yes   |                    |
| --add-exports java.base/jdk.internal.ref= ALL-UNNAMED  |                   |  Yes   |        Yes         |
|      --add-opens java.base/java.nio= ALL-UNNAMED       |                   |  Yes   |        Yes         |
|     --add-opens java.base/sun.nio.ch= ALL-UNNAMED      |                   |        |        Yes         |

* If your application **is a JPMS module** use the following table. Choose the columns that describe your use of the Memory API.  If any of the columns contain a *Yes*, then the JVM argument in the first column of the row containing a *Yes* will be required. If you are not sure the extent of the Memory API being used, there is no harm in specifying all 4 JVM arguments. Note: do not embed any spaces in the full argument.

|                    JVM Arguments for JPMS Applications                    | Direct ByteBuffer | Direct | MemoryMapped Files |
| :-----------------------------------------------------------------------: | :---------------: | :----: | :----------------: |
| --add-exports java.base/jdk.internal.misc= org.apache.datasketches.memory |                   |  Yes   |                    |
| --add-exports java.base/jdk.internal.ref= org.apache.datasketches.memory  |                   |  Yes   |        Yes         |
|      --add-opens java.base/java.nio= org.apache.datasketches.memory       |                   |  Yes   |        Yes         |
|     --add-opens java.base/sun.nio.ch= org.apache.datasketches.memory      |        Yes        |        |        Yes         |


## DEVELOPER USAGE
In this environment the developer needs to build the Memory component from source and run the Memory Component tests.  There are two use cases. The first is for a *System Developer* that needs to build and test their own Jar from source for a specific Java version. The second use case is for a *Memory Component Developer and Contributor*.

* System Developer
    * Compile, test and create a Jar for a specific Java version
        * use the provided script for this purpose

* Memory Component Developer / Contributor
    * Compile & test the library from source code using:
        * Eclipse (version)
        * IntelliJ (version)
        * Maven (version)
        * Command-line or scripts
    * The developer must have installed in their development system at least JDK versions 8 and 11.
    * Unless building with the provided script, the developer must have a valid [Maven toolchain configuration](docs/maven-toolchains.md).


### Build Instructions
__NOTES:__

1) This component accesses resource files for testing. As a result, the directory elements
   of the full absolute path of the target installation directory must qualify as Java identifiers.
   In other words, the directory elements must not have any space characters (or non-Java identifier
   characters) in any of the path elements. This is required by the Oracle Java Specification in
   order to ensure location-independent access to resources:
   [See Oracle Location-Independent Access to Resources](https://docs.oracle.com/javase/8/docs/technotes/guides/lang/resources.html)

#### Dependencies
There are no run-time dependencies. See the pom.xml file for test dependencies.

#### Maven build instructions
The Maven build requires the following JDKs to compile:

- JDK8/Hotspot
- JDK11/Hotspot

Before building, first ensure that your local environment has been configured according to the [Maven Toolchains Configuration](docs/maven-toolchains.md).

There are two types of tests: normal unit tests and continuous integration(CI) tests.
The CI tests target the Multi-Release (MR) JAR and run the entire test suite using a specific version of Java.  Running the CI test command also runs the default unit tests.

To run normal unit tests:

    mvn clean test

To run javadoc on this multi-module project, use:

    mvn clean javadoc:javadoc -DskipTests=true

To build the multi-release JAR, use:

    mvn clean package

To run the eclipse plugin on this multi-module project, use:

    mvn clean eclipse:eclipse -DskipTests=true

To install jars built from the downloaded source:

    mvn clean install -DskipTests=true

This will create the following Jars:

* datasketches-memory-X.Y.Z.jar The compiled main class files.
* datasketches-memory-X.Y.Z-tests.jar The compiled test class files.
* datasketches-memory-X.Y.Z-sources.jar The main source files.
* datasketches-memory-X.Y.Z-test-sources.jar The test source files
* datasketches-memory-X.Y.Z-javadoc.jar The compressed Javadocs.

#### Building for a specific java version

A build script named **package-single-release-jar.sh** has been provided to package a JAR for a specific java version.  This is necessary in cases where a developer is unable to install all the required versions of the JDK that are required as part of the Maven build.

The build script performs the following steps:

1.  Sets up staging directories under **target/** for the package files
2.  Uses git commands to gather information about the current Git commit and branch
3.  Compiles java source tree
4.  Packages a JAR containing compiled sources together with the Manifest, License and Notice files
5.  Checks and tests the assembled JAR by using the API to access four different resource types

The build script is located in the **tools/scripts/** directory and requires the following arguments:

* JDK Home Directory - The first argument is the absolute path of JDK home directory e.g. $JAVA_HOME
* Git Version Tag    - The second argument is the Git Version Tag for this deployment e.g. 1.0.0-SNAPSHOT, 1.0.0-RC1, 1.0.0 etc.
* Project Directory  - The third argument is the absolute path of project.basedir e.g. /src/apache-datasketches-memory

For example, if the project base directory is `/src/datasketches-memory`;

To run the script for a release version:

    ./tools/scripts/package-single-release-jar.sh $JAVA_HOME 2.1.0 /src/datasketches-memory

To run the script for a snapshot version:

    ./tools/scripts/package-single-release-jar.sh $JAVA_HOME 2.2.0-SNAPSHOT /src/datasketches-memory

To run the script for an RC version:

    ./tools/scripts/package-single-release-jar.sh $JAVA_HOME 2.1.0-RC1 /src/datasketches-memory

Note that the script does **not** use the _Git Version Tag_ to adjust the working copy to a remote tag - it is expected that the user has a pristine copy of the desired branch/tag available **before** using the script.

---

### Further documentation for contributors

For more information on the project configuration, the following topics are discussed in more
detail:

* [Maven Configuration](docs/maven.md)
* [Maven Toolchains Configuration](docs/maven-toolchains.md)
* [Multi-Release Jar](docs/multi-release-jar.md)
* [Java Platform Module System](docs/module-system.md)
* [Usage examples](docs/usage-examples.md)

In order to build and contribute to this project, please read the relevant IDE documentation:

- [Eclipse IDE Setup](docs/eclipse.md)
- [IntelliJ IDE Setup](docs/intellij.md)

For releasing to AppNexus, please use the `sign-deploy-jar.sh` script in the scripts directory.
See the documentation within the script for usage instructions.
