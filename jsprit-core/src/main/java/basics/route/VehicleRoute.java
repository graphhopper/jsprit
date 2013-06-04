/*******************************************************************************
 * Copyright (c) 2011 Stefan Schroeder.
 * eMail: stefan.schroeder@kit.edu
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package basics.route;

import basics.route.VehicleImpl.NoVehicle;

public class VehicleRoute {
			
	public static VehicleRoute copyOf(VehicleRoute route) {
		return new VehicleRoute(route);
	}
	
	public static VehicleRoute newInstance(TourActivities tour, Driver driver, Vehicle vehicle) {
		return new VehicleRoute(tour,driver,vehicle);
	}

	public static VehicleRoute emptyRoute() {
		return new VehicleRoute(TourActivities.emptyTour(), DriverImpl.noDriver(), VehicleImpl.noVehicle());
	}
	
	public static class Builder {
		
		public static Builder newInstance(Start start, End end){
			return new Builder(start,end);
		}
		
		private Start start;
		
		private End end;
		
		private Vehicle vehicle = VehicleImpl.noVehicle();
		
		private Driver driver = DriverImpl.noDriver();
		
		private TourActivities tour;
		
		private Builder(Start start, End end) {
			super();
			this.start = start;
			this.end = end;
			this.tour = TourActivities.emptyTour();
		}

		public Builder setVehicle(Vehicle vehicle){
			this.vehicle = vehicle;
			return this;
		}
		
		public Builder setDriver(Driver driver){
			this.driver = driver;
			return this;
		}
		
		public Builder addActivity(TourActivity act){
			if(act instanceof Start || act instanceof End){
				throw new IllegalStateException("tourActivity should be of type Delivery or Pickup, but is of type " + act.getName());
			}
			tour.addActivity(act);
			return this;
		}

		public VehicleRoute build(){
			return new VehicleRoute(this);
		}
	}
	
	private TourActivities tourActivities;

	private Vehicle vehicle;
	
	private Driver driver;
	
	private Start start;
	
	private End end;
	
	private VehicleRouteCostCalculator costCalculator = new DefaultVehicleRouteCostCalculator();
	
	public void setVehicleRouteCostCalculator(VehicleRouteCostCalculator costAccumulator){
		this.costCalculator = costAccumulator;
	}
	
	public VehicleRouteCostCalculator getVehicleRouteCostCalculator(){
		return costCalculator;
	}
	
	public double getCost() {
		if(tourActivities.isEmpty()){
			return 0.0;
		}
		return costCalculator.getCosts();
	}
	
	private VehicleRoute(VehicleRoute route){
		this.start = Start.copyOf(route.getStart());
		this.end = End.copyOf(route.getEnd());
		this.tourActivities = TourActivities.copyOf(route.getTourActivities());
		this.vehicle = route.getVehicle();
		this.driver = route.getDriver();
		this.costCalculator = route.getVehicleRouteCostCalculator().duplicate();
	}
	
	private VehicleRoute(TourActivities tour, Driver driver, Vehicle vehicle) {
		super();
		verify(tour, driver, vehicle);
		this.tourActivities = tour;
		this.vehicle = vehicle;
		this.driver = driver;
		setStartAndEnd(vehicle, vehicle.getEarliestDeparture());
	}
	
	private VehicleRoute(Builder builder){
		this.tourActivities = builder.tour;
		this.vehicle = builder.vehicle;
		this.driver = builder.driver;
		this.start = builder.start;
		this.end = builder.end;
	}

	private void verify(TourActivities tour, Driver driver, Vehicle vehicle) {
		if(tour == null || driver == null || vehicle == null) throw new IllegalStateException("null is not allowed for tour, driver or vehicle. use emptyRoute. use Tour.emptyTour, DriverImpl.noDriver() and VehicleImpl.noVehicle() instead." +
				"\n\tor make it easier and use VehicleRoute.emptyRoute()");
		if(!tour.isEmpty() && vehicle instanceof NoVehicle){
			throw new IllegalStateException("if tour is not empty. there must be a vehicle for this tour, but there is no vehicle.");
		}
	}

	public TourActivities getTourActivities() {
		return tourActivities;
	}
	

	public Vehicle getVehicle() {
		return vehicle;
	}

	public Driver getDriver() {
		return driver;
	}

	public void setVehicle(Vehicle vehicle, double vehicleDepTime){
		this.vehicle = vehicle;
		setStartAndEnd(vehicle, vehicleDepTime);
	}
	
	public void setDepartureTime(double vehicleDepTime){
		if(start == null) throw new IllegalStateException("cannot set departureTime without having a vehicle on this route. use setVehicle(vehicle,departureTime) instead.");
		start.setEndTime(vehicleDepTime);
	}
	
	private void setStartAndEnd(Vehicle vehicle, double vehicleDepTime) {
		if(!(vehicle instanceof NoVehicle)){
			if(start == null && end == null){
				start = Start.newInstance(vehicle.getLocationId(), vehicle.getEarliestDeparture(), vehicle.getLatestArrival());
				end = End.newInstance(vehicle.getLocationId(), vehicle.getEarliestDeparture(), vehicle.getLatestArrival());
			}
			start.setEndTime(vehicleDepTime);
			start.setTheoreticalEarliestOperationStartTime(vehicle.getEarliestDeparture());
			start.setTheoreticalLatestOperationStartTime(vehicle.getLatestArrival());
			start.setLocationId(vehicle.getLocationId());
			end.setLocationId(vehicle.getLocationId());
			end.setTheoreticalEarliestOperationStartTime(vehicle.getEarliestDeparture());
			end.setTheoreticalLatestOperationStartTime(vehicle.getLatestArrival());
		}
		
	}


	public boolean isEmpty() {
		return tourActivities.isEmpty();
	}

	public Start getStart() {
		return start;
	}

	public End getEnd() {
		return end;
	}
	
}
