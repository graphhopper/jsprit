/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package examples;

import java.io.File;
import java.util.Collection;

import util.Coordinate;
import util.Solutions;
import algorithms.SchrimpfFactory;
import analysis.Plotter;
import analysis.SolutionPlotter;
import analysis.SolutionPrinter;
import analysis.Plotter.Label;
import analysis.SolutionPrinter.Print;
import basics.Delivery;
import basics.Pickup;
import basics.Service;
import basics.VehicleRoutingAlgorithm;
import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblem.Constraint;
import basics.VehicleRoutingProblemSolution;
import basics.io.VrpXMLWriter;
import basics.route.Vehicle;
import basics.route.VehicleImpl;
import basics.route.VehicleImpl.Builder;
import basics.route.VehicleType;
import basics.route.VehicleTypeImpl;

public class SimpleVRPWithBackhaulsExample {
	
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
		
		/*
		 * get a vehicle type-builder and build a type with the typeId "vehicleType" and a capacity of 2
		 */
		VehicleTypeImpl.Builder vehicleTypeBuilder = VehicleTypeImpl.Builder.newInstance("vehicleType", 2);
		VehicleType vehicleType = vehicleTypeBuilder.build();
		
		/*
		 * get a vehicle-builder and build a vehicle located at (10,10) with type "vehicleType"
		 */
		Builder vehicleBuilder = VehicleImpl.Builder.newInstance("vehicle");
		vehicleBuilder.setLocationCoord(Coordinate.newInstance(10, 10));
		vehicleBuilder.setType(vehicleType);
		Vehicle vehicle = vehicleBuilder.build();
		
		/*
		 * build pickups and deliveries at the required locations, each with a capacity-demand of 1.
		 */
		Pickup pickup1 = (Pickup) Pickup.Builder.newInstance("1", 1).setCoord(Coordinate.newInstance(5, 7)).build();
		Delivery delivery1 = (Delivery) Delivery.Builder.newInstance("2", 1).setCoord(Coordinate.newInstance(5, 13)).build();
		
		Pickup pickup2 = (Pickup) Pickup.Builder.newInstance("3", 1).setCoord(Coordinate.newInstance(15, 7)).build();
		Delivery delivery2 = (Delivery) Delivery.Builder.newInstance("4", 1).setCoord(Coordinate.newInstance(15, 13)).build();
		
		
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		vrpBuilder.addVehicle(vehicle);
		vrpBuilder.addService(pickup1).addService(pickup2).addService(delivery1).addService(delivery2);
		
		//
		vrpBuilder.addProblemConstraint(Constraint.DELIVERIES_FIRST);
		
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
		Plotter plotter = new Plotter(problem,bestSolution);
		plotter.setLabel(Label.SIZE);
		plotter.setShowFirstActivity(true);
		plotter.plot("output/solution.png", "solution");

	}

}
