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

import jsprit.analysis.toolbox.AlgorithmSearchProgressChartListener;
import jsprit.analysis.toolbox.StopWatch;
import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.box.GreedySchrimpfFactory;
import jsprit.core.algorithm.box.SchrimpfFactory;
import jsprit.core.algorithm.listener.VehicleRoutingAlgorithmListeners.Priority;
import jsprit.core.algorithm.termination.IterationWithoutImprovementTermination;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.instance.reader.SolomonReader;
import jsprit.util.Examples;


public class CompareAlgorithmExample {


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
		
		/*
		 * Get schrimpf with threshold accepting
		 * Note that Priority.LOW is a way to priorize AlgorithmListeners
		 */
		VehicleRoutingAlgorithm vra_withThreshold = new SchrimpfFactory().createAlgorithm(vrp);
		vra_withThreshold.getAlgorithmListeners().addListener(new AlgorithmSearchProgressChartListener("output/schrimpfThreshold_progress.png"), Priority.LOW);
		vra_withThreshold.getAlgorithmListeners().addListener(new StopWatch(), Priority.HIGH);
		/*
		 * Get greedy schrimpf
		 */
		VehicleRoutingAlgorithm vra_greedy = new GreedySchrimpfFactory().createAlgorithm(vrp);
		vra_greedy.getAlgorithmListeners().addListener(new AlgorithmSearchProgressChartListener("output/schrimpfGreedy_progress.png"), Priority.LOW);
		vra_greedy.getAlgorithmListeners().addListener(new StopWatch(), Priority.HIGH);

		/*
		 * run both
		 */
		vra_withThreshold.searchSolutions();
		
		vra_greedy.searchSolutions();	
		vra_greedy.setPrematureAlgorithmTermination(new IterationWithoutImprovementTermination(40));
		vra_greedy.searchSolutions();
	
		
	}

}
