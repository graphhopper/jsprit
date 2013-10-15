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
package algorithms;

import basics.costs.VehicleRoutingActivityCosts;
import basics.route.Driver;
import basics.route.TourActivity;
import basics.route.TourActivity.JobActivity;
import basics.route.Vehicle;


public class ExampleActivityCostFunction implements VehicleRoutingActivityCosts{
		
	public ExampleActivityCostFunction() {
		super();
	}
	
	public double parameter_timeAtAct;

	public double parameter_penaltyTooLate;


	@Override
	public double getActivityCost(TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle) {
		if(arrivalTime == Time.TOURSTART  || arrivalTime == Time.UNDEFINED ){
			return 0.0;
		}
		else{
			//waiting + act-time
			double endTime = Math.max(arrivalTime, tourAct.getTheoreticalEarliestOperationStartTime()) + tourAct.getOperationTime();
			double timeAtAct = endTime - arrivalTime;
			
			double totalCost = timeAtAct * parameter_timeAtAct;

			//penalty tooLate
			if(tourAct instanceof JobActivity){
				if(arrivalTime > tourAct.getTheoreticalLatestOperationStartTime()){
					double penTime = arrivalTime - tourAct.getTheoreticalLatestOperationStartTime();
					totalCost += penTime * parameter_penaltyTooLate;
				}
			}
			return totalCost;

		}
	}

}
