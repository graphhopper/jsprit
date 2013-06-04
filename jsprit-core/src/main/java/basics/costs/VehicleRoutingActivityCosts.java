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
	 * @param driver TODO
	 * @param vehicle TODO
	 * @param earliestStartTime, this is the practical earliest operation start time which considers also previous activities.
	 * @param latestStartTime, this is the practical latest operation start time which consider also future activities in the tour. 
	 * if earliestStartTime > latestStartTime activity operations cannot be conducted within the given time-window.
	 * @return
	 */
	public double getActivityCost(TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle);

//	public Parameter getParameter(TourActivity tourAct, Vehicle vehicle, Driver driver);
	
}
