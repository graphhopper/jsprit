####Requirements
jsprit requires the Java 2 platform (JDK version 1.6.0 or later).

####Modules
jsprit is a multi-module project and consists of:
- jsprit-core
- jsprit-analysis
- jsprit-instances
- jsprit-examples

It is hosted in this repository:

[https://github.com/jsprit/mvn-rep](https://github.com/jsprit/mvn-rep.git)

####Maven way
To use one of these modules, add the following lines to your pom. At first, tell Maven where to find the module-artifacts, i.e. add the jsprit-repository. If you want to use snapshots, add

<pre><code>&lt;repository&gt;
   &lt;id&gt;jsprit-snapshots&lt;/id&gt;
   &lt;url&gt;https://github.com/jsprit/mvn-rep/raw/master/snapshots&lt;/url&gt;
&lt;/repository&gt;
</code></pre>

and for releases, add
<pre><code>&lt;repository&gt;
   &lt;id&gt;jsprit-releases&lt;/id&gt;
   &lt;url&gt;https://github.com/jsprit/mvn-rep/raw/master/releases&lt;/url&gt;
&lt;/repository&gt;
</code></pre>

Now tell Maven how to identify the module-artifact in this repository by adding the artifact-dependency itself:
<pre><code>&lt;dependency&gt;
   &lt;groupId&gt;jsprit&lt;/groupId&gt;
   &lt;artifactId&gt;${module-name}&lt;/artifactId&gt;
   &lt;version&gt;${version}&lt;/version&gt;
&lt;/dependency&gt;
</code></pre>

For example, if you want to use the release v1.0.0 of jsprit-core, add:
<pre><code>&lt;dependency&gt;
   &lt;groupId&gt;jsprit&lt;/groupId&gt;
   &lt;artifactId&gt;jsprit-core&lt;/artifactId&gt;
   &lt;version&gt;1.0.0&lt;/version&gt;
&lt;/dependency&gt;
</code></pre>

[Add the latest snapshot to your pom](Add-latest-snapshot-to-your-pom.md).

[Add the latest release to your pom](Add-latest-release-to-your-pom.md).

####If you do not have an IDE and you want to use Maven

the following documentation is recommended:

<a href="http://docs.geotools.org/latest/userguide/tutorial/quickstart/index.html" target="blank_">GeoTools - Quickstart</a>

Here you learn to setup the Java environment and an Integrated Development Environment (IDE). In the subsection <em>Adding Jars to your Project</em> you learn to integrate external libraries in your project. Just copy/paste the above jsprit releases/snapshots to your pom.xml instead of the GeoTools-artifacts.

#### If you do not want Maven
to manage your dependencies, go to [snapshot-jars](https://github.com/jsprit/mvn-rep/tree/master/snapshots/jsprit) or [release-jars](https://github.com/jsprit/mvn-rep/tree/master/releases/jsprit) to download jsprit-binaries directly. Just click on the jar-file you want to download and use the 'Raw'-button to actually download it. Put the jars into your classpath. Note that you then need to put all [dependencies](https://github.com/jsprit/jsprit/wiki/Modules-and-Dependencies) jsprit relies on manually to your classpath as well.

Go ahead and show me a [simple example](Simple-Example.md) of how to setup and solve a vehicle routing problem.






