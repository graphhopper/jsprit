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
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;
import com.graphhopper.jsprit.util.Examples;

import java.util.Collection;


/**
 * Illustrates how you can use jsprit with an already compiled distance and time matrix.
 *
 * @author schroeder
 */
public class CostMatrixExample {

    public static void main(String[] args) {
        /*
         * some preparation - create output folder
		 */
        Examples.createOutputFolder();

        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0, 2).setCostPerDistance(1).setCostPerTime(2).build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("vehicle")
            .setStartLocation(Location.newInstance("0")).setType(type).build();

        Service s1 = Service.Builder.newInstance("1").addSizeDimension(0, 1).setLocation(Location.newInstance("1")).build();
        Service s2 = Service.Builder.newInstance("2").addSizeDimension(0, 1).setLocation(Location.newInstance("2")).build();
        Service s3 = Service.Builder.newInstance("3").addSizeDimension(0, 1).setLocation(Location.newInstance("3")).build();


		/*
         * Assume the following symmetric distance-matrix
		 * from,to,distance
		 * 0,1,10.0
		 * 0,2,20.0
		 * 0,3,5.0
		 * 1,2,4.0
		 * 1,3,1.0
		 * 2,3,2.0
		 *
		 * and this time-matrix
		 * 0,1,5.0
		 * 0,2,10.0
		 * 0,3,2.5
		 * 1,2,2.0
		 * 1,3,0.5
		 * 2,3,1.0
		 */
        //define a matrix-builder building a symmetric matrix
        VehicleRoutingTransportCostsMatrix.Builder costMatrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
        costMatrixBuilder.addTransportDistance("0", "1", 10.0);
        costMatrixBuilder.addTransportDistance("0", "2", 20.0);
        costMatrixBuilder.addTransportDistance("0", "3", 5.0);
        costMatrixBuilder.addTransportDistance("1", "2", 4.0);
        costMatrixBuilder.addTransportDistance("1", "3", 1.0);
        costMatrixBuilder.addTransportDistance("2", "3", 2.0);

        costMatrixBuilder.addTransportTime("0", "1", 10.0);
        costMatrixBuilder.addTransportTime("0", "2", 20.0);
        costMatrixBuilder.addTransportTime("0", "3", 5.0);
        costMatrixBuilder.addTransportTime("1", "2", 4.0);
        costMatrixBuilder.addTransportTime("1", "3", 1.0);
        costMatrixBuilder.addTransportTime("2", "3", 2.0);

        VehicleRoutingTransportCosts costMatrix = costMatrixBuilder.build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().setFleetSize(FleetSize.INFINITE).setRoutingCost(costMatrix)
            .addVehicle(vehicle).addJob(s1).addJob(s2).addJob(s3).build();

        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);

        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        SolutionPrinter.print(Solutions.bestOf(solutions));

        new Plotter(vrp, Solutions.bestOf(solutions)).plot("output/yo.png", "po");

    }

}
