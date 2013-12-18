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
 * 
 * @author stefan schroeder
 * 
 */

public class VehicleImpl implements Vehicle {

	public static NoVehicle noVehicle(){
		return createNoVehicle();
	}
	
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
	 * builds the vehicle.
	 * 
	 * <p>by default, it returns to the depot.
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
		
		private Builder(String id) {
			super();
			this.id = id;
		}
		
		public Builder setType(VehicleType type){
			this.type = type;
			return this;
		}
		
		public Builder setReturnToDepot(boolean returnToDepot){
			this.returnToDepot = returnToDepot;
			return this;
		}
		
		public Builder setLocationId(String id){
			this.locationId = id;
			return this;
		}
		
		public Builder setLocationCoord(Coordinate coord){
			this.locationCoord = coord;
			return this;
		}
		
		public Builder setEarliestStart(double start){
			this.earliestStart = start;
			return this;
		}
		
		public Builder setLatestArrival(double arr){
			this.latestArrival = arr;
			return this;
		}
		
		public VehicleImpl build(){
			if(locationId == null && locationCoord != null) locationId = locationCoord.toString();
			if(locationId == null && locationCoord == null) throw new IllegalStateException("locationId and locationCoord is missing.");
			if(locationCoord == null) log.warn("locationCoord for vehicle " + id + " is missing.");
			return new VehicleImpl(this);
		}
		
		public static Builder newInstance(String vehicleId){ return new Builder(vehicleId); }
		
	}

	
	public static NoVehicle createNoVehicle(){
		return new NoVehicle();
	}
	
	private final String id;

	private final VehicleType type;

	private final String locationId;

	private final Coordinate coord;

	private final double earliestDeparture;

	private final double latestArrival;
	
	private boolean returnToDepot;

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.matsim.contrib.freight.vrp.basics.Vehicle#getEarliestDeparture()
	 */
	@Override
	public double getEarliestDeparture() {
		return earliestDeparture;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.matsim.contrib.freight.vrp.basics.Vehicle#getLatestArrival()
	 */
	@Override
	public double getLatestArrival() {
		return latestArrival;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.matsim.contrib.freight.vrp.basics.Vehicle#getLocationId()
	 */
	@Override
	public String getLocationId() {
		return locationId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.matsim.contrib.freight.vrp.basics.Vehicle#getType()
	 */
	@Override
	public VehicleType getType() {
		return type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.matsim.contrib.freight.vrp.basics.Vehicle#getId()
	 */
	@Override
	public String getId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.matsim.contrib.freight.vrp.basics.Vehicle#getCapacity()
	 */
	@Override
	public int getCapacity() {
		return type.getCapacity();
	}



	/**
	 * @return the returnToDepot
	 */
	public boolean isReturnToDepot() {
		return returnToDepot;
	}
	
	
	
}
