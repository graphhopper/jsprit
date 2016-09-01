This example covers
- illustrating different problem types,
- specifying heterogeneous fleet with its vehicles and vehicle-types,
- specifying the algorithm
- benchmarking the algorithm

#### Specifying the problem

<a href="http://link.springer.com/article/10.1007%2Fs10732-011-9186-y" target="_blank">Penna et al. (2013)</a> distinguish 5 types of VRP dealing with heterogeneous fleet.

<b>FSMD</b> - Fleet Size and Mix with Dependent costs
<p><b>FSMF</b> - Fleet Size and Mix with Fixed costs
<p><b>FSMFD</b> - Fleet Size and Mix with Fixed and Dependent costs
<p><b>HVRPD</b> - Heterogeneous Vehicle Routing Problem with Dependent costs and finite (limited) fleet
<p><b>HVRPFD</b> - Heterogeneous Vehicle Routing Problem with Fixed and Dependent costs and finite (limited) fleet

Generally, Fleet Size and Mix (FSM) is applied on tactical level to design a vehicle fleet, whereas HVRP is applied on operational level to employ existing vehicles/fleet as efficient as possible.
 
Assuming you know the basics of jsprit, implementing these types is fairly straightforward. Basically, you specify these types with the VehicleRoutingProblem.Builder and a specification of different VehicleType(s).

Lets assume a single depot @(40,40) and the following 3 vehicle types:

<table>
<tr>
<th>vehicleId</th>
<th>capacity</th>
<th>fixed costs</th>
<th>variable costs</th>
<th>#vehicles</th>
</tr>
<tr>
<td>1</td>
<td>120</td>
<td>1000</td>
<td>1.0</td>
<td>2</td>
</tr>
<tr>
<td>2</td>
<td>160</td>
<td>1500</td>
<td>1.1</td>
<td>2</td>
</tr>
<tr>
<td>3</td>
<td>300</td>
<td>3500</td>
<td>1.4</td>
<td>1</td>
</tr>
</table> 

To implement the above problem types you need to code:
<p><b>FSMD</b>
<pre><code>/*
 * build the types and vehicles from table above 
 * here it is assumed the variable costs are dependent on distance (rather than time or any other measure)
 */
VehicleTypeImpl vehicleType1 = VehicleTypeImpl.Builder.newInstance("type1").addCapacityDimension(0,120).setCostPerDistance(1.0).build();
VehicleImpl vehicle1 = VehicleImpl.Builder.newInstance("vehicle1").setStartLocation(Location.newInstance(40, 40)).setType(vehicleType1).build();

VehicleTypeImpl vehicleType2 = VehicleTypeImpl.Builder.newInstance("type2").addCapacityDimension(0,160).setCostPerDistance(1.2).build();
VehicleImpl vehicle2 = VehicleImpl.Builder.newInstance("vehicle2").setStartLocation(Location.newInstance(40, 40)).setType(vehicleType2).build();

VehicleTypeImpl vehicleType3 = VehicleTypeImpl.Builder.newInstance("type3").addCapacityDimension(0,300).setCostPerDistance(1.4).build();
VehicleImpl vehicle3 = VehicleImpl.Builder.newInstance("vehicle3").setStartLocation(Location.newInstance(40, 40)).setType(vehicleType3).build();

//Use VehicleRoutingProblem.Builder to specify the problem
VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
vrpBuilder.addVehicle(vehicle1).addVehicle(vehicle2).addVehicle(vehicle3);

//set fleetSize to FleetSize.INFINITE (which you actually do not need to set since it is the default option)
vrpBuilder.setFleetSize(FleetSize.INFINITE);

//add jobs as you know it from SimpleExample and build the routing problem
...
VehicleRoutingProblem vrp = vrpBuilder.build();
</code></pre>
<p><b>FSMF</b>

The only difference to the FSMD is that you specify fixed costs rather than distance-dependent costs such as
<pre><code>/*
 * Still you probably want to somehow consider variable distance costs, thus distance-costs are equally set
 * to 1.0 (which is the default value - thus you do not need to set explicitly).
 */
VehicleTypeImpl vehicleType1 = VehicleTypeImpl.Builder.newInstance("type1").addCapacityDimension(0,120).setFixedCosts(1000).build();
VehicleImpl vehicle1 = VehicleImpl.Builder.newInstance("vehicle1").setStartLocation(Location.newInstance(40, 40)).setType(vehicleType1).build();
</code></pre>

<p><b>FSMFD</b>

Both fixed and variable costs are specified here such as
<pre><code>VehicleTypeImpl vehicleType2 = VehicleTypeImpl.Builder.newInstance("type2").addCapacityDimension(0,160).setFixedCosts(1500).setCostPerDistance(1.2).build();
VehicleImpl vehicle2 = VehicleImpl.Builder.newInstance("vehicle2").setStartLocation(Location.newInstance(40, 40)).setType(vehicleType2).build();
</code></pre>

<p><b>HVRPD</b>

As already mentioned, the HVRP distinguishes itself from FSM such that the vehicle fleet is given. Thus you need to implement each and every vehicle and set the fleet-size to FINITE. If you have a lean fleet, i.e. sum of available capacities is not much greater than the total demand, you need to allow the algorithm to temporarilly generate infeasable solution (i.e. with vehicles you actually do not have in your fleet). By setting sufficient penalties, you should end up with a feasible solution (assuming there is one).

<pre><code>/*
 * build the types and vehicles from table above 
 * here it is assumed the variable costs are dependent on distance (rather than time or any other measure)
 */
VehicleTypeImpl vehicleType1 = VehicleTypeImpl.Builder.newInstance("type1").addCapacityDimension(0,120).setCostPerDistance(1.0).build();
VehicleImpl vehicle1_1 = VehicleImpl.Builder.newInstance("vehicle1_1").setStartLocation(Location.newInstance(40, 40)).setType(vehicleType1).build();
VehicleImpl vehicle1_2 = VehicleImpl.Builder.newInstance("vehicle1_2").setStartLocation(Location.newInstance(40, 40)).setType(vehicleType1).build();

VehicleTypeImpl vehicleType2 = VehicleTypeImpl.Builder.newInstance("type2").addCapacityDimension(0,160).setCostPerDistance(1.2).build();
VehicleImpl vehicle2_1 = VehicleImpl.Builder.newInstance("vehicle2_1").setStartLocation(Location.newInstance(40, 40)).setType(vehicleType2).build();
VehicleImpl vehicle2_2 = VehicleImpl.Builder.newInstance("vehicle2_2").setStartLocation(Location.newInstance(40, 40)).setType(vehicleType2).build();

VehicleTypeImpl vehicleType3 = VehicleTypeImpl.Builder.newInstance("type3").addCapacityDimension(0,300).setCostPerDistance(1.4).build();
VehicleImpl vehicle3 = VehicleImpl.Builder.newInstance("vehicle3").setStartLocation(Location.newInstance(40, 40)).setType(vehicleType3).build();

//Use VehicleRoutingProblem.Builder to specify the problem
VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
vrpBuilder.addVehicle(vehicle1_1).addVehicle(vehicle1_2).addVehicle(vehicle2_1).addVehicle(vehicle2_2).addVehicle(vehicle3);

//set fleetSize to FleetSize.FINITE 
vrpBuilder.setFleetSize(FleetSize.FINITE);

//add jobs as you know it from SimpleExample and build the routing problem
...
VehicleRoutingProblem vrp = vrpBuilder.build();
</code></pre>

Accordingly, you implement the <b>HVRPFD</b> problem by additionally setting fixed costs as illustrated above (see FSMF).

#### Specifying the algorithm

Have a look at the following xml-file: <a href="https://github.com/jsprit/jsprit/blob/master/jsprit-examples/input/algorithmConfig_considerFixedCosts.xml" target="blank_">algorith-config.xml</a>

The insertion heuristic is specified in line 28-30. Once you ommit 'id' as attribute in insertion-tag all subsequent insertion calls in the xml-file are referred the specification made in line 28-30. The tag 'considerFixedCosts' triggers an approach to consider fixed costs when inserting a job. 

It is a fixed costs allocation approach that distinguishes between different insertion phases depending on the completeness of the solution. It is based on <a href="http://www.jstor.org/discover/10.2307/25769374?uid=3737864&uid=2129&uid=2&uid=70&uid=4&sid=21103259604481" target="blank_">Dell' Amico et al. (2007)</a> and basically works as follows: If a significant share of jobs still have to be inserted, vehicles with a low fixed costs per capacity ratio (which they call relative fixed costs) are preferred which usually prefers bigger vehicles. Thus total capacity is expanded. If almost all jobs are already in the solution, vehicles with low absolute fixed costs are preferred which in turn prefers smaller vehicles. Thus total capacity is tighten and kept lean, respectively. It is implemented <a href="https://github.com/jsprit/jsprit/blob/master/jsprit-core/src/main/java/jsprit/core/algorithm/recreate/JobInsertionConsideringFixCostsCalculator.java" target="blank_">here</a>.

The 'weight' attribute specifies a fixed costs scaling parameter and determines the importance of fixed costs compared to variable costs. If weight is 0.0, fixed costs do not matter (are not considered). You need to find out an appropriate parameter for your problem. 'weight=1.0' is a good point to start from.

Play around with this option and also omit 'considerFixedCosts' to get a notion of its impact.

Loading and using the above algorithm is as simple as taking the following two steps: 

1. Download config-file (open config-file in Browser (just click on link), right click 'Raw' and save target as) and
2. Code <code>VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(yourProblem,"yourPath/downloadedConfigFile.xml")</code>

#### Benchmarking the algorithm

Have a look at [this example](https://github.com/jsprit/jsprit/blob/v1.6/jsprit-examples/src/main/java/jsprit/examples/HVRPBenchmarkExample.java). It shows you how to benchmark an algorithm on classical VRP instances with heterogeneous fleet.

For two algorithm-configuration (an extensive search and a greedy one) you can find benchmarking results for the above problem types [here](https://github.com/jsprit/jsprit/wiki/Benchmark-VRPH).

 