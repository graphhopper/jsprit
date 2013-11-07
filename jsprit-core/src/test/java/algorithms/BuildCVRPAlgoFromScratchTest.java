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

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import util.Solutions;
import algorithms.acceptors.AcceptNewIfBetterThanWorst;
import algorithms.selectors.SelectBest;
import basics.VehicleRoutingAlgorithm;
import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblemSolution;
import basics.algo.IterationStartsListener;
import basics.algo.SearchStrategy;
import basics.algo.SearchStrategyManager;
import basics.algo.SolutionCostCalculator;
import basics.io.VrpXMLReader;
import basics.route.InfiniteFleetManagerFactory;
import basics.route.TourActivity;
import basics.route.VehicleFleetManager;
import basics.route.VehicleRoute;

public class BuildCVRPAlgoFromScratchTest {
	
	VehicleRoutingProblem vrp;
	
	VehicleRoutingAlgorithm vra;
	
	@Before
	public void setup(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(builder).read("src/test/resources/vrpnc1-jsprit.xml");
		vrp = builder.build();
		
		final StateManager stateManager = new StateManager();
		HardActivityLevelConstraint hardActLevelConstraint = new HardActivityLevelConstraint() {
			
			@Override
			public ConstraintsStatus fulfilled(InsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
				return ConstraintsStatus.FULFILLED;
			}
		};


		ActivityInsertionCostsCalculator marginalCalculus = new LocalActivityInsertionCostsCalculator(vrp.getTransportCosts(), vrp.getActivityCosts());
		ServiceInsertionCalculator serviceInsertion = new ServiceInsertionCalculator(vrp.getTransportCosts(), marginalCalculus, new HardLoadConstraint(stateManager), hardActLevelConstraint);
		
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
		
		//listeners
		IterationStartsListener clearStateManager = new IterationStartsListener() {
			
			@Override
			public void informIterationStarts(int i, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
				stateManager.clear();
			}
		};
		vra.getAlgorithmListeners().addListener(clearStateManager);
		vra.getSearchStrategyManager().addSearchStrategyModuleListener(new RemoveEmptyVehicles(fleetManager));
		
		vra.getSearchStrategyManager().addSearchStrategyModuleListener(new UpdateCostsAtRouteLevel(stateManager, vrp.getTransportCosts(), vrp.getActivityCosts()));
		vra.getSearchStrategyManager().addSearchStrategyModuleListener(new UpdateLoadAtRouteLevel(stateManager));
		

		VehicleRoutingProblemSolution iniSolution = new InsertionInitialSolutionFactory(bestInsertion, solutionCostCalculator).createSolution(vrp);

//		System.out.println("ini: costs="+iniSolution.getCost()+";#routes="+iniSolution.getRoutes().size());
		vra.addInitialSolution(iniSolution);
		
		vra.setNuOfIterations(2000);
//		vra.setPrematureBreak(200);
		
	}
	
	@Test
	public void testVRA(){
		Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
		System.out.println("costs="+Solutions.getBest(solutions).getCost()+";#routes="+Solutions.getBest(solutions).getRoutes().size());
		assertEquals(530.0, Solutions.getBest(solutions).getCost(),15.0);
		assertEquals(5, Solutions.getBest(solutions).getRoutes().size());
	}

}
