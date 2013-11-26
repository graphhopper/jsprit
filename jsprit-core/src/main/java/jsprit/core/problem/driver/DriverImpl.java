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
package jsprit.core.problem.driver;

public class DriverImpl implements Driver {

	public static NoDriver noDriver(){
		return new NoDriver();
	}
	
	public static class NoDriver extends DriverImpl {

		public NoDriver() {
			super("noDriver");
		}
		
	}
	
	private String id;

	private double earliestStart = 0.0;

	private double latestEnd = Double.MAX_VALUE;

	private String home;

	private DriverImpl(String id) {
		super();
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public double getEarliestStart() {
		return earliestStart;
	}

	public void setEarliestStart(double earliestStart) {
		this.earliestStart = earliestStart;
	}

	public double getLatestEnd() {
		return latestEnd;
	}

	public void setLatestEnd(double latestEnd) {
		this.latestEnd = latestEnd;
	}

	public void setHomeLocation(String locationId) {
		this.home = locationId;
	}

	public String getHomeLocation() {
		return this.home;
	}

}
