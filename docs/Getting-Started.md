#### Requirements
jsprit requires Java 1.7.0 or later.

#### Modules
jsprit is a multi-module project and consists of:
- jsprit-core
- jsprit-analysis
- jsprit-instances
- jsprit-examples
- jsprit-io

#### Maven way
If you want to use the latest release of jsprit-core, add the following lines to your pom:

```
&lt;dependency&gt;
   &lt;groupId&gt;com.graphhopper&lt;/groupId&gt;
   &lt;artifactId&gt;jsprit-core&lt;/artifactId&gt;
   &lt;version&gt;{version}&lt;/version&gt;
&lt;/dependency&gt;
```

Find the latest versions here: [mvn repository](https://mvnrepository.com/artifact/com.graphhopper/jsprit-core)

#### Build yourself
If you want to build the master branch yourself, do this:

```
git clone https://github.com/graphhopper/jsprit.git
cd jsprit
mvn clean install
```

#### If you do not have an IDE and you want to use Maven

the following documentation is recommended:

<a href="http://docs.geotools.org/latest/userguide/tutorial/quickstart/index.html" target="blank_">GeoTools - Quickstart</a>

Here you learn to setup the Java environment and an Integrated Development Environment (IDE). In the subsection <em>Adding Jars to your Project</em> you learn to integrate external libraries in your project. Just copy/paste the above jsprit releases/snapshots to your pom.xml instead of the GeoTools-artifacts.

#### If you do not want Maven

to manage your dependencies, go to [maven central](https://search.maven.org/), search for jsprit and download the latest binaries to put them into your classpath.

Go ahead and show me a [simple example](Simple-Example.md) of how to setup and solve a vehicle routing problem.






