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
package algorithms;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import util.Solutions;
import algorithms.ConstraintManager.Priority;
import algorithms.StateManager.StateImpl;
import algorithms.acceptors.AcceptNewIfBetterThanWorst;
import algorithms.selectors.SelectBest;
import basics.Delivery;
import basics.Job;
import basics.Pickup;
import basics.VehicleRoutingAlgorithm;
import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblemSolution;
import basics.algo.InsertionStartsListener;
import basics.algo.JobInsertedListener;
import basics.algo.SearchStrategy;
import basics.algo.SearchStrategyManager;
import basics.algo.SolutionCostCalculator;
import basics.io.VrpXMLReader;
import basics.route.InfiniteFleetManagerFactory;
import basics.route.ReverseRouteActivityVisitor;
import basics.route.RouteActivityVisitor;
import basics.route.VehicleFleetManager;
import basics.route.VehicleRoute;

public class BuildPDVRPAlgoFromScratchTest {
	
	VehicleRoutingProblem vrp;
	
	VehicleRoutingAlgorithm vra;

	static Logger log = Logger.getLogger(BuildPDVRPAlgoFromScratchTest.class);
	
	@Before
	public void setup(){
		
			VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
			new VrpXMLReader(builder).read("src/test/resources/pd_solomon_r101.xml");
			vrp = builder.build();
			
			final StateManager stateManager = new StateManager();
			
			ConstraintManager actLevelConstraintAccumulator = new ConstraintManager(vrp,stateManager);
			actLevelConstraintAccumulator.addConstraint(new ServiceLoadActivityLevelConstraint(stateManager),Priority.LOW);
			actLevelConstraintAccumulator.addConstraint(new TimeWindowConstraint(stateManager, vrp.getTransportCosts()),Priority.HIGH);
			
			ActivityInsertionCostsCalculator marginalCalculus = new LocalActivityInsertionCostsCalculator(vrp.getTransportCosts(), vrp.getActivityCosts());


			ServiceInsertionCalculator serviceInsertion = new ServiceInsertionCalculator(vrp.getTransportCosts(), marginalCalculus, new ServiceLoadRouteLevelConstraint(stateManager), actLevelConstraintAccumulator);

//			CalculatesServiceInsertion serviceInsertion = new CalculatesServiceInsertion(vrp.getTransportCosts(), marginalCalculus, new HardConstraints.HardLoadConstraint(stateManager));
			
			VehicleFleetManager fleetManager = new InfiniteFleetManagerFactory(vrp.getVehicles()).createFleetManager();
			JobInsertionCostsCalculator finalServiceInsertion = new VehicleTypeDependentJobInsertionCalculator(fleetManager, serviceInsertion);
			
			BestInsertion bestInsertion = new BestInsertion(finalServiceInsertion);
			
			RuinRadial radial = new RuinRadial(vrp, 0.15, new JobDistanceAvgCosts(vrp.getTransportCosts()));
			RuinRandom random = new RuinRandom(vrp, 0.25);
			
			SolutionCostCalculator solutionCostCalculator = new SolutionCostCalculator() {
				
				@Override
				public double getCosts(VehicleRoutingProblemSolution solution) {
					double costs = 0.0;
					for(VehicleRoute route : solution.getRoutes()){
						costs += stateManager.getRouteState(route, StateFactory.COSTS).toDouble();
					}
					return costs;
				}
			};
			
			SearchStrategy randomStrategy = new SearchStrategy(new SelectBest(), new AcceptNewIfBetterThanWorst(1), solutionCostCalculator);
			RuinAndRecreateModule randomModule = new RuinAndRecreateModule("randomRuin_bestInsertion", bestInsertion, random);
			randomStrategy.addModule(randomModule);
			
			SearchStrategy radialStrategy = new SearchStrategy(new SelectBest(), new AcceptNewIfBetterThanWorst(1), solutionCostCalculator);
			RuinAndRecreateModule radialModule = new RuinAndRecreateModule("radialRuin_bestInsertion", bestInsertion, radial);
			radialStrategy.addModule(radialModule);
			
			SearchStrategyManager strategyManager = new SearchStrategyManager();
			strategyManager.addStrategy(radialStrategy, 0.5);
			strategyManager.addStrategy(randomStrategy, 0.5);
			
			vra = new VehicleRoutingAlgorithm(vrp, strategyManager);
	
			vra.getAlgorithmListeners().addListener(stateManager);
			
			final RouteActivityVisitor iterateForward = new RouteActivityVisitor();
			
			iterateForward.addActivityVisitor(new UpdateActivityTimes(vrp.getTransportCosts()));
//			iterateForward.addActivityVisitor(new UpdateEarliestStartTime(stateManager, vrp.getTransportCosts()));
			iterateForward.addActivityVisitor(new UpdateVariableCosts(vrp.getActivityCosts(), vrp.getTransportCosts(), stateManager));
			
			iterateForward.addActivityVisitor(new UpdateOccuredDeliveries(stateManager));
			iterateForward.addActivityVisitor(new UpdateLoads(stateManager));
			
			final ReverseRouteActivityVisitor iterateBackward = new ReverseRouteActivityVisitor();
			iterateBackward.addActivityVisitor(new TimeWindowUpdater(stateManager, vrp.getTransportCosts()));
			iterateBackward.addActivityVisitor(new UpdateFuturePickups(stateManager));
			
			
			InsertionStartsListener loadVehicleInDepot = new InsertionStartsListener() {
				
				@Override
				public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
					for(VehicleRoute route : vehicleRoutes){
						int loadAtDepot = 0;
						int loadAtEnd = 0;
						for(Job j : route.getTourActivities().getJobs()){
							if(j instanceof Delivery){
								loadAtDepot += j.getCapacityDemand();
							}
							if(j instanceof Pickup){
								loadAtEnd += j.getCapacityDemand();
							}
						}
						stateManager.putRouteState(route, StateFactory.LOAD_AT_BEGINNING, new StateImpl(loadAtDepot));
						stateManager.putRouteState(route, StateFactory.LOAD, new StateImpl(loadAtEnd));
						iterateForward.visit(route);
						iterateBackward.visit(route);
					}
				}
				
			};
			
			vra.getSearchStrategyManager().addSearchStrategyModuleListener(new RemoveEmptyVehicles(fleetManager));
			
			JobInsertedListener updateLoadAfterJobHasBeenInserted = new JobInsertedListener() {
				
				@Override
				public void informJobInserted(Job job2insert, VehicleRoute inRoute, double additionalCosts, double additionalTime) {
//					log.info("insert job " + job2insert.getClass().toString() + " job " + job2insert + "" + job2insert.getCapacityDemand() + " in route " + inRoute.getTourActivities());
					
					if(job2insert instanceof Delivery){
						int loadAtDepot = (int) stateManager.getRouteState(inRoute, StateFactory.LOAD_AT_BEGINNING).toDouble();
//						log.info("loadAtDepot="+loadAtDepot);
						stateManager.putRouteState(inRoute, StateFactory.LOAD_AT_BEGINNING, StateFactory.createState(loadAtDepot + job2insert.getCapacityDemand()));
					}
					if(job2insert instanceof Pickup){
						int loadAtEnd = (int) stateManager.getRouteState(inRoute, StateFactory.LOAD_AT_END).toDouble();
//						log.info("loadAtEnd="+loadAtEnd);
						stateManager.putRouteState(inRoute, StateFactory.LOAD_AT_END, StateFactory.createState(loadAtEnd + job2insert.getCapacityDemand()));
					}
					iterateForward.visit(inRoute);
					iterateBackward.visit(inRoute);
				}
			};
						
			bestInsertion.addListener(loadVehicleInDepot);
			bestInsertion.addListener(updateLoadAfterJobHasBeenInserted);
			
			VehicleRoutingProblemSolution iniSolution = new InsertionInitialSolutionFactory(bestInsertion, solutionCostCalculator).createSolution(vrp);

//			System.out.println("ini: costs="+iniSolution.getCost()+";#routes="+iniSolution.getRoutes().size());
			vra.addInitialSolution(iniSolution);
			vra.setNuOfIterations(10000);
			vra.setPrematureBreak(1000);
			
	}
	
	@Test
	public void test(){
		Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
		System.out.println(Solutions.getBest(solutions).getCost());
//		new VrpXMLWriter(vrp, solutions).write("output/pd_solomon_r101.xml");
		
	}

}
