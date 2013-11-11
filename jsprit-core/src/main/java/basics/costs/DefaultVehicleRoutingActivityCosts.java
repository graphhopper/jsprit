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
package basics.costs;

import basics.route.Driver;
import basics.route.TourActivity;
import basics.route.Vehicle;


/**
 * DefaultActivityCosts = 0.0, i.e. activities do not induce costs at all. 
 * 
 * @author schroeder
 *
 */
public class DefaultVehicleRoutingActivityCosts implements VehicleRoutingActivityCosts{

	@Override
	public double getActivityCost(TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle) {
		return 0;
	}
	
	@Override
	public String toString() {
		return "[name=defaultActivityCosts]";
	}

}
