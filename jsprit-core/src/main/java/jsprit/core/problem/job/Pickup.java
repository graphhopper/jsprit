/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
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
		 * Returns a new instance of builder that builds a pickup.
		 * 
		 * @param id the id of the pickup
		 * @return the builder
		 */
		public static Builder newInstance(String id){
			return new Builder(id);
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
		 * @throws IllegalStateException if neither locationId nor coordinate has been set
		 */
		public Pickup build(){
			if(location == null) throw new IllegalStateException("location is missing");
			this.setType("pickup");
			super.capacity = super.capacityBuilder.build();
            super.skills = super.skillBuilder.build();
			return new Pickup(this);
		}
		
	}

	Pickup(Builder builder) {
		super(builder);
	}
	
}
