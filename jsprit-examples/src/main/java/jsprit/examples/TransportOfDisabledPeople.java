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

import jsprit.analysis.toolbox.GraphStreamViewer;
import jsprit.analysis.toolbox.GraphStreamViewer.Label;
import jsprit.analysis.toolbox.Plotter;
import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.VehicleRoutingAlgorithmBuilder;
import jsprit.core.algorithm.state.StateManager;
import jsprit.core.algorithm.termination.IterationWithoutImprovementTermination;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import jsprit.core.problem.constraint.ConstraintManager;
import jsprit.core.problem.constraint.HardRouteConstraint;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.misc.JobInsertionContext;
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

public class TransportOfDisabledPeople {
	
	static int WHEELCHAIRSPACE_INDEX = 0;
	
	static int PASSENGERSEATS_INDEX = 1;
	
	public static void main(String[] args) {
		/*
		 * some preparation - create output folder
		 */
		Examples.createOutputFolder();
		
		/*
		 * get a vehicle type-builder and build a type with the typeId "vehicleType" and a capacity of 2
		 */
		VehicleTypeImpl.Builder wheelChairTypeBuilder = VehicleTypeImpl.Builder.newInstance("wheelChairBusType")
				.addCapacityDimension(WHEELCHAIRSPACE_INDEX, 2) //can transport two people with wheelchair
				.addCapacityDimension(PASSENGERSEATS_INDEX, 4); //and 4 without
		VehicleType vehicleType_wheelchair = wheelChairTypeBuilder.build();
		
		VehicleTypeImpl.Builder soleyPassengerTypeBuilder = VehicleTypeImpl.Builder.newInstance("passengerBusType")
				.addCapacityDimension(PASSENGERSEATS_INDEX, 6); //and 4 without
		VehicleType vehicleType_solelypassenger = soleyPassengerTypeBuilder.build();
		
		/*
		 * define two vehicles and their locations.
		 * 
		 * this example employs two vehicles. one that has to return to its start-location (vehicle1) and one that has a different
		 * end-location.
		 * 
		 * play with these location to see which impact they have on customer-sequences.
		 */
		Builder vehicleBuilder1 = VehicleImpl.Builder.newInstance("wheelchair_bus");
		vehicleBuilder1.setStartLocation(loc(Coordinate.newInstance(10, 10)));
		vehicleBuilder1.setType(vehicleType_wheelchair);
		VehicleImpl vehicle1 = vehicleBuilder1.build();
		
		Builder vehicleBuilder1_2 = VehicleImpl.Builder.newInstance("wheelchair_bus_2");
		vehicleBuilder1_2.setStartLocation(loc(Coordinate.newInstance(10, 10)));
		vehicleBuilder1_2.setType(vehicleType_wheelchair);
		VehicleImpl vehicle1_2 = vehicleBuilder1_2.build();
		
		Builder vehicleBuilder2 = VehicleImpl.Builder.newInstance("passenger_bus");
		vehicleBuilder2.setStartLocation(loc(Coordinate.newInstance(30, 30))).setEndLocation(loc(Coordinate.newInstance(30, 19)));
		vehicleBuilder2.setType(vehicleType_solelypassenger);
		VehicleImpl vehicle2 = vehicleBuilder2.build();
		
		Builder vehicleBuilder2_2 = VehicleImpl.Builder.newInstance("passenger_bus_2");
		vehicleBuilder2_2.setStartLocation(loc(Coordinate.newInstance(30, 30))).setEndLocation(loc(Coordinate.newInstance(30, 19)));
		vehicleBuilder2_2.setType(vehicleType_solelypassenger);
		VehicleImpl vehicle2_2 = vehicleBuilder2_2.build();
	
		
		/*
		 * build shipments at the required locations, each with a capacity-demand of 1.
		 * 
		 */
		Shipment shipment1 = Shipment.Builder.newInstance("wheelchair_1").addSizeDimension(WHEELCHAIRSPACE_INDEX, 1).setPickupLocation(loc(Coordinate.newInstance(5, 7))).setDeliveryLocation(loc(Coordinate.newInstance(6, 9))).build();
		Shipment shipment2 = Shipment.Builder.newInstance("2").addSizeDimension(PASSENGERSEATS_INDEX, 1).setPickupLocation(loc(Coordinate.newInstance(5, 13))).setDeliveryLocation(loc(Coordinate.newInstance(6, 11))).build();
		
		Shipment shipment3 = Shipment.Builder.newInstance("wheelchair_2").addSizeDimension(WHEELCHAIRSPACE_INDEX, 1).setPickupLocation(loc(Coordinate.newInstance(15, 7))).setDeliveryLocation(loc(Coordinate.newInstance(14, 9))).build();
		Shipment shipment4 = Shipment.Builder.newInstance("4").addSizeDimension(PASSENGERSEATS_INDEX, 1).setPickupLocation(loc(Coordinate.newInstance(15, 13))).setDeliveryLocation(loc(Coordinate.newInstance(14, 11))).build();
		
		Shipment shipment5 = Shipment.Builder.newInstance("wheelchair_3").addSizeDimension(WHEELCHAIRSPACE_INDEX, 1).setPickupLocation(loc(Coordinate.newInstance(25, 27))).setDeliveryLocation(loc(Coordinate.newInstance(26, 29))).build();
		Shipment shipment6 = Shipment.Builder.newInstance("6").addSizeDimension(PASSENGERSEATS_INDEX, 1).setPickupLocation(loc(Coordinate.newInstance(25, 33))).setDeliveryLocation(loc(Coordinate.newInstance(26, 31))).build();
		
		Shipment shipment7 = Shipment.Builder.newInstance("7").addSizeDimension(PASSENGERSEATS_INDEX, 1).setPickupLocation(loc(Coordinate.newInstance(35, 27))).setDeliveryLocation(loc(Coordinate.newInstance(34, 29))).build();
		Shipment shipment8 = Shipment.Builder.newInstance("wheelchair_4").addSizeDimension(WHEELCHAIRSPACE_INDEX, 1).setPickupLocation(loc(Coordinate.newInstance(35, 33))).setDeliveryLocation(loc(Coordinate.newInstance(34, 31))).build();
		
		Shipment shipment9 = Shipment.Builder.newInstance("9").addSizeDimension(PASSENGERSEATS_INDEX, 1).setPickupLocation(loc(Coordinate.newInstance(5, 27))).setDeliveryLocation(loc(Coordinate.newInstance(6, 29))).build();
		Shipment shipment10 = Shipment.Builder.newInstance("wheelchair_5").addSizeDimension(WHEELCHAIRSPACE_INDEX, 1).setPickupLocation(loc(Coordinate.newInstance(5, 33))).setDeliveryLocation(loc(Coordinate.newInstance(6, 31))).build();
		
		Shipment shipment11 = Shipment.Builder.newInstance("11").addSizeDimension(PASSENGERSEATS_INDEX, 1).setPickupLocation(loc(Coordinate.newInstance(15, 27))).setDeliveryLocation(loc(Coordinate.newInstance(14, 29))).build();
		Shipment shipment12 = Shipment.Builder.newInstance("wheelchair_6").addSizeDimension(WHEELCHAIRSPACE_INDEX, 1).setPickupLocation(loc(Coordinate.newInstance(15, 33))).setDeliveryLocation(loc(Coordinate.newInstance(14, 31))).build();
		
		Shipment shipment13 = Shipment.Builder.newInstance("13").addSizeDimension(PASSENGERSEATS_INDEX, 1).setPickupLocation(loc(Coordinate.newInstance(25, 7))).setDeliveryLocation(loc(Coordinate.newInstance(26, 9))).build();
		Shipment shipment14 = Shipment.Builder.newInstance("wheelchair_7").addSizeDimension(WHEELCHAIRSPACE_INDEX, 1).setPickupLocation(loc(Coordinate.newInstance(25, 13))).setDeliveryLocation(loc(Coordinate.newInstance(26, 11))).build();
		
		Shipment shipment15 = Shipment.Builder.newInstance("15").addSizeDimension(PASSENGERSEATS_INDEX, 1).setPickupLocation(loc(Coordinate.newInstance(35, 7))).setDeliveryLocation(loc(Coordinate.newInstance(34, 9))).build();
		Shipment shipment16 = Shipment.Builder.newInstance("wheelchair_8").addSizeDimension(WHEELCHAIRSPACE_INDEX, 1).setPickupLocation(loc(Coordinate.newInstance(35, 13))).setDeliveryLocation(loc(Coordinate.newInstance(34, 11))).build();
		
		Shipment shipment17 = Shipment.Builder.newInstance("17").addSizeDimension(PASSENGERSEATS_INDEX, 1).setPickupLocation(loc(Coordinate.newInstance(5, 14))).setDeliveryLocation(loc(Coordinate.newInstance(6, 16))).build();
		Shipment shipment18 = Shipment.Builder.newInstance("wheelchair_9").addSizeDimension(WHEELCHAIRSPACE_INDEX, 1).setPickupLocation(loc(Coordinate.newInstance(5, 20))).setDeliveryLocation(loc(Coordinate.newInstance(6, 18))).build();
		
		Shipment shipment19 = Shipment.Builder.newInstance("19").addSizeDimension(PASSENGERSEATS_INDEX, 1).setPickupLocation(loc(Coordinate.newInstance(15, 14))).setDeliveryLocation(loc(Coordinate.newInstance(14, 16))).build();
		Shipment shipment20 = Shipment.Builder.newInstance("wheelchair_10").addSizeDimension(WHEELCHAIRSPACE_INDEX, 1).setPickupLocation(loc(Coordinate.newInstance(15, 20))).setDeliveryLocation(loc(Coordinate.newInstance(14, 18))).build();
		
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		vrpBuilder.addVehicle(vehicle1).addVehicle(vehicle2).addVehicle(vehicle1_2).addVehicle(vehicle2_2);
		vrpBuilder.addJob(shipment1).addJob(shipment2).addJob(shipment3).addJob(shipment4);
		vrpBuilder.addJob(shipment5).addJob(shipment6).addJob(shipment7).addJob(shipment8);
		vrpBuilder.addJob(shipment9).addJob(shipment10).addJob(shipment11).addJob(shipment12);
		vrpBuilder.addJob(shipment13).addJob(shipment14).addJob(shipment15).addJob(shipment16);
		vrpBuilder.addJob(shipment17).addJob(shipment18).addJob(shipment19).addJob(shipment20);
		
		//you only have two vehicles
		vrpBuilder.setFleetSize(FleetSize.FINITE);
		
		/*
		 * 
		 * wheelchair-bus can only pickup passenger where x<15
		 */
		HardRouteConstraint wheelchair_bus_passenger_pickup_constraint = new HardRouteConstraint() {
			
			@Override
			public boolean fulfilled(JobInsertionContext insertionContext) {
				Shipment shipment2insert = ((Shipment)insertionContext.getJob()); 
				if(insertionContext.getNewVehicle().getId().equals("wheelchair_bus")){
					if(shipment2insert.getSize().get(PASSENGERSEATS_INDEX)>0){
						if(shipment2insert.getPickupLocation().getCoordinate().getX() > 15. || shipment2insert.getDeliveryLocation().getCoordinate().getX() > 15.){
							return false;
						}
					}
				}
				return true;
			}
		};

		//build the problem
		VehicleRoutingProblem problem = vrpBuilder.build();

		StateManager stateManager = new StateManager(problem);
		
		ConstraintManager constraintManager = new ConstraintManager(problem, stateManager);
		constraintManager.addConstraint(wheelchair_bus_passenger_pickup_constraint);
		
		/*
		 * get a sample algorithm. 
		 * 
		 * Note that you need to make sure to prohibit vehicle-switching by adding the insertion-tag <vehicleSwitchAllowed>false</vehicleSwitchAllowed>.
		 * This way you make sure that no vehicle can take over a route that is employed by another. Allowing this might make sense when dealing with
		 * a heterogeneous fleet and you want to employ a bigger vehicle on a still existing route. However, allowing it makes constraint-checking 
		 * bit more complicated and you cannot just add the above hard-constraint. Latter will be covered in another example.
		 * 
		 */
		VehicleRoutingAlgorithmBuilder algorithmBuilder = new VehicleRoutingAlgorithmBuilder(problem, "input/algorithmConfig_noVehicleSwitch.xml");
		algorithmBuilder.setStateAndConstraintManager(stateManager, constraintManager);
		algorithmBuilder.addCoreConstraints();
		algorithmBuilder.addDefaultCostCalculators();
		
		VehicleRoutingAlgorithm algorithm = algorithmBuilder.build();
		algorithm.setPrematureAlgorithmTermination(new IterationWithoutImprovementTermination(100));

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
//		new VrpXMLWriter(problem, solutions).write("output/shipment-problem-with-solution.xml");
		
		/*
		 * print nRoutes and totalCosts of bestSolution
		 */
		SolutionPrinter.print(problem, bestSolution, SolutionPrinter.Print.VERBOSE);
		
		/*
		 * plot problem without solution
		 */
		Plotter problemPlotter = new Plotter(problem);
		problemPlotter.plotShipments(true);
		problemPlotter.setLabel(jsprit.analysis.toolbox.Plotter.Label.SIZE);
		problemPlotter.plot("output/transportOfDisabledPeopleExample_problem.png", "disabled people tp");

        Plotter solutionPlotter = new Plotter(problem,Solutions.bestOf(solutions));
        solutionPlotter.plotShipments(true);
        solutionPlotter.setLabel(jsprit.analysis.toolbox.Plotter.Label.SIZE);
        solutionPlotter.plot("output/transportOfDisabledPeopleExample_solution.png", "disabled people tp");

		new GraphStreamViewer(problem).labelWith(Label.ID).setRenderDelay(100).setRenderShipments(true).display();
		
		new GraphStreamViewer(problem,Solutions.bestOf(solutions)).labelWith(Label.ACTIVITY).setRenderDelay(100).setRenderShipments(true).display();
		
	}

	private static Location loc(Coordinate coordinate) {
		return Location.Builder.newInstance().setCoordinate(coordinate).build();
	}

}
