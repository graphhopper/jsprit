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
import com.graphhopper.jsprit.analysis.toolbox.GraphStreamViewer;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.selector.SelectBest;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.constraint.ServiceDeliveriesFirstConstraint;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.io.problem.VrpXMLReader;
import com.graphhopper.jsprit.util.Examples;

import java.util.Collection;


public class VRPWithBackhaulsExample {

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
        new VrpXMLReader(vrpBuilder).read("input/pickups_and_deliveries_solomon_r101.xml");

		/*
         * Finally, the problem can be built. By default, transportCosts are crowFlyDistances (as usually used for vrp-instances).
		 */
//
        VehicleRoutingProblem vrp = vrpBuilder.build();

//		SolutionPlotter.plotVrpAsPNG(vrp, "output/vrpwbh_solomon_r101.png", "pd_r101");

		/*
         * Define the required vehicle-routing algorithms to solve the above problem.
		 *
		 * The algorithm can be defined and configured in an xml-file.
		 */

        StateManager stateManager = new StateManager(vrp);
        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);
        constraintManager.addConstraint(new ServiceDeliveriesFirstConstraint(), ConstraintManager.Priority.CRITICAL);

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setStateAndConstraintManager(stateManager,constraintManager).buildAlgorithm();
        vra.getAlgorithmListeners().addListener(new AlgorithmSearchProgressChartListener("output/sol_progress.png"));
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
        SolutionPrinter.print(vrp, solution, SolutionPrinter.Print.VERBOSE);

		/*
		 * Plot solution.
		 */
//		Plotter plotter = new Plotter(vrp, solution);
//		plotter.setLabel(Label.SIZE);
//		plotter.setShowFirstActivity(true);
//		plotter.plot("output/vrpwbh_solomon_r101_solution.png","vrpwbh_r101");

        new GraphStreamViewer(vrp, solution).setRenderDelay(100).display();

    }

}
