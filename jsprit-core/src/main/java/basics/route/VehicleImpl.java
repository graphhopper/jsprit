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

import org.apache.log4j.Logger;

import util.Coordinate;

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

		public NoVehicle() {
			super(VehicleBuilder.newInstance("noVehicle").setType(VehicleType.newInstance(null, 0, null)));
		}
		
		public int getCapacity(){
			return 0;
		}
		
	}
	
	public static class VehicleType {
		
		public static class Builder{
			
			public static Builder newInstance(String id, int capacity){
				return new Builder(id,capacity);
			}
			
			private String id;
			private int capacity;
			/**
			 * default cost values for default vehicle type
			 */
			private double fixedCost = 0.0;
			private double perDistance = 1.0;
			private double perTime = 0.0;

			public Builder(String id, int capacity) {
				super();
				this.id = id;
				this.capacity = capacity;
			}

			public Builder setFixedCost(double fixedCost) { this.fixedCost = fixedCost; return this; }

			public Builder setCostPerDistance(double perDistance){ this.perDistance = perDistance; return this; }

			public Builder setCostPerTime(double perTime){ this.perTime = perTime; return this; }
			
			public VehicleType build(){
				return new VehicleType(this);
			}

		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((typeId == null) ? 0 : typeId.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			VehicleType other = (VehicleType) obj;
			if (typeId == null) {
				if (other.typeId != null)
					return false;
			} else if (!typeId.equals(other.typeId))
				return false;
			return true;
		}

		public final String typeId;
		public final int capacity;
		public final VehicleCostParams vehicleCostParams;

		public static VehicleType newInstance(String typeId, int capacity, VehicleCostParams para){
			return new VehicleType(typeId, capacity, para);
		}
		
		private VehicleType(Builder builder){
			typeId = builder.id;
			capacity = builder.capacity;
			vehicleCostParams = new VehicleCostParams(builder.fixedCost, builder.perTime, builder.perDistance);
		}

		public VehicleType(String typeId, int capacity,VehicleCostParams vehicleCostParams) {
			super();
			this.typeId = typeId;
			this.capacity = capacity;
			this.vehicleCostParams = vehicleCostParams;
		}

		public String getTypeId() {
			return typeId;
		}

		public int getCapacity() {
			return capacity;
		}

		public VehicleCostParams getVehicleCostParams() {
			return vehicleCostParams;
		}

		@Override
		public String toString() {
			return "[typeId="+typeId+"][capacity="+capacity+"]" + vehicleCostParams;
		}
	}

	public static class VehicleCostParams {
		
		public static VehicleCostParams newInstance(double fix, double perTimeUnit,double perDistanceUnit){
			return new VehicleCostParams(fix, perTimeUnit, perDistanceUnit);
		}
		
		public final double fix;
		public final double perTimeUnit;
		public final double perDistanceUnit;

		private VehicleCostParams(double fix, double perTimeUnit,double perDistanceUnit) {
			super();
			this.fix = fix;
			this.perTimeUnit = perTimeUnit;
			this.perDistanceUnit = perDistanceUnit;
		}
		
		@Override
		public String toString() {
			return "[fixed="+fix+"][perTime="+perTimeUnit+"][perDistance="+perDistanceUnit+"]";
		}
	}
	
	public static class VehicleBuilder {
		static Logger log = Logger.getLogger(VehicleBuilder.class); 
		private String id;
		
		private String locationId;
		private Coordinate locationCoord;
		private double earliestStart = 0.0;
		private double latestArrival = Double.MAX_VALUE;
		
		private VehicleType type = VehicleType.Builder.newInstance("default", 0).build();
		
		private VehicleBuilder(String id) {
			super();
			this.id = id;
		}
		
		public VehicleBuilder setType(VehicleType type){
			this.type = type;
			return this;
		}
		
		public VehicleBuilder setLocationId(String id){
			this.locationId = id;
			return this;
		}
		
		public VehicleBuilder setLocationCoord(Coordinate coord){
			this.locationCoord = coord;
			return this;
		}
		
		public VehicleBuilder setEarliestStart(double start){
			this.earliestStart = start;
			return this;
		}
		
		public VehicleBuilder setLatestArrival(double arr){
			this.latestArrival = arr;
			return this;
		}
		
		public VehicleImpl build(){
			if(locationId == null && locationCoord != null) locationId = locationCoord.toString();
			if(locationId == null && locationCoord == null) throw new IllegalStateException("locationId and locationCoord is missing.");
			if(locationCoord == null) log.warn("locationCoord for vehicle " + id + " is missing.");
			return new VehicleImpl(this);
		}
		
		public static VehicleBuilder newInstance(String vehicleId){ return new VehicleBuilder(vehicleId); }
		
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

	private VehicleImpl(VehicleBuilder builder){
		id = builder.id;
		type = builder.type;
		coord = builder.locationCoord;
		locationId = builder.locationId;
		earliestDeparture = builder.earliestStart;
		latestArrival = builder.latestArrival;
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
		return type.capacity;
	}
	
}
