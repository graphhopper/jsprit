####Requirements
jsprit requires the Java 2 platform (JDK version 1.7.0 or later).

####Modules
jsprit is a multi-module project and consists of:
- jsprit-core
- jsprit-analysis
- jsprit-instances
- jsprit-examples
- jsprit-io

####Maven way
To use jsprit-core, add the following lines to your pom:

<pre><code>&lt;dependency&gt;
   &lt;groupId&gt;com.graphhopper&lt;/groupId&gt;
   &lt;artifactId&gt;jsprit-core&lt;/artifactId&gt;
   &lt;version&gt;1.7-RC1&lt;/version&gt;
&lt;/dependency&gt;
</code></pre>

####If you do not have an IDE and you want to use Maven

the following documentation is recommended:

<a href="http://docs.geotools.org/latest/userguide/tutorial/quickstart/index.html" target="blank_">GeoTools - Quickstart</a>

Here you learn to setup the Java environment and an Integrated Development Environment (IDE). In the subsection <em>Adding Jars to your Project</em> you learn to integrate external libraries in your project. Just copy/paste the above jsprit releases/snapshots to your pom.xml instead of the GeoTools-artifacts.

#### If you do not want Maven
to manage your dependencies, go to [snapshot-jars](https://github.com/jsprit/mvn-rep/tree/master/snapshots/jsprit) or [release-jars](https://github.com/jsprit/mvn-rep/tree/master/releases/jsprit) to download jsprit-binaries directly. Just click on the jar-file you want to download and use the 'Raw'-button to actually download it. Put the jars into your classpath. Note that you then need to put all [dependencies](https://github.com/jsprit/jsprit/wiki/Modules-and-Dependencies) jsprit relies on manually to your classpath as well.

Go ahead and show me a [simple example](https://github.com/jsprit/jsprit/wiki/Simple-Example) of how to setup and solve a vehicle routing problem.






