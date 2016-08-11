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
package com.graphhopper.jsprit.core.algorithm;

import com.graphhopper.jsprit.core.algorithm.box.GreedySchrimpfFactory;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;

import static org.junit.Assert.assertEquals;


public class RefuseCollectionWithCostsHigherThanTimesAndFiniteFleet_IT {

    @Test
    public void testAlgo() {


		/*
         * create vehicle-type and vehicle
		 */
        VehicleTypeImpl.Builder typeBuilder = VehicleTypeImpl.Builder.newInstance("vehicle-type").addCapacityDimension(0, 23);
        typeBuilder.setCostPerDistance(1.0);
        VehicleTypeImpl bigType = typeBuilder.build();

        VehicleImpl.Builder vehicleBuilder = VehicleImpl.Builder.newInstance("vehicle");
        vehicleBuilder.setStartLocation(Location.newInstance("1"));
        vehicleBuilder.setType(bigType);
        vehicleBuilder.setLatestArrival(220);
        Vehicle bigVehicle = vehicleBuilder.build();

		/*
         * start building the problem
		 */
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.setFleetSize(VehicleRoutingProblem.FleetSize.INFINITE);
        vrpBuilder.addVehicle(bigVehicle);

		/*
         * create cost-matrix
		 */
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
        /*
         * read demand quantities
		 */
        try {
            readDemandQuantities(vrpBuilder);
            readDistances(matrixBuilder);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        vrpBuilder.setRoutingCost(matrixBuilder.build());
        VehicleRoutingProblem vrp = vrpBuilder.build();
        VehicleRoutingAlgorithm vra = new GreedySchrimpfFactory().createAlgorithm(vrp);
//        vra.setPrematureAlgorithmTermination(new IterationWithoutImprovementTermination(100));
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        assertEquals(2. * 397., Solutions.bestOf(solutions).getCost(), 0.01);
        assertEquals(2, Solutions.bestOf(solutions).getRoutes().size());
    }


    private void readDemandQuantities(VehicleRoutingProblem.Builder vrpBuilder) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("refuseCollectionExample_Quantities")));
        String line = null;
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
            Service service = Service.Builder.newInstance(lineTokens[0]).addSizeDimension(0, Integer.parseInt(lineTokens[1]))
                .setLocation(Location.newInstance(lineTokens[0])).build();
            /*
			 * and add it to problem
			 */
            vrpBuilder.addJob(service);
        }
        reader.close();
    }


    private void readDistances(VehicleRoutingTransportCostsMatrix.Builder matrixBuilder) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("refuseCollectionExample_Distances")));
        String line = null;
        boolean firstLine = true;
        while ((line = reader.readLine()) != null) {
            if (firstLine) {
                firstLine = false;
                continue;
            }
            String[] lineTokens = line.split(",");
            matrixBuilder.addTransportDistance(lineTokens[0], lineTokens[1], 2. * Integer.parseInt(lineTokens[2]));
            matrixBuilder.addTransportTime(lineTokens[0], lineTokens[1], Integer.parseInt(lineTokens[2]));
        }
        reader.close();

    }


}
