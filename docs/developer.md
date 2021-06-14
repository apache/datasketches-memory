Maven multi-module project
--------------------------

This project is a multi-module Maven project.

A multi-module Maven project consists of an aggregator project (the root project), together with a set of
submodules.  The aggregator's configuration is inherited by each submodule, thus reducing duplication.

A multi-module project is also used to target different version-specific features of the Java language APIs.
Furthermore, it is a structure that is supported in most IDEs allowing minimal disruption to developers that
contribute to this project.

This project has been divided into the following submodules:

* datasketches-memory-java8 (base version of the JVM that is currently supported)
* datasketches-memory-java8-tests
