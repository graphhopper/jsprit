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

import com.graphhopper.jsprit.analysis.toolbox.AlgorithmSearchProgressChartListener;
import com.graphhopper.jsprit.analysis.toolbox.Plotter;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.selector.SelectBest;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.analysis.SolutionAnalyser;
import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.constraint.ServiceDeliveriesFirstConstraint;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.io.problem.VrpXMLReader;
import com.graphhopper.jsprit.util.Examples;

import java.util.Collection;


public class VRPWithBackhaulsExample2 {

    public static void main(String[] args) {

		/*
         * some preparation - create output folder
		 */
        Examples.createOutputFolder();

		/*
         * Build the problem.
		 *
		 * But define a problem-builder first.
		 */
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();

		/*
         * A solomonReader reads solomon-instance files, and stores the required information in the builder.
		 */
        new VrpXMLReader(vrpBuilder).read("input/pd_christophides_vrpnc1_vcap50.xml");


		/*
         * Finally, the problem can be built. By default, transportCosts are crowFlyDistances (as usually used for vrp-instances).
		 */
        final VehicleRoutingProblem vrp = vrpBuilder.build();

//		new Plotter(vrp).plot("output/vrpwbh_christophides_vrpnc1.png", "pd_vrpnc1");


		/*
         * Define the required vehicle-routing algorithms to solve the above problem.
		 *
		 * The algorithm can be defined and configured in an xml-file.
		 */
//		VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "input/algorithmConfig_solomon.xml");

//        VehicleRoutingAlgorithmBuilder vraBuilder = new VehicleRoutingAlgorithmBuilder(vrp,"input/algorithmConfig_solomon.xml");
//        vraBuilder.addDefaultCostCalculators();
//        vraBuilder.addCoreConstraints();

        StateManager stateManager = new StateManager(vrp);
        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);
        constraintManager.addConstraint(new ServiceDeliveriesFirstConstraint(), ConstraintManager.Priority.CRITICAL);

//        vraBuilder.setStateAndConstraintManager(stateManager,constraintManager);

//        VehicleRoutingAlgorithm vra = vraBuilder.build();


        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
            .setStateAndConstraintManager(stateManager, constraintManager)
            .setProperty(Jsprit.Parameter.FIXED_COST_PARAM.toString(), "0.")
            .buildAlgorithm();
        vra.setMaxIterations(2000);
        vra.addListener(new AlgorithmSearchProgressChartListener("output/search"));



		/*
         * Solve the problem.
		 *
		 *
		 */
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

		/*
         * Retrieve best solution.
		 */
        VehicleRoutingProblemSolution solution = new SelectBest().selectSolution(solutions);

		/*
         * print solution
		 */
        SolutionPrinter.print(solution);

		/*
		 * Plot solution.
		 */
//		SolutionPlotter.plotSolutionAsPNG(vrp, solution, "output/pd_solomon_r101_solution.png","pd_r101");
        Plotter plotter = new Plotter(vrp, solution);
//		plotter.setLabel(Plotter.Label.SIZE);
        plotter.plot("output/vrpwbh_christophides_vrpnc1_solution.png", "vrpwbh_vrpnc1");

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());

        for (VehicleRoute route : solution.getRoutes()) {
            System.out.println("------");
            System.out.println("vehicleId: " + route.getVehicle().getId());
            System.out.println("vehicleCapacity: " + route.getVehicle().getType().getCapacityDimensions() + " maxLoad: " + analyser.getMaxLoad(route));
            System.out.println("totalDistance: " + analyser.getDistance(route));
            System.out.println("waitingTime: " + analyser.getWaitingTime(route));
            System.out.println("load@beginning: " + analyser.getLoadAtBeginning(route));
            System.out.println("load@end: " + analyser.getLoadAtEnd(route));
            System.out.println("operationTime: " + analyser.getOperationTime(route));
            System.out.println("serviceTime: " + analyser.getServiceTime(route));
            System.out.println("transportTime: " + analyser.getTransportTime(route));
            System.out.println("transportCosts: " + analyser.getVariableTransportCosts(route));
            System.out.println("fixedCosts: " + analyser.getFixedCosts(route));
            System.out.println("capViolationOnRoute: " + analyser.getCapacityViolation(route));
            System.out.println("capViolation@beginning: " + analyser.getCapacityViolationAtBeginning(route));
            System.out.println("capViolation@end: " + analyser.getCapacityViolationAtEnd(route));
            System.out.println("timeWindowViolationOnRoute: " + analyser.getTimeWindowViolation(route));
            System.out.println("skillConstraintViolatedOnRoute: " + analyser.hasSkillConstraintViolation(route));

            System.out.println("dist@" + route.getStart().getLocation().getId() + ": " + analyser.getDistanceAtActivity(route.getStart(), route));
            System.out.println("timeWindowViolation@" + route.getStart().getLocation().getId() + ": " + analyser.getTimeWindowViolationAtActivity(route.getStart(), route));
            for (TourActivity act : route.getActivities()) {
                System.out.println("--");
                System.out.println("actType: " + act.getName() + " demand: " + act.getSize());
                System.out.println("dist@" + act.getLocation().getId() + ": " + analyser.getDistanceAtActivity(act, route));
                System.out.println("load(before)@" + act.getLocation().getId() + ": " + analyser.getLoadJustBeforeActivity(act, route));
                System.out.println("load(after)@" + act.getLocation().getId() + ": " + analyser.getLoadRightAfterActivity(act, route));
                System.out.println("transportCosts@" + act.getLocation().getId() + ": " + analyser.getVariableTransportCostsAtActivity(act, route));
                System.out.println("capViolation(after)@" + act.getLocation().getId() + ": " + analyser.getCapacityViolationAfterActivity(act, route));
                System.out.println("timeWindowViolation@" + act.getLocation().getId() + ": " + analyser.getTimeWindowViolationAtActivity(act, route));
                System.out.println("skillConstraintViolated@" + act.getLocation().getId() + ": " + analyser.hasSkillConstraintViolationAtActivity(act, route));
            }
            System.out.println("--");
            System.out.println("dist@" + route.getEnd().getLocation().getId() + ": " + analyser.getDistanceAtActivity(route.getEnd(), route));
            System.out.println("timeWindowViolation@" + route.getEnd().getLocation().getId() + ": " + analyser.getTimeWindowViolationAtActivity(route.getEnd(), route));
        }

        System.out.println("-----");
        System.out.println("aggreate solution stats");
        System.out.println("total freight moved: " + Capacity.addup(analyser.getLoadAtBeginning(), analyser.getLoadPickedUp()));
        System.out.println("total no. picks at beginning: " + analyser.getNumberOfPickupsAtBeginning());
        System.out.println("total no. picks on routes: " + analyser.getNumberOfPickups());
        System.out.println("total picked load at beginnnig: " + analyser.getLoadAtBeginning());
        System.out.println("total picked load on routes: " + analyser.getLoadPickedUp());
        System.out.println("total no. deliveries at end: " + analyser.getNumberOfDeliveriesAtEnd());
        System.out.println("total no. deliveries on routes: " + analyser.getNumberOfDeliveries());
        System.out.println("total delivered load at end: " + analyser.getLoadAtEnd());
        System.out.println("total delivered load on routes: " + analyser.getLoadDelivered());
        System.out.println("total tp_distance: " + analyser.getDistance());
        System.out.println("total tp_time: " + analyser.getTransportTime());
        System.out.println("total waiting_time: " + analyser.getWaitingTime());
        System.out.println("total service_time: " + analyser.getServiceTime());
        System.out.println("total operation_time: " + analyser.getOperationTime());
        System.out.println("total twViolation: " + analyser.getTimeWindowViolation());
        System.out.println("total capViolation: " + analyser.getCapacityViolation());
        System.out.println("total fixedCosts: " + analyser.getFixedCosts());
        System.out.println("total variableCosts: " + analyser.getVariableTransportCosts());
        System.out.println("total costs: " + analyser.getTotalCosts());

    }

}
