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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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

/**
 * Contains the tour, i.e. a number of activities, a vehicle servicing the tour and a driver.
 * 
 * 
 * @author stefan
 *
 */
public class VehicleRoute {
		
	/**
	 * Returns a deep copy of this vehicleRoute.
	 * 
	 * @param route
	 * @return copied route
	 * @throws IllegalArgumentException if route is null
	 */
	public static VehicleRoute copyOf(VehicleRoute route) {
		if(route == null) throw new IllegalArgumentException("route must not be null");
		return new VehicleRoute(route);
	}
	
	/**
	 * Returns a newInstance of {@link VehicleRoute}.
	 * 
	 * @param tour
	 * @param driver
	 * @param vehicle
	 * @return VehicleRoute
	 * @deprecated use VehicleRoute.Builder instead
	 */
	@Deprecated
	public static VehicleRoute newInstance(TourActivities tour, Driver driver, Vehicle vehicle) {
		return new VehicleRoute(tour,driver,vehicle);
	}

	/**
	 * Returns an empty route.
	 * 
	 * <p>An empty route has an empty list of tour-activities, no driver (DriverImpl.noDriver()) and no vehicle (VehicleImpl.createNoVehicle()).
	 * 
	 * @return
	 */
	public static VehicleRoute emptyRoute() {
		return Builder.newInstance(VehicleImpl.createNoVehicle(), DriverImpl.noDriver()).build();
	}
	
	/**
	 * Builder that builds the vehicle route.
	 * 
	 * @author stefan
	 *
	 */
	public static class Builder {
		
		/**
		 * Returns new instance of this builder.
		 * 
		 * <p><b>Construction-settings of vehicleRoute:</b>
		 * <p>startLocation == vehicle.getStartLocationId()
		 * <p>endLocation == vehicle.getEndLocationId()
		 * <p>departureTime == vehicle.getEarliestDepartureTime()
		 * <p>latestStart == Double.MAX_VALUE
		 * <p>earliestEnd == 0.0
		 * 
		 * @param vehicle
		 * @param driver
		 * @return this builder
		 */
		public static Builder newInstance(Vehicle vehicle, Driver driver){
			if(vehicle == null || driver == null) throw new IllegalArgumentException("null arguments not accepted. ini emptyRoute with VehicleImpl.createNoVehicle() and DriverImpl.noDriver()");
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
		
		/**
		 * Sets the serviceActivityFactory to create serviceActivities.
		 * 
		 * <p>By default {@link DefaultTourActivityFactory} is used.
		 * 
		 * @param serviceActivityFactory
		 */
		public void setServiceActivityFactory(TourActivityFactory serviceActivityFactory) {
			this.serviceActivityFactory = serviceActivityFactory;
		}

		/**
		 * Sets the shipmentActivityFactory to create shipmentActivities.
		 * 
		 * <p>By default {@link DefaultShipmentActivityFactory} is used.
		 * 
		 * @param shipmentActivityFactory
		 */
		public void setShipmentActivityFactory(TourShipmentActivityFactory shipmentActivityFactory) {
			this.shipmentActivityFactory = shipmentActivityFactory;
		}

		/**
		 * Constructs the route-builder.
		 * 
		 * <p>startLocation == vehicle.getStartLocationId()
		 * <p>endLocation == vehicle.getEndLocationId()
		 * <p>departureTime == vehicle.getEarliestDepartureTime()
		 * <p>latestStart == Double.MAX_VALUE
		 * <p>earliestEnd == 0.0
		 * @param vehicle
		 * @param driver
		 */
		private Builder(Vehicle vehicle, Driver driver) {
			super();
			this.vehicle = vehicle;
			this.driver = driver;
			start = Start.newInstance(vehicle.getStartLocationId(), vehicle.getEarliestDeparture(), Double.MAX_VALUE);
			start.setEndTime(vehicle.getEarliestDeparture());
			end = End.newInstance(vehicle.getLocationId(), 0.0, vehicle.getLatestArrival());
		}

		/**
		 * Sets the departure-time of the route, i.e. which is the time the vehicle departs from start-location.
		 * 
		 * <p><b>Note</b> that departureTime cannot be lower than earliestDepartureTime of vehicle.
		 * 
		 * @param departureTime
		 * @return builder
		 * @throws IllegalArgumentException if departureTime < vehicle.getEarliestDeparture()
		 */
		public Builder setDepartureTime(double departureTime){
			if(departureTime < start.getEndTime()) throw new IllegalArgumentException("departureTime < vehicle.getEarliestDepartureTime(). this must not be.");
			start.setEndTime(departureTime);
			return this;
		}
		
		/**
		 * Sets the end-time of the route, i.e. which is the time the vehicle has to be at its end-location at latest.
		 * 
		 * @param endTime
		 * @return this builder
		 * @throws IllegalArgumentException if endTime > vehicle.getLatestArrival()
		 */
		public Builder setRouteEndArrivalTime(double endTime){
			if(endTime > vehicle.getLatestArrival()) throw new IllegalArgumentException("endTime > vehicle.getLatestArrival(). this must not be.");
			end.setArrTime(endTime);
			return this;
		}
		
		/**
		 * Adds a service to this route.
		 * 
		 * <p>This implies that for this service a serviceActivity is created with {@link TourActivityFactory} and added to the sequence of tourActivities.
		 * 
		 * <p>The resulting activity occurs in the activity-sequence in the order adding/inserting.
		 * 
		 * @param service
		 * @return this builder
		 * @throws IllegalArgumentException if service is null
		 */
		public Builder addService(Service service){
			if(service == null) throw new IllegalArgumentException("service must not be null");
			addService(service,0.0,0.0);
			return this;
		}
		
		/**
		 * Adds a service with specified activity arrival- and endTime.  
		 * 
		 * <p>This implies that for this service a serviceActivity is created with {@link TourActivityFactory} and added to the sequence of tourActivities.
		 * 
		 * <p>Basically this activity is then scheduled with an activity arrival and activity endTime.
		 * 
		 * @param service
		 * @param arrTime
		 * @param endTime
		 * @return builder
		 */
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
	
	/**
	 * Copy constructor copying a route.
	 * 
	 * @param route
	 */
	private VehicleRoute(VehicleRoute route){
		this.start = Start.copyOf(route.getStart());
		this.end = End.copyOf(route.getEnd());
		this.tourActivities = TourActivities.copyOf(route.getTourActivities());
		this.vehicle = route.getVehicle();
		this.driver = route.getDriver();
	}
	
	@Deprecated
	private VehicleRoute(TourActivities tour, Driver driver, Vehicle vehicle) {
		super();
		verify(tour, driver, vehicle);
		this.tourActivities = tour;
		this.vehicle = vehicle;
		this.driver = driver;
		setStartAndEnd(vehicle, vehicle.getEarliestDeparture());
	}
	
	/**
	 * Constructs route.
	 * 
	 * @param builder
	 */
	private VehicleRoute(Builder builder){
		this.tourActivities = builder.tourActivities;
		this.vehicle = builder.vehicle;
		this.driver = builder.driver;
		this.start = builder.start;
		this.end = builder.end;
	}

	/**
	 * 
	 * @param tour
	 * @param driver
	 * @param vehicle
	 * @deprecated verification is a task of VehicleRoute.Builder
	 */
	@Deprecated
	private void verify(TourActivities tour, Driver driver, Vehicle vehicle) {
		if(tour == null || driver == null || vehicle == null) throw new IllegalStateException("null is not allowed for tour, driver or vehicle. use emptyRoute. use Tour.emptyTour, DriverImpl.noDriver() and VehicleImpl.noVehicle() instead." +
				"\n\tor make it easier and use VehicleRoute.emptyRoute()");
		if(!tour.isEmpty() && vehicle instanceof NoVehicle){
			throw new IllegalStateException("if tour is not empty. there must be a vehicle for this tour, but there is no vehicle.");
		}
	}

	/**
	 * Returns an unmodifiable list of activities on this route (without start/end).
	 * 
	 * @return
	 */
	public List<TourActivity> getActivities(){
		return Collections.unmodifiableList(tourActivities.getActivities());
	}
	
	/**
	 * Returns TourActivities.
	 * 
	 * @return {@link TourActivities}
	 */
	public TourActivities getTourActivities() {
		return tourActivities;
	}
	
	/**
	 * Returns the vehicle operating this route.
	 * 
	 * @return Vehicle
	 */
	public Vehicle getVehicle() {
		return vehicle;
	}

	/**
	 * Returns the driver operating this route.
	 * 
	 * @return Driver
	 */
	public Driver getDriver() {
		return driver;
	}

	/**
	 * Sets the vehicle and its departureTime from <code>vehicle.getStartLocationId()</code>.
	 * 
	 * <p>This implies the following:<br>
	 * if start and end are null, new start and end activities are created.<br>
	 * <p>startActivity is initialized with the start-location of the specified vehicle (<code>vehicle.getStartLocationId()</code>). the time-window of this activity is initialized 
	 * such that [<code>startActivity.getTheoreticalEarliestOperationStartTime()</code> = <code>vehicle.getEarliestDeparture()</code>][<code>startActivity.getTheoreticalLatestOperationStartTime()</code> = <code>vehicle.getLatestArrival()</code>]
	 * <p>endActivity is initialized with the end-location of the specified vehicle (<code>vehicle.getEndLocationId()</code>). The time-window of the 
	 * endActivity is initialized such that [<code>endActivity.getTheoreticalEarliestOperationStartTime()</code> = <code>vehicle.getEarliestDeparture()</code>][<code>endActivity.getTheoreticalLatestOperationStartTime()</code> = <code>vehicle.getLatestArrival()</code>]
	 * <p>startActivity.endTime (<code>startActivity.getEndTime()</code>) is set to max{<code>vehicle.getEarliestDeparture()</code>, <code>vehicleDepTime</code>}. 
	 * thus, <code>vehicle.getEarliestDeparture()</code> is a physical constraint that has to be met.
	 * 
	 * @param vehicle
	 * @param vehicleDepTime
	 */
	public void setVehicleAndDepartureTime(Vehicle vehicle, double vehicleDepTime){
		this.vehicle = vehicle;
		setStartAndEnd(vehicle, vehicleDepTime);
	}
	
	/**
	 *  Sets the vehicle and its departureTime from <code>vehicle.getStartLocationId()</code>.
	 * 
	 * <p>This implies the following:<br>
	 * if start and end are null, new start and end activities are created.<br>
	 * <p>startActivity is initialized with the start-location of the specified vehicle (<code>vehicle.getStartLocationId()</code>). the time-window of this activity is initialized 
	 * such that [<code>startActivity.getTheoreticalEarliestOperationStartTime()</code> = <code>vehicle.getEarliestDeparture()</code>][<code>startActivity.getTheoreticalLatestOperationStartTime()</code> = <code>vehicle.getLatestArrival()</code>]
	 * <p>endActivity is initialized with the end-location of the specified vehicle (<code>vehicle.getEndLocationId()</code>). The time-window of the 
	 * endActivity is initialized such that [<code>endActivity.getTheoreticalEarliestOperationStartTime()</code> = <code>vehicle.getEarliestDeparture()</code>][<code>endActivity.getTheoreticalLatestOperationStartTime()</code> = <code>vehicle.getLatestArrival()</code>]
	 * <p>startActivity.endTime (<code>startActivity.getEndTime()</code>) is set to max{<code>vehicle.getEarliestDeparture()</code>, <code>vehicleDepTime</code>}. 
	 * thus, <code>vehicle.getEarliestDeparture()</code> is a physical constraint that has to be met.
	 * 
	 * @param vehicle
	 * @param vehicleDepTime
	 * @deprecated use .setVehicleAndDepartureTime(Vehicle vehicle, double vehicleDepTime) instead
	 */
	@Deprecated
	public void setVehicle(Vehicle vehicle, double vehicleDepTime){
		this.vehicle = vehicle;
		setStartAndEnd(vehicle, vehicleDepTime);
	}
	
	private void setStartAndEnd(Vehicle vehicle, double vehicleDepTime) {
		if(!(vehicle instanceof NoVehicle)){
			if(start == null && end == null){
				start = Start.newInstance(vehicle.getStartLocationId(), vehicle.getEarliestDeparture(), vehicle.getLatestArrival());
				end = End.newInstance(vehicle.getEndLocationId(), vehicle.getEarliestDeparture(), vehicle.getLatestArrival());
			}
			start.setEndTime(Math.max(vehicleDepTime, vehicle.getEarliestDeparture()));
			start.setTheoreticalEarliestOperationStartTime(vehicle.getEarliestDeparture());
			start.setTheoreticalLatestOperationStartTime(vehicle.getLatestArrival());
			start.setLocationId(vehicle.getStartLocationId());
			end.setLocationId(vehicle.getEndLocationId());
			end.setTheoreticalEarliestOperationStartTime(vehicle.getEarliestDeparture());
			end.setTheoreticalLatestOperationStartTime(vehicle.getLatestArrival());
		}
		
	}

	/**
	 * Sets departureTime of this route, i.e. the time the vehicle departs from its start-location.
	 * 
	 * @param vehicleDepTime
	 * @deprecated use .setVehicleAndDepartureTime(...) instead (vehicle requires departureTime and the other way around, and earliestDepartureTime
	 * of a vehicle is a physical constraint of the vehicle and cannot be broken. Using this method might break this constraint.)
	 */
	@Deprecated
	public void setDepartureTime(double vehicleDepTime){
		if(start == null) throw new IllegalStateException("cannot set departureTime without having a vehicle on this route. use setVehicle(vehicle,departureTime) instead.");
		start.setEndTime(vehicleDepTime);
	}
	
	/**
	 * Returns the departureTime of this vehicle in this route.
	 * 
	 * @return departureTime
	 * @throws IllegalStateException if start is null
	 */
	public double getDepartureTime(){
		if(start == null) throw new IllegalStateException("cannot get departureTime without having a vehicle on this route. use setVehicle(vehicle,departureTime) instead.");
		return start.getEndTime();
	}
	
	/**
	 * Returns tour if tour-activity-sequence is empty, i.e. to activity on the tour yet.
	 * 
	 * @return true if route is empty
	 */
	public boolean isEmpty() {
		return tourActivities.isEmpty();
	}

	/**
	 * Returns start-activity of this route.
	 * 
	 * @return start
	 */
	public Start getStart() {
		return start;
	}

	/**
	 * Returns end-activity of this route.
	 * 
	 * @return end
	 */
	public End getEnd() {
		return end;
	}
	
	@Override
	public String toString() {
		return "[start="+start+"][end=" + end + "][departureTime=" + start.getEndTime() + "][vehicle=" + vehicle + "][driver=" + driver + "][nuOfActs="+tourActivities.getActivities().size()+"]";
	}

}
