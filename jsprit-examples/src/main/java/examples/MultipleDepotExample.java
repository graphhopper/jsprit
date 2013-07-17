package examples;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import util.Coordinate;
import util.Solutions;
import algorithms.VehicleRoutingAlgorithms;
import analysis.AlgorithmSearchProgressChartListener;
import analysis.SolutionPlotter;
import analysis.SolutionPrinter;
import analysis.StopWatch;
import basics.VehicleRoutingAlgorithm;
import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblem.FleetSize;
import basics.VehicleRoutingProblemSolution;
import basics.algo.VehicleRoutingAlgorithmListeners.Priority;
import basics.io.VrpXMLReader;
import basics.route.Vehicle;
import basics.route.VehicleImpl;
import basics.route.VehicleTypeImpl;

public class MultipleDepotExample {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
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
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		/*
		 * Read cordeau-instance p01, BUT only its services without any vehicles 
		 */
		new VrpXMLReader(vrpBuilder).read("input/vrp_cordeau_01.xml");
		
		/*
		 * add vehicles with its depots
		 * 4 depots:
		 * (20,20)
		 * (30,40)
		 * (50,30)
		 * (60,50)
		 * 
		 * each with 4 vehicles each with a capacity of 80
		 */
		int nuOfVehicles = 4;
		int capacity = 80;
		Coordinate firstDepotCoord = Coordinate.newInstance(20, 20);
		Coordinate second = Coordinate.newInstance(30, 40);
		Coordinate third = Coordinate.newInstance(50, 30);
		Coordinate fourth = Coordinate.newInstance(60, 50);
		
		int depotCounter = 1;
		for(Coordinate depotCoord : Arrays.asList(firstDepotCoord,second,third,fourth)){
			for(int i=0;i<nuOfVehicles;i++){
				VehicleTypeImpl vehicleType = VehicleTypeImpl.Builder.newInstance(depotCounter + "_type", capacity).setCostPerDistance(1.0).build();
				Vehicle vehicle = VehicleImpl.Builder.newInstance(depotCounter + "_" + (i+1) + "_vehicle").setLocationCoord(depotCoord).setType(vehicleType).build();
				vrpBuilder.addVehicle(vehicle);
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
//		SolutionPlotter.plotVrpAsPNG(vrp, "output/problem01.png", "p01");

		/*
		 * solve the problem
		 */
		VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "input/algorithmConfig.xml");
		vra.getAlgorithmListeners().addListener(new StopWatch(),Priority.HIGH);
		vra.getAlgorithmListeners().addListener(new AlgorithmSearchProgressChartListener("output/progress.png"));
		Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
		
		SolutionPrinter.print(Solutions.getBest(solutions));
		SolutionPlotter.plotSolutionAsPNG(vrp, Solutions.getBest(solutions), "output/p01_solution.png", "p01");

	}

}
