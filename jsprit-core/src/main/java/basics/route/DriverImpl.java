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
package basics.route;

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
