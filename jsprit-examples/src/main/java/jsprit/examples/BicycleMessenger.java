package jsprit.examples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import jsprit.analysis.toolbox.AlgorithmSearchProgressChartListener;
import jsprit.analysis.toolbox.Plotter;
import jsprit.analysis.toolbox.SolutionPrinter;
import jsprit.analysis.toolbox.SolutionPrinter.Print;
import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.io.VehicleRoutingAlgorithms;
import jsprit.core.algorithm.state.StateManager;
import jsprit.core.algorithm.state.StateUpdater;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.VehicleRoutingProblem.Builder;
import jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import jsprit.core.problem.constraint.HardActivityStateLevelConstraint;
import jsprit.core.problem.constraint.HardRouteStateLevelConstraint;
import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.driver.DriverImpl;
import jsprit.core.problem.io.VrpXMLWriter;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.ReverseActivityVisitor;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.solution.route.activity.TourActivity.JobActivity;
import jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;
import jsprit.core.problem.solution.route.state.StateFactory;
import jsprit.core.problem.vehicle.PenaltyVehicleType;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.util.Coordinate;
import jsprit.core.util.CrowFlyCosts;
import jsprit.core.util.Solutions;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/**
 * This class provides the/a solution to the following problem:
 * 
 * Statement of the problem (see Stackoverflow: http://stackoverflow.com/questions/19080537/bicycle-messenger-tsppd-with-optaplanner/20412598#20412598):
 * 
 * Optimize the routes for a bicycle messenger service! 
 * Assume 5 messengers that have to pick up 30 envelopes distributed through the city. These 5 messengers are distributed through the city as well. Thus
 * there is no single depot and they do not need to go back to their original starting location.
 * 
 * Additional hard constraints:
 * 1) Every messenger can carry up to fifteen envelopes
 * 2) The way an evelopes travels should be less than three times the direct route (so delivery does not take too long)
 * 
 * Thus this problem is basically a Capacitated VRP with Pickups and Deliveries, Multiple Depots, Open Routes and Time Windows/Restrictions.
 * 
 * @author stefan schroeder
 *
 */
public class BicycleMessenger {

	/**
	 * Hard constraint: delivery of envelope must not take longer than 3*bestDirect (i.e. fastest messenger on direct delivery) 
	 * 
	 * @author stefan
	 *
	 */
	static class ThreeTimesLessThanBestDirectRouteConstraint implements HardActivityStateLevelConstraint {

		private VehicleRoutingTransportCosts routingCosts;
		
		private RouteAndActivityStateGetter stateManager;
		
		//jobId map direct-distance by nearestMessenger
		private Map<String,Double> bestMessengers = new HashMap<String, Double>();
		
		public ThreeTimesLessThanBestDirectRouteConstraint(Map<String, Double> nearestMessengers, VehicleRoutingTransportCosts routingCosts, RouteAndActivityStateGetter stateManager) {
			this.bestMessengers = nearestMessengers;
			this.routingCosts = routingCosts;
			this.stateManager = stateManager;
		}

		@Override
		public ConstraintsStatus fulfilled(JobInsertionContext iFacts,TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
			//make sure vehicle can manage direct path
			double directArr_at_next = prevActDepTime + routingCosts.getTransportTime(prevAct.getLocationId(), nextAct.getLocationId(), prevActDepTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
			if(directArr_at_next > stateManager.getActivityState(nextAct, StateFactory.createId("latest-act-arrival-time")).toDouble()){
				return ConstraintsStatus.NOT_FULFILLED_BREAK;
			}
			
			double arrivalTime_at_newAct = prevActDepTime + routingCosts.getTransportTime(prevAct.getLocationId(), newAct.getLocationId(), prevActDepTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
			//local impact
			//no matter whether it is a pickupShipment or deliverShipment activities. both arrivalTimes must be < 3*best. 
			double directTimeOfNearestMessenger = bestMessengers.get(((JobActivity) newAct).getJob().getId());
			if(arrivalTime_at_newAct > 3 * directTimeOfNearestMessenger){
				//not fulfilled AND it can never be fulfilled anymore by going forward in route, thus NOT_FULFILLED_BREAK
				return ConstraintsStatus.NOT_FULFILLED_BREAK;
			}

			//impact on whole route, since insertion of newAct shifts all subsequent activities forward in time
			double departureTime_at_newAct = arrivalTime_at_newAct + newAct.getOperationTime();
			double arrTimeAtNextAct = departureTime_at_newAct + routingCosts.getTransportTime(newAct.getLocationId(), nextAct.getLocationId(), departureTime_at_newAct, iFacts.getNewDriver(), iFacts.getNewVehicle());;
			//here you need an activity state
			if(arrTimeAtNextAct > stateManager.getActivityState(nextAct, StateFactory.createId("latest-act-arrival-time")).toDouble()){
				return ConstraintsStatus.NOT_FULFILLED;
			}
			return ConstraintsStatus.FULFILLED;
		}
		
	}
	
	/**
	 * When inserting the activities of an envelope which are pickup and deliver envelope, this constraint makes insertion procedure to ignore messengers that are too far away to meet the 3*directTime-Constraint.  
	 * 
	 * <p>one does not need this constraint. but it is faster. the earlier the solution-space can be constraint the better/faster.
	 * @author schroeder
	 *
	 */
	static class IgnoreMessengerThatCanNeverMeetTimeRequirements implements HardRouteStateLevelConstraint {

		private Map<String,Double> bestMessengers = new HashMap<String, Double>();
		
		private VehicleRoutingTransportCosts routingCosts;
		
		public IgnoreMessengerThatCanNeverMeetTimeRequirements(Map<String, Double> bestMessengers,VehicleRoutingTransportCosts routingCosts) {
			super();
			this.bestMessengers = bestMessengers;
			this.routingCosts = routingCosts;
		}

		@Override
		public boolean fulfilled(JobInsertionContext insertionContext) {
			double timeOfDirectRoute = getTimeOfDirectRoute(insertionContext.getJob(), insertionContext.getNewVehicle(), routingCosts); 
			double timeOfNearestMessenger = bestMessengers.get(insertionContext.getJob().getId());
			if(timeOfDirectRoute > 3 * timeOfNearestMessenger){
				return false;
			}
			return true;
		}
		
	}
	
	/**
	 * updates the state "latest-activity-start-time" (required above) once route/activity states changed, i.e. when removing or inserting an envelope-activity
	 * 
	 * <p>thus once either the insertion-procedure starts or an envelope has been inserted, this visitor runs through the route in reverse order (i.e. starting with the end of the route) and 
	 * calculates the latest-activity-start-time (or latest-activity-arrival-time) which is the time to just meet the constraints of subsequent activities.
	 *   
	 * @author schroeder
	 *
	 */
	static class UpdateLatestActivityStartTimes implements StateUpdater, ReverseActivityVisitor {

		private final StateManager stateManager;
		
		private final VehicleRoutingTransportCosts routingCosts;
		
		private final Map<String,Double> bestMessengers;
		
		private VehicleRoute route;
		
		private TourActivity prevAct;
		
		private double latestArrivalTime_at_prevAct;
		
		public UpdateLatestActivityStartTimes(StateManager stateManager, VehicleRoutingTransportCosts routingCosts, Map<String, Double> bestMessengers) {
			super();
			this.stateManager = stateManager;
			this.routingCosts = routingCosts;
			this.bestMessengers = bestMessengers;
		}

		@Override
		public void begin(VehicleRoute route) {
			this.route = route;
			latestArrivalTime_at_prevAct = route.getEnd().getTheoreticalLatestOperationStartTime();
			prevAct = route.getEnd();
		}

		@Override
		public void visit(TourActivity activity) {
			double timeOfNearestMessenger = bestMessengers.get(((JobActivity)activity).getJob().getId());
			double potentialLatestArrivalTimeAtCurrAct = 
					latestArrivalTime_at_prevAct - routingCosts.getBackwardTransportTime(activity.getLocationId(), prevAct.getLocationId(), latestArrivalTime_at_prevAct, route.getDriver(),route.getVehicle()) - activity.getOperationTime();
			double latestArrivalTime_at_activity = Math.min(3*timeOfNearestMessenger, potentialLatestArrivalTimeAtCurrAct);
			stateManager.putActivityState(activity, StateFactory.createId("latest-act-arrival-time"), StateFactory.createState(latestArrivalTime_at_activity));
			assert activity.getArrTime() <= latestArrivalTime_at_activity : "this must not be since it breaks condition; actArrTime: " + activity.getArrTime() + " latestArrTime: " + latestArrivalTime_at_activity + " vehicle: " + route.getVehicle().getId();
			latestArrivalTime_at_prevAct = latestArrivalTime_at_activity;
			prevAct = activity;
		}

		@Override
		public void finish() {}
		
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		createOutputFolder();//for output generated below
		Logger.getRootLogger().setLevel(Level.INFO);
		//build the problem
		VehicleRoutingProblem.Builder problemBuilder = VehicleRoutingProblem.Builder.newInstance();
		problemBuilder.setFleetSize(FleetSize.FINITE);
		readEnvelopes(problemBuilder);
		readMessengers(problemBuilder);
		
		//add constraints to problem
		VehicleRoutingTransportCosts routingCosts = new CrowFlyCosts(problemBuilder.getLocations()); //which is the default VehicleRoutingTransportCosts in builder above
		//map mapping nearest messengers, i.e. for each envelope the direct-delivery-time with the fastest messenger is stored here
		Map<String,Double> nearestMessengers = getNearestMessengers(routingCosts, problemBuilder.getAddedJobs(), problemBuilder.getAddedVehicles());
		//define stateManager to update the required activity-state: "latest-activity-start-time"
		StateManager stateManager = new StateManager(routingCosts);
		//default states makes it more comfortable since state has a value and cannot be null (thus u dont need to check nulls)
		stateManager.addDefaultActivityState(StateFactory.createId("latest-act-arrival-time"), StateFactory.createState(Double.MAX_VALUE));
		
		//add the above problem-constraints
		problemBuilder.addConstraint(new ThreeTimesLessThanBestDirectRouteConstraint(nearestMessengers, routingCosts, stateManager));
		problemBuilder.addConstraint(new IgnoreMessengerThatCanNeverMeetTimeRequirements(nearestMessengers, routingCosts));
		
		//finally build the problem
		VehicleRoutingProblem bicycleMessengerProblem = problemBuilder.build();
		
		//and make sure you update the activity-state "latest-activity-start-time" the way it is defined above
		stateManager.addStateUpdater(new UpdateLatestActivityStartTimes(stateManager, routingCosts, nearestMessengers));
		
		//create your algorithm
		VehicleRoutingAlgorithm algorithm = VehicleRoutingAlgorithms.readAndCreateAlgorithm(bicycleMessengerProblem,"input/algorithmConfig_open.xml", stateManager);
		//if you want, terminate it after 1000 iterations with no change
//		algorithm.setPrematureAlgorithmTermination(new IterationWithoutImprovementTermination(1000));
		algorithm.addListener(new AlgorithmSearchProgressChartListener("output/progress.png"));
		algorithm.setNuOfIterations(2000);
		Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
		
		//this is just to ensure that solution meet the above constraints
		validateSolution(Solutions.bestOf(solutions), bicycleMessengerProblem, nearestMessengers);
		
		
		//you may want to plot the problem
		Plotter plotter = new Plotter(bicycleMessengerProblem);
//		plotter.setBoundingBox(10000, 47500, 20000, 67500);
		plotter.plotShipments(true);
		plotter.plot("output/bicycleMessengerProblem.png", "bicycleMessenger");
		
		//and the problem as well as the solution
		Plotter plotter1 = new Plotter(bicycleMessengerProblem, Solutions.bestOf(solutions));
		plotter1.plotShipments(true);
		plotter1.setShowFirstActivity(true);
//		plotter1.setBoundingBox(5000, 45500, 25000, 66500);
		plotter1.plot("output/bicycleMessengerSolution.png", "bicycleMessenger");
		
		//and write out your solution in xml
		new VrpXMLWriter(bicycleMessengerProblem, solutions).write("output/bicycleMessenger.xml");
		
		SolutionPrinter.print(bicycleMessengerProblem, Solutions.bestOf(solutions), Print.VERBOSE);
		

	}

	//if you wanne run this enable assertion by putting an '-ea' in your vmargument list - Run As --> Run Configurations --> (x)=Arguments --> VM arguments: -ea
	private static void validateSolution(VehicleRoutingProblemSolution bestOf, VehicleRoutingProblem bicycleMessengerProblem, Map<String, Double> nearestMessengers) {
		for(VehicleRoute route : bestOf.getRoutes()){
			for(TourActivity act : route.getActivities()){ 
				if(act.getArrTime() > 3*nearestMessengers.get(((JobActivity)act).getJob().getId())){
					SolutionPrinter.print(bicycleMessengerProblem, bestOf, Print.VERBOSE);
					throw new IllegalStateException("three times less than ... constraint broken. this must not be. act.getArrTime(): " + act.getArrTime() + " allowed: " + 3*nearestMessengers.get(((JobActivity)act).getJob().getId())); 
				}
			}
			if(route.getVehicle().getType() instanceof PenaltyVehicleType){
				SolutionPrinter.print(bicycleMessengerProblem, bestOf, Print.VERBOSE);
				throw new IllegalStateException("penaltyVehicle in solution. if there is a valid solution, this should not be");
			}
		}
	}

	private static void createOutputFolder() {
		/*
		 * some preparation - create output folder
		 */
		File dir = new File("output");
		// if the directory does not exist, create it
		if (!dir.exists()){
			System.out.println("creating directory ./output");
			boolean result = dir.mkdir();  
			if(result) System.out.println("./output created");  
		}
	}

	static Map<String,Double> getNearestMessengers(VehicleRoutingTransportCosts routingCosts, Collection<Job> envelopes, Collection<Vehicle> messengers) {
		Map<String,Double> nearestMessengers = new HashMap<String, Double>();	
		for(Job envelope : envelopes){
			double minDirect = Double.MAX_VALUE;
			for(Vehicle m : messengers){
				double direct = getTimeOfDirectRoute(envelope, m, routingCosts);
				if(direct < minDirect){
					minDirect = direct;
				}
			}
			nearestMessengers.put(envelope.getId(), minDirect);
		}
		return nearestMessengers;
	}
	
	static double getTimeOfDirectRoute(Job job, Vehicle v, VehicleRoutingTransportCosts routingCosts) {
		Shipment envelope = (Shipment) job;
		double direct = routingCosts.getTransportTime(v.getLocationId(), envelope.getPickupLocation(), 0.0, DriverImpl.noDriver(), v) + 
				routingCosts.getTransportTime(envelope.getPickupLocation(), envelope.getDeliveryLocation(), 0.0, DriverImpl.noDriver(), v);
		return direct;
	}

	private static void readEnvelopes(Builder problemBuilder) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(new File("input/bicycle_messenger_demand.txt")));
		String line = null;
		boolean firstLine = true;
		while((line = reader.readLine()) != null){
			if(firstLine) { firstLine = false; continue; }
			String[] tokens = line.split("\\s+");
			//define your envelope which is basically a shipment from A to B
			Shipment envelope = Shipment.Builder.newInstance(tokens[1], 1).setPickupCoord(Coordinate.newInstance(Double.parseDouble(tokens[2]), Double.parseDouble(tokens[3])))
					.setDeliveryCoord(Coordinate.newInstance(Double.parseDouble(tokens[4]), Double.parseDouble(tokens[5]))).build();
			problemBuilder.addJob(envelope);
		}
		reader.close();
	}

	private static void readMessengers(Builder problemBuilder) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(new File("input/bicycle_messenger_supply.txt")));
		String line = null;
		boolean firstLine = true;
		VehicleType messengerType = VehicleTypeImpl.Builder.newInstance("messengerType", 15).setCostPerDistance(1).build();
		/*
		 * the algo requires some time and space to search for a valid solution. if you ommit a penalty-type, it probably throws an Exception once it cannot insert an envelope anymore
		 * thus, give it space by defining a penalty/shadow vehicle with higher variable and fixed costs to up the pressure to find solutions without penalty type
		 * 
		 * it is important to give it the same typeId as the type you want to shadow
		 */
		VehicleType penaltyType = VehicleTypeImpl.Builder.newInstance("messengerType", 15).setFixedCost(50000).setCostPerDistance(4).build();
		PenaltyVehicleType penaltyVehicleType = new PenaltyVehicleType(penaltyType,4);
		
		while((line = reader.readLine()) != null){
			if(firstLine) { firstLine = false; continue; }
			String[] tokens = line.split("\\s+");
			//build your vehicle
			Vehicle vehicle = VehicleImpl.Builder.newInstance(tokens[1]).setLocationCoord(Coordinate.newInstance(Double.parseDouble(tokens[2]), Double.parseDouble(tokens[3])))
					.setReturnToDepot(false).setType(messengerType).build();
			problemBuilder.addVehicle(vehicle);
			//build the penalty vehicle
			Vehicle penaltyVehicle = VehicleImpl.Builder.newInstance(tokens[1]+"_penalty").setLocationCoord(Coordinate.newInstance(Double.parseDouble(tokens[2]), Double.parseDouble(tokens[3])))
					.setReturnToDepot(false).setType(penaltyVehicleType).build();
			problemBuilder.addVehicle(penaltyVehicle);
		}
		reader.close();
	}

}
