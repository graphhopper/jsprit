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
package basics.costs;

import basics.route.Driver;
import basics.route.TourActivity;
import basics.route.Vehicle;


/**
 * Function that basically does not allow soft time-windows. Actually, it is allowed but it is penalized with Double.MaxValue(). 
 * 
 * @author schroeder
 *
 */
public class DefaultVehicleRoutingActivityCosts implements VehicleRoutingActivityCosts{

	@Override
	public double getActivityCost(TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle) {
		if(arrivalTime > tourAct.getTheoreticalLatestOperationStartTime()){
			return Double.MAX_VALUE;
		}
		return 0;
	}
	
	@Override
	public String toString() {
		return "[name=hardTimeWindowActCosts]";
	}

}
