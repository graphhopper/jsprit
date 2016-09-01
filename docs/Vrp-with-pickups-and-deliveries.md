This example assumes that you know [SimpleExample](https://github.com/jsprit/jsprit/wiki/Simple-Example) and covers:
- defining shipments

The only difference compared to SimpleExample is the creation of shipments instead of services. However, you define them much like you define services:
<pre><code>/*
* build shipments at the required locations, each with a capacity-demand of 1.
* 4 shipments
* 1: (5,7)->(6,9)
* 2: (5,13)->(6,11)
* 3: (15,7)->(14,9)
* 4: (15,13)->(14,11)
*/

Shipment shipment1 = Shipment.Builder.newInstance("1").addSizeDimension(0,1).setPickupLocation(Location.newInstance(5,7))
.setDeliveryLocation(Location.newInstance(6, 9)).build();
Shipment shipment2 = Shipment.Builder.newInstance("2").addSizeDimension(0,1).setPickupLocation(Location.newInstance(5,13))
.setDeliveryLocation(Location.newInstance(6, 11)).build();
Shipment shipment3 = Shipment.Builder.newInstance("3").addSizeDimension(0,1).setPickupLocation(Location.newInstance(15,7))
.setDeliveryLocation(Location.newInstance(14, 9)).build();
Shipment shipment4 = Shipment.Builder.newInstance("4").addSizeDimension(0,1).setPickupLocation(Location.newInstance(15,13))
.setDeliveryLocation(Location.newInstance(14, 11)).build();
                
VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();

/*
 * add these shipments to the problem
 */
vrpBuilder.addJob(shimpent1).addJob(shimpent2).addJob(shimpent3).addJob(shimpent4);

</code></pre>

and proceed with what you know from SimpleExample. The entire code of the example can be found <a href="https://github.com/jsprit/jsprit/blob/v1.6/jsprit-examples/src/main/java/jsprit/examples/SimpleEnRoutePickupAndDeliveryExample.java" target="blank_">here</a>.

You might also be interested in combining shipments and (depot-bounded) services. Look at the code of [this](https://github.com/jsprit/jsprit/blob/v1.6/jsprit-examples/src/main/java/jsprit/examples/SimpleEnRoutePickupAndDeliveryWithDepotBoundedDeliveriesExample.java) example.
