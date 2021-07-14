# Java Platform Module System (JPMS)

The [Java Platform Module System](https://openjdk.java.net/projects/jigsaw/spec/) defines a module system for the Java
Platform. For more documentation on the implementation, see [JEP-261](https://openjdk.java.net/jeps/261).

#### Reliable configuration 

> Reliable configuration, to replace the brittle, error-prone class-path mechanism with a means for program components 
> to declare explicit dependences upon one another;

This prevents ClassLoader errors such as `NoClassDefFoundError` that typically occur at runtime and make applications
less reliable.

#### Strong encapsulation

> Strong encapsulation, to allow a component to declare which of its APIs are accessible by other components, and which
> are not;

JDK internals are now strongly encapsulated, except for critical internal APIs such as `sun.misc.Unsafe`
(see [JEP-396](https://openjdk.java.net/jeps/396) and [JEP-403](https://openjdk.java.net/jeps/403)).
`datasketches-memory` can no longer access these APIs by default, and requires explicit access.

### Module declarations

A module declaration is a java file (typically `module-info.java`) that explicitly defines a dependency graph.

#### org.apache.datasketches.memory

In the `datasketches-memory-java9` maven submodule root, the following module declaration has been added:

```java
module org.apache.datasketches.memory {
    requires java.base;
    requires java.logging;
    requires jdk.unsupported;

    exports org.apache.datasketches.memory;
    exports org.apache.datasketches.memory.internal to org.apache.datasketches.memory.tests;
}
```

This declaration explicitly defines the dependencies for `datasketches-memory`, as well as the external API.
The `org.apache.datasketches.internal` package is now inaccessible to the end user, providing better encapsulation. 

#### org.apache.datasketches.memory.tests

The module declaration above makes provision for unit testing.  The `org.apache.datasketches.internal` package is not
accessible to the end user, but is accessible to the `org.apache.datasketches.memory.tests` module:

```java
module org.apache.datasketches.memory.tests {
    requires java.base;
    requires org.testng;
    requires org.apache.datasketches.memory;
}
```

### Compiler arguments

Some dependencies are encapsulated by default, and this causes compilation to fail for Java versions 9 and above.
These dependencies can be made accessible at compile time through the use of the `add-exports` compiler argument.
This argument allows one module to access some of the unexported types of another module.  Datasketches memory has come
to depend on several internal APIs and therefore requires special exposition.

For example, in order to compile the `datasketches-memory-java9` submodule, the following compiler arguments are added
to the Maven compiler plugin in the module's pom.xml file:

```xml
    <compilerArgs>
        <arg>--add-exports</arg>
        <arg>java.base/jdk.internal.ref=org.apache.datasketches.memory</arg>
    </compilerArgs>
```

### Runtime arguments

Reflection is used by the datasketches memory library in cases where fields and methods that do not have `public` visibility
in a class.  Reflective access requires additional arguments to be provided by the user at runtime, in order to use the 
`datasketches-memory` JPMS module in Java versions 9 and above.

The following runtime arguments should be provided when using the library:
```shell
    --add-opens java.base/java.nio=org.apache.datasketches.memory \
    --add-opens java.base/jdk.internal.misc=org.apache.datasketches.memory \
    --add-opens java.base/jdk.internal.ref=org.apache.datasketches.memory
```

### JPMS and Java 8

Java 8 does not support module declarations and the JPMS module system.
However, support is retained for Java 8 users by only including the compiled declaration (`module-info.class`)
in the `datasketches-memory` multi-release JAR for Java9 and above.
