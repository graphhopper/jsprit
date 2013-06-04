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
package util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import basics.Job;
import basics.Service;
import basics.VehicleRoutingAlgorithm;
import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblemSolution;
import basics.algo.AlgorithmStartsListener;
import basics.route.DriverImpl;
import basics.route.Vehicle;

public class VrpVerifier implements AlgorithmStartsListener{

	private static Logger log = Logger.getLogger(VrpVerifier.class);
	
	private boolean hardTimeWindows = true;
	
	@Override
	public void informAlgorithmStarts(VehicleRoutingProblem problem, VehicleRoutingAlgorithm algorithm, Collection<VehicleRoutingProblemSolution> solutions) {
		//check capacity
		log.info("verifying vehicle-routing-problem ...");
		log.info("check vehicle capacities ...");
		Vehicle vehicleWithMaxCapacity = getMaxVehicle(problem);
		if(vehicleWithMaxCapacity == null) throw new IllegalStateException("vehicles are missing.");
		for(Job j : problem.getJobs().values()){
			if(vehicleWithMaxCapacity.getCapacity() < Math.abs(j.getCapacityDemand())){
				throw new IllegalStateException("maximal vehicle-capacity is "+vehicleWithMaxCapacity.getCapacity() + ", but there is a job bigger than this. [job=" + j + "]");
			}
		}
		log.info("ok");
		log.info("check vehicles can manage shuttle tours ...");
		for(Job j : problem.getJobs().values()){
			Service s = (Service)j;
			boolean jobCanBeRoutedWithinTimeWindow = false;
			for(Vehicle v : problem.getVehicles()){
				double transportCost = problem.getTransportCosts().getTransportCost(v.getLocationId(), s.getLocationId(), v.getEarliestDeparture(), DriverImpl.noDriver(), v);
				double transportTime = problem.getTransportCosts().getTransportTime(v.getLocationId(), s.getLocationId(), v.getEarliestDeparture(), DriverImpl.noDriver(), v);
				if(transportTime+v.getEarliestDeparture() < s.getTimeWindow().getEnd()){
					jobCanBeRoutedWithinTimeWindow = true;
					break;
				}
				else{
					log.warn("vehicle " + v + " needs " + transportTime + " time-units to get to " + s.getLocationId() + ". latestOperationStartTime however is " + s.getTimeWindow().getEnd());
				}
				
			}
			if(!jobCanBeRoutedWithinTimeWindow){
				throw new IllegalStateException("no vehicle is able to cover the distance from depot to " + s.getLocationId() + " to meet the time-window " + s.getTimeWindow() + ".");
			}
		}
		log.info("ok");
		log.info("verifying done");
	}
	
	public void verify(VehicleRoutingProblem pblm, VehicleRoutingAlgorithm vra){
		informAlgorithmStarts(pblm, vra, null);
	}

	private Vehicle getMaxVehicle(VehicleRoutingProblem problem) {
		Vehicle maxVehicle = null;
		for(Vehicle v : problem.getVehicles()){
			if(maxVehicle == null) {
				maxVehicle = v;
				continue;
			}
			else if(v.getCapacity() > maxVehicle.getCapacity()){
				maxVehicle = v;
			}
		}
		return maxVehicle;
	}

}
