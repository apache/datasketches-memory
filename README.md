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
[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/apache/datasketches-memory.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/apache/datasketches-memory/context:java)
[![Total alerts](https://img.shields.io/lgtm/alerts/g/apache/datasketches-memory.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/apache/datasketches-memory/alerts/)
[![Coverage Status](https://coveralls.io/repos/github/apache/datasketches-memory/badge.svg?branch=master)](https://coveralls.io/github/apache/datasketches-memory?branch=master)

=================

# DataSketches Java Memory Component
The goal of this component of the DataSketches library is to provide high performance access to heap memory or native memory for primitives, primitive arrays, ByteBuffers and memory mapped files. 
This package is general purpose, has minimal external runtime dependencies and can be used in any 
application that needs to manage data structures outside the Java heap.

Please visit the main [DataSketches website](https://datasketches.apache.org) for more information.

If you are interested in making contributions to this Memory component please see our 
[Community](https://datasketches.apache.org/docs/Community/) page.

## Release 2.0.0+
With releases starting with 2.0.0, this Memory component supports Java 8 through Java 13.

Starting with Java 9, the Java language is built using the Java Platform Module System (JPMS). 
Because this Memory component leverages several JVM internal classes for improved performance in Java 8, we had to redesign the build system to allow the user to be able to use as well as develop with this component. 

__NOTE:__ You may have to provide additional JPMS arguments to the JVM in order to use the library in Java 9 and above as described in the following use cases and environments. Also see the [usage instructions](docs/usage-instructions.md) for more information.


### USE AS A LIBRARY (using jars from Maven Central)
In this environment, the user is using the Jars from Maven Central as a library and not attempting to build the  source code or run the Memory component tests.  Depending on how the user intends to use the Memory API, the Java version used to run the user's application and whether the user's application is a JPMS application or not, will determine if the user will need to supply arguments to the JVM running their application and what those arguments need to be.  

* API USE CASES
    * Restricted API #1 - All On-heap
        * wrapped heap arrays
        * wrapped on-heap ByteBuffers 
        * No off-heap ByteBuffers
        * No allocations of off-heap memory
        * No use of memory-mapped files
    * Restricted API #2 - Add off-heap ByteBuffers
        * wrapped heap arrays
        * wrapped on-heap ByteBuffers 
        * wrapped off-heap ByteBuffers
        * No allocations of off-heap memory
        * No use of memory-mapped files
    * Full API
        * wrapped heap arrays
        * wrapped on-heap ByteBuffers 
        * wrapped off-heap ByteBuffers
        * allocations of off-heap memory
        * use of memory-mapped files

* CALLING APPLICATION JAVA VERSION & JPMS APPLICATION
    * Running Java 8
    * Running Java 9 - 13, non-JPMS application
    * Running Java 9 - 13, JPMS application


#### Summary of API Use Cases, Application Java Version and JPMS Application.

| Java Version & JPMS      | Restricted API 1 | Restricted API 2 API | Full API |
|:------------------------:|:----------------:|:--------------------:|:--------:|
| Java 8                   |   Note 1         |  Note 1              | Note 1   |
| Java 9-13, non-JPMS      |   Note 1         |  Note 1              | Note 3   |
| Java 9-13, JPMS          |   Note 1         |  Note 2              | Note 4   |

#### Note 1
No additional JVM arguments required

#### Note 2
User must supply the following argument to the JVM:

* --add-opens java.base/sun.nio.ch=org.apache.datasketches.memory

#### Note 3
User must supply the following arguments to the JVM:

* --add-exports java.base/jdk.internal.misc=ALL-UNNAMED
* --add-exports java.base/jdk.internal.ref=ALL-UNNAMED
* --add-opens java.base/java.nio=ALL-UNNAMED
* --add-opens java.base/sun.nio.ch=ALL-UNNAMED

#### Note 4
User must supply the following arguments to the JVM:

* --add-exports java.base/jdk.internal.misc=org.apache.datasketches.memory
* --add-exports java.base/jdk.internal.ref=org.apache.datasketches.memory
* --add-opens java.base/java.nio=org.apache.datasketches.memory
* --add-opens java.base/sun.nio.ch=org.apache.datasketches.memory


## DEVELOPER USAGE
In this environent the developer needs to build the Memory component from source and run the Memory Component tests.  There are two use cases. The first is for a *System Developer* that needs to build and test their own Jar from source for a specific Java version. The second use case is for a *Memory Component Developer and Contributor*. 

* System Developer
    * Compile, test and create a Jar for a specific Java version
        * use the privided script for this purpose 

* Memory Component Developer / Contributor
    * Compile & test the library from source code using:
        * Eclipse (version)
        * IntelliJ (version)
        * Maven (version)
        * Command-line
    * The developer must have installed in their development system at least JDK versions 8, 9 and 11.

    
### Build Instructions
__NOTES:__ 

1) This component accesses resource files for testing. As a result, the directory elements
   of the full absolute path of the target installation directory must qualify as Java identifiers.
   In other words, the directory elements must not have any space characters (or non-Java identifier
   characters) in any of the path elements. This is required by the Oracle Java Specification in
   order to ensure location-independent access to resources:
   [See Oracle Location-Independent Access to Resources](https://docs.oracle.com/javase/8/docs/technotes/guides/lang/resources.html)

2) This project is structured as a Maven multi-module project.  
   Building this project might affect plugins that require early dependency resolution, such as the
   javadoc and eclipse plugins.
   The build instructions below have been modified to use the `process-classes` phase (instead of `compile`)
   for these use cases.
   
   For more information, see this [Maven Reactor Issue](https://issues.apache.org/jira/browse/MNG-3283).

#### JDK versions required to compile
This DataSketches component is pure Java and requires the following JDKs to compile:

- JDK8/Hotspot
- JDK9/Hotspot
- JDK11/Hotspot

Ensure that your local environment has been configured according to the 
[Maven Toolchains Configuration](docs/maven-toolchains.md).

#### Recommended Build Tool
This DataSketches component is structured as a Maven project and Maven is the recommended Build 
Tool.

There are two types of tests: normal unit tests and tests run by the strict profile.

To run normal unit tests:

    $ mvn clean test

To run the strict profile tests (only supported in Java 8):

    $ mvn clean test -P strict

To run javadoc on this multi-module project, use:

    $ mvn clean process-classes javadoc:javadoc -DskipTests=true

To run the CI tests against the multi-release JAR for specific JVM versions [9-13], use:

    $ mvn clean package -Denvironment=ci -Dmatrix.jdk.version=9

To run the eclipse plugin on this multi-module project, use:

    $ mvn clean process-classes eclipse:eclipse -DskipTests=true

To install jars built from the downloaded source:

    $ mvn clean install -DskipTests=true

This will create the following Jars:

* datasketches-memory-X.Y.Z.jar The compiled main class files.
* datasketches-memory-X.Y.Z-tests.jar The compiled test class files.
* datasketches-memory-X.Y.Z-sources.jar The main source files.
* datasketches-memory-X.Y.Z-test-sources.jar The test source files
* datasketches-memory-X.Y.Z-javadoc.jar  The compressed Javadocs.

#### Toolchains

This project makes use of Maven toolchains to ensure that the correct Java compiler version is 
used when compiling source files.
See the [Maven Toolchains Configuration](docs/maven-toolchains.md) for more details.

#### Dependencies

There are no run-time dependencies.

#### Testing
See the pom.xml file for test dependencies.

---

### Further documentation for contributors

For more information on the project configuration, the following topics are discussed in more 
detail:

* [Maven Configuration](docs/maven.md)
* [Maven Toolchains Configuration](docs/maven-toolchains.md)
* [Multi-Release Jar](docs/multi-release-jar.md)
* [Java Platform Module System](docs/module-system.md)
* [Usage instructions](docs/usage-instructions.md)

In order to build and contribute to this project, please read the relevant IDE documentation:

- [Eclipse IDE Setup](docs/eclipse.md)
- [IntelliJ IDE Setup](docs/intellij.md)











