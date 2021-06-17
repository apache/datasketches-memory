Maven multi-module project
==========================

This project is a multi-module Maven project. A multi-module Maven project consists of an aggregator project 
(the root project), together with a set of submodules.  The aggregator's configuration is inherited by each
submodule, thus reducing duplication.

`datasketches-memory` makes use of some features of the Java platform, namely `Unsafe`, which have evolved in
Java versions 9 and above.   Therefore, a multi-module project allows us to add support for later versions of
Java by using indepent Maven modules to target platform-specific APIs.  For example, to deallocate references
a `sun.misc.Cleaner` may be used in Java8, but the `jdk.internal.ref.Cleaner` is used in Java 9.

This project has been divided into the following submodules:

* datasketches-memory-java8 (base version of the JVM that is currently supported)
* datasketches-memory-java8-tests
* datasketches-memory (JAR assembly, does not contain source files)

Artifact assembly
-----------------

The maven assembly plugin builds all artifacts for this project from the other modules within the project.  These
modules are complementary and not standalone.  Therefore, they are not installed and downloaded independently by
the end user.

Instead, the maven-assembly plugin builds all jars, and hides the multi-module
configuration from the end user.

The following jars are assembled by the `datasketches-memory` module:

* datasketches-memory-X.Y.Z.jar The compiled main class files.
* datasketches-memory-X.Y.Z-tests.jar The compiled test class files.
* datasketches-memory-X.Y.Z-sources.jar The main source files.
* datasketches-memory-X.Y.Z-test-sources.jar The test source files
* datasketches-memory-X.Y.Z-javadoc.jar  The compressed Javadocs.

Toolchains
----------

From the [maven-toolchain-plugin documentation](https://maven.apache.org/plugins/maven-toolchains-plugin/usage.html):
> A Toolchain is an object that Maven plugins can use to retrieve preconfigured tools (including location and
other information).
With the jdk toolchain, for example, instead of being stuck with the JDK used to run Maven, all plugins 
can use the same other JDK instance without hardcoding absolute paths into the pom.xml and without configuring
every plugin that require a path to JDK tools.

Since this project targets version-specific Java API's, toolchains are used in different maven modules to ensure
that the correct Java compiler version is used when compiling source files.
