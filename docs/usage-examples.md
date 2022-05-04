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

# Usage Examples

You may need to supply additional runtime arguments to the JVM depending on how you are using the Datasketches Memory library.
For more information regarding required JPMS arguments and when they are applicable, see the [README](../README.md).
This document provides examples for the following scenarios:

1. Using the library from a Java 8 application
2. Using the library with on-heap memory only
3. Using off-heap memory in a non-modularized Java 11+ application
4. Using off-heap memory in a modularized Java 11+ application

### 1) Using the library from a Java 8 application

No additional runtime arguments are required.  

As an example, consider the following launch script that compiles and runs a simple Java 8 application:

```shell
  JH=$JAVA8_HOME
  JAVAC=$JH/bin/javac
  JAVA=$JH/bin/java
  JAR=$JH/bin/jar
  
  patha=nomvn-jdk8
  
  cd $patha
  echo PWD:$(pwd)
  echo $JAVA_HOME
  
  echo "--- CLEAN & COMPILE ---"
  rm -rf target
  mkdir -p target/test-classes
  
  $JAVAC\
    -d target/test-classes/\
    -cp libs/*\
    $(find . -name '*.java')
  
  echo "---- RUN ----"
  
  $JAVA\ 
    -cp libs/*:target/test-classes:src/test/resources/\
    org.xyz.memory.RunMain
```

### 2) Using the library with on-heap memory only

Similarly, no additional runtime arguments are required in this scenario - regardless of whether the library is used from a Java 8 or Java 11+ application. 

As an example, consider the following launch script that compiles and runs a simple Java 11 application that only exclusively
uses on-heap memory:

```shell
  JH=$JAVA11_HOME
  JAVAC=$JH/bin/javac
  JAVA=$JH/bin/java
  JAR=$JH/bin/jar
  
  patha=nomvn-nomod-heap-jdk11
  
  cd $patha
  
  echo "--- CLEAN & COMPILE ---"
  rm -rf target
  mkdir -p target/test-classes
  
  $JAVAC\
    -d target/test-classes/\
    -cp "mods/*":"libs/*"
    -p mods
    $(find . -name '*.java')
  
  echo "---- RUN ----"
  
  $JAVA\
    -cp target/test-classes:"mods/*":"libs/*":src/test/resources\
    org.xyz.memory.RunMain
```

### 3) Using off-heap memory in a non-modularized Java 11+ application

The following section applies to applications that are not modularized JPMS applications.

In order to allocate off-heap memory using the `WritableMemory.allocateDirect(...)` method in Java 11 and above, you must provide the
following runtime arguments to the JVM:

```shell
    --add-exports java.base/jdk.internal.misc=ALL-UNNAMED\
    --add-exports java.base/jdk.internal.ref=ALL-UNNAMED\
    --add-opens java.base/java.nio=ALL-UNNAMED\
    --add-opens java.base/sun.nio.ch=ALL-UNNAMED\
```

These arguments expose encapsulated packages in the `java.base` package to the `org.apache.datasketches.memory` module,
which runs as an UNNAMED module in a non-JPMS (non-modularized) application.

The following launch script compiles and runs a non-modularized Java 11 application:

```shell
  JH=$JAVA11_HOME
  JAVAC=$JH/bin/javac
  JAVA=$JH/bin/java
  JAR=$JH/bin/jar

  patha=nomvn-nomod-jdk11
  
  cd $patha
  
  echo "--- CLEAN & COMPILE ---"
  rm -rf target
  mkdir -p target/test-classes
  
  $JAVAC\
    -d target/test-classes/\
    -cp "mods/*":"libs/*"
    -p mods
    $(find . -name '*.java')
  
  echo "---- RUN ----"
  
  $JAVA\
    --add-exports java.base/jdk.internal.misc=ALL-UNNAMED\
    --add-exports java.base/jdk.internal.ref=ALL-UNNAMED\
    --add-opens java.base/java.nio=ALL-UNNAMED\
    --add-opens java.base/sun.nio.ch=ALL-UNNAMED\
    -cp target/test-classes:"mods/*":"libs/*":src/test/resources\
    org.xyz.memory.RunMain
```
where the traditional classpath (`-cp`) argument contains all modules, libraries and resources. 

Note: `mods` is a local directory containing external modules, and `libs` is a local directory for external library
dependencies.  No distinction is made between modules and libraries since they are both appended to the classpath.

### 4) Using off-heap memory in a modularized Java 11+ application

The following section applies to modularized JPMS applications.

In order to allocate off-heap memory using the `WritableMemory.allocateDirect(...)` method in Java 11 and above, you must provide the
following runtime arguments to the JVM:

```shell
    --add-exports java.base/jdk.internal.misc=org.apache.datasketches.memory\
    --add-exports java.base/jdk.internal.ref=org.apache.datasketches.memory\
    --add-opens java.base/java.nio=org.apache.datasketches.memory\
    --add-opens java.base/sun.nio.ch=org.apache.datasketches.memory\
```

These arguments expose encapsulated packages in the `java.base` package to the `org.apache.datasketches.memory` module.

The following launch script compiles and runs a modularized Java 11 application:

```shell
  JH=$JAVA11_HOME
  JAVAC=$JH/bin/javac
  JAVA=$JH/bin/java
  JAR=$JH/bin/jar
  
  patha=nomvn-mod-jdk11
  
  cd $patha
  echo PWD:$(pwd)
  echo $JAVA_HOME
  
  echo "--- CLEAN & COMPILE ---"
  rm -rf target
  mkdir -p target/test-classes
  
  $JAVAC\
    -d target/test-classes/\
    -cp "mods/*":"libs/*"
    -p mods
    $(find . -name '*.java')
  
  echo "---- RUN ----"
  echo PWD:$(pwd)
  
  $JAVA\
    --add-opens java.base/java.nio=org.apache.datasketches.memory\
    --add-opens java.base/sun.nio.ch=org.apache.datasketches.memory\
    -cp "/libs/*":src/test/resources\
    -p target/test-classes:mods\
    -m org.xyz.memory/org.xyz.memory.RunMain
```
where the traditional classpath (`-cp`) argument contains libraries and resources, and the module-path argument (`-p`)
references all external modules and compiled classes for the current user application, which is itself a module.
