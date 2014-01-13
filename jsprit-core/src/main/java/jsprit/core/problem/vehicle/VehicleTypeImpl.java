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

/**
 * Implementation of {@link VehicleType}.
 * 
 * <p>Two vehicle-types are equal if they have the same typeId.
 * 
 * @author schroeder
 *
 */
public class VehicleTypeImpl implements VehicleType {
	
	/**
	 * CostParameter consisting of fixed cost parameter, time-based cost parameter and distance-based cost parameter.
	 * 
	 * @author schroeder
	 *
	 */
	public static class VehicleCostParams {
		
		public static VehicleTypeImpl.VehicleCostParams newInstance(double fix, double perTimeUnit,double perDistanceUnit){
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

	/**
	 * Builder that builds the vehicle-type.
	 * 
	 * @author schroeder
	 *
	 */
	public static class Builder{
		
		/**
		 * Returns a new instance.
		 * 
		 * <p>Input parameters are id and capacity. Note that two vehicle-types are equal
		 * if they have the same vehicleId.
		 * 
		 * @param id
		 * @param capacity
		 * @return
		 */
		public static VehicleTypeImpl.Builder newInstance(String id, int capacity){
			return new Builder(id,capacity);
		}
		
		private String id;
		private int capacity;
		private double maxVelo = Double.MAX_VALUE;
		/**
		 * default cost values for default vehicle type
		 */
		private double fixedCost = 0.0;
		private double perDistance = 1.0;
		private double perTime = 0.0;

		/**
		 * Constructs the builder.
		 * 
		 * @param id
		 * @param capacity
		 */
		private Builder(String id, int capacity) {
			super();
			this.id = id;
			this.capacity = capacity;
		}

		/**
		 * Sets the maximum velocity this vehicle-type can go [in meter per seconds].
		 * 
		 * @param inMeterPerSeconds
		 * @return this builder
		 */
		public VehicleTypeImpl.Builder setMaxVelocity(double inMeterPerSeconds){ this.maxVelo = inMeterPerSeconds; return this; }
		
		/**
		 * Sets the fixed costs of the vehicle-type.
		 * 
		 * <p>by default it is 0.
		 * 
		 * @param fixedCost
		 * @return this builder
		 */
		public VehicleTypeImpl.Builder setFixedCost(double fixedCost) { this.fixedCost = fixedCost; return this; }

		/**
		 * Sets the cost per distance unit, for instance € per meter.
		 * 
		 * <p>by default it is 1.0
		 * 
		 * @param perDistance
		 * @return this builder
		 */
		public VehicleTypeImpl.Builder setCostPerDistance(double perDistance){ this.perDistance = perDistance; return this; }

		/**
		 * Sets cost per time unit, for instance € per second.
		 * 
		 * <p>by default it is 0.0 
		 * 
		 * @param perTime
		 * @return
		 */
		public VehicleTypeImpl.Builder setCostPerTime(double perTime){ this.perTime = perTime; return this; }
		
		/**
		 * Builds the vehicle-type.
		 * 
		 * @return
		 */
		public VehicleTypeImpl build(){
			return new VehicleTypeImpl(this);
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

	/**
	 * Two vehicle-types are equal if they have the same vehicleId.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VehicleTypeImpl other = (VehicleTypeImpl) obj;
		if (typeId == null) {
			if (other.typeId != null)
				return false;
		} else if (!typeId.equals(other.typeId))
			return false;
		return true;
	}

	private final String typeId;
	
	private final int capacity;
	
	private final VehicleTypeImpl.VehicleCostParams vehicleCostParams;
	
	private double maxVelocity;

	/**
	 * @deprecated use builder instead
	 */
	@Deprecated
	public static VehicleTypeImpl newInstance(String typeId, int capacity, VehicleTypeImpl.VehicleCostParams para){
		return new VehicleTypeImpl(typeId, capacity, para);
	}
	
	/**
	 * priv constructor constructing vehicle-type
	 * 
	 * @param builder
	 */
	private VehicleTypeImpl(VehicleTypeImpl.Builder builder){
		typeId = builder.id;
		capacity = builder.capacity;
		maxVelocity = builder.maxVelo;
		vehicleCostParams = new VehicleCostParams(builder.fixedCost, builder.perTime, builder.perDistance);
	}

	public VehicleTypeImpl(String typeId, int capacity,VehicleTypeImpl.VehicleCostParams vehicleCostParams) {
		super();
		this.typeId = typeId;
		this.capacity = capacity;
		this.vehicleCostParams = vehicleCostParams;
	}

	/* (non-Javadoc)
	 * @see basics.route.VehicleType#getTypeId()
	 */
	@Override
	public String getTypeId() {
		return typeId;
	}

	/* (non-Javadoc)
	 * @see basics.route.VehicleType#getCapacity()
	 */
	@Override
	public int getCapacity() {
		return capacity;
	}

	/* (non-Javadoc)
	 * @see basics.route.VehicleType#getVehicleCostParams()
	 */
	@Override
	public VehicleTypeImpl.VehicleCostParams getVehicleCostParams() {
		return vehicleCostParams;
	}

	@Override
	public String toString() {
		return "[typeId="+typeId+"][capacity="+capacity+"]" + vehicleCostParams;
	}

	@Override
	public double getMaxVelocity() {
		return maxVelocity;
	}
}
