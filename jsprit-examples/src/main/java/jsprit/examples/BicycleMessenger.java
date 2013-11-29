package jsprit.examples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;

import jsprit.analysis.toolbox.Plotter;
import jsprit.analysis.toolbox.SolutionPrinter;
import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.io.VehicleRoutingAlgorithms;
import jsprit.core.algorithm.termination.IterationWithoutImprovementTermination;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.VehicleRoutingProblem.Builder;
import jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.util.Coordinate;
import jsprit.core.util.Solutions;

public class BicycleMessenger {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		VehicleRoutingProblem.Builder problemBuilder = VehicleRoutingProblem.Builder.newInstance();
		readEnvelopes(problemBuilder);
		readMessengers(problemBuilder);
		problemBuilder.setFleetSize(FleetSize.FINITE);
		
		VehicleRoutingProblem bicycleMessengerProblem = problemBuilder.build();
		
		VehicleRoutingAlgorithm algorithm = VehicleRoutingAlgorithms.readAndCreateAlgorithm(bicycleMessengerProblem, 6, "input/algorithmConfig_open.xml");
		algorithm.setPrematureAlgorithmTermination(new IterationWithoutImprovementTermination(50));
		Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
		
		SolutionPrinter.print(Solutions.bestOf(solutions));
		Plotter plotter = new Plotter(bicycleMessengerProblem);
		plotter.plotShipments(true);
		plotter.plot("output/bicycleMessengerProblem.png", "bicycleMenssenger");
		
		
		Plotter plotter1 = new Plotter(bicycleMessengerProblem, Solutions.bestOf(solutions));
		plotter1.plotShipments(false);
		plotter1.plot("output/bicycleMessengerSolution.png", "bicycleMenssenger");
		

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
					.setReturnToDepot(false).setType(messengerType).build();
			problemBuilder.addVehicle(vehicle);
		}
		reader.close();
	}

}
