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

import jsprit.analysis.toolbox.ComputationalLaboratory;
import jsprit.analysis.toolbox.ComputationalLaboratory.CalculationListener;
import jsprit.analysis.toolbox.ComputationalLaboratory.DataCollector;
import jsprit.analysis.toolbox.Plotter;
import jsprit.analysis.toolbox.XYLineChartBuilder;
import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.VehicleRoutingAlgorithmFactory;
import jsprit.core.algorithm.io.AlgorithmConfig;
import jsprit.core.algorithm.io.VehicleRoutingAlgorithms;
import jsprit.core.algorithm.listener.IterationStartsListener;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.reporting.SolutionPrinter;
import jsprit.core.reporting.SolutionPrinter.Print;
import jsprit.core.util.BenchmarkInstance;
import jsprit.core.util.Solutions;
import jsprit.instance.reader.SolomonReader;
import jsprit.util.Examples;
import org.apache.commons.configuration.XMLConfiguration;

import java.util.Collection;

/**
 * Based on Solomon's R101 instance
 * 
 * @author schroeder
 *
 */
public class ComputationalExperiments_alphaSenstivity {
	
	
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
		new SolomonReader(vrpBuilder,100).read("input/R101.txt");
		
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
		for(double alphaVal=0.;alphaVal<.4;alphaVal+=.1){
			
			final String alpha = String.valueOf(alphaVal).substring(0, 3);
			computationalLab.addAlgorithmFactory("alpha_"+alpha, new VehicleRoutingAlgorithmFactory() {

				@Override
				public VehicleRoutingAlgorithm createAlgorithm(VehicleRoutingProblem vrp) {
					return VehicleRoutingAlgorithms.createAlgorithm(vrp, getAlgorithmConfig(alpha));
				}
				
			});
			
		}
				
		/*
		 * plot search progress of different algorithms
		 */
		final XYLineChartBuilder chartBuilder = XYLineChartBuilder.newInstance("alpha-sensitivity", "iterations", "costs");
		computationalLab.addListener(new CalculationListener() {
			
			@Override
			public void calculationStarts(BenchmarkInstance p, final String algorithmName,VehicleRoutingAlgorithm algorithm, int run) {
				algorithm.addListener(new IterationStartsListener() {
					
					@Override
					public void informIterationStarts(int i, VehicleRoutingProblem problem,Collection<VehicleRoutingProblemSolution> solutions) {
						/*
						 * plot only distance-costs, i.e. without fixed costs
						 */
						VehicleRoutingProblemSolution bestOf = Solutions.bestOf(solutions);
						chartBuilder.addData(algorithmName, i, bestOf.getCost()-bestOf.getRoutes().size()*100.);
					}
					
				});
				
			}
			
			@Override
			public void calculationEnds(BenchmarkInstance p, String algorithmName,VehicleRoutingAlgorithm algorithm, int run,Collection<VehicleRoutingProblemSolution> solutions) {}
			
		});
		/*
		 * define dataCollector to collect an arbitrary number of indicators as well as solutions
		 */
		final DataCollector dataCollector = new DataCollector();
		computationalLab.addListener(new CalculationListener() {
			
			@Override
			public void calculationStarts(BenchmarkInstance p, String algorithmName,VehicleRoutingAlgorithm algorithm, int run) {}
			
			@Override
			public void calculationEnds(BenchmarkInstance p, String algorithmName,VehicleRoutingAlgorithm algorithm, int run,Collection<VehicleRoutingProblemSolution> solutions) {
				//memorize solution
				dataCollector.addSolution(p.name, algorithmName, run, Solutions.bestOf(solutions));
			}
			
		});
		/*
		 * determine number of threads to be used
		 */
		computationalLab.setThreads(2);
		/*
		 * run the experiments
		 */
		computationalLab.run();
		
		/*
		 * plot the lineChart
		 */
		XYLineChartBuilder.saveChartAsPNG(chartBuilder.build(), "output/computationalStudies_alphaSensitivity.png");
		
		/*
		 * print best solution
		 */
		SolutionPrinter.print(vrp, Solutions.bestOf(dataCollector.getSolutions()), Print.VERBOSE);
		
		/*
		 * plot best
		 */
		Plotter plotter = new Plotter(vrp,Solutions.bestOf(dataCollector.getSolutions()));
		plotter.plot("output/bestOf.png", "bestOfR101");
		
	}
	
	private static AlgorithmConfig getAlgorithmConfig(String alpha) {
		AlgorithmConfig config = new AlgorithmConfig();
		XMLConfiguration xmlConfig = config.getXMLConfiguration();
		xmlConfig.setProperty("iterations",10000);
		xmlConfig.setProperty("construction.insertion[@name]","bestInsertion");
		
		xmlConfig.setProperty("strategy.memory", 1);
		String searchStrategy = "strategy.searchStrategies.searchStrategy";
		
		xmlConfig.setProperty(searchStrategy + "(0).selector[@name]","selectBest");
		xmlConfig.setProperty(searchStrategy + "(0).acceptor[@name]","schrimpfAcceptance");
		xmlConfig.setProperty(searchStrategy + "(0).acceptor.alpha",alpha);
		xmlConfig.setProperty(searchStrategy + "(0).acceptor.warmup","50");
		xmlConfig.setProperty(searchStrategy + "(0).modules.module(0)[@name]","ruin_and_recreate");
		xmlConfig.setProperty(searchStrategy + "(0).modules.module(0).ruin[@name]","randomRuin");
		xmlConfig.setProperty(searchStrategy + "(0).modules.module(0).ruin.share","0.3");
		xmlConfig.setProperty(searchStrategy + "(0).modules.module(0).insertion[@name]","bestInsertion");
		xmlConfig.setProperty(searchStrategy + "(0).probability",".5");
		
		xmlConfig.setProperty(searchStrategy + "(1).selector[@name]","selectBest");
		xmlConfig.setProperty(searchStrategy + "(1).acceptor[@name]","schrimpfAcceptance");
		xmlConfig.setProperty(searchStrategy + "(1).modules.module(0)[@name]","ruin_and_recreate");
		xmlConfig.setProperty(searchStrategy + "(1).modules.module(0).ruin[@name]","radialRuin");
		xmlConfig.setProperty(searchStrategy + "(1).modules.module(0).ruin.share","0.1");
		xmlConfig.setProperty(searchStrategy + "(1).modules.module(0).insertion[@name]","bestInsertion");
		xmlConfig.setProperty(searchStrategy + "(1).probability","0.5");
		
		return config;
	}

}
