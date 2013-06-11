/*******************************************************************************
 * Copyright (c) 2011 Stefan Schroeder.
 * eMail: stefan.schroeder@kit.edu
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package examples;

import readers.SolomonReader;
import algorithms.GreedySchrimpfFactory;
import algorithms.SchrimpfFactory;
import analysis.AlgorithmSearchProgressChartListener;
import analysis.StopWatch;
import basics.VehicleRoutingAlgorithm;
import basics.VehicleRoutingProblem;
import basics.algo.VehicleRoutingAlgorithmListeners.Priority;

public class CompareAlgorithmExample {

	/**
	 * @param args
	 */
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
		
		
		vra_greedy.setPrematureBreak(40);
		vra_greedy.searchSolutions();
		
		
	}

}
