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

# Usage Instructions

You may need to supply additional runtime arguments to the JVM depending on how you are using the Datasketches Memory library.
There are several applicable use cases that are considered:

1. Using the library from a Java 8 application
2. Using the library with on-heap memory only
3. Using off-heap memory in a non-modularized Java 9+ application
4. Using off-heap memory in a modularized Java 9+ application

### 1) Using the library from a Java 8 application

No additional runtime arguments are required.  

As an example, consider the following launch script that compiles and runs a simple Java 8 application:

```shell
  export JAVA_HOME=$JAVA8_HOME
  export JAVAC=$JAVA_HOME/bin/javac
  export JAR=$JAVA_HOME/bin/jar
  export JAVA=$JAVA_HOME/bin/java
  patha=nomvn-jdk8
  
  cd $patha
  echo PWD:$(pwd)
  echo $JAVA_HOME
  
  echo "--- CLEAN & COMPILE ---"
  rm -rf target
  mkdir target
  mkdir target/classes
  mkdir target/test-classes
  
  $JAVAC -d target/test-classes/  -cp "libs/*"  $(find . -name '*.java')
  
  echo "---- RUN ----"
  echo PWD:$(pwd)
  
  $JAVA\ 
    -cp target/test-classes:libs/*:src/test/resources/\
    org.xyz.memory.CheckJava8
```

### 2) Using the library with on-heap memory only, or with off-heap memory allocated via ByteBuffer by the user.

No additional runtime arguments are required, regardless of whether the library is used from a Java 8 or Java 9+
application. 

As an example, consider the following launch script that compiles and runs a simple Java 9 application that only exclusively
uses on-heap memory:

```shell
  export JAVA_HOME=$JAVA9_HOME
  export JAVAC=$JAVA_HOME/bin/javac
  export JAR=$JAVA_HOME/bin/jar
  export JAVA=$JAVA_HOME/bin/java
  patha=nomvn-nomod-heap-jdk9
  
  cd $patha
  echo PWD:$(pwd)
  echo $JAVA_HOME
  
  echo "--- CLEAN & COMPILE ---"
  rm -rf target
  mkdir target
  mkdir target/classes
  mkdir target/test-classes
  
  $JAVAC -d target/test-classes -cp "mods/*":"libs/*" -p mods $(find . -name '*.java')
  
  echo "---- RUN ----"
  
  echo PWD:$(pwd)
  
  $JAVA\
    -cp target/test-classes:"mods/*":"libs/*":src/test/resources\
    org.xyz.memory.CheckJava9plus
```

### 3) Using off-heap memory in a non-modularized Java 9+ application

The following section applies to applications that are not modularized JPMS applications.

In order to allocate off-heap memory using the `WritableMemory.allocateDirect(...)` method in Java 9 and above, you must provide the
following runtime arguments to the JVM:

```shell
    --add-exports java.base/jdk.internal.misc=ALL-UNNAMED\
    --add-exports java.base/jdk.internal.ref=ALL-UNNAMED\
    --add-opens java.base/java.nio=ALL-UNNAMED\
    --add-opens java.base/sun.nio.ch=ALL-UNNAMED\
```

These arguments expose encapsulated packages in the `java.base` package to the `org.apache.datasketches.memory` module,
which runs as an UNNAMED module in a non-JPMS (non-modularized) application.

The following launch script compiles and runs a non-modularized Java 9 application:

```shell

  export JAVA_HOME=$JAVA9_HOME
  export JAVAC=$JAVA_HOME/bin/javac
  export JAR=$JAVA_HOME/bin/jar
  export JAVA=$JAVA_HOME/bin/java
  patha=nomvn-nomod-jdk9
  
  cd $patha
  echo PWD:$(pwd)
  echo $JAVA_HOME
  
  echo "--- CLEAN & COMPILE ---"
  rm -rf target
  mkdir target
  mkdir target/classes
  mkdir target/test-classes
  
  $JAVAC -d target/test-classes -cp "mods/*":"libs/*" -p mods $(find . -name '*.java')
  
  echo "---- RUN ----"
  
  echo PWD:$(pwd)
  
  $JAVA\
    --add-exports java.base/jdk.internal.misc=ALL-UNNAMED\
    --add-exports java.base/jdk.internal.ref=ALL-UNNAMED\
    --add-opens java.base/java.nio=ALL-UNNAMED\
    --add-opens java.base/sun.nio.ch=ALL-UNNAMED\
    -cp target/test-classes:"mods/*":"libs/*":src/test/resources\
    org.xyz.memory.CheckJava9plus
```
where the traditional classpath (`-cp`) argument contains all modules, libraries and resources. 

Note: `mods` is a local directory containing external modules, and `libs` is a localy directory for external library
dependencies.  No distinction is made between modules and libraries since they are both appended to the classpath.

### 4) Using off-heap memory in a modularized Java 9+ application

The following section applies to modularized JPMS applications.

In order to allocate off-heap memory using the `WritableMemory.allocateDirect(...)` method in Java 9 and above, you must provide the
following runtime arguments to the JVM:

```shell
    --add-exports java.base/jdk.internal.misc=org.apache.datasketches.memory\
    --add-exports java.base/jdk.internal.ref=org.apache.datasketches.memory\
    --add-opens java.base/java.nio=org.apache.datasketches.memory\
    --add-opens java.base/sun.nio.ch=org.apache.datasketches.memory\
```

These arguments expose encapsulated packages in the `java.base` package to the `org.apache.datasketches.memory` module.

The following launch script compiles and runs a modularized Java 9 application:

```shell
  export JAVA_HOME=$JAVA9_HOME
  export JAVAC=$JAVA_HOME/bin/javac
  export JAR=$JAVA_HOME/bin/jar
  export JAVA=$JAVA_HOME/bin/java
  patha=nomvn-mod-jdk9
  
  cd $patha
  echo PWD:$(pwd)
  echo $JAVA_HOME
  
  echo "--- CLEAN & COMPILE ---"
  rm -rf target
  mkdir target
  mkdir target/classes
  mkdir target/test-classes
  
  $JAVAC -d target/test-classes -cp "libs/*" -p mods $(find . -name '*.java')
  
  echo "---- RUN ----"
  echo PWD:$(pwd)
  
  $JAVA\
    --add-opens java.base/java.nio=org.apache.datasketches.memory\
    --add-opens java.base/sun.nio.ch=org.apache.datasketches.memory\
    -cp "/libs/*":src/test/resources\
    -p target/test-classes:mods\
    -m org.xyz.memory/org.xyz.memory.CheckJava9plus
```
where the traditional classpath (`-cp`) argument contains libraries and resources, and the module-path argument (`-p`)
references all external modules and compiled classes for the current user application, which is itself a module.
