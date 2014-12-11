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
package jsprit.core.algorithm.recreate;

import jsprit.core.algorithm.listener.VehicleRoutingAlgorithmListeners.PrioritizedVRAListener;
import jsprit.core.algorithm.recreate.listener.InsertionListener;
import jsprit.core.algorithm.state.StateManager;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.constraint.ConstraintManager;
import jsprit.core.problem.vehicle.VehicleFleetManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;


public class BestInsertionBuilder {

	private VehicleRoutingProblem vrp;
	
	private StateManager stateManager;
	
	private boolean local = true;
	
	private ConstraintManager constraintManager;

	private VehicleFleetManager fleetManager;

	private double weightOfFixedCosts;

	private boolean considerFixedCosts = false;

	private ActivityInsertionCostsCalculator actInsertionCostsCalculator = null;

	private int forwaredLooking;

	private int memory;

	private ExecutorService executor;

	private int nuOfThreads;

	private double timeSlice;

	private int nNeighbors;

	private boolean timeScheduling=false;

	private boolean allowVehicleSwitch=true;

	private boolean addDefaultCostCalc=true;
	
	public BestInsertionBuilder(VehicleRoutingProblem vrp, VehicleFleetManager vehicleFleetManager, StateManager stateManager, ConstraintManager constraintManager) {
		super();
		this.vrp = vrp;
		this.stateManager = stateManager;
		this.constraintManager = constraintManager;
		this.fleetManager = vehicleFleetManager;
	}
		
	public BestInsertionBuilder setRouteLevel(int forwardLooking, int memory){
		local = false;
		this.forwaredLooking = forwardLooking;
		this.memory = memory;
		return this;
	};
	
	public BestInsertionBuilder setRouteLevel(int forwardLooking, int memory, boolean addDefaultMarginalCostCalculation){
		local = false;
		this.forwaredLooking = forwardLooking;
		this.memory = memory;
		this.addDefaultCostCalc = addDefaultMarginalCostCalculation;
		return this;
	};
	
	public BestInsertionBuilder setLocalLevel(){
		local = true;
		return this;
	};
	
	/**
	 * If addDefaulMarginalCostCalculation is false, no calculator is set which implicitly assumes that marginal cost calculation 
	 * is controlled by your custom soft constraints.
	 * 
	 * @param addDefaultMarginalCostCalculation
	 * @return
	 */
	public BestInsertionBuilder setLocalLevel(boolean addDefaultMarginalCostCalculation){
		local = true;
		addDefaultCostCalc = addDefaultMarginalCostCalculation;
		return this;
	}
	
	public BestInsertionBuilder considerFixedCosts(double weightOfFixedCosts){
		this.weightOfFixedCosts = weightOfFixedCosts;
		this.considerFixedCosts  = true;
		return this;
	}
	
	public BestInsertionBuilder setActivityInsertionCostCalculator(ActivityInsertionCostsCalculator activityInsertionCostsCalculator){
		this.actInsertionCostsCalculator = activityInsertionCostsCalculator;
		return this;
	};
	
	public BestInsertionBuilder setConcurrentMode(ExecutorService executor, int nuOfThreads){
		this.executor = executor;
		this.nuOfThreads = nuOfThreads;
		return this;
	}
	
	
	public InsertionStrategy build() {
		List<InsertionListener> iListeners = new ArrayList<InsertionListener>();
		List<PrioritizedVRAListener> algorithmListeners = new ArrayList<PrioritizedVRAListener>();
		JobInsertionCostsCalculatorBuilder calcBuilder = new JobInsertionCostsCalculatorBuilder(iListeners, algorithmListeners);
		if(local){
			calcBuilder.setLocalLevel(addDefaultCostCalc);
		}
		else {
			calcBuilder.setRouteLevel(forwaredLooking, memory, addDefaultCostCalc);
		}
		calcBuilder.setConstraintManager(constraintManager);
		calcBuilder.setStateManager(stateManager);
		calcBuilder.setVehicleRoutingProblem(vrp);
		calcBuilder.setVehicleFleetManager(fleetManager);
		calcBuilder.setActivityInsertionCostsCalculator(actInsertionCostsCalculator);
		if(considerFixedCosts) {
			calcBuilder.considerFixedCosts(weightOfFixedCosts);
		}
		if(timeScheduling){
			calcBuilder.experimentalTimeScheduler(timeSlice, nNeighbors);
		}
		calcBuilder.setAllowVehicleSwitch(allowVehicleSwitch);
		JobInsertionCostsCalculator jobInsertions = calcBuilder.build();
		InsertionStrategy bestInsertion;
		if(executor == null){
			bestInsertion = new BestInsertion(jobInsertions,vrp);
		}
		else{
			bestInsertion = new BestInsertionConcurrent(jobInsertions,executor,nuOfThreads,vrp);
        }
		for(InsertionListener l : iListeners) bestInsertion.addListener(l);
		return bestInsertion;
	}

	/**
	 * @deprecated this is experimental and can disappear.
	 * @param timeSlice the time slice
	 * @param nNeighbors number of neighbors
	 */
	@Deprecated
	public void experimentalTimeScheduler(double timeSlice, int nNeighbors) {
		this.timeSlice=timeSlice;
		this.nNeighbors=nNeighbors;
		timeScheduling=true;
	}

	public void setAllowVehicleSwitch(boolean allowVehicleSwitch) {
		this.allowVehicleSwitch = allowVehicleSwitch;
	}

	


}
