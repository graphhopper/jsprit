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
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.instance.reader.CordeauReader;
import com.graphhopper.jsprit.util.Examples;

import java.util.Arrays;
import java.util.Collection;


public class MultipleDepotExample2 {


    public static void main(String[] args) {
        /*
         * some preparation - create output folder
		 */
        Examples.createOutputFolder();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        /*
         * Read cordeau-instance p01, BUT only its services without any vehicles
		 */
        new CordeauReader(vrpBuilder).read("input/p08");

		/*
         * add vehicles with its depots
		 * 2 depots:
		 * (-33,33)
		 * (33,-33)
		 *
		 * each with 14 vehicles each with a capacity of 500 and a maximum duration of 310
		 */
        int nuOfVehicles = 13;
        int capacity = 500;
        double maxDuration = 310;
        Coordinate firstDepotCoord = Coordinate.newInstance(-33, 33);
        Coordinate second = Coordinate.newInstance(33, -33);

        int depotCounter = 1;
        for (Coordinate depotCoord : Arrays.asList(firstDepotCoord, second)) {
            for (int i = 0; i < nuOfVehicles; i++) {
                VehicleType vehicleType = VehicleTypeImpl.Builder.newInstance(depotCounter + "_type")
                    .addCapacityDimension(0, capacity).setCostPerDistance(1.0).build();
                String vehicleId = depotCounter + "_" + (i + 1) + "_vehicle";
                VehicleImpl.Builder vehicleBuilder = VehicleImpl.Builder.newInstance(vehicleId);
                vehicleBuilder.setStartLocation(Location.newInstance(depotCoord.getX(), depotCoord.getY()));
                vehicleBuilder.setType(vehicleType);
                vehicleBuilder.setLatestArrival(maxDuration);
                VehicleImpl vehicle = vehicleBuilder.build();
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
//		SolutionPlotter.plotVrpAsPNG(vrp, "output/problem08.png", "p08");

		/*
         * solve the problem
		 */
        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
            .setProperty(Jsprit.Parameter.FAST_REGRET, "true")
            .setProperty(Jsprit.Parameter.THREADS, "5").buildAlgorithm();
        vra.setMaxIterations(2000);
        vra.getAlgorithmListeners().addListener(new StopWatch(), Priority.HIGH);
        vra.getAlgorithmListeners().addListener(new AlgorithmSearchProgressChartListener("output/progress.png"));
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        SolutionPrinter.print(vrp, Solutions.bestOf(solutions), SolutionPrinter.Print.VERBOSE);

        new Plotter(vrp, Solutions.bestOf(solutions)).plot("output/p08_solution.png", "p08");

        new GraphStreamViewer(vrp, Solutions.bestOf(solutions)).setRenderDelay(50).display();
    }

}
