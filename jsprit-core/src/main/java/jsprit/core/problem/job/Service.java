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
package jsprit.core.problem.job;

import jsprit.core.problem.solution.route.activity.TimeWindow;
import jsprit.core.util.Coordinate;

/**
 * Service implementation of a job.
 * 
 * <p>A service distinguishes itself from a shipment such that it has only one locations. Thus a service
 * is a single point in space (where a service-activity occurs).
 * 
 * <p>Note that two services are equal if they have the same id.
 * 
 * @author schroeder
 *
 */
public class Service implements Job {

	/**
	 * Builder that builds a service.
	 * 
	 * @author schroeder
	 *
	 */
	public static class Builder {
		
		/**
		 * Returns a new instance of service-builder.
		 * 
		 * @param id of service
		 * @param size of capacity-demand
		 * @return builder
		 */
		public static Builder newInstance(String id, int size){
			return new Builder(id,size);
		}
		
		private String id;
		
		protected String locationId;
		
		private String type = "service";
		
		protected Coordinate coord;
		
		protected double serviceTime;
		
		protected TimeWindow timeWindow = TimeWindow.newInstance(0.0, Double.MAX_VALUE);
		
		protected int demand;
		
		/**
		 * Constructs the builder.
		 * 
		 * @param id
		 * @param size
		 */
		Builder(String id, int size) {
			if(size < 0) throw new IllegalArgumentException("size must be greater than or equal to zero");
			this.id = id;
			this.demand = size;
		}
		
		/**
		 * Protected method to set the type-name of the service.
		 * 
		 * <p>Currently there are {@link Service}, {@link Pickup} and {@link Delivery}.
		 * 
		 * @param name
		 * @return the builder
		 */
		protected Builder setType(String name){
			this.type = name;
			return this;
		}
		
		/**
		 * Sets the location-id of this service.
		 * 
		 * @param locationId
		 * @return builder
		 */
		public Builder setLocationId(String locationId){
			this.locationId = locationId;
			return this;
		}
		
		/**
		 * Sets the coordinate of this service.
		 * 
		 * @param coord
		 * @return builder
		 */
		public Builder setCoord(Coordinate coord){
			this.coord = coord;
			return this;
		}
		
		/**
		 * Sets the serviceTime of this service.
		 * 
		 * <p>It is understood as time that a service or its implied activity takes at the service-location, for instance
		 * to unload goods.
		 * 
		 * @param serviceTime
		 * @return builder
		 */
		public Builder setServiceTime(double serviceTime){
			if(serviceTime < 0) throw new IllegalArgumentException("serviceTime must be greate than or equal to zero");
			this.serviceTime = serviceTime;
			return this;
		}
		
		/**
		 * Sets the time-window of this service.
		 * 
		 * <p>The time-window indicates the time period a service/activity/operation is allowed to start. 
		 * 
		 * @param tw
		 * @return builder
		 */
		public Builder setTimeWindow(TimeWindow tw){
			this.timeWindow = tw;
			return this;
		}
		
		/**
		 * Builds the service.
		 * 
		 * @return {@link Service}
		 * @throws IllegalStateException if neither locationId nor coordinate is set.
		 */
		public Service build(){
			if(locationId == null) { 
				if(coord == null) throw new IllegalStateException("either locationId or a coordinate must be given. But is not.");
				locationId = coord.toString();
			}
			this.setType("service");
			return new Service(this);
		}
		
	}
	
	
	private final String id;

	private final String locationId;
	
	private final String type;

	private final Coordinate coord;
	
	private final double serviceTime;

	private final TimeWindow timeWindow;

	private final int demand;

	Service(Builder builder){
		id = builder.id;
		locationId = builder.locationId;
		coord = builder.coord;
		serviceTime = builder.serviceTime;
		timeWindow = builder.timeWindow;
		demand = builder.demand;
		type = builder.type;
	}

	@Override
	public String getId() {
		return id;
	}

	/**
	 * Returns the location-id of this service.
	 * 
	 * @return String that indicates the location
	 */
	public String getLocationId() {
		return locationId;
	}
	
	/**
	 * Returns the coordinate of this service.
	 * 
	 * @return {@link Coordinate}
	 */
	public Coordinate getCoord(){
		return coord;
	}

	/**
	 * Returns the service-time/duration a service takes at service-location.
	 * 
	 * @return service duration
	 */
	public double getServiceDuration() {
		return serviceTime;
	}

	/**
	 * Returns the time-window a service(-operation) is allowed to start.
	 * 
	 * @return time window
	 */
	public TimeWindow getTimeWindow(){
		return timeWindow;
	}
	
	@Override
	public int getCapacityDemand() {
		return demand;
	}
	
	/**
	 * @return the name
	 */
	public String getType() {
		return type;
	}

	/**
	 * Returns a string with the service's attributes.
	 * 
	 * <p>String is built as follows: [attr1=val1][attr2=val2]...
	 */
	@Override
	public String toString() {
		return "[id=" + id + "][type="+type+"][locationId=" + locationId + "][coord="+coord+"][size=" + demand + "][serviceTime=" + serviceTime + "][timeWindow=" + timeWindow + "]";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/**
	 * Two services are equal if they have the same id.
	 * 
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Service other = (Service) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	
	
}
