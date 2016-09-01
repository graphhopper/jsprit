This example covers:
- defining and creating different 'depots', vehicles and their types
- defining a problem with finite fleet-size
- reading and creating an algorithm
- plotting the solution

It is based on the problem instance P08 defined by 

<em>Cordeau, J.-F., Gendreau, M. and Laporte, G. (1997), A tabu search heuristic for periodic and multi-depot vehicle routing problems. Networks, 30: 105–119.</em> 

Please visit for example <a href="http://neo.lcc.uma.es/vrp/vrp-flavors/multiple-depot-vrp/" target="_blank">this site</a> to get more information on Multiple Depot VRP.

Before you start, [add the latest release to your pom](https://github.com/jsprit/jsprit/wiki/Add-latest-release-to-your-pom). Additionally, create an output folder in your project directory. Either do it manually or add the following lines to your code:
<pre><code>File dir = new File("output");
// if the directory does not exist, create it
if (!dir.exists()){
	System.out.println("creating directory ./output");
	boolean result = dir.mkdir();  
	if(result) System.out.println("./output created");  
}
</code></pre>

All services of P08 are stored in an xml-file called vrp_cordeau_08.xml (you can find it [here](https://github.com/jsprit/jsprit/tree/master/jsprit-examples/input)). You read them into your problemBuilder as follows

<pre><code>VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
/*
 * Read cordeau-instance p08, BUT only its services without any vehicles 
 */
new VrpXMLReader(vrpBuilder).read("input/vrp_cordeau_08.xml");
</code></pre>

Define depots and vehicles:

<pre><code>/*
 * add vehicles with its depots
 * 2 depots with the following coordinates:
 * (-33,33), (33,-33)
 * 
 * each with 14 vehicles each with a capacity of 500 and a maximum duration of 310
 */
int nuOfVehicles = 14;
int capacity = 500;
double maxDuration = 310;
Coordinate firstDepotCoord = Coordinate.newInstance(-33, 33);
Coordinate second = Coordinate.newInstance(33, -33);		
int depotCounter = 1;

for(Coordinate depotCoord : Arrays.asList(firstDepotCoord,second)){
&nbsp;&nbsp;&nbsp;&nbsp;for(int i=0;i&lt;nuOfVehicles;i++){
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;String typeId = depotCounter + "_type";
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;VehicleType vehicleType = VehicleTypeImpl.Builder.newInstance(typeId).addCapacityDimension(0,capacity).setCostPerDistance(1.0).build();
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;String vehicleId = depotCounter + "_" + (i+1) + "_vehicle";
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;VehicleImpl.VehicleBuilder vehicleBuilder = VehicleImpl.Builder.newInstance(vehicleId);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;vehicleBuilder.setStartLocation(depotCoord);  //defines the location of the vehicle and thus the depot
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;vehicleBuilder.setType(vehicleType)
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;vehicleBuilder.setLatestArrival(maxDuration);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Vehicle vehicle = vehicleBuilder.build();
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;vrpBuilder.addVehicle(vehicle);
	}
	depotCounter++;
}
</code></pre>

Build the problem, and define and run an algorithm like this:
<pre><code>
/*
 * define problem with finite fleet
 */
vrpBuilder.setFleetSize(FleetSize.FINITE);
		
/*
 * build the problem
 */
VehicleRoutingProblem vrp = vrpBuilder.build();
/*
 * solve the problem
 */
VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp,"input/algorithmConfig.xml");
Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
</code></pre>

The problem will be looking like this:

![p08](https://github.com/jsprit/misc-rep/raw/master/wiki-images/problem08.png)

Running <a href="https://github.com/jsprit/jsprit/blob/master/jsprit-examples/input/algorithmConfig.xml" target="_blank">this algorithm</a> yields to the following solution:

![solution_p08](https://github.com/jsprit/misc-rep/raw/master/wiki-images/p08_solution.png)

You can find the entire code <a href="https://github.com/jsprit/jsprit/blob/v1.4/jsprit-examples/src/main/java/jsprit/examples/MultipleDepotExampleWithPenaltyVehicles.java" target="_blank">here</a>.