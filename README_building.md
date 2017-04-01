## Building memory
Use Apache Maven 3.0 to build this project

### Compiling and JDK7 vs JDK8
The internal com.yahoo.sketches.memory (Memory) repository leverages new API methods introduced with JDK8. 
The Memory package detects the JDK version, and if it is not JDK8, replaces these
JDK8 methods with surrogates using JDK7, which will be slower. 
 
In order for this to work, this library must be compiled using jdk8 
but with both source and target versions of jdk7 specified in pom.xml. 
The resultant jar will work on jdk7 and jdk8. 

Note that at some point in the near future jdk7 will no longer be supported, 
so you are advised to bring your execution environments up to jdk8 sooner rather than later.
 
