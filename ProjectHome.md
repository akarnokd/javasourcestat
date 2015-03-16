# Introduction #
Java Source Statistics calculator aims to compute statistics of the source code not computed by other tools: the number of various java constructs, namely number of public methods, number of single line comments, etc.

Utilizes the [ANTLR](http://www.antlr.org/) technology along with the [JAXMEJS](http://ws.apache.org/jaxme/js/index.html) library.

For developers, it is an example howto use the [ExecutorService](http://java.sun.com/javase/6/docs/api/java/util/concurrent/ExecutorService.html) to recursively and in parallel process java source files, when you have multiple cores available.

# Installation #
  * Download and run with a command line parameter specifying the location of a Java source.

`java -jar javasourcestat.jar c:\projects\p1\src`

Note that the source must be compilable by `javac`, e.g. no syntax errors allowed.
The calculation fails on these java source files and they get an incomplete evaluation.

# System requirements #
  * Java 1.6+
  * 64MB RAM (might need more on larger projects. Use -Xmx128M to enlarge)
  * 2MB disk space

# Coming soon #

  * A more detailed Wiki page.

# Screenshot #

![http://karnokd.uw.hu/javasourcestat.png](http://karnokd.uw.hu/javasourcestat.png)