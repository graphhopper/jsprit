This example covers:
- defining and creating different 'depots', vehicles and their types
- defining a problem with finite fleet-size
- reading and creating an algorithm
- plotting the solution

It is based on the problem instance P01 defined by 

<em>Cordeau, J.-F., Gendreau, M. and Laporte, G. (1997), A tabu search heuristic for periodic and multi-depot vehicle routing problems. Networks, 30: 105–119.</em> 

Please visit for example <a href="http://neo.lcc.uma.es/vrp/vrp-flavors/multiple-depot-vrp/" target="_blank">this site</a> to get more information on Multiple Depot VRP.

Before you start, [add the latest release to your pom](https://github.com/jsprit/jsprit/wiki/Add-latest-release-to-your-pom). Additionally, create an output folder in your project directory. Either do it manually or add the following lines to your code (even this obfuscates the code-example a bit):
<pre><code>File dir = new File("output");
// if the directory does not exist, create it
if (!dir.exists()){
	System.out.println("creating directory ./output");
	boolean result = dir.mkdir();  
	if(result) System.out.println("./output created");  
}
</code></pre>

All services of P01 are stored in an xml-file called vrp_cordeau_01.xml (you can find it [here](https://github.com/jsprit/jsprit/tree/master/jsprit-examples/input)). To read them into your problemBuilder, code the following lines:

<pre><code>VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
/*
 * Read cordeau-instance p01, BUT only its services without any vehicles 
 */
new VrpXMLReader(vrpBuilder).read("input/vrp_cordeau_01.xml");
</code></pre>

Define and add depots, vehicles and their types as follows:

<pre><code>/*
 * add vehicles with its depots
 * 4 depots with the following coordinates:
 * (20,20), (30,40), (50,30), (60,50)
 * 
 * each with 4 vehicles each with a capacity of 80
 */
int nuOfVehicles = 4;
int capacity = 80;
Coordinate firstDepotCoord = Coordinate.newInstance(20, 20);
Coordinate second = Coordinate.newInstance(30, 40);
Coordinate third = Coordinate.newInstance(50, 30);
Coordinate fourth = Coordinate.newInstance(60, 50);
		
int depotCounter = 1;
for(Coordinate depotCoord : Arrays.asList(firstDepotCoord,second,third,fourth)){
&nbsp;&nbsp;&nbsp;&nbsp;for(int i=0;i&lt;nuOfVehicles;i++){
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;String typeId = depotCounter + "_type";
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;VehicleType vehicleType = VehicleTypeImpl.Builder.newInstance(typeId).addCapacityDimension(0,capacity).setCostPerDistance(1.0).build();
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;String vehicleId = depotCounter + "_" + (i+1) + "_vehicle";
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;VehicleImpl.Builder vehicleBuilder = VehicleImpl.Builder.newInstance(vehicleId);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;vehicleBuilder.setStartLocation(Location.newInstance(depotCoord.getX(),depotCoord.getY()));  //defines the location of the vehicle and thus the depot
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;vehicleBuilder.setType(vehicleType)
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;VehicleImpl vehicle = vehicleBuilder.build();
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;vrpBuilder.addVehicle(vehicle);
	}
	depotCounter++;
}
</code></pre>

<em>Note that there is no explicit depot definition. Depots are defined by the location of vehicles.</em>

Set finite fleet-size and build the problem.
<pre><code>/*
 * define problem with finite fleet
 */
vrpBuilder.setFleetSize(FleetSize.FINITE);
		
/*
 * build the problem
 */
VehicleRoutingProblem vrp = vrpBuilder.build();
</code></pre>

Plot it, to see how it looks like.
<pre><code>Plotter plotter = new Plotter(vrp).plot("output/problem01.png", "p01");
</code></pre>

It looks like this:
![p01](https://github.com/jsprit/misc-rep/raw/master/wiki-images/problem01.png)

Define and run an algorithm to solve the problem.
<pre><code>/*
 * solve the problem
 */
VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
</code></pre>

You can plot it by:

<pre><code>Plotter plotter = new Plotter(vrp,Solutions.bestOf(solutions));
plotter.plot("output/p01_solution.png", "p01");
</code></pre>

![p01](https://github.com/jsprit/misc-rep/raw/master/wiki-images/p01_solution.png)

and print the results to your console by:

<pre><code>SolutionPrinter.print(vrp,Solutions.bestOf(solutions),Print.VERBOSE);
</code></pre>

<pre><samp>+--------------------------+
| problem                  |
+---------------+----------+
| indicator     | value    |
+---------------+----------+
| nJobs         | 50       | 
| nServices     | 50       | 
| nShipments    | 0        | 
| fleetsize     | FINITE   | 
+--------------------------+
+----------------------------------------------------------+
| solution                                                 |
+---------------+------------------------------------------+
| indicator     | value                                    |
+---------------+------------------------------------------+
| costs         | 582.9805315622696                        | 
| nVehicles     | 11                                       | 
+----------------------------------------------------------+
+--------------------------------------------------------------------------------------------------------------------------------+
| detailed solution                                                                                                              |
+---------+----------------------+-----------------------+-----------------+-----------------+-----------------+-----------------+
| route   | vehicle              | activity              | job             | arrTime         | endTime         | costs           |
+---------+----------------------+-----------------------+-----------------+-----------------+-----------------+-----------------+
| 1       | 3_1_vehicle          | start                 | -               | undef           | 0               | 0               |
| 1       | 3_1_vehicle          | service               | 9               | 4               | 4               | 4               |
| 1       | 3_1_vehicle          | service               | 34              | 13              | 13              | 13              |
| 1       | 3_1_vehicle          | service               | 30              | 19              | 19              | 19              |
| 1       | 3_1_vehicle          | service               | 39              | 31              | 31              | 31              |
| 1       | 3_1_vehicle          | service               | 10              | 41              | 41              | 41              |
| 1       | 3_1_vehicle          | end                   | -               | 50              | undef           | 50              |
+---------+----------------------+-----------------------+-----------------+-----------------+-----------------+-----------------+
| 2       | 2_2_vehicle          | start                 | -               | undef           | 0               | 0               |
| 2       | 2_2_vehicle          | service               | 11              | 12              | 12              | 12              |
| 2       | 2_2_vehicle          | service               | 32              | 18              | 18              | 18              |
| 2       | 2_2_vehicle          | service               | 1               | 25              | 25              | 25              |
| 2       | 2_2_vehicle          | service               | 22              | 32              | 32              | 32              |
| 2       | 2_2_vehicle          | service               | 28              | 42              | 42              | 42              |
| 2       | 2_2_vehicle          | service               | 31              | 48              | 48              | 48              |
| 2       | 2_2_vehicle          | service               | 26              | 58              | 58              | 58              |
| 2       | 2_2_vehicle          | end                   | -               | 86              | undef           | 86              |
+---------+----------------------+-----------------------+-----------------+-----------------+-----------------+-----------------+
| 3       | 2_4_vehicle          | start                 | -               | undef           | 0               | 0               |
| 3       | 2_4_vehicle          | service               | 46              | 2               | 2               | 2               |
| 3       | 2_4_vehicle          | service               | 12              | 9               | 9               | 9               |
| 3       | 2_4_vehicle          | service               | 47              | 15              | 15              | 15              |
| 3       | 2_4_vehicle          | end                   | -               | 25              | undef           | 25              |
+---------+----------------------+-----------------------+-----------------+-----------------+-----------------+-----------------+
| 4       | 2_1_vehicle          | start                 | -               | undef           | 0               | 0               |
| 4       | 2_1_vehicle          | service               | 23              | 22              | 22              | 22              |
| 4       | 2_1_vehicle          | service               | 7               | 28              | 28              | 28              |
| 4       | 2_1_vehicle          | service               | 43              | 40              | 40              | 40              |
| 4       | 2_1_vehicle          | service               | 24              | 53              | 53              | 53              |
| 4       | 2_1_vehicle          | service               | 14              | 63              | 63              | 63              |
| 4       | 2_1_vehicle          | end                   | -               | 81              | undef           | 81              |
+---------+----------------------+-----------------------+-----------------+-----------------+-----------------+-----------------+
| 5       | 4_2_vehicle          | start                 | -               | undef           | 0               | 0               |
| 5       | 4_2_vehicle          | service               | 20              | 9               | 9               | 9               |
| 5       | 4_2_vehicle          | service               | 3               | 16              | 16              | 16              |
| 5       | 4_2_vehicle          | service               | 36              | 28              | 28              | 28              |
| 5       | 4_2_vehicle          | service               | 35              | 35              | 35              | 35              |
| 5       | 4_2_vehicle          | end                   | -               | 48              | undef           | 48              |
+---------+----------------------+-----------------------+-----------------+-----------------+-----------------+-----------------+
| 6       | 1_2_vehicle          | start                 | -               | undef           | 0               | 0               |
| 6       | 1_2_vehicle          | service               | 44              | 11              | 11              | 11              |
| 6       | 1_2_vehicle          | service               | 45              | 21              | 21              | 21              |
| 6       | 1_2_vehicle          | service               | 33              | 28              | 28              | 28              |
| 6       | 1_2_vehicle          | service               | 15              | 40              | 40              | 40              |
| 6       | 1_2_vehicle          | service               | 37              | 47              | 47              | 47              |
| 6       | 1_2_vehicle          | service               | 17              | 52              | 52              | 52              |
| 6       | 1_2_vehicle          | end                   | -               | 60              | undef           | 60              |
+---------+----------------------+-----------------------+-----------------+-----------------+-----------------+-----------------+
| 7       | 3_2_vehicle          | start                 | -               | undef           | 0               | 0               |
| 7       | 3_2_vehicle          | service               | 49              | 3               | 3               | 3               |
| 7       | 3_2_vehicle          | service               | 5               | 11              | 11              | 11              |
| 7       | 3_2_vehicle          | service               | 38              | 18              | 18              | 18              |
| 7       | 3_2_vehicle          | end                   | -               | 25              | undef           | 25              |
+---------+----------------------+-----------------------+-----------------+-----------------+-----------------+-----------------+
| 8       | 1_3_vehicle          | start                 | -               | undef           | 0               | 0               |
| 8       | 1_3_vehicle          | service               | 25              | 22              | 22              | 22              |
| 8       | 1_3_vehicle          | service               | 18              | 33              | 33              | 33              |
| 8       | 1_3_vehicle          | service               | 4               | 41              | 41              | 41              |
| 8       | 1_3_vehicle          | end                   | -               | 47              | undef           | 47              |
+---------+----------------------+-----------------------+-----------------+-----------------+-----------------+-----------------+
| 9       | 2_3_vehicle          | start                 | -               | undef           | 0               | 0               |
| 9       | 2_3_vehicle          | service               | 6               | 11              | 11              | 11              |
| 9       | 2_3_vehicle          | service               | 48              | 20              | 20              | 20              |
| 9       | 2_3_vehicle          | service               | 8               | 30              | 30              | 30              |
| 9       | 2_3_vehicle          | service               | 27              | 44              | 44              | 44              |
| 9       | 2_3_vehicle          | end                   | -               | 52              | undef           | 52              |
+---------+----------------------+-----------------------+-----------------+-----------------+-----------------+-----------------+
| 10      | 4_3_vehicle          | start                 | -               | undef           | 0               | 0               |
| 10      | 4_3_vehicle          | service               | 29              | 3               | 3               | 3               |
| 10      | 4_3_vehicle          | service               | 2               | 12              | 12              | 12              |
| 10      | 4_3_vehicle          | service               | 16              | 20              | 20              | 20              |
| 10      | 4_3_vehicle          | service               | 50              | 26              | 26              | 26              |
| 10      | 4_3_vehicle          | service               | 21              | 34              | 34              | 34              |
| 10      | 4_3_vehicle          | end                   | -               | 42              | undef           | 42              |
+---------+----------------------+-----------------------+-----------------+-----------------+-----------------+-----------------+
| 11      | 1_4_vehicle          | start                 | -               | undef           | 0               | 0               |
| 11      | 1_4_vehicle          | service               | 13              | 16              | 16              | 16              |
| 11      | 1_4_vehicle          | service               | 41              | 25              | 25              | 25              |
| 11      | 1_4_vehicle          | service               | 40              | 37              | 37              | 37              |
| 11      | 1_4_vehicle          | service               | 19              | 48              | 48              | 48              |
| 11      | 1_4_vehicle          | service               | 42              | 57              | 57              | 57              |
| 11      | 1_4_vehicle          | end                   | -               | 67              | undef           | 67              |
+--------------------------------------------------------------------------------------------------------------------------------+

</samp></pre>

You can find the entire code <a href="https://github.com/jsprit/jsprit/blob/v1.6/jsprit-examples/src/main/java/jsprit/examples/MultipleDepotExample.java" target="_blank">here</a>.
