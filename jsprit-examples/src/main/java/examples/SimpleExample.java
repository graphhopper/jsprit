package examples;

import java.util.Collection;

import util.Coordinate;
import util.Solutions;
import algorithms.SchrimpfFactory;
import analysis.SolutionPlotter;
import analysis.SolutionPrinter;
import analysis.SolutionPrinter.Print;
import basics.Service;
import basics.VehicleRoutingAlgorithm;
import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblemSolution;
import basics.io.VrpXMLWriter;
import basics.route.Vehicle;
import basics.route.VehicleImpl;
import basics.route.VehicleImpl.VehicleBuilder;
import basics.route.VehicleImpl.VehicleType;

public class SimpleExample {
	
	public static void main(String[] args) {
		
		/*
		 * get a vehicle type-builder and build a type with the typeId "vehicleType" and a capacity of 2
		 */
		VehicleType.Builder vehicleTypeBuilder = VehicleImpl.VehicleType.Builder.newInstance("vehicleType", 2);
		VehicleType vehicleType = vehicleTypeBuilder.build();
		
		/*
		 * get a vehicle-builder and build a vehicle located at (10,10) with type "vehicleType"
		 */
		VehicleBuilder vehicleBuilder = VehicleImpl.VehicleBuilder.newInstance("vehicle");
		vehicleBuilder.setLocationCoord(Coordinate.newInstance(10, 10));
		vehicleBuilder.setType(vehicleType);
		Vehicle vehicle = vehicleBuilder.build();
		
		/*
		 * build services at the required locations, each with a capacity-demand of 1.
		 */
		Service service1 = Service.Builder.newInstance("1", 1).setCoord(Coordinate.newInstance(5, 7)).build();
		Service service2 = Service.Builder.newInstance("2", 1).setCoord(Coordinate.newInstance(5, 13)).build();
		
		Service service3 = Service.Builder.newInstance("3", 1).setCoord(Coordinate.newInstance(15, 7)).build();
		Service service4 = Service.Builder.newInstance("4", 1).setCoord(Coordinate.newInstance(15, 13)).build();
		
		
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		vrpBuilder.addVehicle(vehicle);
		vrpBuilder.addService(service1).addService(service2).addService(service3).addService(service4);
		
		VehicleRoutingProblem problem = vrpBuilder.build();
		
		/*
		 * get the algorithm out-of-the-box. 
		 */
		VehicleRoutingAlgorithm algorithm = new SchrimpfFactory().createAlgorithm(problem);
		
		/*
		 * and search a solution
		 */
		Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
		
		/*
		 * get the best 
		 */
		VehicleRoutingProblemSolution bestSolution = Solutions.getBest(solutions);
		
		new VrpXMLWriter(problem, solutions).write("output/problem-with-solution.xml");
		
		SolutionPrinter.print(bestSolution,Print.VERBOSE);
		
		/*
		 * plot
		 */
		SolutionPlotter.plotSolutionAsPNG(problem, bestSolution, "output/solution.png", "solution");
	}

}
