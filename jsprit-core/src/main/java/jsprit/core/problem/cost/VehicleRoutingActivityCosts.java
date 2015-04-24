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
package jsprit.core.problem.cost;

import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.vehicle.Vehicle;

/**
 * Interface for overall routing and operation costs.
 * 
 * <p>This calculates activity and leg-based costs. If you want to consider for example costs incurred by missed time-windows, you can do it here.
 * 
 * @author schroeder
 *
 */
public interface VehicleRoutingActivityCosts {
	
	public static class Time {
		
		public static double TOUREND = -2.0;
		
		public static double TOURSTART = -1.0;
		
		public static double UNDEFINED = -3.0;
	}
	
	public static interface Parameter {
		
		public double getPenaltyForMissedTimeWindow();
		
	}
	
	/**
	 * Calculates and returns the activity cost at tourAct.
	 * 
	 * <p>Here waiting-times, service-times and missed time-windows can be considered.
	 * 
	 * @param tourAct
	 * @param arrivalTime is actually the arrival time at this tourActivity, which must not nessecarrily be the operation start time. If the theoretical earliest
	 * operation start time at this activity is later than actualStartTime, the driver must wait at this activity.
	 * @param driver
	 * @param vehicle
	 *
	 * if earliestStartTime > latestStartTime activity operations cannot be conducted within the given time-window.
	 * @return
	 */
	public double getActivityCost(TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle);

	
}
