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
import com.graphhopper.jsprit.analysis.toolbox.Plotter;
import com.graphhopper.jsprit.analysis.toolbox.Plotter.Label;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.selector.SelectBest;
import com.graphhopper.jsprit.core.analysis.SolutionAnalyser;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.cost.TransportDistance;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.io.problem.VrpXMLReader;
import com.graphhopper.jsprit.util.Examples;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Collection;


public class PickupAndDeliveryExample {

    public static void main(String[] args) {

//        List<SizeDimension> caps = new ArrayList<>();
//        Random rnd = new Random(42);
//
//        for (int i = 0; i < 10000; i++) {
//            int dim = rnd.nextInt(10) + 1;
//            SizeDimension.Builder b = SizeDimension.Builder.newInstance();
//            for (int j = 0; j < dim; j++) {
//                b.addDimension(j, rnd.nextInt(1000) - 500);
//            }
//            caps.add(b.build());
//        }
//
//        LocalTime st1 = LocalTime.now();
//        for (int i = 0; i < caps.size(); i++) {
//            for (int j = 0; j < caps.size(); j++) {
//                SizeDimension c2 = caps.get(i).subtract(caps.get(j));
//                if (c2.getNuOfDimensions() > 1000) {
//                    System.out.println("x");
//                }
//            }
//        }
//        LocalTime en1 = LocalTime.now();
//        Duration dur1 = Duration.between(en1, st1);
//        System.out.println(dur1);
//
//        LocalTime st2 = LocalTime.now();
//        for (int i = 0; i < caps.size(); i++) {
//            for (int j = 0; j < caps.size(); j++) {
//                SizeDimension c2 = SizeDimension.subtract(caps.get(i), caps.get(j));
//                if (c2.getNuOfDimensions() > 1000) {
//                    System.out.println("x");
//                }
//            }
//        }
//        LocalTime en2 = LocalTime.now();
//        Duration dur2 = Duration.between(en2, st2);
//        System.out.println(dur2);
//        System.out.println((dur2.getSeconds() + (double) dur2.getNano() / 1000000000) / dur1.getSeconds() + (double) dur1.getNano() / 1000000000);
//        System.exit(1);

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
        new VrpXMLReader(vrpBuilder).read("input/pickups_and_deliveries_solomon_r101_withoutTWs.xml");

        /*
         * Finally, the problem can be built. By default, transportCosts are crowFlyDistances (as usually used for
         * vrp-instances).
         */

        final VehicleRoutingProblem vrp = vrpBuilder.build();

        new Plotter(vrp).plot("output/pd_solomon_r101.png", "pd_r101");


        /*
         * Define the required vehicle-routing algorithms to solve the above problem.
         *
         * The algorithm can be defined and configured in an xml-file.
         */
//		VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(vrp);
        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        vra.getAlgorithmListeners().addListener(new AlgorithmSearchProgressChartListener("output/sol_progress.png"));
        /*
         * Solve the problem.
         *
         *
         */

        LocalTime st2 = LocalTime.now();
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        LocalTime en2 = LocalTime.now();
        Duration dur2 = Duration.between(en2, st2);
        System.out.println(dur2);

        /*
         * Retrieve best solution.
         */
        VehicleRoutingProblemSolution solution = new SelectBest().selectSolution(solutions);

        /*
         * print solution
         */
        SolutionPrinter.print(solution);

        /*
         * Plot solution.
         */
//		SolutionPlotter.plotSolutionAsPNG(vrp, solution, "output/pd_solomon_r101_solution.png","pd_r101");
        Plotter plotter = new Plotter(vrp, solution);
        plotter.setLabel(Label.SIZE);
        plotter.plot("output/pd_solomon_r101_solution.png", "pd_r101");

        //some stats
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, new TransportDistance() {

            @Override
            public double getDistance(Location from, Location to, double departureTime, Vehicle vehicle) {
                return vrp.getTransportCosts().getTransportCost(from, to, 0., null, null);
            }

        });

        System.out.println("tp_distance: " + analyser.getDistance());
        System.out.println("tp_time: " + analyser.getTransportTime());
        System.out.println("waiting: " + analyser.getWaitingTime());
        System.out.println("service: " + analyser.getServiceTime());
        System.out.println("#picks: " + analyser.getNumberOfPickups());
        System.out.println("#deliveries: " + analyser.getNumberOfDeliveries());


    }

}
