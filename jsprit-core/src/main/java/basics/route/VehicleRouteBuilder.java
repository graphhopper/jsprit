package basics.route;

import java.util.HashSet;
import java.util.Set;

import basics.Service;
import basics.Shipment;

/**
 * Builds a {@link VehicleRoute}.
 * 
 * @author schroeder
 *
 */
public class VehicleRouteBuilder {
	
	private Vehicle vehicle;
	
	private Driver driver;
	
	private Start start;
	
	private TourActivities tourActivities = new TourActivities();
	
	private TourActivityFactory serviceActivityFactory = new DefaultTourActivityFactory();
	
	private TourShipmentActivityFactory shipmentActivityFactory = new DefaultShipmentActivityFactory();
	
	private Set<Shipment> openShipments = new HashSet<Shipment>();
	
	public void setServiceActivityFactory(TourActivityFactory serviceActivityFactory) {
		this.serviceActivityFactory = serviceActivityFactory;
	}

	public void setShipmentActivityFactory(TourShipmentActivityFactory shipmentActivityFactory) {
		this.shipmentActivityFactory = shipmentActivityFactory;
	}

	/**
	 * Constructs the route-builder.
	 * @param vehicle
	 * @param driver
	 */
	public VehicleRouteBuilder(Vehicle vehicle, Driver driver) {
		super();
		this.vehicle = vehicle;
		this.driver = driver;
		start = Start.newInstance(vehicle.getLocationId(), vehicle.getEarliestDeparture(), vehicle.getLatestArrival());
		start.setEndTime(vehicle.getEarliestDeparture());
		End.newInstance(vehicle.getLocationId(), vehicle.getEarliestDeparture(), vehicle.getLatestArrival());
	}

	/**
	 * Sets the departure-time of the route.
	 * 
	 * @param departureTime
	 * @return
	 */
	public VehicleRouteBuilder setDepartureTime(double departureTime){
		start.setEndTime(departureTime);
		return this;
	}
	
	public VehicleRouteBuilder addService(Service service){
		tourActivities.addActivity(serviceActivityFactory.createActivity(service));
		return this;
	}
	
	/**
	 * Adds a the pickup of the specified shipment.
	 * 
	 * @param shipment
	 * @throws IllegalStateException if method has already been called with the specified shipment.
	 * @return
	 */
	public VehicleRouteBuilder addPickup(Shipment shipment){
		if(openShipments.contains(shipment)) throw new IllegalStateException("shipment has already been added. cannot add it twice.");
		tourActivities.addActivity(shipmentActivityFactory.createPickup(shipment));
		openShipments.add(shipment);
		return this;
	}
	
	/**
	 * Adds a the delivery of the specified shipment.
	 * 
	 * @param shipment
	 * @throws IllegalStateException if specified shipment has not been picked up yet (i.e. method addPickup(shipment) has not been called yet).
	 * @return
	 */
	public VehicleRouteBuilder addDelivery(Shipment shipment){
		if(openShipments.contains(shipment)){
			tourActivities.addActivity(shipmentActivityFactory.createDelivery(shipment));
			openShipments.remove(shipment);
		}
		else{ throw new IllegalStateException("cannot deliver shipment. shipment " + shipment + " needs to be picked up first."); }
		return this;
	}
	
	/**
	 * Builds the route.
	 * 
	 * @return {@link VehicleRoute}
	 * @throws IllegalStateException if there are still shipments that have been picked up though but not delivery. 
	 */
	public VehicleRoute build(){
		if(!openShipments.isEmpty()){
			throw new IllegalStateException("there are still shipments that have not been delivered yet.");
		}
		VehicleRoute route = VehicleRoute.newInstance(tourActivities, driver, vehicle);
		return route;
	}

}
