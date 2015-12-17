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

import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.box.Jsprit;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleImpl.Builder;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.reporting.SolutionPrinter;
import jsprit.core.util.ManhattanCosts;
import jsprit.core.util.Solutions;

import java.util.Collection;


public class MultipleTimeWindowExample {


    public static void main(String[] args) {

		/*
         * get a vehicle type-builder and build a type with the typeId "vehicleType" and one capacity dimension, i.e. weight, and capacity dimension value of 2
		 */
        final int WEIGHT_INDEX = 0;
        VehicleTypeImpl.Builder vehicleTypeBuilder = VehicleTypeImpl.Builder.newInstance("vehicleType")
            .addCapacityDimension(WEIGHT_INDEX, 10).setCostPerWaitingTime(1.);
        VehicleType vehicleType = vehicleTypeBuilder.build();

		/*
         * get a vehicle-builder and build a vehicle located at (10,10) with type "vehicleType"
		 */
        Builder vehicleBuilder = Builder.newInstance("vehicle");
        vehicleBuilder.setStartLocation(Location.newInstance(0, 0));
        vehicleBuilder.setType(vehicleType);
        VehicleImpl vehicle = vehicleBuilder.build();

		/*
         * build services at the required locations, each with a capacity-demand of 1.
		 */
        Service service1 = Service.Builder.newInstance("1")
            .addTimeWindow(50,100)
            .addTimeWindow(20,35)
            .addSizeDimension(WEIGHT_INDEX, 1).setLocation(Location.newInstance(10, 0)).build();

        Service service2 = Service.Builder.newInstance("2")
            .addSizeDimension(WEIGHT_INDEX, 1)
//            .setServiceTime(10)
            .setLocation(Location.newInstance(20, 0)).setServiceTime(10).build();

        Service service3 = Service.Builder.newInstance("3")
            .addTimeWindow(5, 10)
            .addTimeWindow(35, 50)
            .addSizeDimension(WEIGHT_INDEX, 1).setLocation(Location.newInstance(30, 0)).build();

        Service service4 = Service.Builder.newInstance("4")
//            .addTimeWindow(5,10)
            .addTimeWindow(20, 40)
            .addTimeWindow(45, 80)
            .addSizeDimension(WEIGHT_INDEX, 1).setLocation(Location.newInstance(40, 0)).build();

        Service service5 = Service.Builder.newInstance("5")
            .addTimeWindow(5,10)
            .addTimeWindow(20, 40)
            .addTimeWindow(60,100)
            .addSizeDimension(WEIGHT_INDEX, 1).setLocation(Location.newInstance(20, 0)).build();


        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.addVehicle(vehicle);
        vrpBuilder.addJob(service1).addJob(service2)
            .addJob(service3)
            .addJob(service4)
            .addJob(service5)
        ;
        vrpBuilder.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);
        vrpBuilder.setRoutingCost(new ManhattanCosts());
        VehicleRoutingProblem problem = vrpBuilder.build();

		/*
         * get the algorithm out-of-the-box.
		 */
        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(problem);

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
//        new Plotter(problem,bestSolution).setLabel(Plotter.Label.ID).plot("output/plot", "mtw");

//        new GraphStreamViewer(problem, bestSolution).labelWith(Label.ID).setRenderDelay(200).display();
    }

}
