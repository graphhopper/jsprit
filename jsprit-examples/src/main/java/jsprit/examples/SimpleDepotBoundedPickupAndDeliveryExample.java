/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
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

import jsprit.analysis.toolbox.Plotter;
import jsprit.analysis.toolbox.Plotter.Label;
import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.box.SchrimpfFactory;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.io.VrpXMLWriter;
import jsprit.core.problem.job.Delivery;
import jsprit.core.problem.job.Pickup;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleImpl.Builder;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.reporting.SolutionPrinter;
import jsprit.core.util.Solutions;
import jsprit.util.Examples;

import java.util.Collection;


public class SimpleDepotBoundedPickupAndDeliveryExample {
	
	public static void main(String[] args) {
		/*
		 * some preparation - create output folder
		 */
		Examples.createOutputFolder();
		
		/*
		 * get a vehicle type-builder and build a type with the typeId "vehicleType" and a capacity of 2
		 */
		VehicleTypeImpl.Builder vehicleTypeBuilder = VehicleTypeImpl.Builder.newInstance("vehicleType").addCapacityDimension(0, 2);
		VehicleType vehicleType = vehicleTypeBuilder.build();
		
		/*
		 * get a vehicle-builder and build a vehicle located at (10,10) with type "vehicleType"
		 */
		Builder vehicleBuilder = VehicleImpl.Builder.newInstance("vehicle");
		vehicleBuilder.setStartLocation(Location.newInstance(10, 10));
		vehicleBuilder.setType(vehicleType);
		VehicleImpl vehicle = vehicleBuilder.build();
		
		/*
		 * build pickups and deliveries at the required locations, each with a capacity-demand of 1.
		 */
		Pickup pickup1 = (Pickup) Pickup.Builder.newInstance("1").addSizeDimension(0, 1).setLocation(Location.newInstance(5, 7)).build();
		Delivery delivery1 = (Delivery) Delivery.Builder.newInstance("2").addSizeDimension(0, 1).setLocation(Location.newInstance(5, 13)).build();
		
		Pickup pickup2 = (Pickup) Pickup.Builder.newInstance("3").addSizeDimension(0, 1).setLocation(Location.newInstance(15, 7)).build();
		Delivery delivery2 = (Delivery) Delivery.Builder.newInstance("4").addSizeDimension(0, 1).setLocation(Location.newInstance(15, 13)).build();
		
		
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		vrpBuilder.addVehicle(vehicle);
		vrpBuilder.addJob(pickup1).addJob(pickup2).addJob(delivery1).addJob(delivery2);
		
		
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
		
		new VrpXMLWriter(problem, solutions).write("output/problem-with-solution.xml");
		
		SolutionPrinter.print(bestSolution);
		
		/*
		 * plot
		 */
		Plotter plotter = new Plotter(problem,bestSolution);
		plotter.setLabel(Label.SIZE);
		plotter.plot("output/solution.png", "solution");

	}

}
