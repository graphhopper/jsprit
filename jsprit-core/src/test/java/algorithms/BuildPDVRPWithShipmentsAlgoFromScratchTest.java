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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import util.Solutions;
import algorithms.acceptors.AcceptNewIfBetterThanWorst;
import algorithms.selectors.SelectBest;
import basics.Job;
import basics.Service;
import basics.Shipment;
import basics.VehicleRoutingAlgorithm;
import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblemSolution;
import basics.algo.InsertionStartsListener;
import basics.algo.JobInsertedListener;
import basics.algo.SearchStrategy;
import basics.algo.SearchStrategyManager;
import basics.algo.SolutionCostCalculator;
import basics.io.VrpXMLReader;
import basics.io.VrpXMLWriter;
import basics.route.InfiniteFleetManagerFactory;
import basics.route.ReverseRouteActivityVisitor;
import basics.route.RouteActivityVisitor;
import basics.route.VehicleFleetManager;
import basics.route.VehicleRoute;

public class BuildPDVRPWithShipmentsAlgoFromScratchTest {
	
	VehicleRoutingProblem vrp;
	
	VehicleRoutingAlgorithm vra;

	static Logger log = Logger.getLogger(BuildPDVRPWithShipmentsAlgoFromScratchTest.class);
	
	ExecutorService executorService;
	
	@Before
	public void setup(){
		
			VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
			new VrpXMLReader(builder).read("src/test/resources/pdp.xml");
//			VehicleType type = VehicleTypeImpl.Builder.newInstance("t", 2).setCostPerDistance(1.0).build();
//			Vehicle v = VehicleImpl.Builder.newInstance("v").setLocationCoord(Coordinate.newInstance(-1, -1)).setType(type).build();
//			
//			Shipment s1 = Shipment.Builder.newInstance("s1", 1).setPickupCoord(Coordinate.newInstance(0, 0)).setDeliveryCoord(Coordinate.newInstance(10, 10)).build();
//			Shipment s2 = Shipment.Builder.newInstance("s2", 1).setPickupCoord(Coordinate.newInstance(1, 1)).setDeliveryCoord(Coordinate.newInstance(10, 10)).build();
//			
//			Service serv1 = Service.Builder.newInstance("serv1", 1).setCoord(Coordinate.newInstance(0, 5)).build();
//			Service serv2 = Service.Builder.newInstance("serv2", 1).setCoord(Coordinate.newInstance(5, 0)).build();
//			
//			builder.addJob(s1).addJob(s2).addJob(serv1).addJob(serv2);
//			builder.addVehicle(v);
			
			vrp = builder.build();
			
			final StateManager stateManager = new StateManager();
			
			ConstraintManager constraintManager = new ConstraintManager(vrp,stateManager);
			constraintManager.addTimeWindowConstraint();
			constraintManager.addLoadConstraint();
//			constraintManager.addConstraint(new HardPickupAndDeliveryShipmentActivityLevelConstraint(stateManager));
			
			VehicleFleetManager fleetManager = new InfiniteFleetManagerFactory(vrp.getVehicles()).createFleetManager();
			
			int nuOfThreads = 10;
			executorService = Executors.newFixedThreadPool(nuOfThreads);
			
			BestInsertionBuilder bestIBuilder = new BestInsertionBuilder(vrp, fleetManager, stateManager,constraintManager);
//			bestIBuilder.setConstraintManager(constraintManager);
			bestIBuilder.setConcurrentMode(executorService, nuOfThreads);
			InsertionStrategy bestInsertion = bestIBuilder.build();
			
//			bestIBuilder.
			
//			ActivityInsertionCostsCalculator marginalCalculus = new LocalActivityInsertionCostsCalculator(vrp.getTransportCosts(), vrp.getActivityCosts());
//
//			ShipmentInsertionCalculator shipmentInsertion = new ShipmentInsertionCalculator(vrp.getTransportCosts(), marginalCalculus, constraintManager, constraintManager);
//			
//			ServiceInsertionCalculator serviceInsertion = new ServiceInsertionCalculator(vrp.getTransportCosts(), marginalCalculus, constraintManager, constraintManager);
//			
//			JobCalculatorSwitcher switcher = new JobCalculatorSwitcher();
//			switcher.put(Shipment.class, shipmentInsertion);
//			switcher.put(Service.class, serviceInsertion);
//			
//			
//			JobInsertionCostsCalculator finalServiceInsertion = new VehicleTypeDependentJobInsertionCalculator(fleetManager, switcher);
//			
//			BestInsertion bestInsertion = new BestInsertion(finalServiceInsertion);
			
			
//			BestInsertionConc bestInsertion = new BestInsertionConc(finalServiceInsertion, executorService, 2);
			
			RuinRadial radial = new RuinRadial(vrp, 0.3, new AvgJobDistance(vrp.getTransportCosts()));
			RuinRandom random = new RuinRandom(vrp, 0.5);
			
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
			
			vra = new VehicleRoutingAlgorithmFactoryImpl(strategyManager, stateManager, fleetManager).createAlgorithm(vrp);
//			
////			vra.getAlgorithmListeners().addListener(stateManager);
//			
////			final RouteActivityVisitor iterateForward = new RouteActivityVisitor();
//			
////			iterateForward.addActivityVisitor(new UpdateActivityTimes(vrp.getTransportCosts()));
////			iterateForward.addActivityVisitor(new UpdateVariableCosts(vrp.getActivityCosts(), vrp.getTransportCosts(), stateManager));
////			
////			iterateForward.addActivityVisitor(new UpdateOccuredDeliveries(stateManager));
////			iterateForward.addActivityVisitor(new UpdateLoads(stateManager));
////			
////			final ReverseRouteActivityVisitor iterateBackward = new ReverseRouteActivityVisitor();
////			iterateBackward.addActivityVisitor(new TimeWindowUpdater(stateManager, vrp.getTransportCosts()));
////			iterateBackward.addActivityVisitor(new UpdateFuturePickups(stateManager));
////			
////			JobInsertedListener updateWhenJobHasBeenInserted = new JobInsertedListener() {
////				
////				@Override
////				public void informJobInserted(Job job2insert, VehicleRoute inRoute, double additionalCosts, double additionalTime) {
////					iterateForward.visit(inRoute);
////					iterateBackward.visit(inRoute);
////				}
////				
////			};
////			
////			InsertionStartsListener updateRoutesWhenInsertionStarts = new InsertionStartsListener() {
////				
////				@Override
////				public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
////					for(VehicleRoute route : vehicleRoutes){
////						iterateForward.visit(route);
////						iterateBackward.visit(route);
////					}
////					
////				}
////			};
//			
//<<<<<<< HEAD
////			vra.getSearchStrategyManager().addSearchStrategyModuleListener(new RemoveEmptyVehicles(fleetManager));
//=======
//			iterateForward.addActivityVisitor(new UpdatePrevMaxLoad(stateManager));
//			iterateForward.addActivityVisitor(new UpdateLoads(stateManager));
//>>>>>>> branch 'PickupAndDelivery' of https://github.com/jsprit/jsprit.git
//			
//<<<<<<< HEAD
////			bestInsertion.addListener(new UpdateLoads(stateManager));
////			bestInsertion.addListener(updateWhenJobHasBeenInserted);
////			bestInsertion.addListener(updateRoutesWhenInsertionStarts);
//=======
//			final ReverseRouteActivityVisitor iterateBackward = new ReverseRouteActivityVisitor();
//			iterateBackward.addActivityVisitor(new TimeWindowUpdater(stateManager, vrp.getTransportCosts()));
//			iterateBackward.addActivityVisitor(new UpdateMaxLoad(stateManager));
//			
//			JobInsertedListener updateWhenJobHasBeenInserted = new JobInsertedListener() {
//				
//				@Override
//				public void informJobInserted(Job job2insert, VehicleRoute inRoute, double additionalCosts, double additionalTime) {
//					iterateForward.visit(inRoute);
//					iterateBackward.visit(inRoute);
//				}
//				
//			};
//			
//			InsertionStartsListener updateRoutesWhenInsertionStarts = new InsertionStartsListener() {
//				
//				@Override
//				public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
//					for(VehicleRoute route : vehicleRoutes){
//						iterateForward.visit(route);
//						iterateBackward.visit(route);
//					}
//					
//				}
//			};
//			
//			vra.getSearchStrategyManager().addSearchStrategyModuleListener(new RemoveEmptyVehicles(fleetManager));
//			
//			bestInsertion.addListener(new UpdateLoads(stateManager));
//			bestInsertion.addListener(updateWhenJobHasBeenInserted);
//			bestInsertion.addListener(updateRoutesWhenInsertionStarts);
//>>>>>>> branch 'PickupAndDelivery' of https://github.com/jsprit/jsprit.git
//			
			VehicleRoutingProblemSolution iniSolution = new InsertionInitialSolutionFactory(bestInsertion, solutionCostCalculator).createSolution(vrp);
//			System.out.println("ini: costs="+iniSolution.getCost()+";#routes="+iniSolution.getRoutes().size());
			vra.addInitialSolution(iniSolution);
			
			vra.setNuOfIterations(100);
//			vra.setPrematureBreak(500);
			
	}
	
	@Test
	public void test(){
		Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
		VehicleRoutingProblemSolution best = Solutions.getBest(solutions);
		
		executorService.shutdown();
		    // Wait until all threads are finish
//		executorService.awaitTermination();
		
//		for(VehicleRoute r : best.getRoutes()){
//			System.out.println(r);
//			System.out.println("#jobs="+r.getTourActivities().jobSize());
//			System.out.println(r.getStart());
//			for(TourActivity act : r.getTourActivities().getActivities()){
//				System.out.println(act);
//			}
//			System.out.println(r.getEnd());
//		}
//		
		System.out.println("total="+best.getCost());
		System.out.println("#routes="+best.getRoutes().size());
		
//		for()
		
		new VrpXMLWriter(vrp, solutions).write("src/test/resources/pdp_sol.xml");
		
	}

}
