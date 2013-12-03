package jsprit.examples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import jsprit.analysis.toolbox.Plotter;
import jsprit.analysis.toolbox.SolutionPrinter;
import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.io.VehicleRoutingAlgorithms;
import jsprit.core.algorithm.state.StateManager;
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
import jsprit.core.problem.solution.route.activity.DeliverShipment;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.util.Coordinate;
import jsprit.core.util.CrowFlyCosts;
import jsprit.core.util.Solutions;

public class BicycleMessenger {

	static class ThreeTimesLessThanDirectRouteConstraint implements HardActivityStateLevelConstraint {

		private VehicleRoutingTransportCosts routingCosts;
		
		//jobId map direct-distance by nearestMessenger
		private Map<String,Double> bestMessengers = new HashMap<String, Double>();
		
		public ThreeTimesLessThanDirectRouteConstraint(Map<String, Double> nearestMessengers, VehicleRoutingTransportCosts routingCosts) {
			this.bestMessengers = nearestMessengers;
			this.routingCosts = routingCosts;
		}

		@Override
		public ConstraintsStatus fulfilled(JobInsertionContext iFacts,TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
			if(newAct instanceof DeliverShipment){
				double deliveryTime = prevActDepTime + routingCosts.getTransportTime(prevAct.getLocationId(), newAct.getLocationId(), prevActDepTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
				double directTimeOfNearestMessenger = bestMessengers.get(((DeliverShipment) newAct).getJob().getId());
				if(deliveryTime > 3 * directTimeOfNearestMessenger){
					return ConstraintsStatus.NOT_FULFILLED_BREAK;
				}
			}
			return ConstraintsStatus.FULFILLED;
		}
		
	}
	
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
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		
		VehicleRoutingProblem.Builder problemBuilder = VehicleRoutingProblem.Builder.newInstance();
		readEnvelopes(problemBuilder);
		readMessengers(problemBuilder);
		
		VehicleRoutingTransportCosts routingCosts = new CrowFlyCosts(problemBuilder.getLocations());
		Map<String,Double> nearestMessengers = getNearestMessengers(routingCosts, problemBuilder.getAddedJobs(), problemBuilder.getAddedVehicles());
		
		problemBuilder.setFleetSize(FleetSize.FINITE);
		problemBuilder.addConstraint(new ThreeTimesLessThanDirectRouteConstraint(nearestMessengers, routingCosts));
		problemBuilder.addConstraint(new IgnoreMessengerThatCanNeverMeetTimeRequirements(nearestMessengers, routingCosts));
		
		VehicleRoutingProblem bicycleMessengerProblem = problemBuilder.build();
		
		StateManager stateManager = new StateManager(bicycleMessengerProblem);
		
		VehicleRoutingAlgorithm algorithm = VehicleRoutingAlgorithms.readAndCreateAlgorithm(bicycleMessengerProblem,"input/algorithmConfig_open.xml");
		algorithm.setPrematureAlgorithmTermination(new IterationWithoutImprovementTermination(200));
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
		while((line = reader.readLine()) != null){
			if(firstLine) { firstLine = false; continue; }
			String[] tokens = line.split("\\s+");
			Vehicle vehicle = VehicleImpl.Builder.newInstance(tokens[1]).setLocationCoord(Coordinate.newInstance(Double.parseDouble(tokens[2]), Double.parseDouble(tokens[3])))
					.setReturnToDepot(true).setType(messengerType).build();
			problemBuilder.addVehicle(vehicle);
		}
		reader.close();
	}

}
