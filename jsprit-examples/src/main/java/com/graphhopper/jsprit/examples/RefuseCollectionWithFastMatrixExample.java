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

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.GreedySchrimpfFactory;
import com.graphhopper.jsprit.core.algorithm.termination.IterationWithoutImprovementTermination;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.FastVehicleRoutingTransportCostsMatrix;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.io.problem.VrpXMLWriter;
import com.graphhopper.jsprit.util.Examples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;


/**
 * This example is base on
 * http://web.mit.edu/urban_or_book/www/book/chapter6/6.4.12.html
 *
 * @author stefan schroeder
 */
public class RefuseCollectionWithFastMatrixExample {

    public static void main(String[] args) throws IOException {
        /*
         * some preparation - create output folder
		 */
        Examples.createOutputFolder();

		/*
         * create vehicle-type and vehicle
		 */
        VehicleTypeImpl.Builder typeBuilder = VehicleTypeImpl.Builder.newInstance("vehicle-type").addCapacityDimension(0, 23);
        typeBuilder.setCostPerDistance(1.0);
        VehicleTypeImpl bigType = typeBuilder.build();

        VehicleImpl.Builder vehicleBuilder = VehicleImpl.Builder.newInstance("vehicle");
        vehicleBuilder.setStartLocation(Location.Builder.newInstance().setIndex(1).build());
        vehicleBuilder.setType(bigType);
        VehicleImpl bigVehicle = vehicleBuilder.build();

		/*
         * start building the problem
		 */
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.setFleetSize(FleetSize.INFINITE);
        vrpBuilder.addVehicle(bigVehicle);

		/*
         * read demand quantities
		 */
        readDemandQuantities(vrpBuilder);

		/*
         * create cost-matrix
		 */
        FastVehicleRoutingTransportCostsMatrix.Builder matrixBuilder = FastVehicleRoutingTransportCostsMatrix.Builder.newInstance(11, true);
        readDistances(matrixBuilder);

        vrpBuilder.setRoutingCost(matrixBuilder.build());

        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = new GreedySchrimpfFactory().createAlgorithm(vrp);
        vra.setPrematureAlgorithmTermination(new IterationWithoutImprovementTermination(100));
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        SolutionPrinter.print(Solutions.bestOf(solutions));

        new VrpXMLWriter(vrp, solutions).write("output/refuseCollectionExampleSolution.xml");

    }


    private static void readDemandQuantities(VehicleRoutingProblem.Builder vrpBuilder) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(new File("input/RefuseCollectionExample_Quantities")));
        String line;
        boolean firstLine = true;
        while ((line = reader.readLine()) != null) {
            if (firstLine) {
                firstLine = false;
                continue;
            }
            String[] lineTokens = line.split(",");
            /*
             * build service
			 */
            Service service = Service.Builder.newInstance(lineTokens[0])
                .addSizeDimension(0, Integer.parseInt(lineTokens[1]))
                .setLocation(Location.Builder.newInstance().setIndex(Integer.parseInt(lineTokens[0])).build())
                .build();
            /*
			 * and add it to problem
			 */
            vrpBuilder.addJob(service);
        }
        reader.close();
    }


    private static void readDistances(FastVehicleRoutingTransportCostsMatrix.Builder matrixBuilder) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(new File("input/RefuseCollectionExample_Distances")));
        String line;
        boolean firstLine = true;
        while ((line = reader.readLine()) != null) {
            if (firstLine) {
                firstLine = false;
                continue;
            }
            String[] lineTokens = line.split(",");
            matrixBuilder.addTransportDistance(Integer.parseInt(lineTokens[0]), Integer.parseInt(lineTokens[1]), Integer.parseInt(lineTokens[2]));
        }
        reader.close();

    }

}
