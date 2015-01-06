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

import jsprit.analysis.toolbox.AlgorithmEventsRecorder;
import jsprit.analysis.toolbox.AlgorithmEventsViewer;
import jsprit.analysis.toolbox.GraphStreamViewer;
import jsprit.analysis.toolbox.Plotter;
import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.box.GreedySchrimpfFactory;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.util.Coordinate;
import jsprit.core.util.Solutions;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by schroeder on 27.11.14.
 */
public class CircleExample {

    public static Collection<Coordinate> createCoordinates(double center_x, double center_y, double radius, double step){
        Collection<Coordinate> coords = new ArrayList<Coordinate>();
        for(double theta = 0; theta < 2*Math.PI; theta += step){
            double x = center_x + radius*Math.cos(theta);
            double y = center_y - radius*Math.sin(theta);
            coords.add(Coordinate.newInstance(x,y));
        }
        return coords;
    }

    public static void main(String[] args) {
        File dir = new File("output");
        // if the directory does not exist, create it
        if (!dir.exists()){
            System.out.println("creating directory ./output");
            boolean result = dir.mkdir();
            if(result) System.out.println("./output created");
        }

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v")
                .setStartLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(0, 0)).build()).build();
        vrpBuilder.addVehicle(v);

        double step = 2*Math.PI/50.;
        Collection<Coordinate> circle = createCoordinates(0,0,20,step);
        int id = 1;
        for(Coordinate c : circle){
            Service s = Service.Builder.newInstance(Integer.toString(id)).setLocation(Location.Builder.newInstance().setCoordinate(c).build()).build();
            vrpBuilder.addJob(s);
            id++;
        }
        VehicleRoutingProblem vrp = vrpBuilder.build();

        //only works with latest snapshot: 1.4.3
        AlgorithmEventsRecorder eventsRecorder = new AlgorithmEventsRecorder(vrp,"output/events.dgs.gz");
        eventsRecorder.setRecordingRange(0,50);

        VehicleRoutingAlgorithm vra = new GreedySchrimpfFactory().createAlgorithm(vrp);
        vra.setMaxIterations(50);

        //only works with latest snapshot: 1.4.3
        vra.addListener(eventsRecorder);

        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());

        new Plotter(vrp,solution).plot("output/circle.png","circleProblem");
        new GraphStreamViewer(vrp,solution).display();

        //only works with latest snapshot: 1.4.3
        AlgorithmEventsViewer viewer = new AlgorithmEventsViewer();
        viewer.setRuinDelay(16);
        viewer.setRecreationDelay(8);
        viewer.display("output/events.dgs.gz");

    }

}
