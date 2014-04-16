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
package jsprit.examples;

import java.util.Collection;

import jsprit.analysis.toolbox.ComputationalLaboratory;
import jsprit.analysis.toolbox.ComputationalLaboratory.CalculationListener;
import jsprit.analysis.toolbox.XYLineChartBuilder;
import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.VehicleRoutingAlgorithmFactory;
import jsprit.core.algorithm.io.VehicleRoutingAlgorithms;
import jsprit.core.algorithm.listener.IterationStartsListener;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.util.BenchmarkInstance;
import jsprit.core.util.Solutions;
import jsprit.instance.reader.SolomonReader;
import jsprit.util.Examples;

/**
 * Based on Solomon's R101 instance
 * 
 * @author schroeder
 *
 */
public class ComputationalExperiments_schrimpf_vs_greedy {
	
	
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
		new SolomonReader(vrpBuilder).read("input/R101.txt");
		
		/*
		 * Finally, the problem can be built. By default, transportCosts are crowFlyDistances (as usually used for vrp-instances).
		 */
		VehicleRoutingProblem vrp = vrpBuilder.build();
		
		/*
		 * Create ComputationalLaboratory
		 */
		ComputationalLaboratory computationalLab = new ComputationalLaboratory();
		/*
		 * add benchmarking instance
		 */
		computationalLab.addInstance("SolomonR101", vrp);
		/*
		 * add algorithms through factories
		 * 
		 * 
		 * Define 2 algorithms 
		 * - algorithmsConfigWithSchrimpfAcceptance (4000 iterations)
		 * - algorithmsConfigWithGreedyAcceptance (4000 iterations)
		 * 
		 */
		computationalLab.addAlgorithmFactory("schrimpfAcceptance", new VehicleRoutingAlgorithmFactory() {
			
			@Override
			public VehicleRoutingAlgorithm createAlgorithm(VehicleRoutingProblem vrp) {
				VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "input/algorithmConfigWithSchrimpfAcceptance.xml");
				vra.setNuOfIterations(4000);
				return vra;
			}
		});
		computationalLab.addAlgorithmFactory("greedyAcceptance", new VehicleRoutingAlgorithmFactory() {
			
			@Override
			public VehicleRoutingAlgorithm createAlgorithm(VehicleRoutingProblem vrp) {
				VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "input/algorithmConfig_solomon.xml");
				vra.setNuOfIterations(4000);
				return vra;
			}
		});
				
		/*
		 * plot search progress of different algorithms
		 */
		final XYLineChartBuilder chartBuilder = XYLineChartBuilder.newInstance("schrimpf vs greedy", "iterations", "costs");
		computationalLab.addListener(new CalculationListener() {
			
			@Override
			public void calculationStarts(BenchmarkInstance p, final String algorithmName,VehicleRoutingAlgorithm algorithm, int run) {
				algorithm.addListener(new IterationStartsListener() {
					
					@Override
					public void informIterationStarts(int i, VehicleRoutingProblem problem,Collection<VehicleRoutingProblemSolution> solutions) {
						chartBuilder.addData(algorithmName, i, Solutions.bestOf(solutions).getCost());
					}
					
				});
				
			}
			
			@Override
			public void calculationEnds(BenchmarkInstance p, String algorithmName,VehicleRoutingAlgorithm algorithm, int run,Collection<VehicleRoutingProblemSolution> solutions) {}
			
		});
		
		computationalLab.setThreads(2);
		/*
		 * run the experiments
		 */
		computationalLab.run();
		
		/*
		 * plot the lineChart
		 */
		XYLineChartBuilder.saveChartAsPNG(chartBuilder.build(), "output/computationalStudies_schrimpf_vs_greedy.png");
		
	}

}
