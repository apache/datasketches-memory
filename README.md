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
 The goal of this component of the library is to provide high performance access to native memory for primitives
 and primitive arrays. It also provides consistent views into heap-based primitive arrays,
 Java ByteBuffers and memory mapped files. This package is general purpose, has minimal external
 runtime dependencies and can be used in any application that needs to manage data structures outside
 the Java heap.

Please visit the main [DataSketches website](https://datasketches.apache.org) for more information.

If you are interested in making contributions to this site please see our [Community](https://datasketches.apache.org/docs/Community/) page for how to contact us.

---

## Java Support

Datasketches memory currently supports Java 8 up to and including Java 13.

In order to use the library in Java 9 and above, you must provide the following runtime arguments to the JVM:

```shell
    --add-opens java.base/java.nio=org.apache.datasketches.memory \
    --add-opens java.base/jdk.internal.misc=org.apache.datasketches.memory \
    --add-opens java.base/jdk.internal.ref=org.apache.datasketches.memory
```

For example, to run your local application with full compatibility for the Java module system, you might use the following command:
```shell
  $JAVA \
    --module-path mods \
    --add-opens java.base/java.nio=org.apache.datasketches.memory \
    --add-opens java.base/jdk.internal.misc=org.apache.datasketches.memory \
    --add-opens java.base/jdk.internal.ref=org.apache.datasketches.memory \
    --module my.main.application.module
```

where `mods` is your module path and `my.main.application.module` is your own JPMS module:

```java
module datasketches.memory.multirelease.test {
    requires org.apache.datasketches.memory;
}
```


---

## Build Instructions
__NOTE:__ This component accesses resource files for testing. As a result, the directory elements of the full absolute path of the target installation directory must qualify as Java identifiers. In other words, the directory elements must not have any space characters (or non-Java identifier characters) in any of the path elements. This is required by the Oracle Java Specification in order to ensure location-independent access to resources: [See Oracle Location-Independent Access to Resources](https://docs.oracle.com/javase/8/docs/technotes/guides/lang/resources.html)

__IMPORTANT:__ This project is structured as a maven multi-module project.  Building this project might affect plugins that require early dependency resolution, such as the javadoc and eclipse plugins.  The build instructions below have been modified to use the `process-classes` phase (instead of `compile`) for these use cases.  For more information, see [this Maven Reactor issue](https://issues.apache.org/jira/browse/MNG-3283).

### JDK versions required to compile
This DataSketches component is pure Java and requires the following JDKs to compile:
- JDK8/Hotspot
- JDK9/Hotspot
- JDK11/Hotspot

### Recommended Build Tool
This DataSketches component is structured as a Maven project and Maven is the recommended Build Tool.

There are two types of tests: normal unit tests and tests run by the strict profile.

To run normal unit tests:

    $ mvn clean test

To run the strict profile tests:

    $ mvn clean test -P strict

To run javadoc on this multi-module project, use:

    $ mvn clean process-classes javadoc:javadoc -DskipTests=true

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

### Toolchains

This project makes use of Maven toolchains to ensure that the correct Java compiler version is used when compiling source files.
Ensure that your local environment has been configured according to the [toolchain documentation](docs/maven-toolchains.md).

### Dependencies

There are no run-time dependencies.

#### Testing
See the pom.xml file for test dependencies.

---

## Further documentation for contributors

For more information on the project configuration, the following topics are discussed in more detail:

* [Maven configuration](docs/maven.md)
* [Maven toolchain configuration](docs/maven-toolchains.md)
* [Multi-release jar](docs/multi-release-jar.md)
* [Java Platform Module System](docs/module-system.md)

In order to build and contribute to this project, please read the relevant IDE documentation:

- [Eclipse IDE](docs/eclipse.md)
- [IntelliJ IDE](docs/intellij.md)
