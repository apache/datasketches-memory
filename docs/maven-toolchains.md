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

# Maven Toolchains Configuration

From the [maven-toolchain-plugin documentation](https://maven.apache.org/plugins/maven-toolchains-plugin/usage.html):

> A Toolchain is an object that Maven plugins can use to retrieve preconfigured tools 
> (including location and other information).
> With the jdk toolchain, for example, instead of being stuck with the JDK used to run Maven,
> all plugins can use the same or other JDK instances without hardcoding absolute paths 
> into the pom.xml and without configuring every plugin that require a path to JDK tools.  


### Motivation

Toolchains are used in different maven modules to ensure that the correct Java compiler version 
is used when compiling source files.  This is because Datasketches Memory uses some JDK 
version-specific APIs, which require different JDKs to compile correctly.

### Toolchains template

Your local environment requires toolchain entries for Java 8, 9 and 11 to build this project.  
These can be found in a reference `toolchains.xml` template in the `tools` directory.
Any maven commands used during development can be supplemented with: 
`--toolchains tools/toolchains.xml`, without permanently modifying the local 
`~/.m2/toolchains.xml` file.

Alternatively, to avoid having to add this extra argument to every Maven command, 
the toolchain template can be copied to your local maven `toolchains.xml`, 
e.g. `~/.m2/toolchains.xml`.  If there is already a locally configured `toolchains.xml` file, 
the requisite entries should be merged into the existing file if they do not already exist.

### Environment variables

The DataSketches Memory component is pure Java and requires the following JDKs to compile:

- JDK8/Hotspot
- JDK9/Hotspot
- JDK11/Hotspot

The following environment variables should be set as follows:

| Environment variable              | Value                                 |
| --------------------------------- | ------------------------------------- |
| JAVA8_HOME                        |  Home directory for Java 8 (openJDK)  |
| JAVA9_HOME                        |  Home directory for Java 9 (openJDK)  |
| JAVA11_HOME                       |  Home directory for Java 11 (openJDK) |

For example, if you are using [SDKMAN!](https://sdkman.io/), your environment 
might be configured as follows:

- JAVA8_HOME: `/Users/me/.sdkman/candidates/java/8.0.282.hs-adpt`
- JAVA9_HOME: `/Users/me/.sdkman/candidates/java/9.0.4-open`
- JAVA11_HOME: `/Users/me/.sdkman/candidates/java/11.0.10.hs-adpt`

#### For MacOS or Linux variants
Users can discover what JDKs have been loaded into their environment by using the following 
command:

    /usr/libexec/java_home -V

### Eclipse configuration

If you are an Eclipse user, you may need further configuration for your IDE to use the 
appropriate JDK for each module - see the [Eclipse IDE Setup](eclipse.md).

### IntelliJ configuration

Similarly, if you are an IntelliJ user, you may need further configuration for your IDE to use the 
appropriate JDK for each module - see the [IntelliJ IDE Setup](intellij.md).
