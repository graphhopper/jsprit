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
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Break;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.activity.TimeWindow;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleImpl.Builder;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.reporting.SolutionPrinter;
import jsprit.core.util.Solutions;

import java.util.Collection;


public class BreakExample {


    public static void main(String[] args) {


		/*
         * get a vehicle type-builder and build a type with the typeId "vehicleType" and one capacity dimension, i.e. weight, and capacity dimension value of 2
		 */
        final int WEIGHT_INDEX = 0;
        VehicleTypeImpl.Builder vehicleTypeBuilder = VehicleTypeImpl.Builder.newInstance("vehicleType")
            .addCapacityDimension(WEIGHT_INDEX, 2).setCostPerWaitingTime(1.0);
        VehicleType vehicleType = vehicleTypeBuilder.build();

		/*
		 * get a vehicle-builder and build a vehicle located at (10,10) with type "vehicleType"
		 */
        Builder vehicleBuilder = Builder.newInstance("v1");
        vehicleBuilder.setStartLocation(Location.newInstance(10, 10));
        Break myFirstBreak = Break.Builder.newInstance("myFirstBreak")
            .setTimeWindow(TimeWindow.newInstance(10, 15)).setServiceTime(100).build();
        vehicleBuilder.setBreak(myFirstBreak);
        vehicleBuilder.setType(vehicleType);
        VehicleImpl vehicle = vehicleBuilder.build();

        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(Location.newInstance(0, 10)).setType(vehicleType)
            .setBreak((Break) Break.Builder.newInstance("mySecondBreak").setTimeWindow(TimeWindow.newInstance(5, 10)).setServiceTime(10).build()).build();
		/*
		 * build services at the required locations, each with a capacity-demand of 1.
		 */
        Service service1 = Service.Builder.newInstance("1").addSizeDimension(WEIGHT_INDEX, 1).setLocation(Location.newInstance(5, 7)).build();
        Service service2 = Service.Builder.newInstance("2").addSizeDimension(WEIGHT_INDEX, 1).setLocation(Location.newInstance(5, 13)).build();

        Service service3 = Service.Builder.newInstance("3").addSizeDimension(WEIGHT_INDEX, 1).setLocation(Location.newInstance(15, 7)).build();
        Service service4 = Service.Builder.newInstance("4").addSizeDimension(WEIGHT_INDEX, 1).setLocation(Location.newInstance(15, 13)).build();


        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.addVehicle(vehicle);
        vrpBuilder.addJob(service1).addJob(service2).addJob(service3).addJob(service4).addVehicle(v2);
        vrpBuilder.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);
        VehicleRoutingProblem problem = vrpBuilder.build();

		/*
		 * get the algorithm out-of-the-box.
		 */
        VehicleRoutingAlgorithm algorithm = Jsprit.Builder.newInstance(problem)
            .setProperty(Jsprit.Strategy.CLUSTER_REGRET, "0.")
            .setProperty(Jsprit.Strategy.CLUSTER_BEST, "0.").buildAlgorithm();
		/*
		 * and search a solution
		 */
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();

		/*
		 * get the best
		 */
        VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);

        SolutionPrinter.print(problem, bestSolution, SolutionPrinter.Print.VERBOSE);

		/*
		 * plot
		 */
        new Plotter(problem, bestSolution).plot("output/plot", "breaks");


    }

}
