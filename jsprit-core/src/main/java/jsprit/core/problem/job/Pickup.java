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


/**
 * Pickup extends Service and is intended to model a Service where smth is LOADED (i.e. picked up) to a transport unit.
 * 
 * @author schroeder
 *
 */
public class Pickup extends Service {

	public static class Builder extends Service.Builder {
		
		/**
		 * Returns a new instance of Pickup.Builder
		 * 
		 * @param id
		 * @param size
		 * @return builder
		 * @throws IllegalArgumentException if size < 0 or id is null
		 * @deprecated use <code>.newInstance(String id)</code> instead, and add a capacity dimension
		 * with dimensionIndex='your index' and and dimsionValue=size to the returned builder
		 */
		@Deprecated
		public static Builder newInstance(String id, int size){
			Builder builder = new Builder(id,size);
			builder.addSizeDimension(0, size);
			return builder;
		}
		
		/**
		 * Returns a new instance of builder that builds a pickup.
		 * 
		 * @param id
		 * @return the builder
		 */
		public static Builder newInstance(String id){
			return new Builder(id);
		}
		
		/**
		 * Constructs the builder.
		 * 
		 * @param id
		 * @param size
		 * @throws IllegalArgumentException if size < 0 or id is null
		 */
		Builder(String id, int size) {
			super(id, size);
		}
		
		Builder(String id) {
			super(id);
		}
		
		/**
		 * Builds Pickup.
		 * 
		 *<p>Pickup type is "pickup"
		 *
		 * @return pickup
		 * @throw IllegalStateException if neither locationId nor coordinate has been set
		 */
		public Pickup build(){
			if(locationId == null) { 
				if(coord == null) throw new IllegalStateException("either locationId or a coordinate must be given. But is not.");
				locationId = coord.toString();
			}
			this.setType("pickup");
			super.capacity = super.capacityBuilder.build();
			return new Pickup(this);
		}
		
	}
	
	/**
	 * Constructs the Pickup
	 * 
	 * @param builder
	 */
	Pickup(Builder builder) {
		super(builder);
	}
	
}
