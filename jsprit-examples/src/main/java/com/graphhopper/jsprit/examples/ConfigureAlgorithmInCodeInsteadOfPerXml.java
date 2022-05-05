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

import com.graphhopper.jsprit.analysis.toolbox.Plotter;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl.Builder;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.io.algorithm.AlgorithmConfig;
import com.graphhopper.jsprit.io.algorithm.VehicleRoutingAlgorithms;
import com.graphhopper.jsprit.io.problem.VrpXMLWriter;
import com.graphhopper.jsprit.util.Examples;
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
        VehicleRoutingAlgorithm algorithm = VehicleRoutingAlgorithms.createAlgorithm(problem, algorithmConfig);

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
        new Plotter(problem, bestSolution).plot("output/solution.png", "solution");
    }

    private static AlgorithmConfig getAlgorithmConfig() {
        AlgorithmConfig config = new AlgorithmConfig();
        XMLConfiguration xmlConfig = config.getXMLConfiguration();
        xmlConfig.setProperty("iterations", "2000");
        xmlConfig.setProperty("construction.insertion[@name]", "bestInsertion");

        xmlConfig.setProperty("strategy.memory", 1);
        String searchStrategy = "strategy.searchStrategies.searchStrategy";

        xmlConfig.setProperty(searchStrategy + "(0)[@name]", "random_best");
        xmlConfig.setProperty(searchStrategy + "(0).selector[@name]", "selectBest");
        xmlConfig.setProperty(searchStrategy + "(0).acceptor[@name]", "acceptNewRemoveWorst");
        xmlConfig.setProperty(searchStrategy + "(0).modules.module(0)[@name]", "ruin_and_recreate");
        xmlConfig.setProperty(searchStrategy + "(0).modules.module(0).ruin[@name]", "randomRuin");
        xmlConfig.setProperty(searchStrategy + "(0).modules.module(0).ruin.share", "0.3");
        xmlConfig.setProperty(searchStrategy + "(0).modules.module(0).insertion[@name]", "bestInsertion");
        xmlConfig.setProperty(searchStrategy + "(0).probability", "0.5");

        xmlConfig.setProperty(searchStrategy + "(1)[@name]", "radial_best");
        xmlConfig.setProperty(searchStrategy + "(1).selector[@name]", "selectBest");
        xmlConfig.setProperty(searchStrategy + "(1).acceptor[@name]", "acceptNewRemoveWorst");
        xmlConfig.setProperty(searchStrategy + "(1).modules.module(0)[@name]", "ruin_and_recreate");
        xmlConfig.setProperty(searchStrategy + "(1).modules.module(0).ruin[@name]", "radialRuin");
        xmlConfig.setProperty(searchStrategy + "(1).modules.module(0).ruin.share", "0.15");
        xmlConfig.setProperty(searchStrategy + "(1).modules.module(0).insertion[@name]", "bestInsertion");
        xmlConfig.setProperty(searchStrategy + "(1).probability", "0.5");

        return config;
    }

}
