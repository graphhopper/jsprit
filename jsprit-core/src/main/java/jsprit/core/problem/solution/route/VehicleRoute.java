/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package jsprit.core.problem.solution.route;

import java.util.HashSet;
import java.util.Set;

import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.driver.DriverImpl;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.solution.route.activity.DefaultShipmentActivityFactory;
import jsprit.core.problem.solution.route.activity.DefaultTourActivityFactory;
import jsprit.core.problem.solution.route.activity.End;
import jsprit.core.problem.solution.route.activity.Start;
import jsprit.core.problem.solution.route.activity.TourActivities;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.solution.route.activity.TourActivityFactory;
import jsprit.core.problem.solution.route.activity.TourShipmentActivityFactory;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleImpl.NoVehicle;

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
		
		public static Builder newInstance(Vehicle vehicle, Driver driver){
			return new Builder(vehicle,driver);
		}
		
		private Vehicle vehicle;
		
		private Driver driver;
		
		private Start start;
		
		private End end;
		
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
		 * 
		 * <p>Default startLocation is vehicle.getLocationId()<br>
		 * Default departureTime is vehicle.getEarliestDeparture()<br>
		 * Default endLocation is either vehicle.getLocationId() or (if !vehicle.isReturnToDepot()) last specified activityLocation
		 * @param vehicle
		 * @param driver
		 */
		private Builder(Vehicle vehicle, Driver driver) {
			super();
			this.vehicle = vehicle;
			this.driver = driver;
			start = Start.newInstance(vehicle.getLocationId(), vehicle.getEarliestDeparture(), vehicle.getLatestArrival());
			start.setEndTime(vehicle.getEarliestDeparture());
			end = End.newInstance(vehicle.getLocationId(), vehicle.getEarliestDeparture(), vehicle.getLatestArrival());
		}

		/**
		 * Sets the departure-time of the route.
		 * 
		 * @param departureTime
		 * @return
		 */
		public Builder setDepartureTime(double departureTime){
			start.setEndTime(departureTime);
			return this;
		}
		
		public Builder setRouteEndArrivalTime(double endTime){
			end.setArrTime(endTime);
			return this;
		}
		
		public Builder addService(Service service){
			addService(service,0.0,0.0);
			return this;
		}
		
		public Builder addService(Service service, double arrTime, double endTime){
			TourActivity act = serviceActivityFactory.createActivity(service);
			act.setArrTime(arrTime);
			act.setEndTime(endTime);
			tourActivities.addActivity(act);
			return this;
		}
		
		/**
		 * Adds a the pickup of the specified shipment.
		 * 
		 * @param shipment
		 * @throws IllegalStateException if method has already been called with the specified shipment.
		 * @return
		 */
		public Builder addPickup(Shipment shipment){
			addPickup(shipment,0.0,0.0);
			return this;
		}
		
		/**
		 * Adds a the pickup of the specified shipment at specified arrival and end-time.
		 * 
		 * @param shipment
		 * @throws IllegalStateException if method has already been called with the specified shipment.
		 * @return
		 */
		public Builder addPickup(Shipment shipment, double arrTime, double endTime){
			if(openShipments.contains(shipment)) throw new IllegalStateException("shipment has already been added. cannot add it twice.");
			TourActivity act = shipmentActivityFactory.createPickup(shipment);
			act.setArrTime(arrTime);
			act.setEndTime(endTime);
			tourActivities.addActivity(act);
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
		public Builder addDelivery(Shipment shipment){
			addDelivery(shipment,0.0,0.0);
			return this;
		}
		
		/**
		 * Adds a the delivery of the specified shipment at a specified arrival and endTime.
		 * 
		 * @param shipment
		 * @throws IllegalStateException if specified shipment has not been picked up yet (i.e. method addPickup(shipment) has not been called yet).
		 * @return
		 */
		public Builder addDelivery(Shipment shipment, double arrTime, double endTime){
			if(openShipments.contains(shipment)){
				TourActivity act = shipmentActivityFactory.createDelivery(shipment);
				act.setArrTime(arrTime);
				act.setEndTime(endTime);
				tourActivities.addActivity(act);
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
			if(!vehicle.isReturnToDepot()){
				if(!tourActivities.isEmpty()){
					end.setLocationId(tourActivities.getActivities().get(tourActivities.getActivities().size()-1).getLocationId());
				}
			}
			VehicleRoute route = new VehicleRoute(this);
			return route;
		}

	}
	
	private TourActivities tourActivities;

	private Vehicle vehicle;
	
	private Driver driver;
	
	private Start start;
	
	private End end;
	
	private VehicleRoute(VehicleRoute route){
		this.start = Start.copyOf(route.getStart());
		this.end = End.copyOf(route.getEnd());
		this.tourActivities = TourActivities.copyOf(route.getTourActivities());
		this.vehicle = route.getVehicle();
		this.driver = route.getDriver();
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
		this.tourActivities = builder.tourActivities;
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
	
	public double getDepartureTime(){
		if(start == null) throw new IllegalStateException("cannot get departureTime without having a vehicle on this route. use setVehicle(vehicle,departureTime) instead.");
		return start.getEndTime();
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
	
	@Override
	public String toString() {
		return "[start="+start+"][end=" + end + "][departureTime=" + start.getEndTime() + "][vehicle=" + vehicle + "][driver=" + driver + "][nuOfActs="+tourActivities.getActivities().size()+"]";
	}

}
