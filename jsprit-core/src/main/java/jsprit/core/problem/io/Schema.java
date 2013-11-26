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
package jsprit.core.problem.io;

final class Schema {

	public static final String PROBLEM = "problem";
	public static final String VEHICLE = "vehicle";
	public static final String TYPES = "vehicleTypes";
	public static final String VEHICLES = "vehicles";
	public static final String SHIPMENTS = "shipments";
	public static final String SHIPMENT = "shipment";
	public static final String SERVICETIME = "serviceTime";
	public static final String PICKUP = "pickup";
	public static final String TYPE = "type";
	
	
	public void dot(){
		
	}
	
	public static class PathBuilder {
		
		StringBuilder stringBuilder = new StringBuilder();
		boolean justCreated = true;
		
		
		public PathBuilder dot(String string){
			stringBuilder.append(".").append(string);
			return this;
		}
		
		public PathBuilder append(String string){
			stringBuilder.append(string);
			return this;
		}
		
		public String build(){ return stringBuilder.toString(); }
		
	}
	
	public static PathBuilder builder(){
		return new PathBuilder();
	}
	
	private Schema(){
		
	}
}
