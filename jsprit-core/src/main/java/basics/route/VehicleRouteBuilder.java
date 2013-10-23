package basics.route;

import basics.Shipment;

public class VehicleRouteBuilder {
	
	private Vehicle vehicle;
	
	private Driver driver;
	
	private Start start;
	
	private End end;
	
	private TourActivities tourActivities = new TourActivities();
	
	private TourActivityFactory serviceActivityFactory = new DefaultTourActivityFactory();
	
	private TourShipmentActivityFactory shipmentActivityFactory = new DefaultShipmentActivityFactory();
	
	public void setServiceActivityFactory(TourActivityFactory serviceActivityFactory) {
		this.serviceActivityFactory = serviceActivityFactory;
	}

	public void setShipmentActivityFactory(
			TourShipmentActivityFactory shipmentActivityFactory) {
		this.shipmentActivityFactory = shipmentActivityFactory;
	}

	public VehicleRouteBuilder(Vehicle vehicle, Driver driver) {
		super();
		this.vehicle = vehicle;
		this.driver = driver;
		start = Start.newInstance(vehicle.getLocationId(), vehicle.getEarliestDeparture(), vehicle.getLatestArrival());
		start.setEndTime(vehicle.getEarliestDeparture());
		end = End.newInstance(vehicle.getLocationId(), vehicle.getEarliestDeparture(), vehicle.getLatestArrival());
	}

	public VehicleRouteBuilder setDepartureTime(double departureTime){
		start.setEndTime(departureTime);
		return this;
	}
	
	public VehicleRouteBuilder addPickup(Shipment shipment){
		tourActivities.addActivity(shipmentActivityFactory.createPickup(shipment));
		return this;
	}
	
	public VehicleRouteBuilder addDelivery(Shipment shipment){
		tourActivities.addActivity(shipmentActivityFactory.createDelivery(shipment));
		return this;
	}
	
	public VehicleRoute build(){
		VehicleRoute route = VehicleRoute.newInstance(tourActivities, driver, vehicle);
		return route;
	}

}
