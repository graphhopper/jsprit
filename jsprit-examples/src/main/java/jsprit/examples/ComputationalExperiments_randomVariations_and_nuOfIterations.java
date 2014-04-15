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
import jsprit.analysis.toolbox.ComputationalLaboratory.DataCollector;
import jsprit.analysis.toolbox.XYLineChartBuilder;
import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.VehicleRoutingAlgorithmFactory;
import jsprit.core.algorithm.io.VehicleRoutingAlgorithms;
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
public class ComputationalExperiments_randomVariations_and_nuOfIterations {
	
	
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
		 * 
		 */
		computationalLab.addAlgorithmFactory("schrimpfAcceptance_1", getAlgorithmFactory(1));
		computationalLab.addAlgorithmFactory("schrimpfAcceptance_100", getAlgorithmFactory(100));
		computationalLab.addAlgorithmFactory("schrimpfAcceptance_500", getAlgorithmFactory(500));
		computationalLab.addAlgorithmFactory("schrimpfAcceptance_1000", getAlgorithmFactory(1000));
		computationalLab.addAlgorithmFactory("schrimpfAcceptance_2000", getAlgorithmFactory(2000));
		computationalLab.addAlgorithmFactory("schrimpfAcceptance_4000", getAlgorithmFactory(4000));
		computationalLab.addAlgorithmFactory("schrimpfAcceptance_8000", getAlgorithmFactory(8000));
		computationalLab.addAlgorithmFactory("schrimpfAcceptance_12000", getAlgorithmFactory(12000));
		
		
		/*
		 * run schrimpfAcceptance 5 times (and thus each with another seed of random number generator)
		 */
		computationalLab.setNuOfRuns(5);
				
		/*
		 * plot search progress of different algorithms
		 */
		final DataCollector dataCollector = new DataCollector();
		computationalLab.addListener(new CalculationListener() {
			
			@Override
			public void calculationStarts(BenchmarkInstance p, final String algorithmName,VehicleRoutingAlgorithm algorithm, final int run) {}
			
			@Override
			public void calculationEnds(BenchmarkInstance p, String algorithmName,VehicleRoutingAlgorithm algorithm, int run,Collection<VehicleRoutingProblemSolution> solutions) {
				dataCollector.addDate("R101", algorithmName, run, "costs", Solutions.bestOf(solutions).getCost());
			}
			
		});
		
		computationalLab.setThreads(2);
		/*
		 * run the experiments
		 */
		computationalLab.run();
		
		/*
		 * plot min, avg and max
		 */
		XYLineChartBuilder chartBuilder = XYLineChartBuilder.newInstance("variations with iterations", "iterations", "costs");
		for(String algorithmName : computationalLab.getAlgorithmNames()){
			String[] nameTokens = algorithmName.split("_");
			int iteration = Integer.parseInt(nameTokens[1]);
			chartBuilder.addData("min", iteration, min(dataCollector.getData("R101", algorithmName, "costs")));
			chartBuilder.addData("max", iteration, max(dataCollector.getData("R101", algorithmName, "costs")));
			chartBuilder.addData("avg", iteration, avg(dataCollector.getData("R101", algorithmName, "costs")));
		}
		
		XYLineChartBuilder.saveChartAsPNG(chartBuilder.build(), "output/computationalStudies_min_max_avg.png");
		
		
	}
	
	public static double min(Collection<Double> doubles){
		double min = Double.MAX_VALUE;
		for(Double d : doubles){
			if(d<min) min=d;
		}
		return min;
	}
	
	public static double max(Collection<Double> doubles){
		double max = 0.;
		for(Double d : doubles){
			if(d>max) max=d;
		}
		return max;
	}
	
	public static double avg(Collection<Double> doubles){
		if(doubles.isEmpty()) return 0.;
		double sum = 0.;
		for(Double d : doubles){
			sum+=d;
		}
		return sum/(double)doubles.size();
	}
	

	private static VehicleRoutingAlgorithmFactory getAlgorithmFactory(final int iterations) {
		return new VehicleRoutingAlgorithmFactory() {
			
			@Override
			public VehicleRoutingAlgorithm createAlgorithm(VehicleRoutingProblem vrp) {
				VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "input/algorithmConfigWithSchrimpfAcceptance.xml");
				vra.setNuOfIterations(iterations);
				return vra;
			}
		};
	}

}
