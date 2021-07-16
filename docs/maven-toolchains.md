# Maven toolchains

From the [maven-toolchain-plugin documentation](https://maven.apache.org/plugins/maven-toolchains-plugin/usage.html):

> A Toolchain is an object that Maven plugins can use to retrieve preconfigured tools (including location and
other information).
With the jdk toolchain, for example, instead of being stuck with the JDK used to run Maven, all plugins
can use the same other JDK instance without hardcoding absolute paths into the pom.xml and without configuring
every plugin that require a path to JDK tools.
> 

##### Motivation

Toolchains are used in different maven modules to ensure that the correct Java compiler version is used when compiling source files.  This is because `datasketches-memory` uses some JDK version-specific APIs, which require different JDKs to compile correctly.

##### Toolchain template

Your local environment requires toolchain entries for Java 8, 9 and 11 to build this project.  These can be found in a reference `toolchains.xml` template in the `tools` directory.
Any maven commands used during development can be supplemented with: `--toolchains tools/toolchains.xml`, without permanently modifying a local `~/.m2/toolchains.xml` file (recommended).

Alternatively, the toolchain template can be copied to your local maven `toolchains.xml` e.g. `~/.m2/toolchains.xml`.  If there is already a locally configured `toolchains.xml` file, the requisite entries should be merged into the existing file if they do not already exist.

##### Environment variables

The `dataSketches-memory` component is pure Java and requires the following JDKs to compile:
- JDK8/Hotspot
- JDK9/Hotspot
- JDK11/Hotspot

The following environment variables should be set as follows:

| Environment variable              | Value                                 |
| --------------------------------- | ------------------------------------- |
| JAVA8_HOME                        |  Home directory for Java 8 (openJDK)  |
| JAVA9_HOME                        |  Home directory for Java 9 (openJDK)  |
| JAVA11_HOME                       |  Home directory for Java 11 (openJDK) |

For example, if you are using [SDKMAN!](https://sdkman.io/), your environment might be configured as follows:

- JAVA8_HOME: `/Users/me/.sdkman/candidates/java/8.0.282.hs-adpt`
- JAVA9_HOME: `/Users/me/.sdkman/candidates/java/9.0.4-open`
- JAVA11_HOME: `/Users/me/.sdkman/candidates/java/11.0.10.hs-adpt`

##### Eclipse configuration

If you are an Eclipse user, you may need further configuration for your IDE to use the appropriate JDK for each module - see the [eclipse IDE setup](eclipse.md).

### IntelliJ configuration

Similarly, if you are an Eclipse user, you may need further configuration for your IDE to use the appropriate JDK for each module - see the [eclipse IDE setup](eclipse.md).
