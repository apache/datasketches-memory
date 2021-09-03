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

# Java Platform Module System (JPMS) For JDK 9+

The [Java Platform Module System](https://openjdk.java.net/projects/jigsaw/spec/) defines a module 
system for the Java Platform. For more documentation on the implementation, see 
[JEP-261](https://openjdk.java.net/jeps/261).

#### Reliable configuration 

> Reliable configuration, to replace the brittle, error-prone class-path mechanism with a means 
for program components 
> to declare explicit dependences upon one another;

This prevents ClassLoader errors such as `NoClassDefFoundError` that typically occur at runtime 
and make applications less reliable.

#### Strong encapsulation

> Strong encapsulation, to allow a component to declare which of its APIs are accessible by other 
components, and which are not;

JDK internals are now strongly encapsulated, except for critical internal APIs such as 
`sun.misc.Unsafe` (see [JEP-396](https://openjdk.java.net/jeps/396) and 
[JEP-403](https://openjdk.java.net/jeps/403)).
Datasketches Memory can no longer access these APIs by default, and requires explicit access.

### Module declarations

A module declaration is a java file (typically `module-info.java`) that explicitly defines a 
dependency graph.

#### org.apache.datasketches.memory

In the `datasketches-memory-java9` maven submodule root, the following module declaration has 
been added:

```java
module org.apache.datasketches.memory {
    requires java.base;
    requires java.logging;
    requires jdk.unsupported;

    exports org.apache.datasketches.memory;
}
```

This declaration explicitly defines the dependencies for the `org.apache.datasketches.memory` module, as well as the 
external API. The `org.apache.datasketches.memory.internal` package is now inaccessible to the end user, 
providing better encapsulation. 

### Compiler arguments

Some dependencies are encapsulated by default, and this causes compilation to fail for 
Java versions 9 and above.
These dependencies can be made accessible at compile time through the use of the 
`add-exports` compiler argument.
This argument allows one module to access some of the unexported types of another module.  
Datasketches Memory depends on several internal APIs and therefore requires special 
exposition.

For example, in order to compile the `datasketches-memory-java9` submodule, the following compiler 
arguments are added to the Maven compiler plugin in the module's pom.xml file:

```xml
    <compilerArgs>
        <arg>--add-exports</arg>
        <arg>java.base/jdk.internal.ref=org.apache.datasketches.memory</arg>
    </compilerArgs>
```

### Runtime arguments (only when allocating off-heap memory)

When allocating off-heap memory using `WritableMemory.allocateDirect(...)`, 
reflection is used by the Datasketches Memory component to access JVM internal class 
fields and methods that do not have `public` visibility.  For JDK 9+, the JPMS
requires that the user add additional JVM run-time arguments (`add-opens...`, which permit this reflection.

Note that if the user has allocated off-heap memory using ByteBuffer.allocateDirect(...),
the DataSketches memory component can still read and write to this memory without these `add-opens...` arguments.

See the main [README](../README.md) and the [usage examples](usage-examples.md) for more details.

### JPMS and Java 8

Java 8 does not support module declarations and the JPMS module system, and no additional
runtime arguments are necessary.
However, support is retained for Java 8 users by only including the compiled declaration 
(`module-info.class`) in the `datasketches-memory` multi-release JAR for Java9 and above.
