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
import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.io.VehicleRoutingAlgorithms;
import jsprit.core.algorithm.state.StateManager;
import jsprit.core.algorithm.state.StateUpdater;
import jsprit.core.algorithm.termination.IterationWithoutImprovementTermination;
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
import jsprit.core.problem.solution.route.activity.DeliverShipment;
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

public class BicycleMessenger {

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
			double arrivalTime_at_newAct = prevActDepTime + routingCosts.getTransportTime(prevAct.getLocationId(), newAct.getLocationId(), prevActDepTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
			//local impact
			if(newAct instanceof DeliverShipment){
				double directTimeOfNearestMessenger = bestMessengers.get(((DeliverShipment) newAct).getJob().getId());
				if(arrivalTime_at_newAct > 3 * directTimeOfNearestMessenger){
					//not fulfilled AND it can never be fulfilled anymore by going forward in route, thus NOT_FULFILLED_BREAK
					return ConstraintsStatus.NOT_FULFILLED_BREAK;
				}
			}
			//impact on whole route, since insertion of newAct shifts all subsequent activities forward in time
			double departureTime_at_newAct = arrivalTime_at_newAct + newAct.getOperationTime();
			double deliverTimeAtNextAct = departureTime_at_newAct + routingCosts.getTransportTime(newAct.getLocationId(), nextAct.getLocationId(), departureTime_at_newAct, iFacts.getNewDriver(), iFacts.getNewVehicle());;
			if(deliverTimeAtNextAct > stateManager.getActivityState(nextAct, StateFactory.createId("latest-activity-start-time")).toDouble()){
				return ConstraintsStatus.NOT_FULFILLED;
			}
			return ConstraintsStatus.FULFILLED;
		}
		
	}
	
	/**
	 * one does not need this constraint. but it is faster. the earlier the solution-space can be constraint the better/faster.
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
	 * updates the state "latest-activity-start-time" once route/activity states changed, i.e. when removing or inserting an envelope-activity
	 * 
	 * @author schroeder
	 *
	 */
	static class UpdateLatestActivityStartTimes implements StateUpdater, ReverseActivityVisitor {

		private StateManager stateManager;
		
		private VehicleRoutingTransportCosts routingCosts;
		
		private Map<String,Double> bestMessengers;
		
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
			double potentialLatestArrivalTimeAtCurrAct = latestArrivalTime_at_prevAct - routingCosts.getBackwardTransportTime(activity.getLocationId(), prevAct.getLocationId(), latestArrivalTime_at_prevAct, route.getDriver(),route.getVehicle()) - activity.getOperationTime();
			double latestArrivalTime_at_activity = Math.min(3*timeOfNearestMessenger, potentialLatestArrivalTimeAtCurrAct);
			stateManager.putActivityState(activity, StateFactory.createId("latest-activity-start-time"), StateFactory.createState(latestArrivalTime_at_activity));
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
		
		
		VehicleRoutingProblem.Builder problemBuilder = VehicleRoutingProblem.Builder.newInstance();
		readEnvelopes(problemBuilder);
		readMessengers(problemBuilder);
		
		VehicleRoutingTransportCosts routingCosts = new CrowFlyCosts(problemBuilder.getLocations());
		Map<String,Double> nearestMessengers = getNearestMessengers(routingCosts, problemBuilder.getAddedJobs(), problemBuilder.getAddedVehicles());
		StateManager stateManager = new StateManager(routingCosts);
		stateManager.addDefaultActivityState(StateFactory.createId("latest-activity-start-time"), StateFactory.createState(Double.MAX_VALUE));
		
		problemBuilder.setFleetSize(FleetSize.FINITE);
		problemBuilder.addConstraint(new ThreeTimesLessThanBestDirectRouteConstraint(nearestMessengers, routingCosts, stateManager));
		problemBuilder.addConstraint(new IgnoreMessengerThatCanNeverMeetTimeRequirements(nearestMessengers, routingCosts));
		
		VehicleRoutingProblem bicycleMessengerProblem = problemBuilder.build();
		
		stateManager.addStateUpdater(new UpdateLatestActivityStartTimes(stateManager, routingCosts, nearestMessengers));
		
		VehicleRoutingAlgorithm algorithm = VehicleRoutingAlgorithms.readAndCreateAlgorithm(bicycleMessengerProblem,"input/algorithmConfig_open.xml", stateManager);
//		algorithm.setPrematureAlgorithmTermination(new IterationWithoutImprovementTermination(500));
		algorithm.addListener(new AlgorithmSearchProgressChartListener("output/progress.png"));
		Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
		
		SolutionPrinter.print(Solutions.bestOf(solutions));
		Plotter plotter = new Plotter(bicycleMessengerProblem);
//		plotter.setBoundingBox(10000, 47500, 20000, 67500);
		plotter.plotShipments(true);
		plotter.plot("output/bicycleMessengerProblem.png", "bicycleMessenger");
		
		
		Plotter plotter1 = new Plotter(bicycleMessengerProblem, Solutions.bestOf(solutions));
		plotter1.plotShipments(false);
		plotter1.setShowFirstActivity(true);
//		plotter1.setBoundingBox(10000, 47500, 20000, 67500);
		plotter1.plot("output/bicycleMessengerSolution.png", "bicycleMessenger");
		
		new VrpXMLWriter(bicycleMessengerProblem, solutions).write("output/bicycleMessenger.xml");
		

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
		VehicleType penaltyType = VehicleTypeImpl.Builder.newInstance("messengerType", 15).setFixedCost(200).setCostPerDistance(4).build();
		PenaltyVehicleType penaltyVehicleType = new PenaltyVehicleType(penaltyType);
		
		while((line = reader.readLine()) != null){
			if(firstLine) { firstLine = false; continue; }
			String[] tokens = line.split("\\s+");
			Vehicle vehicle = VehicleImpl.Builder.newInstance(tokens[1]).setLocationCoord(Coordinate.newInstance(Double.parseDouble(tokens[2]), Double.parseDouble(tokens[3])))
					.setReturnToDepot(true).setType(messengerType).build();
			problemBuilder.addVehicle(vehicle);
			Vehicle penaltyVehicle = VehicleImpl.Builder.newInstance(tokens[1]).setLocationCoord(Coordinate.newInstance(Double.parseDouble(tokens[2]), Double.parseDouble(tokens[3])))
					.setReturnToDepot(true).setType(penaltyVehicleType).build();
			problemBuilder.addVehicle(penaltyVehicle);
		}
		reader.close();
	}

}
