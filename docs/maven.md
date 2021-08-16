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

# Maven Configuration: Multi-Module Project

This project is a multi-module Maven project. A multi-module Maven project consists of an 
aggregator project (the `datasketches-memory-root` project), together with a set of submodules. 
The aggregator's configuration is inherited by each submodule, thus reducing duplication.

Datasketches Memory makes use of some features of the Java platform, for example, `Unsafe`, 
which have evolved in Java versions 9 and above.   Therefore, a multi-module project allows us to 
add support for later versions of Java by using independent Maven modules to target 
platform-specific APIs.  For example, to deallocate references a `sun.misc.Cleaner` will be used 
in Java8, but the `jdk.internal.ref.Cleaner` is used in Java 9.

This project has been divided into the following submodules:

* datasketches-memory-java8 (base version of the JVM that is currently supported)
* datasketches-memory-java8-tests
* datasketches-memory-java9 (Java9 equivalent of some platform specific classes in 
datasketches-memory-java8)
* datasketches-memory-java11 (Java11 equivalent of some platform specific classes in 
datasketches-memory-java8)
* datasketches-memory (JAR assembly, does not contain source files)
* datasketches-memory-resources (Runs test suite against assembled JAR)

### Artifact assembly

The [Maven assembly plugin](https://maven.apache.org/plugins/maven-assembly-plugin/) builds all 
artifacts for this project from the other modules within the project.  
These modules are complementary and not standalone. 
Therefore, they are not installed and downloaded independently by the end user.

Instead, the Maven assembly plugin builds all jars, and hides the multi-module configuration 
from the end user.

The following jars are assembled by the `datasketches-memory` module:

* datasketches-memory-X.Y.Z.jar The compiled main class files.
* datasketches-memory-X.Y.Z-tests.jar The compiled test class files.
* datasketches-memory-X.Y.Z-sources.jar The main source files.
* datasketches-memory-X.Y.Z-test-sources.jar The test source files
* datasketches-memory-X.Y.Z-javadoc.jar  The compressed Javadocs.
