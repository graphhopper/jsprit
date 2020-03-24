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

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.state.*;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl.Builder;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Solutions;

import java.util.Collection;


public class SimpleExampleWithoutLocation {


    public static void main(String[] args) {

        VehicleTypeImpl.Builder vehicleTypeBuilder = VehicleTypeImpl.Builder.newInstance("vehicleType").addCapacityDimension(0, 2);
        VehicleType vehicleType = vehicleTypeBuilder.build();

        /*
         * get a vehicle-builder and build a vehicle located at (10,10) with type "vehicleType"
         */
        Builder vehicleBuilder = Builder.newInstance("vehicle");
        vehicleBuilder.setStartLocation(Location.newInstance(10, 10));
        vehicleBuilder.setType(vehicleType);
        VehicleImpl vehicle = vehicleBuilder.build();

        /*
         * build services at the required locations, each with a capacity-demand of 1.
         */
        Service service1 = Service.Builder.newInstance("1").setServiceTime(3600).build();
        Service service2 = Service.Builder.newInstance("2").addSizeDimension(0, 1).setLocation(Location.newInstance(5, 13)).build();

        Service service3 = Service.Builder.newInstance("3").addSizeDimension(0, 1).setLocation(Location.newInstance(15, 7)).build();
        Service service4 = Service.Builder.newInstance("4").addSizeDimension(0, 1).setLocation(Location.newInstance(15, 13)).build();


        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.addVehicle(vehicle);
        vrpBuilder.addJob(service1).addJob(service2).addJob(service3).addJob(service4);

        VehicleRoutingProblem problem = vrpBuilder.build();

        /*
         * get the algorithm out-of-the-box.
         */
        StateManager stateManager = new StateManager(problem);
        stateManager.addStateUpdater(new UpdateEndLocationIfRouteIsOpen());
        stateManager.addStateUpdater(new UpdateActivityNextLocations());
        stateManager.addStateUpdater(new UpdateActivityPrevLocations());
//        stateManager.addStateUpdater(new UpdateActivityLocations());
        stateManager.addStateUpdater(new UpdateActivityTimes(problem.getTransportCosts(), problem.getActivityCosts()));

        ConstraintManager constraintManager = new ConstraintManager(problem, stateManager);

        Jsprit.Builder builder = Jsprit.Builder.newInstance(problem);
        builder.setStateAndConstraintManager(stateManager, constraintManager);
        builder.addCoreStateAndConstraintStuff(false);
        builder.setProperty(Jsprit.Strategy.RADIAL_BEST, "0.0")
            .setProperty(Jsprit.Strategy.RADIAL_REGRET, "0.0")
            .setProperty(Jsprit.Strategy.CLUSTER_BEST, "0.0")
            .setProperty(Jsprit.Strategy.CLUSTER_REGRET, "0.0")
            .setProperty(Jsprit.Strategy.STRING_BEST, "0.0")
            .setProperty(Jsprit.Strategy.STRING_REGRET, "0.0")
            .setProperty(Jsprit.Strategy.RANDOM_REGRET, "0.0")
            .setProperty(Jsprit.Strategy.WORST_REGRET, "0.0")

            .setProperty(Jsprit.Parameter.CONSTRUCTION, Jsprit.Construction.BEST_INSERTION.toString());

        VehicleRoutingAlgorithm algorithm = builder.buildAlgorithm();

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

//		/*
//         * plot
//		 */
//        new Plotter(problem,bestSolution).plot("output/plot.png","simple example");
//
//        /*
//        render problem and solution with GraphStream
//         */
//        new GraphStreamViewer(problem, bestSolution).labelWith(Label.ID).setRenderDelay(200).display();
    }

}
