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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;

import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblem.FleetComposition;
import basics.algo.InsertionListener;
import basics.algo.VehicleRoutingAlgorithmListeners.PrioritizedVRAListener;

class InsertionFactory {
	
	private static Logger log = Logger.getLogger(InsertionFactory.class);
	
	public static InsertionStrategy createInsertion(VehicleRoutingProblem vrp, HierarchicalConfiguration config, 
			VehicleFleetManager vehicleFleetManager, StatesContainerImpl routeStates, List<PrioritizedVRAListener> algorithmListeners, ExecutorService executorService, int nuOfThreads){
		boolean concurrentInsertion = false;
		if(executorService != null) concurrentInsertion = true;
		if(config.containsKey("[@name]")){
			String insertionName = config.getString("[@name]");
			if(!insertionName.equals("bestInsertion") && !insertionName.equals("regretInsertion")){
				new IllegalStateException(insertionName + " is not supported. use either \"bestInsertion\" or \"regretInsertion\"");
			}
			InsertionStrategy insertionStrategy = null;
			List<InsertionListener> insertionListeners = new ArrayList<InsertionListener>();
			List<PrioritizedVRAListener> algoListeners = new ArrayList<PrioritizedVRAListener>();
	
			CalculatorBuilder calcBuilder = new CalculatorBuilder(insertionListeners, algorithmListeners);
			calcBuilder.setStates(routeStates);
			calcBuilder.setVehicleRoutingProblem(vrp);
			calcBuilder.setVehicleFleetManager(vehicleFleetManager);
			
			if(config.containsKey("level")){
				String level = config.getString("level");
				if(level.equals("local")){
					calcBuilder.setLocalLevel();
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
					calcBuilder.setRouteLevel(forwardLooking, memory);
				}
				else throw new IllegalStateException("level " + level + " is not known. currently it only knows \"local\" or \"route\"");
			}
			else calcBuilder.setLocalLevel(); 
			
			if(config.containsKey("considerFixedCosts") || config.containsKey("considerFixedCost")){
				String val = config.getString("considerFixedCosts");
				if(val == null) val = config.getString("considerFixedCost");
				if(val.equals("true")){
					double fixedCostWeight = 0.5;
					String weight = config.getString("considerFixedCosts[@weight]");
					if(weight == null) weight = config.getString("considerFixedCost[@weight]");
					if(weight != null) fixedCostWeight = Double.parseDouble(weight);
					else log.warn("parameter considerFixedCosts[@weight] is missing. by default, it is 0.5.");
					calcBuilder.considerFixedCosts(fixedCostWeight);
				}
			}
			String timeSliceString = config.getString("experimental[@timeSlice]");
			String neighbors = config.getString("experimental[@neighboringSlices]");
			if(timeSliceString != null && neighbors != null){
				calcBuilder.experimentalTimeScheduler(Double.parseDouble(timeSliceString),Integer.parseInt(neighbors));
			}
			
			JobInsertionCalculator jic = calcBuilder.build();

	
			if(insertionName.equals("bestInsertion")){		
				insertionStrategy = new BestInsertion(jic);
			}
//			else if(insertionName.equals("regretInsertion")){
//				insertionStrategy = RegretInsertion.newInstance(routeAlgorithm);
//			}
		
			insertionStrategy.addListener(new RemoveEmptyVehicles(vehicleFleetManager));
			insertionStrategy.addListener(new ResetAndIniFleetManager(vehicleFleetManager));
			insertionStrategy.addListener(new VehicleSwitched(vehicleFleetManager));
			
//			insertionStrategy.addListener(new UpdateLoadAtRouteLevel(routeStates));
			
			insertionStrategy.addListener(new UpdateStates(routeStates, vrp.getTransportCosts(), vrp.getActivityCosts()));
			for(InsertionListener l : insertionListeners) insertionStrategy.addListener(l);
//			insertionStrategy.addListener(new FindCheaperVehicle(
//					new FindCheaperVehicleAlgoNew(vehicleFleetManager, tourStateCalculator, auxCalculator)));
			
			algorithmListeners.addAll(algoListeners);
			
			return insertionStrategy;
		}
		else throw new IllegalStateException("cannot create insertionStrategy, since it has no name.");
	}

	
}



