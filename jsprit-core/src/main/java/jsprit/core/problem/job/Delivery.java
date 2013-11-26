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


public final class Delivery extends Service{
	
	public static class Builder extends Service.Builder {

		public static Builder newInstance(String id, int size){
			return new Builder(id,size);
		}
		
		Builder(String id, int size) {
			super(id, size);
		}
		
		public Delivery build(){
			if(locationId == null) { 
				if(coord == null) throw new IllegalStateException("either locationId or a coordinate must be given. But is not.");
				locationId = coord.toString();
			}
			this.setType("delivery");
			return new Delivery(this);
		}
		
	}
	
	Delivery(Builder builder) {
		super(builder);
		
	}

}
