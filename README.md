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

The goal of this component of the DataSketches library is to provide a high performance access API for accessing four different types of memory resources:

* On-heap Memory via primitive arrays
* On-heap Memory via ByteBuffers
* Off-heap (Direct) Memory via direct allocation
* Off-Heap Memory-Mapped files

Each of the four resource types is accessed using different API methods in the Memory component.

Note: *primitive* := *{byte, short, int, long, float, double}*

### Contiguous bytes on the Java Heap constructed by these examples:

* **Heap via primitive arrays:**
    * *Memory.wrap(primitive[])* (read only)
    * *WritableMemory.allocate(int)*
    * *WritableMemory.writableWrap(primitive[])*

* **Heap via ByteBuffer**
    * *Memory.wrap(ByteBuffer.wrap(byte[]))* (read only)
    * *WritableMemory.writableWrap(ByteBuffer.allocate(int))*

### Contiguous bytes off the Java Heap constructed by these examples:

* **Off-Heap, Direct via direct allocation:**
    * *WritableMemory.allocateDirect(long)* 

* **Off-Heap, Direct via (Direct) ByteBuffer** 
    *  *WritableMemory.writableWrap(ByteBuffer.allocateDirect(int))*

* **Off-Heap, Memory-Mapped Files**
    * *Memory.map(File)*  (read only)
    * *WritableMemory.writableMap(File)*

## Release 4.0.0 (inclusive) to 5.0.0 (exclusive)
Starting with release *datasketches-memory-4.0.0*, this Memory component supports Java 17 when compiling from source and should work with later Java versions at runtime. This component is not designed as a Java Module, so the Jar file should be part of the application classpath.

### *NOTE: The DataSketches Java Memory Component is not thread-safe.*

## Build Instructions
__NOTES:__

* This component accesses resource files for testing. 
As a result, the directory elements of the full absolute path of the target installation directory must qualify as Java identifiers.
In other words, the directory elements must not have any space characters (or non-Java identifier characters) in any of the path elements. This is required by the Oracle Java Specification in order to ensure location-independent access to resources:
[See Oracle Location-Independent Access to Resources](https://docs.oracle.com/javase/8/docs/technotes/guides/lang/resources.html)
* The compile command line must contain the JVM flag *--add-modules=jdk.incubator.foreign*.
* This component is not designed as a Java Module, so the Jar file should be part of the application classpath.

#### Dependencies
There are no run-time dependencies. See the pom.xml file for test dependencies.

#### Maven build instructions
The Maven build requires JDK-17 to compile:

To run normal unit tests:

    mvn clean test

To run javadoc:

    mvn clean javadoc:javadoc -DskipTests=true

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

### Deployment to Nexus 

For releasing to Nexus, please use the `sign-deploy-jar.sh` script in the scripts directory.
See the documentation within the script for usage instructions.
