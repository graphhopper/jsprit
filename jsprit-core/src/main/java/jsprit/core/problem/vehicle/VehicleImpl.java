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
		
		private String startLocationId;
		private Coordinate startLocationCoord;
		
		private String endLocationId;
		private Coordinate endLocationCoord;
		
		private boolean returnToDepot = true;
		
		private VehicleType type = VehicleTypeImpl.Builder.newInstance("default").build();
		
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
		 * Sets the {@link VehicleType}.<br>
		 * 
		 * @param type
		 * @throws IllegalStateException if type is null
		 * @return this builder
		 */
		public Builder setType(VehicleType type){
			if(type==null) throw new IllegalStateException("type cannot be null.");
			this.type = type;
			return this;
		}
		
		/**
		 * Sets the flag whether the vehicle must return to depot or not.
		 * 
		 * <p>If returnToDepot is true, the vehicle must return to specified end-location. If you
		 * omit specifying the end-location, vehicle returns to start-location (that must to be set). If
		 * you specify it, it returns to specified end-location.
		 * 
		 * <p>If returnToDepot is false, the end-location of the vehicle is endogenous.
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
		 * @deprecated use setStartLocationId(..) instead
		 */
		@Deprecated
		public Builder setLocationId(String id){
			this.locationId = id;
			this.startLocationId = id;
			return this;
		}
		
		/**
		 * Sets coordinate of the vehicle which should be the coordinate of start-location.
		 * 
		 * <p>If returnToDepot is true, it is also the coordinate of the end-location.
		 * 
		 * @param coord
		 * @return this builder
		 * @deprecated use setStartLocationCoordinate(...) instead
		 */
		@Deprecated 
		public Builder setLocationCoord(Coordinate coord){
			this.locationCoord = coord;
			this.startLocationCoord = coord;
			return this;
		}
		
		/**
		 * Sets the start-location of this vehicle.
		 * 
		 * @param startLocationId
		 * @return this builder
		 * @throws IllegalArgumentException if startLocationId is null
		 */
		public Builder setStartLocationId(String startLocationId){
			if(startLocationId == null) throw new IllegalArgumentException("startLocationId cannot be null");
			this.startLocationId = startLocationId;
			this.locationId = startLocationId;
			return this;
		}
		
		/**
		 * Sets the start-coordinate of this vehicle.
		 * 
		 * @param coord
		 * @return this builder
		 */
		public Builder setStartLocationCoordinate(Coordinate coord){
			this.startLocationCoord = coord;
			this.locationCoord = coord;
			return this;
		}
		
		/**
		 * Sets the end-locationId of this vehicle.
		 * 
		 * @param endLocationId
		 * @return this builder
		 */
		public Builder setEndLocationId(String endLocationId){
			this.endLocationId = endLocationId;
			return this;
		}
		
		/**
		 * Sets the end-coordinate of this vehicle.
		 * 
		 * @param coord
		 * @return this builder
		 */
		public Builder setEndLocationCoordinate(Coordinate coord){
			this.endLocationCoord = coord;
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
		 * <p>if {@link VehicleType} is not set, default vehicle-type is set with id="default" and 
		 * capacity=0
		 * 
		 * <p>if startLocationId || locationId is null (=> startLocationCoordinate || locationCoordinate must be set) then startLocationId=startLocationCoordinate.toString() 
		 * and locationId=locationCoordinate.toString() [coord.toString() --> [x=x_val][y=y_val])
		 * <p>if endLocationId is null and endLocationCoordinate is set then endLocationId=endLocationCoordinate.toString()
		 * <p>if endLocationId==null AND endLocationCoordinate==null then endLocationId=startLocationId AND endLocationCoord=startLocationCoord
		 * Thus endLocationId can never be null even returnToDepot is false.
		 * 
		 * @return vehicle
		 * @throws IllegalStateException if both locationId and locationCoord is not set or (endLocationCoord!=null AND returnToDepot=false) 
		 * or (endLocationId!=null AND returnToDepot=false)  
		 */
		public VehicleImpl build(){
			if((locationId == null && locationCoord == null) && (startLocationId == null && startLocationCoord == null)){
				throw new IllegalStateException("vehicle requires startLocation. but neither locationId nor locationCoord nor startLocationId nor startLocationCoord has been set");
			}
			if(locationId == null && locationCoord != null) {
				locationId = locationCoord.toString();
				startLocationId = locationCoord.toString();
			}
			if(locationId == null && locationCoord == null) throw new IllegalStateException("locationId and locationCoord is missing.");
			if(locationCoord == null) log.warn("locationCoord for vehicle " + id + " is missing.");
			if(endLocationId == null && endLocationCoord != null) endLocationId = endLocationCoord.toString();
			if(endLocationId == null && endLocationCoord == null) {
				endLocationId = startLocationId;
				endLocationCoord = startLocationCoord;
			}
			if( !startLocationId.equals(endLocationId) && returnToDepot == false) throw new IllegalStateException("this must not be. you specified both endLocationId and open-routes. this is contradictory. <br>" +
					"if you set endLocation, returnToDepot must be true. if returnToDepot is false, endLocationCoord must not be specified.");
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
	 * <p>NoVehicle has id="noVehicle" and extends {@link VehicleImpl}
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

	private final Coordinate endLocationCoord;

	private final String endLocationId;

	private final Coordinate startLocationCoord;

	private final String startLocationId;

	private VehicleImpl(Builder builder){
		id = builder.id;
		type = builder.type;
		coord = builder.locationCoord;
		locationId = builder.locationId;
		earliestDeparture = builder.earliestStart;
		latestArrival = builder.latestArrival;
		returnToDepot = builder.returnToDepot;
		startLocationId = builder.startLocationId;
		startLocationCoord = builder.startLocationCoord;
		endLocationId = builder.endLocationId;
		endLocationCoord = builder.endLocationCoord;
	}
	
	/**
	 * Returns String with attributes of this vehicle
	 * 
	 * <p>String has the following format [attr1=val1][attr2=val2]...[attrn=valn]
	 */
	@Override
	public String toString() {
		return "[id="+id+"][type="+type+"][locationId="+locationId+"][coord=" + coord + "][isReturnToDepot=" + isReturnToDepot() + "]";
	}

	/**
	 * @deprecated use getStartLocationCoordinate() instead
	 */
	@Deprecated
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

	/**
	 * @deprecated use getStartLocationId() instead
	 */
	@Deprecated
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
	@Deprecated
	public int getCapacity() {
		return type.getCapacity();
	}

	public boolean isReturnToDepot() {
		return returnToDepot;
	}

	@Override
	public String getStartLocationId() {
		return this.startLocationId;
	}

	@Override
	public Coordinate getStartLocationCoordinate() {
		return this.startLocationCoord;
	}

	@Override
	public String getEndLocationId() {
		return this.endLocationId;
	}

	@Override
	public Coordinate getEndLocationCoordinate() {
		return this.endLocationCoord;
	}
	
}
