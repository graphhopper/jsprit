/*
 * Licensed to GraphHopper GmbH under one or more contributor
 * license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * GraphHopper GmbH licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.graphhopper.jsprit.examples;

import com.graphhopper.jsprit.analysis.toolbox.GraphStreamViewer;
import com.graphhopper.jsprit.analysis.toolbox.GraphStreamViewer.Label;
import com.graphhopper.jsprit.analysis.toolbox.Plotter;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl.Builder;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.io.problem.VrpXMLWriter;
import com.graphhopper.jsprit.util.Examples;

import java.util.Arrays;
import java.util.Collection;


public class EnRoutePickupAndDeliveryWithMultipleDepotsAndOpenRoutesExample {

    public static void main(String[] args) {
        /*
         * some preparation - create output folder
		 */
        Examples.createOutputFolder();

		/*
         * get a vehicle type-builder and build a type with the typeId "vehicleType" and a capacity of 2
		 */
        VehicleTypeImpl.Builder vehicleTypeBuilder = VehicleTypeImpl.Builder.newInstance("vehicleType").addCapacityDimension(0, 2);
        vehicleTypeBuilder.setCostPerDistance(1.0);
        VehicleType vehicleType = vehicleTypeBuilder.build();

		/*
         * define two vehicles and their start-locations
		 *
		 * the first two do need to return to depot
		 */
        Builder vehicleBuilder1 = VehicleImpl.Builder.newInstance("vehicles@[10,10]");
        vehicleBuilder1.setStartLocation(loc(Coordinate.newInstance(10, 10))).setReturnToDepot(false);
        vehicleBuilder1.setType(vehicleType);
        VehicleImpl vehicle1 = vehicleBuilder1.build();

        Builder vehicleBuilder2 = VehicleImpl.Builder.newInstance("vehicles@[30,30]");
        vehicleBuilder2.setStartLocation(loc(Coordinate.newInstance(30, 30))).setReturnToDepot(false);
        vehicleBuilder2.setType(vehicleType);
        VehicleImpl vehicle2 = vehicleBuilder2.build();

        Builder vehicleBuilder3 = VehicleImpl.Builder.newInstance("vehicles@[10,30]");
        vehicleBuilder3.setStartLocation(loc(Coordinate.newInstance(10, 30)));
        vehicleBuilder3.setType(vehicleType);
        VehicleImpl vehicle3 = vehicleBuilder3.build();

        Builder vehicleBuilder4 = VehicleImpl.Builder.newInstance("vehicles@[30,10]");
        vehicleBuilder4.setStartLocation(loc(Coordinate.newInstance(30, 10)));
        vehicleBuilder4.setType(vehicleType);
        VehicleImpl vehicle4 = vehicleBuilder4.build();

		/*
         * build shipments at the required locations, each with a capacity-demand of 1.

		 */

        Shipment shipment1 = Shipment.Builder.newInstance("1").addSizeDimension(0, 1).setPickupLocation(loc(Coordinate.newInstance(5, 7))).setDeliveryLocation(loc(Coordinate.newInstance(6, 9))).build();
        Shipment shipment2 = Shipment.Builder.newInstance("2").addSizeDimension(0, 1).setPickupLocation(loc(Coordinate.newInstance(5, 13))).setDeliveryLocation(loc(Coordinate.newInstance(6, 11))).build();

        Shipment shipment3 = Shipment.Builder.newInstance("3").addSizeDimension(0, 1).setPickupLocation(loc(Coordinate.newInstance(15, 7))).setDeliveryLocation(loc(Coordinate.newInstance(14, 9))).build();
        Shipment shipment4 = Shipment.Builder.newInstance("4").addSizeDimension(0, 1).setPickupLocation(loc(Coordinate.newInstance(15, 13))).setDeliveryLocation(loc(Coordinate.newInstance(14, 11))).build();

        Shipment shipment5 = Shipment.Builder.newInstance("5").addSizeDimension(0, 1).setPickupLocation(loc(Coordinate.newInstance(25, 27))).setDeliveryLocation(loc(Coordinate.newInstance(26, 29))).build();
        Shipment shipment6 = Shipment.Builder.newInstance("6").addSizeDimension(0, 1).setPickupLocation(loc(Coordinate.newInstance(25, 33))).setDeliveryLocation(loc(Coordinate.newInstance(26, 31))).build();

        Shipment shipment7 = Shipment.Builder.newInstance("7").addSizeDimension(0, 1).setPickupLocation(loc(Coordinate.newInstance(35, 27))).setDeliveryLocation(loc(Coordinate.newInstance(34, 29))).build();
        Shipment shipment8 = Shipment.Builder.newInstance("8").addSizeDimension(0, 1).setPickupLocation(loc(Coordinate.newInstance(35, 33))).setDeliveryLocation(loc(Coordinate.newInstance(34, 31))).build();

        Shipment shipment9 = Shipment.Builder.newInstance("9").addSizeDimension(0, 1).setPickupLocation(loc(Coordinate.newInstance(5, 27))).setDeliveryLocation(loc(Coordinate.newInstance(6, 29))).build();
        Shipment shipment10 = Shipment.Builder.newInstance("10").addSizeDimension(0, 1).setPickupLocation(loc(Coordinate.newInstance(5, 33))).setDeliveryLocation(loc(Coordinate.newInstance(6, 31))).build();

        Shipment shipment11 = Shipment.Builder.newInstance("11").addSizeDimension(0, 1).setPickupLocation(loc(Coordinate.newInstance(15, 27))).setDeliveryLocation(loc(Coordinate.newInstance(14, 29))).build();
        Shipment shipment12 = Shipment.Builder.newInstance("12").addSizeDimension(0, 1).setPickupLocation(loc(Coordinate.newInstance(15, 33))).setDeliveryLocation(loc(Coordinate.newInstance(14, 31))).build();

        Shipment shipment13 = Shipment.Builder.newInstance("13").addSizeDimension(0, 1).setPickupLocation(loc(Coordinate.newInstance(25, 7))).setDeliveryLocation(loc(Coordinate.newInstance(26, 9))).build();
        Shipment shipment14 = Shipment.Builder.newInstance("14").addSizeDimension(0, 1).setPickupLocation(loc(Coordinate.newInstance(25, 13))).setDeliveryLocation(loc(Coordinate.newInstance(26, 11))).build();

        Shipment shipment15 = Shipment.Builder.newInstance("15").addSizeDimension(0, 1).setPickupLocation(loc(Coordinate.newInstance(35, 7))).setDeliveryLocation(loc(Coordinate.newInstance(34, 9))).build();
        Shipment shipment16 = Shipment.Builder.newInstance("16").addSizeDimension(0, 1).setPickupLocation(loc(Coordinate.newInstance(35, 13))).setDeliveryLocation(loc(Coordinate.newInstance(34, 11))).build();


        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.addVehicle(vehicle1).addVehicle(vehicle2).addVehicle(vehicle3).addVehicle(vehicle4);
        vrpBuilder.addJob(shipment1).addJob(shipment2).addJob(shipment3).addJob(shipment4);
        vrpBuilder.addJob(shipment5).addJob(shipment6).addJob(shipment7).addJob(shipment8);
        vrpBuilder.addJob(shipment9).addJob(shipment10).addJob(shipment11).addJob(shipment12);
        vrpBuilder.addJob(shipment13).addJob(shipment14).addJob(shipment15).addJob(shipment16);

        vrpBuilder.setFleetSize(FleetSize.FINITE);
        VehicleRoutingProblem problem = vrpBuilder.build();

		/*
         * get the algorithm out-of-the-box.
		 */
        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(problem);
//		algorithm.setMaxIterations(30000);
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
        SolutionPrinter.print(bestSolution);

		/*
		 * plot problem without solution
		 */
        Plotter problemPlotter = new Plotter(problem);
        problemPlotter.plotShipments(true);
        problemPlotter.plot("output/enRoutePickupAndDeliveryWithMultipleLocationsExample_problem.png", "en-route pickup and delivery");

		/*
		 * plot problem with solution
		 */
        Plotter solutionPlotter = new Plotter(problem, Arrays.asList(Solutions.bestOf(solutions).getRoutes().iterator().next()));
        solutionPlotter.plotShipments(true);
        solutionPlotter.plot("output/enRoutePickupAndDeliveryWithMultipleLocationsExample_solution.png", "en-route pickup and delivery");

        new GraphStreamViewer(problem, Solutions.bestOf(solutions)).labelWith(Label.ACTIVITY).setRenderDelay(100).setRenderShipments(true).display();

    }

    private static Location loc(Coordinate coordinate) {
        return Location.Builder.newInstance().setCoordinate(coordinate).build();
    }

}

