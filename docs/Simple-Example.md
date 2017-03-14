This example covers
- building a problem,
- building vehicleTypes and vehicles with capacity restriction,
- building services (or customer locations),
- plotting the problem,
- reading and running a predefined algorithm,
- writing out problem and solution,
- plotting the solution,
- printing solution stats.

Add the latest release to your pom (see [Getting Started](Getting-Started.md)).

Assume the following problem. We can employ one vehicle(-type) located at (10,10) with one capacity dimension, e.g. weight, and a capacity value of 2 to deliver four customers located at [(5,7),(5,13),(15,7),(15,13)], each with a demand that has a weight of 1. All employed vehicles need to return to their start-locations. Setting up this problem and solving it is as simple as coding the following lines:

First, build a vehicle with its vehicle-type:

<pre><code>/*
 * get a vehicle type-builder and build a type with the typeId "vehicleType" and a capacity of 2
 * you are free to add an arbitrary number of capacity dimensions with .addCapacityDimension(dimensionIndex,dimensionValue)
 */
final int WEIGHT_INDEX = 0;
VehicleTypeImpl.Builder vehicleTypeBuilder = VehicleTypeImpl.Builder.newInstance("vehicleType").addCapacityDimension(WEIGHT_INDEX,2);
VehicleType vehicleType = vehicleTypeBuilder.build();

/*
 * get a vehicle-builder and build a vehicle located at (10,10) with type "vehicleType"
 */
VehicleImpl.Builder vehicleBuilder = VehicleImpl.Builder.newInstance("vehicle");
vehicleBuilder.setStartLocation(Location.newInstance(10, 10));
vehicleBuilder.setType(vehicleType);
VehicleImpl vehicle = vehicleBuilder.build();
</code></pre>

Second, define the deliveries as services. Make sure their size dimensions are in line with your vehicle capacity dimensions (thus here the weight-index is made final and also used to define services).
<pre><code>/*
 * build services with id 1...4 at the required locations, each with a capacity-demand of 1.
 * Note, that the builder allows chaining which makes building quite handy
 */
Service service1 = Service.Builder.newInstance("1").addSizeDimension(WEIGHT_INDEX,1).setLocation(Location.newInstance(5, 7)).build();
Service service2 = Service.Builder.newInstance("2").addSizeDimension(WEIGHT_INDEX,1).setLocation(Location.newInstance(5, 13)).build();
Service service3 = Service.Builder.newInstance("3").addSizeDimension(WEIGHT_INDEX,1).setLocation(Location.newInstance(15, 7)).build();
Service service4 = Service.Builder.newInstance("4").addSizeDimension(WEIGHT_INDEX,1).setLocation(Location.newInstance(15, 13)).build();
</code></pre>

and put vehicles and services together to setup the problem.
<pre><code>/*
 * again define a builder to build the VehicleRoutingProblem
 */
VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
vrpBuilder.addVehicle(vehicle);
vrpBuilder.addJob(service1).addJob(service2).addJob(service3).addJob(service4);
/*
 * build the problem
 * by default, the problem is specified such that FleetSize is INFINITE, i.e. an infinite number of
 * the defined vehicles can be used to solve the problem
 * by default, transport costs are computed as Euclidean distances
 */
VehicleRoutingProblem problem = vrpBuilder.build();
</code></pre>


Third, solve the problem by defining and running an algorithm. Here it comes out-of-the-box.
<pre><code>/*
* get the algorithm out-of-the-box.
*/
VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(problem);

/*
* and search a solution which returns a collection of solutions (here only one solution is constructed)
*/
Collection&lt;VehicleRoutingProblemSolution&gt; solutions = algorithm.searchSolutions();

/*
 * use the static helper-method in the utility class Solutions to get the best solution (in terms of least costs)
 */
VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);
</code></pre>

If you want to print a concise summary of the results to the console, do this:

<code>SolutionPrinter.print(problem, bestSolution, Print.CONCISE);</code>

which results in
<pre><samp>+--------------------------+
| problem                  |
+---------------+----------+
| indicator     | value    |
+---------------+----------+
| nJobs         | 4        |
| nServices     | 4        |
| nShipments    | 0        |
| fleetsize     | INFINITE |
+--------------------------+
+----------------------------------------------------------+
| solution                                                 |
+---------------+------------------------------------------+
| indicator     | value                                    |
+---------------+------------------------------------------+
| costs         | 35.3238075793812                         |
| nVehicles     | 2                                        |
+----------------------------------------------------------+
</samp></pre>

If you want to have more information about individual routes, use the verbose level such as

<code>SolutionPrinter.print(problem, bestSolution, Print.VERBOSE);</code>

and you get this addtionally:
<pre><samp>+--------------------------------------------------------------------------------------------------------------------------------+
| detailed solution                                                                                                              |
+---------+----------------------+-----------------------+-----------------+-----------------+-----------------+-----------------+
| route   | vehicle              | activity              | job             | arrTime         | endTime         | costs           |
+---------+----------------------+-----------------------+-----------------+-----------------+-----------------+-----------------+
| 1       | vehicle              | start                 | -               | undef           | 0               | 0               |
| 1       | vehicle              | service               | 2               | 6               | 6               | 6               |
| 1       | vehicle              | service               | 1               | 12              | 12              | 12              |
| 1       | vehicle              | end                   | -               | 18              | undef           | 18              |
+---------+----------------------+-----------------------+-----------------+-----------------+-----------------+-----------------+
| 2       | vehicle              | start                 | -               | undef           | 0               | 0               |
| 2       | vehicle              | service               | 3               | 6               | 6               | 6               |
| 2       | vehicle              | service               | 4               | 12              | 12              | 12              |
| 2       | vehicle              | end                   | -               | 18              | undef           | 18              |
+--------------------------------------------------------------------------------------------------------------------------------+
</samp></pre>

If you want to write out problem and solution, you require the writer that is located in jsprit-io. Thus, you need to add the
corresponding module to your pom.xml much like you added jsprit-core. Just use jsprit-io. You can then use this:

<pre><code>new VrpXMLWriter(problem, solutions).write("output/problem-with-solution.xml");
</code></pre>
which looks like this: [problem-with-solution.xml](https://github.com/jsprit/misc-rep/raw/master/wiki-images/problem-with-solution.xml).

If you want to further analyse your solution, add the module jsprit-analysis to your pom. Then you can plot the routes like this

<code>new Plotter(problem,bestSolution).plot("output/solution.png", "solution");</code>

and you get [solution.png](https://github.com/jsprit/misc-rep/blob/master/wiki-images/solution.png)

or use the GraphStreamViewer which dynamically renders the problem and its according solution by coding

<code>new GraphStreamViewer(problem, bestSolution).setRenderDelay(100).display();</code>

You can find the entire code [here](https://github.com/graphhopper/jsprit/blob/master/jsprit-examples/src/main/java/com/graphhopper/jsprit/examples/SimpleExample.java).
