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

import com.graphhopper.jsprit.analysis.toolbox.AlgorithmEventsRecorder;
import com.graphhopper.jsprit.analysis.toolbox.AlgorithmEventsViewer;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.selector.SelectBest;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.io.problem.VrpXMLReader;
import com.graphhopper.jsprit.util.Examples;

import java.util.Collection;


public class SolomonOpenExample {

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
        new VrpXMLReader(vrpBuilder).read("input/deliveries_solomon_open_c101.xml");

		/*
         * Finally, the problem can be built. By default, transportCosts are crowFlyDistances (as usually used for vrp-instances).
		 */
        VehicleRoutingProblem vrp = vrpBuilder.build();

//        new Plotter(vrp).plot("output/solomon_C101_open.png", "C101");


        AlgorithmEventsRecorder eventsRecorder = new AlgorithmEventsRecorder(vrp, "output/events.dgs.gz");
        eventsRecorder.setRecordingRange(0, 50);

		/*
         * Define the required vehicle-routing algorithms to solve the above problem.
		 *
		 * The algorithm can be defined and configured in an xml-file.
		 */
//		VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(vrp);
//		VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "input/algorithmConfig_fix.xml");
        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setProperty(Jsprit.Parameter.THREADS, "4")
        .setProperty(Jsprit.Parameter.FAST_REGRET, "true")
            .setProperty(Jsprit.Parameter.CONSTRUCTION, Jsprit.Construction.BEST_INSERTION.toString()).buildAlgorithm();
//		vra.setPrematureBreak(100);
//		vra.getAlgorithmListeners().addListener(new AlgorithmSearchProgressChartListener("output/sol_progress.png"));
        vra.addListener(eventsRecorder);
        vra.setMaxIterations(200);
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
//		SolutionPlotter.plotSolutionAsPNG(vrp, solution, "output/solomon_C101_open_solution.png","C101");


//        new GraphStreamViewer(vrp, solution).setRenderDelay(100).labelWith(Label.ID).display();


        AlgorithmEventsViewer viewer = new AlgorithmEventsViewer();
        viewer.setRuinDelay(8);
        viewer.setRecreationDelay(2);
        viewer.display("output/events.dgs.gz");

    }

}
