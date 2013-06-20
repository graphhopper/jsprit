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
			super(VehicleBuilder.newInstance("noVehicle").setType(VehicleTypeImpl.newInstance(null, 0, null)));
		}
		
		public int getCapacity(){
			return 0;
		}
		
	}
	
	public static class VehicleBuilder {
		static Logger log = Logger.getLogger(VehicleBuilder.class); 
		private String id;
		
		private String locationId;
		private Coordinate locationCoord;
		private double earliestStart = 0.0;
		private double latestArrival = Double.MAX_VALUE;
		
		private VehicleType type = VehicleTypeImpl.Builder.newInstance("default", 0).build();
		
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
		return type.getCapacity();
	}
	
}
