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

import com.graphhopper.jsprit.analysis.toolbox.AlgorithmSearchProgressChartListener;
import com.graphhopper.jsprit.analysis.toolbox.GraphStreamViewer;
import com.graphhopper.jsprit.analysis.toolbox.Plotter;
import com.graphhopper.jsprit.analysis.toolbox.StopWatch;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.listener.VehicleRoutingAlgorithmListeners.Priority;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.io.problem.VrpXMLReader;
import com.graphhopper.jsprit.util.Examples;

import java.util.Arrays;
import java.util.Collection;


public class MultipleDepotExample {

    public static void main(String[] args) {
        /*
         * some preparation - create output folder
		 */
        Examples.createOutputFolder();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        /*
         * Read cordeau-instance p01, BUT only its services without any vehicles
		 */
        new VrpXMLReader(vrpBuilder).read("input/vrp_cordeau_01.xml");

		/*
         * add vehicles with its depots
		 * 4 depots:
		 * (20,20)
		 * (30,40)
		 * (50,30)
		 * (60,50)
		 *
		 * each with 4 vehicles each with a capacity of 80
		 */
        int nuOfVehicles = 4;
        int capacity = 80;
        Coordinate firstDepotCoord = Coordinate.newInstance(20, 20);
        Coordinate second = Coordinate.newInstance(30, 40);
        Coordinate third = Coordinate.newInstance(50, 30);
        Coordinate fourth = Coordinate.newInstance(60, 50);

        int depotCounter = 1;
        for (Coordinate depotCoord : Arrays.asList(firstDepotCoord, second, third, fourth)) {
            for (int i = 0; i < nuOfVehicles; i++) {
                VehicleTypeImpl vehicleType = VehicleTypeImpl.Builder.newInstance(depotCounter + "_type").addCapacityDimension(0, capacity).setCostPerDistance(1.0).build();
                VehicleImpl vehicle = VehicleImpl.Builder.newInstance(depotCounter + "_" + (i + 1) + "_vehicle").setStartLocation(Location.newInstance(depotCoord.getX(), depotCoord.getY())).setType(vehicleType).build();
                vrpBuilder.addVehicle(vehicle);
            }
            depotCounter++;
        }

		/*
         * define problem with finite fleet
		 */
        vrpBuilder.setFleetSize(FleetSize.FINITE);

		/*
         * build the problem
		 */
        VehicleRoutingProblem vrp = vrpBuilder.build();

		/*
         * plot to see how the problem looks like
		 */
//		SolutionPlotter.plotVrpAsPNG(vrp, "output/problem01.png", "p01");

		/*
         * solve the problem
		 */
        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setProperty(Jsprit.Parameter.THREADS, "5").buildAlgorithm();
        vra.getAlgorithmListeners().addListener(new StopWatch(), Priority.HIGH);
        vra.getAlgorithmListeners().addListener(new AlgorithmSearchProgressChartListener("output/progress.png"));
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        SolutionPrinter.print(Solutions.bestOf(solutions));

        new Plotter(vrp, Solutions.bestOf(solutions)).setLabel(Plotter.Label.ID).plot("output/p01_solution.png", "p01");

        new GraphStreamViewer(vrp, Solutions.bestOf(solutions)).setRenderDelay(100).display();

    }

}
