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
import java.util.Arrays;
import java.util.Collection;

import util.Coordinate;
import util.Solutions;
import algorithms.SchrimpfFactory;
import analysis.Plotter;
import analysis.SolutionPlotter;
import analysis.SolutionPrinter;
import analysis.SolutionPrinter.Print;
import basics.Delivery;
import basics.Service;
import basics.Shipment;
import basics.VehicleRoutingAlgorithm;
import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblemSolution;
import basics.VehicleRoutingProblem.Constraint;
import basics.io.VrpXMLWriter;
import basics.route.Vehicle;
import basics.route.VehicleImpl;
import basics.route.VehicleImpl.Builder;
import basics.route.VehicleType;
import basics.route.VehicleTypeImpl;

public class SimpleEnRoutePickupAndDeliveryWithDepotBoundedDeliveriesExample {
	
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
		 * build shipments at the required locations, each with a capacity-demand of 1.
		 * 4 shipments
		 * 1: (5,7)->(6,9)
		 * 2: (5,13)->(6,11)
		 * 3: (15,7)->(14,9)
		 * 4: (15,13)->(14,11)
		 */
		
		Shipment shipment1 = Shipment.Builder.newInstance("1", 1).setPickupCoord(Coordinate.newInstance(5, 7)).setDeliveryCoord(Coordinate.newInstance(6, 9)).build();
		Shipment shipment2 = Shipment.Builder.newInstance("2", 1).setPickupCoord(Coordinate.newInstance(5, 13)).setDeliveryCoord(Coordinate.newInstance(6, 11)).build();
		
		Shipment shipment3 = Shipment.Builder.newInstance("3", 1).setPickupCoord(Coordinate.newInstance(15, 7)).setDeliveryCoord(Coordinate.newInstance(14, 9)).build();
		Shipment shipment4 = Shipment.Builder.newInstance("4", 1).setPickupCoord(Coordinate.newInstance(15, 13)).setDeliveryCoord(Coordinate.newInstance(14, 11)).build();
//		
		/*
		 * build deliveries, (implicitly picked up in the depot)
		 * 1: (4,8)
		 * 2: (4,12)
		 * 3: (16,8)
		 * 4: (16,12)
		 */
		Delivery delivery1 = (Delivery) Delivery.Builder.newInstance("5", 1).setCoord(Coordinate.newInstance(4, 8)).build();
		Delivery delivery2 = (Delivery) Delivery.Builder.newInstance("6", 1).setCoord(Coordinate.newInstance(4, 12)).build();
		Delivery delivery3 = (Delivery) Delivery.Builder.newInstance("7", 1).setCoord(Coordinate.newInstance(16, 8)).build();
		Delivery delivery4 = (Delivery) Delivery.Builder.newInstance("8", 1).setCoord(Coordinate.newInstance(16, 12)).build();
		
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		vrpBuilder.addVehicle(vehicle);
		vrpBuilder.addJob(shipment1).addJob(shipment2).addJob(shipment3).addJob(shipment4)
			.addJob(delivery1).addJob(delivery2).addJob(delivery3).addJob(delivery4).build();
		
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
		
		new VrpXMLWriter(problem, solutions).write("output/mixed-shipments-services-problem-with-solution.xml");
		
		SolutionPrinter.print(bestSolution,Print.VERBOSE);
		
		/*
		 * plot
		 */
		Plotter problemPlotter = new Plotter(problem);
		problemPlotter.plotShipments(true);
		problemPlotter.plot("output/simpleMixedEnRoutePickupAndDeliveryExample_problem.png", "en-route pd and depot bounded deliveries");
		
		Plotter solutionPlotter = new Plotter(problem,Solutions.bestOf(solutions));
		solutionPlotter.plotShipments(true);
		solutionPlotter.plot("output/simpleMixedEnRoutePickupAndDeliveryExample_solution.png", "en-route pd and depot bounded deliveries");
		
	}

}
