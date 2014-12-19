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
import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.VehicleRoutingAlgorithmBuilder;
import jsprit.core.algorithm.state.StateManager;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.constraint.ConstraintManager;
import jsprit.core.problem.constraint.ServiceDeliveriesFirstConstraint;
import jsprit.core.problem.io.VrpXMLWriter;
import jsprit.core.problem.job.Delivery;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleImpl.Builder;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.reporting.SolutionPrinter;
import jsprit.core.util.Coordinate;
import jsprit.core.util.Solutions;
import jsprit.util.Examples;

import java.util.Collection;


public class SimpleEnRoutePickupAndDeliveryWithDepotBoundedDeliveriesExample {
	
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
		vehicleBuilder.setStartLocation(loc(Coordinate.newInstance(10, 10)));
		vehicleBuilder.setType(vehicleType);
		VehicleImpl vehicle = vehicleBuilder.build();
		
		/*
		 * build shipments at the required locations, each with a capacity-demand of 1.
		 * 4 shipments
		 * 1: (5,7)->(6,9)
		 * 2: (5,13)->(6,11)
		 * 3: (15,7)->(14,9)
		 * 4: (15,13)->(14,11)
		 */
		
		Shipment shipment1 = Shipment.Builder.newInstance("1").addSizeDimension(0, 1).setPickupLocation(loc(Coordinate.newInstance(5, 7))).setDeliveryLocation(loc(Coordinate.newInstance(6, 9))).build();
		Shipment shipment2 = Shipment.Builder.newInstance("2").addSizeDimension(0, 1).setPickupLocation(loc(Coordinate.newInstance(5, 13))).setDeliveryLocation(loc(Coordinate.newInstance(6, 11))).build();
		
		Shipment shipment3 = Shipment.Builder.newInstance("3").addSizeDimension(0, 1).setPickupLocation(loc(Coordinate.newInstance(15, 7))).setDeliveryLocation(loc(Coordinate.newInstance(14, 9))).build();
		Shipment shipment4 = Shipment.Builder.newInstance("4").addSizeDimension(0, 1).setPickupLocation(loc(Coordinate.newInstance(15, 13))).setDeliveryLocation(loc(Coordinate.newInstance(14, 11))).build();
//		
		/*
		 * build deliveries, (implicitly picked up in the depot)
		 * 1: (4,8)
		 * 2: (4,12)
		 * 3: (16,8)
		 * 4: (16,12)
		 */
		Delivery delivery1 = (Delivery) Delivery.Builder.newInstance("5").addSizeDimension(0, 1).setLocation(loc(Coordinate.newInstance(4, 8))).build();
		Delivery delivery2 = (Delivery) Delivery.Builder.newInstance("6").addSizeDimension(0, 1).setLocation(loc(Coordinate.newInstance(4, 12))).build();
		Delivery delivery3 = (Delivery) Delivery.Builder.newInstance("7").addSizeDimension(0, 1).setLocation(loc(Coordinate.newInstance(16, 8))).build();
		Delivery delivery4 = (Delivery) Delivery.Builder.newInstance("8").addSizeDimension(0, 1).setLocation(loc(Coordinate.newInstance(16, 12))).build();
		
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		vrpBuilder.addVehicle(vehicle);
		vrpBuilder.addJob(shipment1).addJob(shipment2).addJob(shipment3).addJob(shipment4)
			.addJob(delivery1).addJob(delivery2).addJob(delivery3).addJob(delivery4).build();

		VehicleRoutingProblem problem = vrpBuilder.build();
		
		/*
		 * build the algorithm
		 */
        VehicleRoutingAlgorithmBuilder vraBuilder = new VehicleRoutingAlgorithmBuilder(problem,"input/algorithmConfig.xml");
        vraBuilder.addCoreConstraints();
        vraBuilder.addDefaultCostCalculators();
        StateManager stateManager = new StateManager(problem);
        ConstraintManager constraintManager = new ConstraintManager(problem,stateManager);
        constraintManager.addConstraint(new ServiceDeliveriesFirstConstraint(), ConstraintManager.Priority.CRITICAL);
        vraBuilder.setStateAndConstraintManager(stateManager,constraintManager);
        VehicleRoutingAlgorithm algorithm = vraBuilder.build();
		
		/*
		 * and search a solution
		 */
		Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
		
		/*
		 * get the best 
		 */
		VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);
		
		new VrpXMLWriter(problem, solutions).write("output/mixed-shipments-services-problem-with-solution.xml");
		
		SolutionPrinter.print(bestSolution);
		
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

	private static Location loc(Coordinate coordinate) {
		return Location.Builder.newInstance().setCoordinate(coordinate).build();
	}

}
