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

[![Build Status](https://travis-ci.org/apache/incubator-datasketches-memory.svg?branch=master)](https://travis-ci.org/apache/incubator-datasketches-memory) 
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.datasketches/datasketches-memory/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.apache.datasketches/datasketches-memory) 
[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/apache/incubator-datasketches-memory.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/apache/incubator-datasketches-memory/context:java)
[![Total alerts](https://img.shields.io/lgtm/alerts/g/apache/incubator-datasketches-memory.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/apache/incubator-datasketches-memory/alerts/)
[![Coverage Status](https://coveralls.io/repos/github/apache/incubator-datasketches-memory/badge.svg?branch=master&service=github)](https://coveralls.io/github/apache/incubator-datasketches-memory?branch=master)

=================

# DataSketches Memory Component
 The goal of this component of the library is to provide high performance access to native memory for primitives
 and primitive arrays. It also provides consistent views into heap-based primitive arrays,
 Java ByteBuffers and memory mapped files. This package is general purpose, has minimal external
 runtime dependencies and can be used in any application that needs to manage data structures outside
 the Java heap.

## Documentation

### [DataSketches Library Website](https://datasketches.apache.org/)

### [Memory Package Overview Documentation](https://datasketches.apache.org/docs/Memory/MemoryPackage.html)

### [Memory Package Performance](https://datasketches.apache.org/docs/Memory/MemoryPerformance.html)

### [Memory JavaDocs](https://datasketches.apache.org/api/memory/snapshot/apidocs/index.html)

## Downloading Latest Release
__NOTE:__ This component accesses resource files for testing. As a result, the directory elements of the full absolute path of the target installation directory 
    must qualify as Java identifiers. In other words, the directory elements must not have any space characters (or non-Java identifier characters) in any of the path elements.
    
This is required by the Oracle Java Specification in order to ensure location-independent 
    access to resources: [See Oracle Location-Independent Access to Resources](https://docs.oracle.com/javase/8/docs/technotes/guides/lang/resources.html)

### [Zip File from www.apache.org/dist](http://www.apache.org/dist/incubator/datasketches/memory/)

### [Jar Files from Maven Central](https://repository.apache.org/content/repositories/releases/org/apache/datasketches/datasketches-memory/)

### [GitHub](https://github.com/apache/incubator-datasketches-memory/releases)

## Downloading Earlier Releases

### [Zip File from archive.apache.org/dist](http://archive.apache.org/dist/incubator/datasketches/memory/)

## Build Instructions

### JDK8 is required to compile
This DataSketches component is pure Java and you must compile using JDK 8.

### Recommended Build Tool
This DataSketches component is structured as a Maven project and Maven is the recommended Build Tool.

There are two types of tests: normal unit tests and tests run by the strict profile.  

To run normal unit tests:

    $ mvn clean test

To run the strict profile tests:

    $ mvn clean test -P strict

To install jars built from the downloaded source:

    $ mvn clean install -DskipTests=true

This will create the following Jars:

* datasketches-memory-X.Y.Z-incubating.jar The compiled main class files.
* datasketches-memory-X.Y.Z-incubating-tests.jar The compiled test class files.
* datasketches-memory-X.Y.Z-incubating-sources.jar The main source files.
* datasketches-memory-X.Y.Z-incubating-test-sources.jar The test source files
* datasketches-memory-X.Y.Z-incubating-javadoc.jar  The compressed Javadocs.

### Dependencies

#### Run-time
There is one run-time dependency: 

* org.slf4j:slf4j-api

#### Testing
See the pom.xml file for test dependencies.

## Resources

### [Issues for datasketches-memory](https://github.com/apache/incubator-datasketches-memory/issues)

### [Forum](https://groups.google.com/forum/#!forum/sketches-user)

### [Dev mailing list](dev@datasketches.apache.org)
