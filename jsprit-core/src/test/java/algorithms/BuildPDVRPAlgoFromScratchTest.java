package algorithms;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import util.Solutions;
import algorithms.BackwardInTimeListeners.BackwardInTimeListener;
import algorithms.ForwardInTimeListeners.ForwardInTimeListener;
import algorithms.HardConstraints.HardActivityLevelConstraint;
import algorithms.StateManager.State;
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
import basics.algo.IterationStartsListener;
import basics.algo.JobInsertedListener;
import basics.algo.SearchStrategy;
import basics.algo.SearchStrategyManager;
import basics.io.VrpXMLReader;
import basics.io.VrpXMLWriter;
import basics.route.DeliveryActivity;
import basics.route.End;
import basics.route.PickupActivity;
import basics.route.Start;
import basics.route.TourActivity;
import basics.route.VehicleRoute;

public class BuildPDVRPAlgoFromScratchTest {
	
	VehicleRoutingProblem vrp;
	
	VehicleRoutingAlgorithm vra;

	static Logger log = Logger.getLogger(BuildPDVRPAlgoFromScratchTest.class);
	
	@Before
	public void setup(){
		
			VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
			new VrpXMLReader(builder).read("src/test/resources/pdVRP_vrpnc1_jsprit.xml");
			vrp = builder.build();
			
			final StateManagerImpl stateManager = new StateManagerImpl();
			
			HardActivityLevelConstraint hardActLevelConstraint = new HardActivityLevelConstraint() {
				
				@Override
				public boolean fulfilled(InsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
//					if(newAct instanceof PickupActivity && nextAct instanceof DeliveryActivity){ return false; }
//					if(newAct instanceof DeliveryActivity && prevAct instanceof PickupActivity){ return false; }
					int loadAtPrevAct;
					int futurePicks;
					int pastDeliveries;
					if(prevAct instanceof Start){
						loadAtPrevAct = (int)stateManager.getRouteState(iFacts.getRoute(), StateTypes.LOAD_AT_DEPOT).toDouble();
						futurePicks = (int)stateManager.getRouteState(iFacts.getRoute(), StateTypes.LOAD).toDouble();
						pastDeliveries = 0;
					}
					else{
						loadAtPrevAct = (int) stateManager.getActivityState(prevAct, StateTypes.LOAD).toDouble();
						State futurePickState = stateManager.getActivityState(prevAct, "futurePicks");
						if(futurePickState == null) {
							futurePicks = 0;
						}
						else {
							futurePicks = (int) futurePickState.toDouble();
						}
						State pastDeliveryState = stateManager.getActivityState(prevAct, "pastDeliveries");
						if(pastDeliveryState == null){
							pastDeliveries = 0;
						}
						else {
							pastDeliveries = (int) pastDeliveryState.toDouble();
						}
					}
					if(newAct instanceof PickupActivity){
						if(loadAtPrevAct + newAct.getCapacityDemand() + futurePicks > iFacts.getNewVehicle().getCapacity()){
							return false;
						}
					}
					if(newAct instanceof DeliveryActivity){
						if(loadAtPrevAct + Math.abs(newAct.getCapacityDemand()) + pastDeliveries > iFacts.getNewVehicle().getCapacity()){
							return false;
						}
						
					}
					return true;
				}
				
			};
			
			MarginalsCalculus marginalCalculus = new MarginalsCalculusTriangleInequality(vrp.getTransportCosts(), vrp.getActivityCosts(), hardActLevelConstraint);
			CalculatesServiceInsertion serviceInsertion = new CalculatesServiceInsertion(vrp.getTransportCosts(), marginalCalculus, new HardConstraints.HardRouteLevelConstraint() {
				
				@Override
				public boolean fulfilled(InsertionContext insertionContext) {
					if(insertionContext.getJob() instanceof Delivery){
						int loadAtDepot = (int) stateManager.getRouteState(insertionContext.getRoute(), StateTypes.LOAD_AT_DEPOT).toDouble();
						if(loadAtDepot + insertionContext.getJob().getCapacityDemand() > insertionContext.getNewVehicle().getCapacity()){
							return false;
						}
					}
					else if(insertionContext.getJob() instanceof Pickup){
						int loadAtEnd = (int) stateManager.getRouteState(insertionContext.getRoute(), StateTypes.LOAD).toDouble();
						if(loadAtEnd + insertionContext.getJob().getCapacityDemand() > insertionContext.getNewVehicle().getCapacity()){
							return false;
						}
					}
					else throw new IllegalStateException("ähh");
					return true;
				}
				
			});
			
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
			
			final IterateRouteForwardInTime iterateForward = new IterateRouteForwardInTime(vrp.getTransportCosts());
			
			iterateForward.addListener(new UpdateCostsAtAllLevels(vrp.getActivityCosts(), vrp.getTransportCosts(), stateManager));
			iterateForward.addListener(new ForwardInTimeListener() {
				
				private int currentLoad = 0;
				private int deliveries = 0;  
				
				@Override
				public void start(VehicleRoute route, Start start, double departureTime) {
//					log.info("iterate forward");
					currentLoad = (int) stateManager.getRouteState(route, StateTypes.LOAD_AT_DEPOT).toDouble();
//					log.info("currentLoad="+currentLoad);
				}
				
				@Override
				public void nextActivity(TourActivity act, double arrTime, double endTime) {
//					log.info("nextAct="+act.getClass().toString()+ " capDemand=" + act.getCapacityDemand() + " currentLoad="+currentLoad);
					currentLoad += act.getCapacityDemand();
					if(act instanceof DeliveryActivity){
						deliveries += Math.abs(act.getCapacityDemand());
					}
					stateManager.putActivityState(act, StateTypes.LOAD, new StateImpl(currentLoad));
					stateManager.putActivityState(act, "pastDeliveries", new StateImpl(deliveries));
					if(currentLoad < 0) throw new IllegalStateException("currentload < 0");
					if(currentLoad > 50){
						throw new IllegalStateException("currentload="+currentLoad+" wich is > 50");
					}
				}
				
				@Override
				public void end(End end, double arrivalTime) {
//					log.info("currentLoad="+currentLoad);
//					stateManager.putRouteState(route, StateTypes.LOAD, new StateImpl(currentLoad));
					currentLoad = 0;
					deliveries = 0;
				}
			});
			
			final IterateRouteBackwardInTime iterateBackward = new IterateRouteBackwardInTime(vrp.getTransportCosts());
			iterateBackward.addListener(new BackwardInTimeListener() {
				
				int futurePicks = 0;
				@Override
				public void start(VehicleRoute route, End end, double latestArrivalTime) {
					
					
				}
				
				@Override
				public void prevActivity(TourActivity act, double latestDepartureTime, double latestOperationStartTime) {
					stateManager.putActivityState(act, "futurePicks", new StateImpl(futurePicks));
					if(act.getCapacityDemand() > 0){
						futurePicks += act.getCapacityDemand();
					}
					
				}
				
				@Override
				public void end(Start start, double latestDepartureTime) {
					futurePicks = 0;
					
				}
			});
			
			
			
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
						stateManager.putRouteState(route, StateTypes.LOAD_AT_DEPOT, new StateImpl(loadAtDepot));
						stateManager.putRouteState(route, StateTypes.LOAD, new StateImpl(loadAtEnd));
						iterateForward.iterate(route);
						iterateBackward.iterate(route);
					}
				}
				
			};
			
			vra.getSearchStrategyManager().addSearchStrategyModuleListener(new RemoveEmptyVehicles(fleetManager));
			vra.getSearchStrategyManager().addSearchStrategyModuleListener(loadVehicleInDepot);
			
			
			JobInsertedListener updateLoadAfterJobHasBeenInserted = new JobInsertedListener() {
				
				@Override
				public void informJobInserted(Job job2insert, VehicleRoute inRoute, double additionalCosts, double additionalTime) {
//					log.info("insert job " + job2insert.getClass().toString() + " " + job2insert.getCapacityDemand() + " in route " + inRoute.getTourActivities());
					if(job2insert instanceof Delivery){
						int loadAtDepot = (int) stateManager.getRouteState(inRoute, StateTypes.LOAD_AT_DEPOT).toDouble();
//						log.info("loadAtDepot="+loadAtDepot);
						stateManager.putRouteState(inRoute, StateTypes.LOAD_AT_DEPOT, new StateImpl(loadAtDepot + job2insert.getCapacityDemand()));
					}
					if(job2insert instanceof Pickup){
						int loadAtEnd = (int) stateManager.getRouteState(inRoute, StateTypes.LOAD).toDouble();
//						log.info("loadAtEnd="+loadAtEnd);
						stateManager.putRouteState(inRoute, StateTypes.LOAD, new StateImpl(loadAtEnd + job2insert.getCapacityDemand()));
					}
					
					iterateForward.iterate(inRoute);
					iterateBackward.iterate(inRoute);
				}
			};
			
			vra.getSearchStrategyManager().addSearchStrategyModuleListener(updateLoadAfterJobHasBeenInserted);
			
			VehicleRoutingProblemSolution iniSolution = new CreateInitialSolution(bestInsertion).createInitialSolution(vrp);
//			System.out.println("ini: costs="+iniSolution.getCost()+";#routes="+iniSolution.getRoutes().size());
			vra.addInitialSolution(iniSolution);
			
			vra.setNuOfIterations(2000);
//			vra.setPrematureBreak(200);
			
	}
	
	@Test
	public void test(){
		Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
		System.out.println(Solutions.getBest(solutions).getCost());
		new VrpXMLWriter(vrp, solutions).write("output/pdvrp_sol.xml");
		
	}

}
