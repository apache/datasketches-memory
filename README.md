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

[![JDK & OS Matrix CI](https://github.com/apache/datasketches-memory/actions/workflows/auto-jdk-os-matrix.yml/badge.svg)](https://github.com/apache/datasketches-memory/actions/workflows/auto-jdk-os-matrix.yml)
[![Maven Central Latest Version](https://img.shields.io/maven-central/v/org.apache.datasketches/datasketches-memory.svg)](https://search.maven.org/artifact/org.apache.datasketches/datasketches-memory "Maven Central Latest Version")
[![Coveralls Main Branch Test Coverage](https://coveralls.io/repos/github/apache/datasketches-memory/badge.svg?branch=master)](https://coveralls.io/github/apache/datasketches-memory?branch=master)

=================

# DataSketches Memory Project
This Memory Project was initially developed to enable the DataSketches Java Project to manage off-heap data structures efficiently. The first version was developed as an internal project at Yahoo in 2014 using Java 8 and released to open source in 2015. This Memory Project is general purpose, has no external runtime dependencies and can be used in any application that needs to manage data structures on or off the Java heap.

## Basic Capabilities
* **Speed**: Fast Read & Write using Unsafe
* **Types**: All Java primitives plus Java Strings
* **Target**: A contiguous *region* of on-heap or off-heap memory. (Note: Java FFM later named this *region* a *MemorySegment*)
* **Addressing**: Arbitrary Byte Offset (long) as well as relative positional offset.
* **Size**: A region of off-heap memory may exceed the standard Java limit of 2^31-1 bytes.

To make this useful, this project also provided:

## Capability Set A:
* Wrapping of Java primitive arrays as a region.
* Wrapping of ByteBuffers (both on and off-heap) as a region.
* Creation and immediate closing of off-heap regions of memory.
* Creation and immediate closing of off-heap memory-mapped regions of a file.

Providing these capabilities without sacrificing performance required leveraging internal JVM classes such as *Unsafe*, *FileChannelImpl*, *DirectBuffer*, *MappedByteBuffer internals* and *sun.nio.ch*. 

However, starting with Java 11, Oracle started restricting access to a many JVM internal classes. With Java 17 the internals of *FileChannelImpl* and *MappedByteBuffer* were hidden or relocated. This blocked the ability to provide direct access to file-mapped memory. 

Meanwhile, the Java Panama project, which had the potential to provide all the above capabilities as part of the Java language, became available as the Foreign Function and Memory (FFM) API in the form of "Incubation" code in Java 17 and as "Preview" code in Java 21.  

Unfortunately, the DataSketches Memory Project releases 4.X, 5.X and 6.X for Java 17 and 21 were developed using the Incubation and Preview versions of FFM not realizing that Incubation and Preview codes are only available until the next java version is released, after which they are no longer available.

The actual LTS release of FFM did not occur until Java 25 was released on September 16, 2025. Since Java 17 was released on September 14, 2021, there is a gap of 4 years where Oracle had blocked access to internals of the JVM without providing a LTS release API with comparable capabilities.  This means that for the LTS Java versions 17 and 21, the DataSketches Memory Project can not provide access to file-mapped memory.  Nonetheless, it is still possible to provide these capabilities:

## Capability Set B:
* Wrapping of Java primitive arrays as a region.
* Wrapping of ByteBuffers (both on and off-heap) as a region.
* Creation and immediate closing of off-heap regions of memory.

The objective of this 7.X release of the Memory Project is to provide **Capability Set A** with Java 11 and **Capability Set B** for Java versions 17 and 21 for users that are still dependent on the these older LTS releases.

In addition, with this release the build code has been completely refactored:

* A clean all-Maven build, install and deploy process ( no shell scripts required ).
* New GitHub Actions Workflows that test this project in matrix of 3 OSs (Windows, MacOS, Ubuntu) and 3 Java versions (11, 17, 21).
* All code jar files, including the test jars, can be successfully run as jars.
* The Apache release source is a multi-module Maven project
* The release to Maven Central is two jar sets as two new artifacts:
    * datasketches-memory-java11
    * datasketches-memory-java17_21
* All documentation has been brought up-to-date.

Because the FFM capabilities built into Java 25 (released September, 2025) are a major superset of this Memory Project's capabilities (open sourced in 2015), all of these capabilities are now available directly from the Java Language. There is little reason to continue to provide new releases of this project. Alas, this project must come to an end. Any further releases will only be for bug fixes. Nonetheless, we were able to provide these fast, low-level, off-heap memory capabilities for 10 years before the Java language finally provided it. 

## Comments about ByteBuffer
Although the ByteBuffer provides similar capabilities, it has some severe limitations.

* The deallocation of a ByteBuffer is governed by the Garbage Collector. In large systems this delay can lead to out-of-memory failures. 
* The addressing is limited to 2^31 -1 elements.
* Reading and writing multi-byte primitive arrays require recasting the ByteBuffer to a <*type*>Buffer where the alignment becomes modulo the *type*.  This severely complicates the creation of packed structs.
* The API has some serious footguns, which can catch even experienced programmers and can be challenging to debug:
    * Every duplicate() or slice() resets the endianness to Big Endian.
    * Numerous common operations silently discard the *mark*.

----

## Release Comments 

### For Use With Java 8
Please download datasketches-memory 3.X from the website [downloads](https://datasketches.apache.org/docs/Community/Downloads.html) page and refer to the README.md there.
The 3.X set of releases will be the last that support Java 8.

### Releases 4.X, 5.X and 6.X
Deprecated with the 7.X releases.

### Releases 7.X (THIS RELEASE)
* This release no longer uses JPMS but the default jar manifests do provide an Automatic Module Name.
* In addition to the source code obtained from Apache, this release provides two sets of jars in Maven Central based on two artifacts:

#### Artifact: datasketches-memory-java11 (Capability Set A)
This artifact only supports Java 11

#### Artifact: datasketches-memory-java17_21 (Capability Set B)
This artifact only supports Java 17 and Java 21

----

## LIBRARY USAGE (using jars from Maven Central)
In this environment, the user is using the Jars from Maven Central as a library dependency and not attempting to build this Memory Project from the source code.


### If you are running Java 11 you must use artifact *`datasketches-memory-java11`*
You will need to add these java modular arguments to your JVM:

* --add-exports=java.base/jdk.internal.misc=ALL-UNNAMED
* --add-exports=java.base/sun.nio.ch=ALL-UNNAMED
* --add-opens=java.base/java.nio=ALL-UNNAMED
* --add-opens=java.base/sun.nio.ch=ALL-UNNAMED

### If you are running Java 17 or Java 21 you must use artifact *`datasketches-memory-java17_21`*
You will need to add these java modular arguments to your JVM:

* --add-exports=java.base/jdk.internal.misc=ALL-UNNAMED
* --add-exports=java.base/sun.nio.ch=ALL-UNNAMED

## DEVELOPER USAGE
In this environment the developer needs to build the Memory component from source and run the Memory Component tests.  

* Fork this project at https://github.com/apache/datasketches-memory into your own GitHub development site. The 7.0.X releases will be located in the branch 7.0.X.
* Provide a [toolchains.xml](https://maven.apache.org/guides/mini/guide-using-toolchains.html) in your local system at `~/.m2/toolchains.xml` and configured with at least JDK versions 11, 17 and 21.
    * This project uses the maven-toolchains-plugin to ensure that each module is strictly compiled against its target Java release version, regardless of your default JAVA_HOME. 
* You must have `gpg-agent` running in your terminal.
    * Note: The Maven release plugins run GPG in batch mode. Ensure your `gpg-agent.conf` is configured with a long cache TTL, or signing will fail with a no pinentry error during longer builds. 
* Your local machine JAVA_HOME should be set to at least Java 8, but Java 17 or higher is recommended. This is just to run Maven, the `pom.xml` files along with the `toolchains.xml` determine the JDK version used to compile the projects.

### Maven Build Instructions
__NOTES:__

* This component accesses resource files for testing. As a result, the directory elements of the full absolute path of the target installation directory must qualify as Java identifiers. In other words, the directory elements must not have any space characters (or non-Java identifier characters) in any of the path elements. This is required by the Oracle Java Specification in order to ensure location-independent access to resources: [See Oracle Location-Independent Access to Resources](https://docs.oracle.com/javase/8/docs/technotes/guides/lang/resources.html).

__TESTS:__

* There are two types of tests: unit tests and continuous integration (CI) tests run from GitHub Actions workflows.
The CI tests target the released jars and run all the unit tests against Java versions 11, 17 and 21 and three different OSs, Windows, MacOS and Ubuntu.

To run normal unit tests on the forked site:

* `mvn clean test`
   * The test results can be found in `/target/test-output`

To run javadoc on this multi-module project, use:

*  `mvn clean javadoc:javadoc -DskipTests=true`
   *  The javadocs will be located in `/target/site/apidocs/`

To install the jars in ~/.m2

* `mvn clean install -DskipTests=true -Pnexus-jars`
    * The `-Pnexus-jars` is required to generage the GPG signatures.

This will create the following sets of Jars and POMs, with associated GPG .asc signatures (not listed here) in the folder `~/.m2/repository/org/apache/datasketches/` and under

* `datasketches-memory-java11/X.Y.Z/`:

   * datasketches-memory-java11-X.Y.Z.jar
   * datasketches-memory-java11-X.Y.Z-tests.jar
   * datasketches-memory-java11-X.Y.Z-sources.jar
   * datasketches-memory-java11-X.Y.Z-test-sources.jar
   * datasketches-memory-java11-X.Y.Z-javadoc.jar
   * datasketches-memory-java11-X.Y.Z-pom

* `datasketches-memory-java17_21/X.Y.Z/`:
   * datasketches-memory-java17_21-X.Y.Z.jar
   * datasketches-memory-java17_21-X.Y.Z-tests.jar
   * datasketches-memory-java17_21-X.Y.Z-sources.jar
   * datasketches-memory-java17_21-X.Y.Z-test-sources.jar
   * datasketches-memory-java17_21-X.Y.Z-javadoc.jar
   * datasketches-memory-java17_21-X.Y.Z-pom

---

Please visit the main [DataSketches website](https://datasketches.apache.org) for more information about the DataSketches Library and all the different languages it is available in.

If you are interested in making contributions to this Memory Project or to any of the other DataSketches Projects please visit our
[Community](https://datasketches.apache.org/docs/Community/) page.

/- LR
