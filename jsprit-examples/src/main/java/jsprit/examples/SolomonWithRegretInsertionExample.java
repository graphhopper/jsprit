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

import jsprit.analysis.toolbox.AlgorithmEventsRecorder;
import jsprit.analysis.toolbox.AlgorithmEventsViewer;
import jsprit.analysis.toolbox.GraphStreamViewer;
import jsprit.analysis.toolbox.GraphStreamViewer.Label;
import jsprit.analysis.toolbox.Plotter;
import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.io.VehicleRoutingAlgorithms;
import jsprit.core.algorithm.selector.SelectBest;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.reporting.SolutionPrinter;
import jsprit.instance.reader.SolomonReader;
import jsprit.util.Examples;

import java.util.Collection;


public class SolomonWithRegretInsertionExample {
	
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
		new SolomonReader(vrpBuilder).read("input/C101_solomon.txt");


		/*
		 * Finally, the problem can be built. By default, transportCosts are crowFlyDistances (as usually used for vrp-instances).
		 */
		VehicleRoutingProblem vrp = vrpBuilder.build();

		new Plotter(vrp).plot("output/solomon_C101.png", "C101");
		
		/*
		 * Define the required vehicle-routing algorithms to solve the above problem.
		 * 
		 * The algorithm can be defined and configured in an xml-file.
		 */
		VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "input/algorithmConfig_greedyWithRegret.xml");
        vra.setMaxIterations(2);

        AlgorithmEventsRecorder eventsRecorder = new AlgorithmEventsRecorder(vrp,"output/events.dgs.gz");
        eventsRecorder.setRecordingRange(0,50);
        vra.addListener(eventsRecorder);

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
		Plotter plotter = new Plotter(vrp,solution);
//		plotter.setBoundingBox(30, 0, 50, 20);
		plotter.plot("output/solomon_C101_solution.png", "C101");

		new GraphStreamViewer(vrp,solution).labelWith(Label.ID).setRenderDelay(100).display();

        AlgorithmEventsViewer viewer = new AlgorithmEventsViewer();
        viewer.setRuinDelay(16);
        viewer.setRecreationDelay(8);
        viewer.display("output/events.dgs.gz");
		
	}

}
