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
import algorithms.StateManager.StateImpl;
import algorithms.acceptors.AcceptNewIfBetterThanWorst;
import algorithms.selectors.SelectBest;
import basics.Delivery;
import basics.Job;
import basics.Pickup;
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
import basics.route.TourActivity;
import basics.route.VehicleFleetManager;
import basics.route.VehicleRoute;

public class BuildPDVRPWithShipmentsAlgoFromScratchTest {
	
	VehicleRoutingProblem vrp;
	
	VehicleRoutingAlgorithm vra;

	static Logger log = Logger.getLogger(BuildPDVRPWithShipmentsAlgoFromScratchTest.class);
	
	@Before
	public void setup(){
		
			VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
			new VrpXMLReader(builder).read("src/test/resources/C101_solomon_pd.xml");
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
			
			ConstraintManager constraintManager = new ConstraintManager();
			constraintManager.addConstraint(new HardTimeWindowActivityLevelConstraint(stateManager, vrp.getTransportCosts()));
			constraintManager.addConstraint(new HardPickupAndDeliveryActivityLevelConstraint(stateManager));
			constraintManager.addConstraint(new HardPickupAndDeliveryShipmentActivityLevelConstraint(stateManager));
			constraintManager.addConstraint(new HardPickupAndDeliveryLoadRouteLevelConstraint(stateManager));
			
			ActivityInsertionCostsCalculator marginalCalculus = new LocalActivityInsertionCostsCalculator(vrp.getTransportCosts(), vrp.getActivityCosts());

			ShipmentInsertionCalculator shipmentInsertion = new ShipmentInsertionCalculator(vrp.getTransportCosts(), marginalCalculus, constraintManager, constraintManager);
			
			ServiceInsertionCalculator serviceInsertion = new ServiceInsertionCalculator(vrp.getTransportCosts(), marginalCalculus, constraintManager, constraintManager);
			
			JobCalculatorSwitcher switcher = new JobCalculatorSwitcher();
			switcher.put(Shipment.class, shipmentInsertion);
			switcher.put(Service.class, serviceInsertion);
			
			VehicleFleetManager fleetManager = new InfiniteFleetManagerFactory(vrp.getVehicles()).createFleetManager();
			JobInsertionCostsCalculator finalServiceInsertion = new VehicleTypeDependentJobInsertionCalculator(fleetManager, switcher);
			
			BestInsertion bestInsertion = new BestInsertion(finalServiceInsertion);
			
			RuinRadial radial = new RuinRadial(vrp, 0.15, new AvgJobDistance(vrp.getTransportCosts()));
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
			iterateForward.addActivityVisitor(new UpdateEarliestStartTimeWindowAtActLocations(stateManager, vrp.getTransportCosts()));
			iterateForward.addActivityVisitor(new UpdateCostsAtAllLevels(vrp.getActivityCosts(), vrp.getTransportCosts(), stateManager));
			
			iterateForward.addActivityVisitor(new UpdateOccuredDeliveriesAtActivityLevel(stateManager));
			iterateForward.addActivityVisitor(new UpdateLoadAtActivityLevel(stateManager));
			
			final ReverseRouteActivityVisitor iterateBackward = new ReverseRouteActivityVisitor();
			iterateBackward.addActivityVisitor(new UpdateLatestOperationStartTimeAtActLocations(stateManager, vrp.getTransportCosts()));
			iterateBackward.addActivityVisitor(new UpdateFuturePickupsAtActivityLevel(stateManager));
			
			
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
						stateManager.putRouteState(inRoute, StateFactory.LOAD_AT_BEGINNING, new StateImpl(loadAtDepot + job2insert.getCapacityDemand()));
					}
					if(job2insert instanceof Pickup){
						int loadAtEnd = (int) stateManager.getRouteState(inRoute, StateFactory.LOAD_AT_END).toDouble();
//						log.info("loadAtEnd="+loadAtEnd);
						stateManager.putRouteState(inRoute, StateFactory.LOAD_AT_END, new StateImpl(loadAtEnd + job2insert.getCapacityDemand()));
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
			
			vra.setNuOfIterations(100);
			vra.setPrematureBreak(100);
			
	}
	
	@Test
	public void test(){
		Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
		VehicleRoutingProblemSolution best = Solutions.getBest(solutions);
		System.out.println(best.getCost());
		for(VehicleRoute r : best.getRoutes()){
			System.out.println(r);
			System.out.println("#jobs="+r.getTourActivities().jobSize());
			System.out.println(r.getStart());
			for(TourActivity act : r.getTourActivities().getActivities()){
				System.out.println(act);
			}
			System.out.println(r.getEnd());
		}
		
//		for()
		
		new VrpXMLWriter(vrp, solutions).write("src/test/resources/pd_solomon_c101_sol.xml");
		
	}

}
