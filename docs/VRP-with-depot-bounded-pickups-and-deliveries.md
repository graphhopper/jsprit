This example assumes that you know [SimpleExample](https://github.com/jsprit/jsprit/wiki/Simple-Example) and covers:
- defining pickups and deliveries

<strong>Note</strong> that the VRP with Backhauls with mixed pickups and deliveries described here assumes that all deliveries are loaded in the depot at the beginning of a tour and all pickups are unloaded in the depot as well (at the end of the tour). En route pickup and deliveries such that a shipment is loaded at location A and unloaded again at location B within the same route (where A and B are unequal to the depot location) is covered [here](https://github.com/jsprit/jsprit/wiki/VRP-with-pickups-and-deliveries/).

The only difference compared to SimpleExample is the creation of pickups and deliveries instead of the more general services. However, you define them much like you define services:
<pre><code>/*
 * build pickups and deliveries at the required locations, each with a capacity-demand of 1.
 */
Pickup pickup1 = (Pickup) Pickup.Builder.newInstance("1").addSizeDimension(0,1).setCoord(Coordinate.newInstance(5, 7)).build();
Delivery delivery1 = (Delivery) Delivery.Builder.newInstance("2").addSizeDimension(0,1).setCoord(Coordinate.newInstance(5, 13)).build();
		
Pickup pickup2 = (Pickup) Pickup.Builder.newInstance("3").addSizeDimension(0,1).setCoord(Coordinate.newInstance(15, 7)).build();
Delivery delivery2 = (Delivery) Delivery.Builder.newInstance("4").addSizeDimension(0,1).setCoord(Coordinate.newInstance(15, 13)).build();

VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
vrpBuilder.addVehicle(vehicle);

/*
 * add pickups and deliveries to the problem
 */
vrpBuilder.addJob(pickup1).addJob(pickup2).addJob(delivery1).addJob(delivery2);

VehicleRoutingProblem problem = vrpBuilder.build();
</code></pre>

and proceed with what you know from SimpleExample. The entire code of the example can be found <a href="https://github.com/jsprit/jsprit/blob/master/jsprit-examples/src/main/java/examples/SimplePickupAndDeliveryExample.java" target="blank_">here</a>.

The code of more advanced examples dealing with the pickup and deliveries can be found here:
- <a href="https://github.com/jsprit/jsprit/blob/v1.6/jsprit-examples/src/main/java/jsprit/examples/PickupAndDeliveryExample.java" target="blank_">Example 1</a> which is an adopted problem from Solomon R101 with random pickups and deliveries with time-windows
- <a href="https://github.com/jsprit/jsprit/blob/v1.6/jsprit-examples/src/main/java/jsprit/examples/PickupAndDeliveryExample2.java" target="blank_">Example 2</a> which is an adopted problem from Christophides vrpnc1 with random pickups and deliveries and a vehicle capacity of 50 yielding to the following solution:

<img src="https://github.com/jsprit/misc-rep/raw/master/wiki-images/pd_christophides_vrpnc1_solution.png">

The first customer in a route is marked with a red circle to indicate the orientation of the route. The labels represent the size of the pickup and delivery respectively. It is interesting to compare this with the solution of the same problem but with backhauls constraint, i.e. deliveries have to be conducted first (before pickups can be loaded) (see [VRP with backhauls](https://github.com/jsprit/jsprit/wiki/VRP-with-backhauls-example)).

<img src="https://github.com/jsprit/misc-rep/raw/master/wiki-images/vrpwbh_christophides_vrpnc1_solution.png">