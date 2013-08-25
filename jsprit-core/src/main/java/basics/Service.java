/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package basics;

import basics.route.TimeWindow;
import util.Coordinate;


public class Service implements Job {

	public static class Builder {
		
		public static Builder newInstance(String id, int size){
			return new Builder(id,size);
		}
		
		private String id;
		private String locationId;
		private String type = "service";
		private Coordinate coord;
		private double serviceTime;
		private TimeWindow timeWindow = TimeWindow.newInstance(0.0, Double.MAX_VALUE);
		private int demand;
		
		Builder(String id, int size) {
			super();
			this.id = id;
			this.demand = size;
		}
		
		protected Builder setType(String name){
			this.type = name;
			return this;
		}
		
		public Builder setLocationId(String locationId){
			this.locationId = locationId;
			return this;
		}
		
		public Builder setCoord(Coordinate coord){
			this.coord = coord;
			return this;
		}
		
		public Builder setServiceTime(double serviceTime){
			this.serviceTime = serviceTime;
			return this;
		}
		
		public Builder setTimeWindow(TimeWindow tw){
			this.timeWindow = tw;
			return this;
		}
		
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

	public String getLocationId() {
		return locationId;
	}
	
	public Coordinate getCoord(){
		return coord;
	}

	public double getServiceDuration() {
		return serviceTime;
	}

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

	@Override
	public String toString() {
		return "[id=" + id + "][locationId=" + locationId + "][coord="+coord+"][size=" + demand + "][serviceTime=" + serviceTime + "][timeWindow=" + timeWindow + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
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
