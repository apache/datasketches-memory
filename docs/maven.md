# Maven multi-module project

This project is a multi-module Maven project. A multi-module Maven project consists of an aggregator project 
(the `datasketches-memory-root` project), together with a set of submodules.  The aggregator's configuration is inherited
by each submodule, thus reducing duplication.

`datasketches-memory` makes use of some features of the Java platform, namely `Unsafe`, which have evolved in
Java versions 9 and above.   Therefore, a multi-module project allows us to add support for later versions of
Java by using indepent Maven modules to target platform-specific APIs.  For example, to deallocate references
a `sun.misc.Cleaner` may be used in Java8, but the `jdk.internal.ref.Cleaner` is used in Java 9.

This project has been divided into the following submodules:

* datasketches-memory-java8 (base version of the JVM that is currently supported)
* datasketches-memory-java8-tests
* datasketches-memory-java9 (Java9 equivalent of some platform specific classes in datasketches-memory-java8)
* datasketches-memory-java9-tests
* datasketches-memory-java11 (Java11 equivalent of some platform specific classes in datasketches-memory-java8)
* datasketches-memory-java11-tests
* datasketches-memory (JAR assembly, does not contain source files)
* datasketches-memory-tests (Runs test suite against assembled JAR)

### Artifact assembly

The [Maven assembly plugin](https://maven.apache.org/plugins/maven-assembly-plugin/) builds all artifacts for this
project from the other modules within the project.  These modules are complementary and not standalone. 
Therefore, they are not installed and downloaded independently by the end user.

Instead, the Maven assembly plugin builds all jars, and hides the multi-module configuration from the end user.

The following jars are assembled by the `datasketches-memory` module:

* datasketches-memory-X.Y.Z.jar The compiled main class files.
* datasketches-memory-X.Y.Z-tests.jar The compiled test class files.
* datasketches-memory-X.Y.Z-sources.jar The main source files.
* datasketches-memory-X.Y.Z-test-sources.jar The test source files
* datasketches-memory-X.Y.Z-javadoc.jar  The compressed Javadocs.
