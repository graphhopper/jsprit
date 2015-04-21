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
 * Delivery extends Service and is intended to model a Service where smth is UNLOADED (i.e. delivered) from a transport unit.
 * 
 * @author schroeder
 *
 */
public class Delivery extends Service{
	
	public static class Builder extends Service.Builder {

		/**
		 * Returns a new instance of builder that builds a delivery.
		 * 
		 * @param id the id of the delivery
		 * @return the builder
		 */
		public static Builder newInstance(String id){
			return new Builder(id);
		}

		Builder(String id) {
			super(id);
		}
		
		/**
		 * Builds Delivery.
		 * 
		 * @return delivery
		 * @throws IllegalStateException if neither locationId nor coord is set
		 */
		public Delivery build(){
			if(location == null) throw new IllegalStateException("location is missing");
			this.setType("delivery");
			super.capacity = super.capacityBuilder.build();
            super.skills = super.skillBuilder.build();
			return new Delivery(this);
		}
		
	}

	Delivery(Builder builder) {
		super(builder);
		
	}

}
