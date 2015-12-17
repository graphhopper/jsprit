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
import jsprit.core.algorithm.box.Jsprit;
import jsprit.core.analysis.SolutionAnalyser;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.cost.TransportDistance;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleImpl.Builder;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.reporting.SolutionPrinter;
import jsprit.core.util.RandomNumberGeneration;
import jsprit.core.util.Solutions;

import java.util.Collection;
import java.util.Random;


public class MultipleTimeWindowExample2 {


    public static void main(String[] args) {

		/*
         * get a vehicle type-builder and build a type with the typeId "vehicleType" and one capacity dimension, i.e. weight, and capacity dimension value of 2
		 */
        VehicleTypeImpl.Builder vehicleTypeBuilder = VehicleTypeImpl.Builder.newInstance("vehicleType")
            .addCapacityDimension(0, 60)
            .setCostPerWaitingTime(0.8)
            ;
        VehicleType vehicleType = vehicleTypeBuilder.build();

		/*
         * get a vehicle-builder and build a vehicle located at (10,10) with type "vehicleType"
		 */
        Builder vehicleBuilder = Builder.newInstance("vehicle");
        vehicleBuilder.setStartLocation(Location.newInstance(0, 0));
        vehicleBuilder.setType(vehicleType);
        vehicleBuilder.setLatestArrival(800);
        VehicleImpl vehicle = vehicleBuilder.build();

//        Builder vehicleBuilder2 = Builder.newInstance("vehicle2");
//        vehicleBuilder2.setStartLocation(Location.newInstance(0, 0));
//        vehicleBuilder2.setType(vehicleType);
//        vehicleBuilder2.setEarliestStart(250).setLatestArrival(450);
//        VehicleImpl vehicle2 = vehicleBuilder2.build();
//
//
//        Builder vehicleBuilder3 = Builder.newInstance("vehicle3");
//        vehicleBuilder3.setStartLocation(Location.newInstance(0, 0));
//        vehicleBuilder3.setType(vehicleType);
//        vehicleBuilder3.setEarliestStart(380).setLatestArrival(600);
//        VehicleImpl vehicle3 = vehicleBuilder3.build();

		/*
         * build services at the required locations, each with a capacity-demand of 1.
		 */
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.addVehicle(vehicle);
//            .addVehicle(vehicle2).addVehicle(vehicle3);
        vrpBuilder.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);


        Random random = RandomNumberGeneration.newInstance();
        for(int i=0;i<40;i++){
            Service service = Service.Builder.newInstance("" + (i + 1))
                .addTimeWindow(random.nextInt(50), 200)
                .addTimeWindow(220 + random.nextInt(50), 350)
                .addTimeWindow(400 + random.nextInt(50), 550)
//                .addSizeDimension(0, 1)
                .setServiceTime(1)
                .setLocation(Location.newInstance(random.nextInt(50), random.nextInt(50))).build();
            vrpBuilder.addJob(service);
        }

        for(int i=0;i<12;i++){
            Service service = Service.Builder.newInstance(""+(i+51))
//                .addTimeWindow(0, 80)
////                .addTimeWindow(120, 200)
//                .addTimeWindow(250,500)
//                .addSizeDimension(0, 1)
                .setServiceTime(2)
                .setLocation(Location.newInstance(50 + random.nextInt(20), 20 + random.nextInt(25))).build();
            vrpBuilder.addJob(service);
        }

        Service service = Service.Builder.newInstance("100")
            .addTimeWindow(50, 80)
            .setServiceTime(10)
            .setLocation(Location.newInstance(40, 1)).build();
        vrpBuilder.addJob(service);

        final VehicleRoutingProblem problem = vrpBuilder.build();

        VehicleRoutingAlgorithm algorithm = Jsprit.Builder.newInstance(problem).buildAlgorithm();

		/*
         * and search a solution
		 */
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();

		/*
         * get the best
		 */
        VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);

//        new VrpXMLWriter(problem, solutions).write("output/problem-with-solution.xml");

        SolutionPrinter.print(problem, bestSolution, SolutionPrinter.Print.VERBOSE);

		/*
         * plot
		 */
        new Plotter(problem,bestSolution).setLabel(Plotter.Label.ID).plot("output/plot", "mtw");

        SolutionAnalyser a = new SolutionAnalyser(problem, bestSolution, new TransportDistance() {
            @Override
            public double getDistance(Location from, Location to) {
                return problem.getTransportCosts().getTransportTime(from,to,0.,null,null);
            }
        });

        System.out.println("distance: " + a.getDistance());
        System.out.println("ttime: " + a.getTransportTime());
        System.out.println("completion: " + a.getOperationTime());
        System.out.println("waiting: " + a.getWaitingTime());

//        new GraphStreamViewer(problem, bestSolution).labelWith(Label.ID).setRenderDelay(200).display();
    }

}
