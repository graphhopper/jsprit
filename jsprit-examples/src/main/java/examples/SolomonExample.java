/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package examples;

import java.util.Collection;

import readers.SolomonReader;
import algorithms.VehicleRoutingAlgorithms;
import algorithms.selectors.SelectBest;
import analysis.AlgorithmSearchProgressChartListener;
import analysis.SolutionPlotter;
import analysis.SolutionPrinter;
import analysis.SolutionPrinter.Print;
import analysis.StopWatch;
import basics.VehicleRoutingAlgorithm;
import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblemSolution;
import basics.algo.VehicleRoutingAlgorithmListeners.Priority;
import basics.io.AlgorithmConfig;
import basics.io.AlgorithmConfigXmlReader;

public class SolomonExample {
	
	public static void main(String[] args) {
		
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
		
		
		/*
		 * Define the required vehicle-routing algorithms to solve the above problem.
		 * 
		 * The algorithm can be defined and configured in an xml-file.
		 */
		AlgorithmConfig algorithmConfig = new AlgorithmConfig();
		new AlgorithmConfigXmlReader(algorithmConfig).read("input/algorithmConfig_solomon.xml");
		VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.createAlgorithm(vrp, algorithmConfig);
		vra.setPrematureBreak(500);
		
		/*
		 * Listen to the search-progress.
		 */
		vra.getAlgorithmListeners().addListener(new AlgorithmSearchProgressChartListener("output/searchProgress.png"));
		
		/*
		 * StopWatch to measure computation time.
		 */
		StopWatch stopWatch = new StopWatch();
		vra.getAlgorithmListeners().addListener(stopWatch,Priority.HIGH);
		
//		vra.setPrematureBreak(200);
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
		 * Print computation time.
		 */
		System.out.println(stopWatch);
		
		/*
		 * Print solution.
		 */
		
		SolutionPrinter.print(solution, Print.VERBOSE);
		
		/*
		 * Plot solution. 
		 */
		SolutionPlotter.plotSolutionAsPNG(vrp, solution, "output/solomonSolution.png","C101");
		
	}

}
