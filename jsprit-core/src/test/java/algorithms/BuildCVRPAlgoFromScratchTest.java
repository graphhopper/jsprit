package algorithms;

import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import util.Solutions;
import algorithms.HardConstraints.HardActivityLevelConstraint;
import algorithms.acceptors.AcceptNewIfBetterThanWorst;
import algorithms.selectors.SelectBest;
import basics.VehicleRoutingAlgorithm;
import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblemSolution;
import basics.algo.IterationStartsListener;
import basics.algo.SearchStrategy;
import basics.algo.SearchStrategyManager;
import basics.io.VrpXMLReader;
import basics.route.TourActivity;

public class BuildCVRPAlgoFromScratchTest {
	
	VehicleRoutingProblem vrp;
	
	VehicleRoutingAlgorithm vra;
	
	@Before
	public void setup(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(builder).read("src/test/resources/vrpnc1-jsprit.xml");
		vrp = builder.build();
		
		final StateManagerImpl stateManager = new StateManagerImpl();
		HardActivityLevelConstraint hardActLevelConstraint = new HardActivityLevelConstraint() {
			
			@Override
			public boolean fulfilled(InsertionFacts iFacts, TourActivity act, double arrTime) {
				return true;
			}
		};
		MarginalsCalculus marginalCalculus = new MarginalsCalculusTriangleInequality(vrp.getTransportCosts(), vrp.getActivityCosts(), hardActLevelConstraint);
		CalculatesServiceInsertion serviceInsertion = new CalculatesServiceInsertion(vrp.getTransportCosts(), marginalCalculus, new HardConstraints.HardLoadConstraint(stateManager));
		
		VehicleFleetManager fleetManager = new InfiniteVehicles(vrp.getVehicles());
		JobInsertionCalculator finalServiceInsertion = new CalculatesVehTypeDepServiceInsertion(fleetManager, serviceInsertion);
		
		BestInsertion bestInsertion = new BestInsertion(finalServiceInsertion);
		
		RuinRadial radial = new RuinRadial(vrp, 0.15, new JobDistanceAvgCosts(vrp.getTransportCosts()));
		RuinRandom random = new RuinRandom(vrp, 0.25);
		
		SearchStrategy randomStrategy = new SearchStrategy(new SelectBest(), new AcceptNewIfBetterThanWorst(1));
		RuinAndRecreateModule randomModule = new RuinAndRecreateModule("randomRuin_bestInsertion", bestInsertion, random);
		randomStrategy.addModule(randomModule);
		
		SearchStrategy radialStrategy = new SearchStrategy(new SelectBest(), new AcceptNewIfBetterThanWorst(1));
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
		
		vra.getSearchStrategyManager().addSearchStrategyModuleListener(new UdateCostsAtRouteLevel(stateManager, vrp.getTransportCosts(), vrp.getActivityCosts()));
		vra.getSearchStrategyManager().addSearchStrategyModuleListener(new UpdateLoadAtRouteLevel(stateManager));
		
		VehicleRoutingProblemSolution iniSolution = new CreateInitialSolution(bestInsertion).createInitialSolution(vrp);
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
