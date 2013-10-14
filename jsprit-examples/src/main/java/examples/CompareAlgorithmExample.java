/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
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
package examples;

import java.io.File;

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
		 * some preparation - create output folder
		 */
		File dir = new File("output");
		// if the directory does not exist, create it
		if (!dir.exists()){
			System.out.println("creating directory ./output");
			boolean result = dir.mkdir();  
			if(result) System.out.println("./output created");  
		}
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
