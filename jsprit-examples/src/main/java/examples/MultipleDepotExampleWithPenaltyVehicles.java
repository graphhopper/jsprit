package examples;

import java.util.Arrays;
import java.util.Collection;

import util.Coordinate;
import util.Solutions;
import algorithms.VehicleRoutingAlgorithms;
import analysis.AlgorithmSearchProgressChartListener;
import analysis.SolutionPlotter;
import analysis.SolutionPrinter;
import basics.VehicleRoutingAlgorithm;
import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblem.FleetSize;
import basics.VehicleRoutingProblemSolution;
import basics.io.VrpXMLReader;
import basics.route.Vehicle;
import basics.route.VehicleImpl;
import basics.route.VehicleImpl.VehicleType;

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
		
		/*
		 * number of penalty vehicles
		 */
		int nuOfPenaltyVehicles = 4;
		int depotCounter = 1;
		for(Coordinate depotCoord : Arrays.asList(firstDepotCoord,second)){
			for(int i=0;i<nuOfVehicles;i++){
				VehicleType vehicleType = VehicleType.Builder.newInstance(depotCounter + "_" + (i+1) + "_type", capacity).setCostPerDistance(1.0).build();
				String vehicleId = depotCounter + "_" + (i+1) + "_vehicle";
				VehicleImpl.VehicleBuilder vehicleBuilder = VehicleImpl.VehicleBuilder.newInstance(vehicleId);
				vehicleBuilder.setLocationCoord(depotCoord);
				vehicleBuilder.setType(vehicleType);
				vehicleBuilder.setLatestArrival(maxDuration);
				Vehicle vehicle = vehicleBuilder.build();
				vrpBuilder.addVehicle(vehicle);
			}
			for(int i=0;i<nuOfPenaltyVehicles;i++){
				VehicleType penaltyType = VehicleType.Builder.newInstance(depotCounter + "_" + (i+1) + "_penaltyType", capacity).setFixedCost(50).setCostPerDistance(3.0).build();
				String vehicleId = depotCounter + "_" + (i+1) + "_penaltyVehicle";
				VehicleImpl.VehicleBuilder vehicleBuilder = VehicleImpl.VehicleBuilder.newInstance(vehicleId);
				vehicleBuilder.setLocationCoord(depotCoord);
				vehicleBuilder.setType(penaltyType);
				vehicleBuilder.setLatestArrival(maxDuration);
				Vehicle penaltyVehicle = vehicleBuilder.build();
				vrpBuilder.addVehicle(penaltyVehicle);
			}
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
		SolutionPlotter.plotVrpAsPNG(vrp, "output/problem08.png", "p08");

		/*
		 * solve the problem
		 */
		VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "input/algorithmConfig.xml");
		Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
		
		SolutionPrinter.print(Solutions.getBest(solutions));
		SolutionPlotter.plotSolutionAsPNG(vrp, Solutions.getBest(solutions), "output/p08_solution.png", "p08");

	}

}
