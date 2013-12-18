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
package jsprit.core.algorithm.recreate;

import java.util.ArrayList;
import java.util.List;

import jsprit.core.algorithm.listener.VehicleRoutingAlgorithmListeners.PrioritizedVRAListener;
import jsprit.core.algorithm.recreate.listener.InsertionListener;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.constraint.ConstraintManager;
import jsprit.core.problem.job.Delivery;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Pickup;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;
import jsprit.core.problem.vehicle.VehicleFleetManager;




class CalculatorBuilder {

	private static class CalculatorPlusListeners {

		private JobInsertionCostsCalculator calculator;

		public JobInsertionCostsCalculator getCalculator() {
			return calculator;
		}

		private List<PrioritizedVRAListener> algorithmListener = new ArrayList<PrioritizedVRAListener>();
		private List<InsertionListener> insertionListener = new ArrayList<InsertionListener>();

		public CalculatorPlusListeners(JobInsertionCostsCalculator calculator) {
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

	private RouteAndActivityStateGetter states;

	private boolean local = true;

	private int forwardLooking = 0;

	private int memory = 1;

	private boolean considerFixedCost = false;

	private double weightOfFixedCost = 0;

	private VehicleFleetManager fleetManager;

	private boolean timeScheduling;

	private double timeSlice;

	private int neighbors;
	
	private ConstraintManager constraintManager;
	
	private ActivityInsertionCostsCalculator activityInsertionCostCalculator = null;

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
	 * @param states TODO
	 * 
	 * @return
	 */
	public CalculatorBuilder setStates(RouteAndActivityStateGetter states){
		this.states = states;
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
	
	public void setActivityInsertionCostsCalculator(ActivityInsertionCostsCalculator activityInsertionCostsCalculator){
		this.activityInsertionCostCalculator = activityInsertionCostsCalculator;
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
	public JobInsertionCostsCalculator build(){
		if(vrp == null) throw new IllegalStateException("vehicle-routing-problem is null, but it must be set (this.setVehicleRoutingProblem(vrp))");
		if(states == null) throw new IllegalStateException("states is null, but is must be set (this.setStates(states))");
		if(fleetManager == null) throw new IllegalStateException("fleetManager is null, but it must be set (this.setVehicleFleetManager(fleetManager))");
		JobInsertionCostsCalculator baseCalculator = null;
		CalculatorPlusListeners standardLocal = null;
		if(local){
			standardLocal = createStandardLocal(vrp, states);
		}
		else{
			checkServicesOnly();
			standardLocal = createStandardRoute(vrp, states,forwardLooking,memory);
		}
		baseCalculator = standardLocal.getCalculator();
		addAlgorithmListeners(standardLocal.getAlgorithmListener());
		addInsertionListeners(standardLocal.getInsertionListener());
		if(considerFixedCost){
			CalculatorPlusListeners withFixed = createCalculatorConsideringFixedCosts(vrp, baseCalculator, states, weightOfFixedCost);
			baseCalculator = withFixed.getCalculator();
			addAlgorithmListeners(withFixed.getAlgorithmListener());
			addInsertionListeners(withFixed.getInsertionListener());
		}
		if(timeScheduling){
			baseCalculator = new CalculatesServiceInsertionWithTimeScheduling(baseCalculator,timeSlice,neighbors);
		}
		return createFinalInsertion(fleetManager, baseCalculator, states);
	}

	private void checkServicesOnly() {
		for(Job j : vrp.getJobs().values()){
			if(j instanceof Shipment){
				throw new UnsupportedOperationException("currently the 'insert-on-route-level' option is only available for services (i.e. service, pickup, delivery), \n" +
						"if you want to deal with shipments switch to option 'local-level' by either setting bestInsertionBuilder.setLocalLevel() or \n"
						+ "by omitting the xml-tag '<level forwardLooking=2 memory=1>route</level>' when defining your insertionStrategy in algo-config.xml file");
			}
		}
		
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

	private CalculatorPlusListeners createStandardLocal(VehicleRoutingProblem vrp, RouteAndActivityStateGetter statesManager){
		if(constraintManager == null) throw new IllegalStateException("constraint-manager is null");
 		

		ActivityInsertionCostsCalculator actInsertionCalc;
		if(activityInsertionCostCalculator == null){
			actInsertionCalc = new LocalActivityInsertionCostsCalculator(vrp.getTransportCosts(), vrp.getActivityCosts());
		}
		else{
			actInsertionCalc = activityInsertionCostCalculator;
		}

		ShipmentInsertionCalculator shipmentInsertion = new ShipmentInsertionCalculator(vrp.getTransportCosts(), actInsertionCalc, constraintManager, constraintManager);
		ServiceInsertionCalculator serviceInsertion = new ServiceInsertionCalculator(vrp.getTransportCosts(), actInsertionCalc, constraintManager, constraintManager);
		
		JobCalculatorSwitcher switcher = new JobCalculatorSwitcher();
		switcher.put(Shipment.class, shipmentInsertion);
		switcher.put(Service.class, serviceInsertion);
		switcher.put(Pickup.class, serviceInsertion);
		switcher.put(Delivery.class, serviceInsertion);
		
		PenalyzeInsertionCostsWithPenaltyVehicle penalyzeInsertionCosts = new PenalyzeInsertionCostsWithPenaltyVehicle(switcher);
		
//		JobInsertionCostsCalculator standardServiceInsertion = new ServiceInsertionCalculator(vrp.getTransportCosts(), actInsertionCalc, constraintManager, constraintManager);
//		((ServiceInsertionCalculator) standardServiceInsertion).setNeighborhood(vrp.getNeighborhood());
		CalculatorPlusListeners calcPlusListeners = new CalculatorPlusListeners(penalyzeInsertionCosts);
		
		return calcPlusListeners;
	}

	private CalculatorPlusListeners createCalculatorConsideringFixedCosts(VehicleRoutingProblem vrp, JobInsertionCostsCalculator baseCalculator, RouteAndActivityStateGetter activityStates2, double weightOfFixedCosts){
		final JobInsertionConsideringFixCostsCalculator withFixCost = new JobInsertionConsideringFixCostsCalculator(baseCalculator, activityStates2);
		withFixCost.setWeightOfFixCost(weightOfFixedCosts);
		CalculatorPlusListeners calcPlusListeners = new CalculatorPlusListeners(withFixCost);
		calcPlusListeners.getInsertionListener().add(new ConfigureFixCostCalculator(vrp, withFixCost));
		return calcPlusListeners;
	}

	private CalculatorPlusListeners createStandardRoute(VehicleRoutingProblem vrp, RouteAndActivityStateGetter activityStates2, int forwardLooking, int solutionMemory){
		int after = forwardLooking;
		ActivityInsertionCostsCalculator routeLevelCostEstimator;
		if(activityInsertionCostCalculator == null){
			routeLevelCostEstimator = new RouteLevelActivityInsertionCostsEstimator(vrp.getTransportCosts(), vrp.getActivityCosts(), activityStates2);
		}
		else{
			routeLevelCostEstimator = activityInsertionCostCalculator;
		}
		JobInsertionCostsCalculator jobInsertionCalculator = new ServiceInsertionOnRouteLevelCalculator(vrp.getTransportCosts(), vrp.getActivityCosts(), routeLevelCostEstimator, constraintManager, constraintManager);
		((ServiceInsertionOnRouteLevelCalculator)jobInsertionCalculator).setNuOfActsForwardLooking(after);
		((ServiceInsertionOnRouteLevelCalculator)jobInsertionCalculator).setMemorySize(solutionMemory);
		((ServiceInsertionOnRouteLevelCalculator) jobInsertionCalculator).setStates(activityStates2);
		
		PenalyzeInsertionCostsWithPenaltyVehicle penalyzeInsertionCosts = new PenalyzeInsertionCostsWithPenaltyVehicle(jobInsertionCalculator);
		
		CalculatorPlusListeners calcPlusListener = new CalculatorPlusListeners(penalyzeInsertionCosts);
		return calcPlusListener;
	}

	private JobInsertionCostsCalculator createFinalInsertion(VehicleFleetManager fleetManager, JobInsertionCostsCalculator baseCalc, RouteAndActivityStateGetter activityStates2){
		return new VehicleTypeDependentJobInsertionCalculator(fleetManager, baseCalc);
	}

	public void setConstraintManager(ConstraintManager constraintManager) {
		this.constraintManager = constraintManager;
	}

}












