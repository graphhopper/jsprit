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

import jsprit.analysis.toolbox.Plotter;
import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.io.AlgorithmConfig;
import jsprit.core.algorithm.io.VehicleRoutingAlgorithms;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.io.VrpXMLWriter;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleImpl.Builder;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.reporting.SolutionPrinter;
import jsprit.core.util.Solutions;
import jsprit.util.Examples;
import org.apache.commons.configuration.XMLConfiguration;

import java.util.Collection;


public class ConfigureAlgorithmInCodeInsteadOfPerXml {
	
	public static void main(String[] args) {
		/*
		 * some preparation - create output folder
		 */
		Examples.createOutputFolder();
		
		/*
		 * get a vehicle type-builder and build a type with the typeId "vehicleType" and a capacity of 2
		 */
		VehicleTypeImpl.Builder vehicleTypeBuilder = VehicleTypeImpl.Builder.newInstance("vehicleType").addCapacityDimension(0, 2);
		VehicleType vehicleType = vehicleTypeBuilder.build();
		
		/*
		 * get a vehicle-builder and build a vehicle located at (10,10) with type "vehicleType"
		 */
		Builder vehicleBuilder = VehicleImpl.Builder.newInstance("vehicle");
		vehicleBuilder.setStartLocation(Location.newInstance(10, 10));
		vehicleBuilder.setType(vehicleType);
		VehicleImpl vehicle = vehicleBuilder.build();
		
		/*
		 * build services at the required locations, each with a capacity-demand of 1.
		 */
		Service service1 = Service.Builder.newInstance("1").addSizeDimension(0, 1).setLocation(Location.newInstance(5, 7)).build();
		Service service2 = Service.Builder.newInstance("2").addSizeDimension(0, 1).setLocation(Location.newInstance(5, 13)).build();
		
		Service service3 = Service.Builder.newInstance("3").addSizeDimension(0, 1).setLocation(Location.newInstance(15, 7)).build();
		Service service4 = Service.Builder.newInstance("4").addSizeDimension(0, 1).setLocation(Location.newInstance(15, 13)).build();
		
		
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		vrpBuilder.addVehicle(vehicle);
		vrpBuilder.addJob(service1).addJob(service2).addJob(service3).addJob(service4);
		
		VehicleRoutingProblem problem = vrpBuilder.build();
		
		/*
		 * get the algorithm out-of-the-box. 
		 */
		AlgorithmConfig algorithmConfig = getAlgorithmConfig();
		VehicleRoutingAlgorithm algorithm = VehicleRoutingAlgorithms.createAlgorithm(problem,algorithmConfig);
		
		/*
		 * and search a solution
		 */
		Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
		
		/*
		 * get the best 
		 */
		VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);
		
		new VrpXMLWriter(problem, solutions).write("output/problem-with-solution.xml");
		
		SolutionPrinter.print(bestSolution);
		
		/*
		 * plot
		 */
		new Plotter(problem,bestSolution).plot("output/solution.png", "solution");
	}

	private static AlgorithmConfig getAlgorithmConfig() {
		AlgorithmConfig config = new AlgorithmConfig();
		XMLConfiguration xmlConfig = config.getXMLConfiguration();
		xmlConfig.setProperty("iterations", "2000");
		xmlConfig.setProperty("construction.insertion[@name]","bestInsertion");
		
		xmlConfig.setProperty("strategy.memory", 1);
		String searchStrategy = "strategy.searchStrategies.searchStrategy";

		xmlConfig.setProperty(searchStrategy + "(0)[@name]","random_best");
		xmlConfig.setProperty(searchStrategy + "(0).selector[@name]","selectBest");
		xmlConfig.setProperty(searchStrategy + "(0).acceptor[@name]","acceptNewRemoveWorst");
		xmlConfig.setProperty(searchStrategy + "(0).modules.module(0)[@name]","ruin_and_recreate");
		xmlConfig.setProperty(searchStrategy + "(0).modules.module(0).ruin[@name]","randomRuin");
		xmlConfig.setProperty(searchStrategy + "(0).modules.module(0).ruin.share","0.3");
		xmlConfig.setProperty(searchStrategy + "(0).modules.module(0).insertion[@name]","bestInsertion");
		xmlConfig.setProperty(searchStrategy + "(0).probability","0.5");

		xmlConfig.setProperty(searchStrategy + "(1)[@name]","radial_best");
		xmlConfig.setProperty(searchStrategy + "(1).selector[@name]","selectBest");
		xmlConfig.setProperty(searchStrategy + "(1).acceptor[@name]","acceptNewRemoveWorst");
		xmlConfig.setProperty(searchStrategy + "(1).modules.module(0)[@name]","ruin_and_recreate");
		xmlConfig.setProperty(searchStrategy + "(1).modules.module(0).ruin[@name]","radialRuin");
		xmlConfig.setProperty(searchStrategy + "(1).modules.module(0).ruin.share","0.15");
		xmlConfig.setProperty(searchStrategy + "(1).modules.module(0).insertion[@name]","bestInsertion");
		xmlConfig.setProperty(searchStrategy + "(1).probability","0.5");
		
		return config;
	}

}
