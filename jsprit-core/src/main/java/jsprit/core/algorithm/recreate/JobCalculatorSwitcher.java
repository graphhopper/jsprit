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
import jsprit.core.problem.vehicle.Vehicle;

import java.util.HashMap;
import java.util.Map;


class JobCalculatorSwitcher implements JobInsertionCostsCalculator{

	private Map<Class<? extends Job>,JobInsertionCostsCalculator> calcMap = new HashMap<Class<? extends Job>, JobInsertionCostsCalculator>();
	
	void put(Class<? extends Job> jobClass, JobInsertionCostsCalculator jic){
		calcMap.put(jobClass, jic);
	}
	
	public InsertionData getInsertionData(VehicleRoute currentRoute, Job jobToInsert, Vehicle newVehicle, double newVehicleDepartureTime, Driver newDriver, double bestKnownScore){
		JobInsertionCostsCalculator jic = calcMap.get(jobToInsert.getClass());
		if(jic==null) throw new IllegalStateException("cannot find calculator for " + jobToInsert.getClass());
		return jic.getInsertionData(currentRoute, jobToInsert, newVehicle, newVehicleDepartureTime, newDriver, bestKnownScore);
	}
	
}
