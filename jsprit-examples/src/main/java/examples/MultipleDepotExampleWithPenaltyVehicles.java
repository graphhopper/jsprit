package examples;

import java.util.Arrays;
import java.util.Collection;

import util.Coordinate;
import util.Solutions;
import algorithms.VehicleRoutingAlgorithms;
import analysis.AlgorithmSearchProgressChartListener;
import analysis.SolutionPlotter;
import analysis.SolutionPrinter;
import analysis.SolutionPrinter.Print;
import analysis.StopWatch;
import basics.VehicleRoutingAlgorithm;
import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblem.FleetSize;
import basics.VehicleRoutingProblemSolution;
import basics.algo.VehicleRoutingAlgorithmListeners.Priority;
import basics.io.VrpXMLReader;
import basics.route.PenaltyVehicleType;
import basics.route.Vehicle;
import basics.route.VehicleImpl;
import basics.route.VehicleTypeImpl;

public class MultipleDepotExampleWithPenaltyVehicles {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		/*
		 * Read cordeau-instance p01, BUT only its services without any vehicles 
		 */
		new VrpXMLReader(vrpBuilder).read("input/vrp_cordeau_08.xml");
		
		/*
		 * add vehicles with its depots
		 * 2 depots:
		 * (-33,33)
		 * (33,-33)
		 * 
		 * each with 14 vehicles each with a capacity of 500 and a maximum duration of 310
		 */
		int nuOfVehicles = 14;
		int capacity = 500;
		double maxDuration = 310;
		Coordinate firstDepotCoord = Coordinate.newInstance(-33, 33);
		Coordinate second = Coordinate.newInstance(33, -33);
		
		int depotCounter = 1;
		for(Coordinate depotCoord : Arrays.asList(firstDepotCoord,second)){
			for(int i=0;i<nuOfVehicles;i++){
				VehicleTypeImpl vehicleType = VehicleTypeImpl.Builder.newInstance(depotCounter + "_type", capacity).setCostPerDistance(1.0).build();
				String vehicleId = depotCounter + "_" + (i+1) + "_vehicle";
				VehicleImpl.VehicleBuilder vehicleBuilder = VehicleImpl.VehicleBuilder.newInstance(vehicleId);
				vehicleBuilder.setLocationCoord(depotCoord);
				vehicleBuilder.setType(vehicleType);
				vehicleBuilder.setLatestArrival(maxDuration);
				Vehicle vehicle = vehicleBuilder.build();
				vrpBuilder.addVehicle(vehicle);
			}
			VehicleTypeImpl penaltyType = VehicleTypeImpl.Builder.newInstance(depotCounter + "_type", capacity).setFixedCost(50).setCostPerDistance(3.0).build();
			PenaltyVehicleType penaltyVehicleType = new PenaltyVehicleType(penaltyType);
			String vehicleId = depotCounter + "_vehicle#penalty";
			VehicleImpl.VehicleBuilder vehicleBuilder = VehicleImpl.VehicleBuilder.newInstance(vehicleId);
			vehicleBuilder.setLocationCoord(depotCoord);
			vehicleBuilder.setType(penaltyVehicleType);
			vehicleBuilder.setLatestArrival(maxDuration);
			Vehicle penaltyVehicle = vehicleBuilder.build();
			vrpBuilder.addVehicle(penaltyVehicle);

			depotCounter++;
		}
		
		
		
		/*
		 * define problem with finite fleet
		 */
		vrpBuilder.setFleetSize(FleetSize.FINITE);
		
		/*
		 * build the problem
		 */
		VehicleRoutingProblem vrp = vrpBuilder.build();
		
		/*
		 * plot to see how the problem looks like
		 */
//		SolutionPlotter.plotVrpAsPNG(vrp, "output/problem08.png", "p08");

		/*
		 * solve the problem
		 */
		VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "input/algorithmConfig.xml");
		vra.setNuOfIterations(80000);
		vra.setPrematureBreak(1000);
		vra.getAlgorithmListeners().addListener(new StopWatch(),Priority.HIGH);
		vra.getAlgorithmListeners().addListener(new AlgorithmSearchProgressChartListener("output/progress.png"));
		Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
		
		SolutionPrinter.print(Solutions.getBest(solutions),Print.VERBOSE);
		SolutionPlotter.plotSolutionAsPNG(vrp, Solutions.getBest(solutions), "output/p08_solution.png", "p08");

	}

}
