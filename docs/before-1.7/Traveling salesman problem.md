TSP problem can be modelled by defining a vehicle routing problem with either a vehicle that has a sufficiently high capacity (to accomodate all services) 

<pre><code>/*
 * get a vehicle type-builder and build a type with the typeId "vehicleType" and a sufficently high capacity
 */
VehicleTypeImpl.Builder vehicleTypeBuilder = VehicleTypeImpl.Builder.newInstance("vehicleType").addCapacityDimension(0,Integer.MAX_VALUE);
VehicleType vehicleType = vehicleTypeBuilder.build();

/*
 * get a vehicle-builder and build a vehicle located at (10,10) with type "vehicleType"
 */
VehicleBuilder vehicleBuilder = VehicleImpl.Builder.newInstance("vehicle");
vehicleBuilder.setStartLocation(Location.newInstance(10, 10));
vehicleBuilder.setType(vehicleType); 
Vehicle vehicle = vehicleBuilder.build();
</code></pre>

or services that have a capacity-demand of 0.

<pre><code>/*
 * build services with id 1...4 at the required locations, each with a capacity-demand of 0 (which is the default).
 * Note, that the builder allows chaining which makes building quite handy
 */
Service service1 = Service.Builder.newInstance("1").setLocation(Location.newInstance(5, 7)).build();
Service service2 = Service.Builder.newInstance("2").setLocation(Location.newInstance(5, 13)).build();
Service service3 = Service.Builder.newInstance("3").setLocation(Location.newInstance(15, 7)).build();
Service service4 = Service.Builder.newInstance("4").setLocation(Location.newInstance(15, 13)).build();
</code></pre>