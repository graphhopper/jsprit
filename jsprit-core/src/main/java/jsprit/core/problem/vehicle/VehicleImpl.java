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
package jsprit.core.problem.vehicle;

import jsprit.core.util.Coordinate;

import org.apache.log4j.Logger;


/**
 * Implementation of {@link Vehicle}.
 * 
 * @author stefan schroeder
 * 
 */

public class VehicleImpl implements Vehicle {

	/**
	 * @deprecated use createNoVehicle() instead.
	 * 
	 * @return
	 */
	@Deprecated
	public static NoVehicle noVehicle(){
		return createNoVehicle();
	}
	
	/**
	 * Extension of {@link VehicleImpl} representing an unspecified vehicle with the id 'noVehicle'
	 * (to avoid null).
	 * 
	 * @author schroeder
	 *
	 */
	public static class NoVehicle extends VehicleImpl {

		@SuppressWarnings("deprecation")
		public NoVehicle() {
			super(Builder.newInstance("noVehicle").setType(VehicleTypeImpl.newInstance(null, 0, null)));
		}
		
		public int getCapacity(){
			return 0;
		}
		
	}
	
	/**
	 * Builder that builds the vehicle.
	 * 
	 * <p>By default, earliestDepartureTime is 0.0, latestDepartureTime is Double.MAX_VALUE,
	 * it returns to the depot and its {@link VehicleType} is the DefaultType with typeId equal to 'default'
	 * and a capacity of 0.
	 * 
	 * @author stefan
	 *
	 */
	public static class Builder {
		static Logger log = Logger.getLogger(Builder.class); 
		private String id;
		
		private String locationId;
		private Coordinate locationCoord;
		private double earliestStart = 0.0;
		private double latestArrival = Double.MAX_VALUE;
		
		private boolean returnToDepot = true;
		
		private VehicleType type = VehicleTypeImpl.Builder.newInstance("default", 0).build();
		
		/**
		 * Constructs the builder with the vehicleId.
		 * 
		 * @param id
		 */
		private Builder(String id) {
			super();
			this.id = id;
		}
		
		/**
		 * Sets the {@link VehicleType}.
		 * 
		 * @param type
		 * @return this builder
		 */
		public Builder setType(VehicleType type){
			this.type = type;
			return this;
		}
		
		/**
		 * Sets the flag whether the vehicle must return to depot or not.
		 * 
		 * @param returnToDepot
		 * @return this builder
		 */
		public Builder setReturnToDepot(boolean returnToDepot){
			this.returnToDepot = returnToDepot;
			return this;
		}
		
		/**
		 * Sets location-id of the vehicle which should be its start-location.
		 * 
		 * <p>If returnToDepot is true, it is also its end-location.
		 * 
		 * @param id
		 * @return this builder
		 */
		public Builder setLocationId(String id){
			this.locationId = id;
			return this;
		}
		
		/**
		 * Sets coordinate of the vehicle which should be the coordinate of start-location.
		 * 
		 * <p>If returnToDepot is true, it is also the coordinate of the end-location.
		 * 
		 * @param coord
		 * @return this builder
		 */
		public Builder setLocationCoord(Coordinate coord){
			this.locationCoord = coord;
			return this;
		}
		
		/**
		 * Sets earliest-start of vehicle which should be the lower bound of the vehicle's departure times.
		 * 
		 * @param start
		 * @return this builder
		 */
		public Builder setEarliestStart(double start){
			this.earliestStart = start;
			return this;
		}
		
		/**
		 * Sets the latest arrival at vehicle's end-location which is the upper bound of the vehicle's arrival times.
		 * 
		 * @param arr
		 * @return this builder
		 */
		public Builder setLatestArrival(double arr){
			this.latestArrival = arr;
			return this;
		}
		
		/**
		 * Builds and returns the vehicle.
		 * 
		 * @return vehicle
		 * @throw IllegalStateException if both locationId and locationCoord is not set
		 */
		public VehicleImpl build(){
			if(locationId == null && locationCoord != null) locationId = locationCoord.toString();
			if(locationId == null && locationCoord == null) throw new IllegalStateException("locationId and locationCoord is missing.");
			if(locationCoord == null) log.warn("locationCoord for vehicle " + id + " is missing.");
			return new VehicleImpl(this);
		}
		
		/**
		 * Returns new instance of vehicle builder.
		 * 
		 * @param vehicleId
		 * @return vehicle builder
		 */
		public static Builder newInstance(String vehicleId){ return new Builder(vehicleId); }
		
	}

	/**
	 * Returns empty/noVehicle which is a vehicle having no capacity, no type and no reasonable id.
	 * 
	 * @return emptyVehicle
	 */
	public static NoVehicle createNoVehicle(){
		return new NoVehicle();
	}
	
	private final String id;

	private final VehicleType type;

	private final String locationId;

	private final Coordinate coord;

	private final double earliestDeparture;

	private final double latestArrival;
	
	private final boolean returnToDepot;

	private VehicleImpl(Builder builder){
		id = builder.id;
		type = builder.type;
		coord = builder.locationCoord;
		locationId = builder.locationId;
		earliestDeparture = builder.earliestStart;
		latestArrival = builder.latestArrival;
		returnToDepot = builder.returnToDepot;
	}
	
	@Override
	public String toString() {
		return "[id="+id+"][type="+type+"][locationId="+locationId+"][coord=" + coord + "]";
	}

	public Coordinate getCoord() {
		return coord;
	}

	@Override
	public double getEarliestDeparture() {
		return earliestDeparture;
	}

	@Override
	public double getLatestArrival() {
		return latestArrival;
	}

	@Override
	public String getLocationId() {
		return locationId;
	}

	@Override
	public VehicleType getType() {
		return type;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public int getCapacity() {
		return type.getCapacity();
	}

	public boolean isReturnToDepot() {
		return returnToDepot;
	}
	
}
