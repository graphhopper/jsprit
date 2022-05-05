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

import com.graphhopper.jsprit.analysis.toolbox.AlgorithmEventsRecorder;
import com.graphhopper.jsprit.analysis.toolbox.AlgorithmEventsViewer;
import com.graphhopper.jsprit.analysis.toolbox.GraphStreamViewer;
import com.graphhopper.jsprit.analysis.toolbox.Plotter;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.GreedySchrimpfFactory;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.Solutions;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by schroeder on 27.11.14.
 */
public class CircleExample {

    public static Collection<Coordinate> createCoordinates(double center_x, double center_y, double radius, double step) {
        Collection<Coordinate> coords = new ArrayList<Coordinate>();
        for (double theta = 0; theta < 2 * Math.PI; theta += step) {
            double x = center_x + radius * Math.cos(theta);
            double y = center_y - radius * Math.sin(theta);
            coords.add(Coordinate.newInstance(x, y));
        }
        return coords;
    }

    public static void main(String[] args) {
        File dir = new File("output");
        // if the directory does not exist, create it
        if (!dir.exists()) {
            System.out.println("creating directory ./output");
            boolean result = dir.mkdir();
            if (result) System.out.println("./output created");
        }

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v")
            .setStartLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(0, 0)).build()).build();
        vrpBuilder.addVehicle(v);

        double step = 2 * Math.PI / 50.;
        Collection<Coordinate> circle = createCoordinates(0, 0, 20, step);
        int id = 1;
        for (Coordinate c : circle) {
            Service s = Service.Builder.newInstance(Integer.toString(id)).setLocation(Location.Builder.newInstance().setCoordinate(c).build()).build();
            vrpBuilder.addJob(s);
            id++;
        }
        VehicleRoutingProblem vrp = vrpBuilder.build();

        //only works with latest snapshot: 1.4.3
        AlgorithmEventsRecorder eventsRecorder = new AlgorithmEventsRecorder(vrp, "output/events.dgs.gz");
        eventsRecorder.setRecordingRange(0, 50);

        VehicleRoutingAlgorithm vra = new GreedySchrimpfFactory().createAlgorithm(vrp);
        vra.setMaxIterations(50);

        //only works with latest snapshot: 1.4.3
        vra.addListener(eventsRecorder);

        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());

        new Plotter(vrp, solution).plot("output/circle.png", "circleProblem");
        new GraphStreamViewer(vrp, solution).display();

        //only works with latest snapshot: 1.4.3
        AlgorithmEventsViewer viewer = new AlgorithmEventsViewer();
        viewer.setRuinDelay(16);
        viewer.setRecreationDelay(8);
        viewer.display("output/events.dgs.gz");

    }

}
