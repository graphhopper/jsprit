This example covers:
- defining and creating vehicles and their types
- defining services with time-windows and service times
- defining a problem with infinite fleet-size
- reading, creating and running an algorithm

Before you start, [add the latest release to your pom](https://github.com/jsprit/jsprit/wiki/Add-latest-release-to-your-pom). Additionally, create an output folder in your project directory. Either do it manually or add the following lines to your code (even this obfuscates the code-example a bit):
<pre><code>File dir = new File("output");
// if the directory does not exist, create it
if (!dir.exists()){
	System.out.println("creating directory ./output");
	boolean result = dir.mkdir();  
	if(result) System.out.println("./output created");  
}
</code></pre>

Let us assume the following problem setup (being an excerpt of Solomon's C101 problem instance).

<img src="https://github.com/jsprit/misc-rep/raw/master/wiki-images/vrptw_setup.png">

First, build a vehicle with a capacity of 200. Its maximum operating time is 1236 and it is located at (40,50):

<pre><code>/*
 * get a vehicle type-builder and build a type with the typeId "vehicleType" and a capacity of 200
 */
VehicleTypeImpl.Builder vehicleTypeBuilder = VehicleTypeImpl.Builder.newInstance("vehicleType").addCapacityDimension(0,200);
VehicleType vehicleType = vehicleTypeBuilder.build();

/*
 * get a vehicle-builder and build a vehicle located at (40,50) with type "vehicleType" and a latest arrival
 * time of 1236 (which corresponds to a operation time of 1236 since the earliestStart of the vehicle is set
 * to 0 by default).
 */
VehicleImpl.Builder vehicleBuilder = VehicleImpl.Builder.newInstance("vehicle");
vehicleBuilder.setStartLocation(Location.newInstance(40, 50));
vehicleBuilder.setLatestArrival(1236);
vehicleBuilder.setType(vehicleType); 
Vehicle vehicle = vehicleBuilder.build();
</code></pre>

Build services 1-6 now by coding:

<pre><code>/*
 * build services with id 1...6 at the required locations
 * Note, that the builder allows chaining which makes building quite handy
 */
//define a service-builder and initialise it with serviceId=1 and demand=10
Service.Builder sBuilder1 = Service.Builder.newInstance("1").addSizeDimension(0,10);
//set coordinate
sBuilder1.setLocation(Location.newInstance(45, 68));
//set service-time
sBuilder1.setServiceTime(90);
//set time-window
sBuilder1.setTimeWindow(TimeWindow.newInstance(912,967));
//and build service
Service service1 = sBuilder1.build();

Service.Builder sBuilder2 = Service.Builder.newInstance("2").addSizeDimension(0,30);
sBuilder2.setLocation(Location.newInstance(45, 70));
sBuilder2.setServiceTime(90);
sBuilder2.setTimeWindow(TimeWindow.newInstance(825,870));
Service service2 = sBuilder2.build();
/*
Service service3 = ...
Service service4 = ...
Service service5 = ...
Service service6 = ...
*/
</code></pre>

Put vehicle and services together to setup the problem.
<pre><code>/*
 * again define a builder to build the VehicleRoutingProblem
 */
VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
vrpBuilder.addVehicle(vehicle);
vrpBuilder.addJob(service1).addJob(service2);
/*
vrpBuilder.addJob(service3).addJob(service4).addJob(service5).addJob(service6);
*/
/*
 * build the problem
 * by default, the problem is specified such that FleetSize is INFINITE, i.e. an infinite number of 
 * the defined vehicles can be used to solve the problem
 * by default, transport costs are computed as Euclidean distances
 */
VehicleRoutingProblem problem = vrpBuilder.build();
</code></pre>


To solve it, define an algorithm. Here, it comes out-of-the-box. The SchrimpfFactory creates an algo which is an implemenation of [Schrimpf et al.](http://www.sciencedirect.com/science/article/pii/S0021999199964136).

You might be interested in other algorithm configurations, <a href="https://github.com/jsprit/jsprit/wiki/Benchmark-VRPTW" target="_blank">here</a> you can find a set of ready-to-use and benchmarked algorithms.

<pre><code>/*
* get the algorithm out-of-the-box. 
*/
VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(problem);

/*
* and search a solution which returns a collection of solution (here only one solution is in the collection)
*/
Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
	
/*
 * use helper to get the best 
 */
VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);
</code></pre>

Please visit [Simple Example](https://github.com/graphhopper/jsprit/blob/master/jsprit-examples/src/main/java/com/graphhopper/jsprit/examples/SimpleExample.java) to get to know how you can analyse the solution.