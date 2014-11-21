/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
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
package jsprit.core.algorithm.io;

import jsprit.core.algorithm.listener.VehicleRoutingAlgorithmListeners.PrioritizedVRAListener;
import jsprit.core.algorithm.recreate.InsertionBuilder;
import jsprit.core.algorithm.recreate.InsertionStrategy;
import jsprit.core.algorithm.state.StateManager;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.constraint.ConstraintManager;
import jsprit.core.problem.vehicle.VehicleFleetManager;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.ExecutorService;


class InsertionFactory {
	
	private static Logger log = LogManager.getLogger(InsertionFactory.class.getName());
	
	@SuppressWarnings("deprecation")
	public static InsertionStrategy createInsertion(VehicleRoutingProblem vrp, HierarchicalConfiguration config, 
			VehicleFleetManager vehicleFleetManager, StateManager stateManager, List<PrioritizedVRAListener> algorithmListeners, ExecutorService executorService, int nuOfThreads, ConstraintManager constraintManager, boolean addDefaultCostCalculators){

		if(config.containsKey("[@name]")){
			String insertionName = config.getString("[@name]");
			if(!insertionName.equals("bestInsertion") && !insertionName.equals("regretInsertion")){
				throw new IllegalStateException(insertionName + " is not supported. use either \"bestInsertion\" or \"regretInsertion\"");
			}


            InsertionBuilder iBuilder = new InsertionBuilder(vrp, vehicleFleetManager, stateManager, constraintManager);

			if(executorService != null){
				iBuilder.setConcurrentMode(executorService, nuOfThreads);
			}
			
			if(config.containsKey("level")){
				String level = config.getString("level");
				if(level.equals("local")){
					iBuilder.setLocalLevel(addDefaultCostCalculators);
				}
				else if(level.equals("route")){
					int forwardLooking = 0;
					int memory = 1;
					String forward = config.getString("level[@forwardLooking]");
					String mem = config.getString("level[@memory]");
					if(forward != null) forwardLooking = Integer.parseInt(forward);
					else log.warn("parameter route[@forwardLooking] is missing. by default it is 0 which equals to local level");
					if(mem != null) memory = Integer.parseInt(mem);
					else log.warn("parameter route[@memory] is missing. by default it is 1");
					iBuilder.setRouteLevel(forwardLooking, memory, addDefaultCostCalculators);
				}
				else throw new IllegalStateException("level " + level + " is not known. currently it only knows \"local\" or \"route\"");
			}
			else iBuilder.setLocalLevel(addDefaultCostCalculators); 
			
			if(config.containsKey("considerFixedCosts") || config.containsKey("considerFixedCost")){
				if(addDefaultCostCalculators){
					String val = config.getString("considerFixedCosts");
					if(val == null) val = config.getString("considerFixedCost");
					if(val.equals("true")){
						double fixedCostWeight = 0.5;
						String weight = config.getString("considerFixedCosts[@weight]");
						if(weight == null) weight = config.getString("considerFixedCost[@weight]");
						if(weight != null) fixedCostWeight = Double.parseDouble(weight);
						else throw new IllegalStateException("fixedCostsParameter 'weight' must be set, e.g. <considerFixedCosts weight=1.0>true</considerFixedCosts>.\n" +
								"this has to be changed in algorithm-config-xml-file.");
						iBuilder.considerFixedCosts(fixedCostWeight);
					}
					else if(val.equals("false")){

					}
					else throw new IllegalStateException("considerFixedCosts must either be true or false, i.e. <considerFixedCosts weight=1.0>true</considerFixedCosts> or \n<considerFixedCosts weight=1.0>false</considerFixedCosts>. " +
							"if latter, you can also omit the tag. this has to be changed in algorithm-config-xml-file");
				}
			}
			String timeSliceString = config.getString("experimental[@timeSlice]");
			String neighbors = config.getString("experimental[@neighboringSlices]");
			if(timeSliceString != null && neighbors != null){
				iBuilder.experimentalTimeScheduler(Double.parseDouble(timeSliceString),Integer.parseInt(neighbors));
			}
			String allowVehicleSwitch = config.getString("allowVehicleSwitch");
			if(allowVehicleSwitch != null){
				iBuilder.setAllowVehicleSwitch(Boolean.parseBoolean(allowVehicleSwitch));
			}

            if(insertionName.equals("regretInsertion")){
                iBuilder.setInsertionStrategy(InsertionBuilder.Strategy.REGRET);
            }
			return iBuilder.build();
		}
		else throw new IllegalStateException("cannot create insertionStrategy, since it has no name.");
	}

	
}



