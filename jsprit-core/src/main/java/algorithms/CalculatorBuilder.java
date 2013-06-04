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
import java.util.Set;

import org.apache.commons.configuration.XMLConfiguration;

import util.NeighborhoodImpl;

import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblem.FleetComposition;
import basics.algo.InsertionListener;
import basics.algo.VehicleRoutingAlgorithmListeners.PrioritizedVRAListener;
import basics.algo.VehicleRoutingAlgorithmListeners.Priority;
import basics.costs.VehicleRoutingActivityCosts;



class CalculatorBuilder {

	private static class CalculatorPlusListeners {

		private JobInsertionCalculator calculator;

		public JobInsertionCalculator getCalculator() {
			return calculator;
		}

		private List<PrioritizedVRAListener> algorithmListener = new ArrayList<PrioritizedVRAListener>();
		private List<InsertionListener> insertionListener = new ArrayList<InsertionListener>();

		public CalculatorPlusListeners(JobInsertionCalculator calculator) {
			super();
			this.calculator = calculator;
		}

		public List<PrioritizedVRAListener> getAlgorithmListener() {
			return algorithmListener;
		}

		public List<InsertionListener> getInsertionListener() {
			return insertionListener;
		}
	}

	private List<InsertionListener> insertionListeners;

	private List<PrioritizedVRAListener> algorithmListeners;

	private VehicleRoutingProblem vrp;

	private RouteStates activityStates;

	private boolean local = true;

	private int forwardLooking = 0;

	private int memory = 1;

	private boolean considerFixedCost = false;

	private double weightOfFixedCost = 0;

	private VehicleFleetManager fleetManager;

	private boolean timeScheduling;

	private double timeSlice;

	private int neighbors;

	/**
	 * Constructs the builder.
	 * 
	 * <p>Some calculators require information from the overall algorithm or the higher-level insertion procedure. Thus listeners inform them. 
	 * These listeners are cached in the according list and can thus be added when its time to add them.
	 * 
	 * @param insertionListeners
	 * @param algorithmListeners
	 */
	public CalculatorBuilder(List<InsertionListener> insertionListeners, List<PrioritizedVRAListener> algorithmListeners) {
		super();
		this.insertionListeners = insertionListeners;
		this.algorithmListeners = algorithmListeners;
	}

	/**
	 * Sets activityStates. MUST be set.
	 * 
	 * @param activityStates
	 * @return
	 */
	public CalculatorBuilder setActivityStates(RouteStates activityStates){
		this.activityStates = activityStates;
		return this;
	}

	/**
	 * Sets routingProblem. MUST be set.
	 * 
	 * @param vehicleRoutingProblem
	 * @return
	 */
	public CalculatorBuilder setVehicleRoutingProblem(VehicleRoutingProblem vehicleRoutingProblem){
		this.vrp = vehicleRoutingProblem;
		return this;
	}

	/**
	 * Sets fleetManager. MUST be set.
	 * 
	 * @param fleetManager
	 * @return
	 */
	public CalculatorBuilder setVehicleFleetManager(VehicleFleetManager fleetManager){
		this.fleetManager = fleetManager;
		return this;
	}

	/**
	 * Sets a flag to build a calculator based on local calculations.
	 * 
	 * <p>Insertion of a job and job-activity is evaluated based on the previous and next activity.
	 */
	public void setLocalLevel(){
		local = true;
	}

	/**
	 * Sets a flag to build a calculator that evaluates job insertion on route-level.
	 * 
	 * @param forwardLooking
	 * @param memory
	 */
	public void setRouteLevel(int forwardLooking, int memory){
		local = false;
		this.forwardLooking = forwardLooking;
		this.memory = memory;
	}

	/**
	 * Sets a flag to consider also fixed-cost when evaluating the insertion of a job. The weight of the fixed-cost can be determined by setting
	 * weightofFixedCosts.
	 * 
	 * @param weightOfFixedCosts
	 */
	public void considerFixedCosts(double weightOfFixedCosts){
		considerFixedCost = true;
		this.weightOfFixedCost = weightOfFixedCosts; 
	}
	
	public void experimentalTimeScheduler(double timeSlice, int neighbors){
		timeScheduling = true;
		this.timeSlice = timeSlice;
		this.neighbors = neighbors;
	}

	/**
	 * Builds the jobInsertionCalculator.
	 *  
	 * @return jobInsertionCalculator.
	 * @throws IllegalStateException if vrp == null or activityStates == null or fleetManager == null.
	 */
	public JobInsertionCalculator build(){
		if(vrp == null) throw new IllegalStateException("vehicle-routing-problem is null, but it must be set (this.setVehicleRoutingProblem(vrp))");
		if(activityStates == null) throw new IllegalStateException("activity states is null, but is must be set (this.setActivityStates(states))");
		if(fleetManager == null) throw new IllegalStateException("fleetManager is null, but it must be set (this.setVehicleFleetManager(fleetManager))");
		JobInsertionCalculator baseCalculator = null;
		CalculatorPlusListeners standardLocal = null;
		if(local){
			standardLocal = createStandardLocal(vrp, activityStates);
		}
		else{
			standardLocal = createStandardRoute(vrp, activityStates,forwardLooking,memory);
		}
		baseCalculator = standardLocal.getCalculator();
		addAlgorithmListeners(standardLocal.getAlgorithmListener());
		addInsertionListeners(standardLocal.getInsertionListener());
		if(considerFixedCost){
			CalculatorPlusListeners withFixed = createCalculatorConsideringFixedCosts(vrp, baseCalculator, activityStates, weightOfFixedCost);
			baseCalculator = withFixed.getCalculator();
			addAlgorithmListeners(withFixed.getAlgorithmListener());
			addInsertionListeners(withFixed.getInsertionListener());
		}
		if(timeScheduling){
			baseCalculator = new CalculatesServiceInsertionWithTimeScheduling(baseCalculator,timeSlice,neighbors);
		}
		return createFinalInsertion(fleetManager, baseCalculator, activityStates);
	}

	private void addInsertionListeners(List<InsertionListener> list) {
		for(InsertionListener iL : list){
			insertionListeners.add(iL);
		}
	}

	private void addAlgorithmListeners(List<PrioritizedVRAListener> list) {
		for(PrioritizedVRAListener aL : list){
			algorithmListeners.add(aL);
		}
	}

	private CalculatorPlusListeners createStandardLocal(VehicleRoutingProblem vrp, RouteStates activityStates){
		JobInsertionCalculator standardServiceInsertion = new CalculatesServiceInsertion(vrp.getTransportCosts(), vrp.getActivityCosts());
		((CalculatesServiceInsertion) standardServiceInsertion).setActivityStates(activityStates);
		((CalculatesServiceInsertion) standardServiceInsertion).setNeighborhood(vrp.getNeighborhood());
		CalculatorPlusListeners calcPlusListeners = new CalculatorPlusListeners(standardServiceInsertion);
		
		return calcPlusListeners;
	}

	private CalculatorPlusListeners createCalculatorConsideringFixedCosts(VehicleRoutingProblem vrp, JobInsertionCalculator baseCalculator, RouteStates activityStates, double weightOfFixedCosts){
		final CalculatesServiceInsertionConsideringFixCost withFixCost = new CalculatesServiceInsertionConsideringFixCost(baseCalculator, activityStates);
		withFixCost.setWeightOfFixCost(weightOfFixedCosts);
		CalculatorPlusListeners calcPlusListeners = new CalculatorPlusListeners(withFixCost);
		calcPlusListeners.getInsertionListener().add(new ConfigureFixCostCalculator(vrp, withFixCost));
		return calcPlusListeners;
	}

	private CalculatorPlusListeners createStandardRoute(VehicleRoutingProblem vrp, RouteStates activityStates, int forwardLooking, int solutionMemory){
		int after = forwardLooking;
		JobInsertionCalculator jobInsertionCalculator = new CalculatesServiceInsertionOnRouteLevel(vrp.getTransportCosts(), vrp.getActivityCosts());
		((CalculatesServiceInsertionOnRouteLevel)jobInsertionCalculator).setNuOfActsForwardLooking(after);
		((CalculatesServiceInsertionOnRouteLevel)jobInsertionCalculator).setMemorySize(solutionMemory);
		((CalculatesServiceInsertionOnRouteLevel)jobInsertionCalculator).setNeighborhood(vrp.getNeighborhood());
		((CalculatesServiceInsertionOnRouteLevel) jobInsertionCalculator).setActivityStates(activityStates);
		CalculatorPlusListeners calcPlusListener = new CalculatorPlusListeners(jobInsertionCalculator);
		return calcPlusListener;
	}

	private JobInsertionCalculator createFinalInsertion(VehicleFleetManager fleetManager, JobInsertionCalculator baseCalc, RouteStates routeStates){
		return new CalculatesVehTypeDepServiceInsertion(fleetManager, baseCalc);
	}

}












