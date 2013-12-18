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
package jsprit.examples;

import java.io.File;
import java.util.Collection;

import jsprit.analysis.toolbox.GraphStreamViewer;
import jsprit.analysis.toolbox.Plotter;
import jsprit.analysis.toolbox.SolutionPrinter;
import jsprit.analysis.toolbox.SolutionPrinter.Print;
import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.box.SchrimpfFactory;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.io.VrpXMLWriter;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleImpl.Builder;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.util.Coordinate;
import jsprit.core.util.Solutions;


public class SimpleEnRoutePickupAndDeliveryOpenRoutesExample {
	
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
		vehicleBuilder.setReturnToDepot(false);
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
		
		
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		vrpBuilder.addVehicle(vehicle);
		vrpBuilder.addJob(shipment1).addJob(shipment2).addJob(shipment3).addJob(shipment4);
		
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
		VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);
		
		/*
		 * write out problem and solution to xml-file
		 */
		new VrpXMLWriter(problem, solutions).write("output/shipment-problem-with-solution.xml");
		
		/*
		 * print nRoutes and totalCosts of bestSolution
		 */
		SolutionPrinter.print(problem,bestSolution,Print.VERBOSE);
		
		/*
		 * plot problem without solution
		 */
		Plotter problemPlotter = new Plotter(problem);
		problemPlotter.plotShipments(true);
		problemPlotter.plot("output/simpleEnRoutePickupAndDeliveryExample_problem.png", "en-route pickup and delivery");
		
		/*
		 * plot problem with solution
		 */
		Plotter solutionPlotter = new Plotter(problem,Solutions.bestOf(solutions).getRoutes());
		solutionPlotter.plotShipments(true);
		solutionPlotter.plot("output/simpleEnRoutePickupAndDeliveryExample_solution.png", "en-route pickup and delivery");
		
		new GraphStreamViewer(problem, bestSolution).setRenderShipments(true).setRenderDelay(100).display();
	}

}
