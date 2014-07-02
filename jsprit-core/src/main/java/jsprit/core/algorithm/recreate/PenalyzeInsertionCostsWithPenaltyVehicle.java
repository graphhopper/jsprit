/*******************************************************************************
 * Copyright (c) 2014 Stefan Schroeder.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 3.0 of the License, or (at your option) any later version.
 *  
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package jsprit.core.algorithm.recreate;

import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.vehicle.PenaltyVehicleType;
import jsprit.core.problem.vehicle.Vehicle;

class PenalyzeInsertionCostsWithPenaltyVehicle implements JobInsertionCostsCalculator{

	JobInsertionCostsCalculator base;
	
	public PenalyzeInsertionCostsWithPenaltyVehicle(JobInsertionCostsCalculator baseInsertionCostsCalculator) {
		super();
		this.base = baseInsertionCostsCalculator;
	}

	@Override
	public InsertionData getInsertionData(VehicleRoute currentRoute,Job newJob, Vehicle newVehicle, double newVehicleDepartureTime, Driver newDriver, double bestKnownCosts) {
		if(newVehicle.getType() instanceof PenaltyVehicleType){
			if(currentRoute.getVehicle().getType() instanceof PenaltyVehicleType){
				InsertionData iData = base.getInsertionData(currentRoute, newJob, newVehicle, newVehicleDepartureTime, newDriver, bestKnownCosts);
				double penaltyC = iData.getInsertionCost()*((PenaltyVehicleType)newVehicle.getType()).getPenaltyFactor();
				InsertionData newData = new InsertionData(penaltyC, iData.getPickupInsertionIndex(), iData.getDeliveryInsertionIndex(), iData.getSelectedVehicle(), iData.getSelectedDriver());
				newData.setAdditionalTime(iData.getAdditionalTime());
				newData.setVehicleDepartureTime(iData.getVehicleDepartureTime());
				return newData;
			}
		}
		return base.getInsertionData(currentRoute, newJob, newVehicle, newVehicleDepartureTime, newDriver, bestKnownCosts);
	}



}
