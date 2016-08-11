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


import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.box.SchrimpfFactory;
import com.graphhopper.jsprit.core.algorithm.termination.IterationWithoutImprovementTermination;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.job.Pickup;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;

import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;


public class RefuseCollection_IT {


    @Test
    public void whenReadingServices_itShouldCalculateCorrectly() {

		/*
         * create vehicle-type and vehicle
		 */
        VehicleTypeImpl.Builder typeBuilder = VehicleTypeImpl.Builder.newInstance("vehicle-type").addCapacityDimension(0, 23);
        typeBuilder.setCostPerDistance(1.0);
        VehicleTypeImpl bigType = typeBuilder.build();

        VehicleImpl.Builder vehicleBuilder = VehicleImpl.Builder.newInstance("vehicle");
        vehicleBuilder.setStartLocation(Location.newInstance("1"));
        vehicleBuilder.setType(bigType);
        VehicleImpl bigVehicle = vehicleBuilder.build();

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
        readDemandQuantitiesAsServices(vrpBuilder);
        readDistances(matrixBuilder);

        vrpBuilder.setRoutingCost(matrixBuilder.build());
        VehicleRoutingProblem vrp = vrpBuilder.build();
        VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(vrp);
        vra.setPrematureAlgorithmTermination(new IterationWithoutImprovementTermination(100));
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        Assert.assertEquals(397.0, Solutions.bestOf(solutions).getCost(), 40.);
        Assert.assertEquals(2, Solutions.bestOf(solutions).getRoutes().size());
    }

    @Test
    public void whenReadingServices_usingJsprit_itShouldCalculateCorrectly() {

		/*
         * create vehicle-type and vehicle
		 */
        VehicleTypeImpl.Builder typeBuilder = VehicleTypeImpl.Builder.newInstance("vehicle-type").addCapacityDimension(0, 23);
        typeBuilder.setCostPerDistance(1.0);
        VehicleTypeImpl bigType = typeBuilder.build();

        VehicleImpl.Builder vehicleBuilder = VehicleImpl.Builder.newInstance("vehicle");
        vehicleBuilder.setStartLocation(Location.newInstance("1"));
        vehicleBuilder.setType(bigType);
        VehicleImpl bigVehicle = vehicleBuilder.build();

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
        readDemandQuantitiesAsServices(vrpBuilder);
        readDistances(matrixBuilder);

        vrpBuilder.setRoutingCost(matrixBuilder.build());
        VehicleRoutingProblem vrp = vrpBuilder.build();
        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        vra.setPrematureAlgorithmTermination(new IterationWithoutImprovementTermination(100));
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        Assert.assertEquals(397.0, Solutions.bestOf(solutions).getCost(), 40.);
        Assert.assertEquals(2, Solutions.bestOf(solutions).getRoutes().size());
    }

    @Test
    public void whenReadingPickups_itShouldCalculateCorrectly() {

		/*
		 * create vehicle-type and vehicle
		 */
        VehicleTypeImpl.Builder typeBuilder = VehicleTypeImpl.Builder.newInstance("vehicle-type").addCapacityDimension(0, 23);
        typeBuilder.setCostPerDistance(1.0);
        VehicleTypeImpl bigType = typeBuilder.build();

        VehicleImpl.Builder vehicleBuilder = VehicleImpl.Builder.newInstance("vehicle");
        vehicleBuilder.setStartLocation(Location.newInstance("1"));
        vehicleBuilder.setType(bigType);
        VehicleImpl bigVehicle = vehicleBuilder.build();

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
        readDemandQuantitiesAsPickups(vrpBuilder);
        readDistances(matrixBuilder);

        vrpBuilder.setRoutingCost(matrixBuilder.build());
        VehicleRoutingProblem vrp = vrpBuilder.build();
        VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(vrp);
        vra.setPrematureAlgorithmTermination(new IterationWithoutImprovementTermination(100));
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        Assert.assertEquals(397.0, Solutions.bestOf(solutions).getCost(), 40.);
        Assert.assertEquals(2, Solutions.bestOf(solutions).getRoutes().size());
    }

    @Test
    public void whenReadingDeliveries_itShouldCalculateCorrectly() {

		/*
		 * create vehicle-type and vehicle
		 */
        VehicleTypeImpl.Builder typeBuilder = VehicleTypeImpl.Builder.newInstance("vehicle-type").addCapacityDimension(0, 23);
        typeBuilder.setCostPerDistance(1.0);
        VehicleTypeImpl bigType = typeBuilder.build();

        VehicleImpl.Builder vehicleBuilder = VehicleImpl.Builder.newInstance("vehicle");
        vehicleBuilder.setStartLocation(Location.newInstance("1"));
        vehicleBuilder.setType(bigType);
        VehicleImpl bigVehicle = vehicleBuilder.build();

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
        readDemandQuantitiesAsDeliveries(vrpBuilder);
        readDistances(matrixBuilder);

        vrpBuilder.setRoutingCost(matrixBuilder.build());
        VehicleRoutingProblem vrp = vrpBuilder.build();
        VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(vrp);
        vra.setPrematureAlgorithmTermination(new IterationWithoutImprovementTermination(100));
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        Assert.assertEquals(397.0, Solutions.bestOf(solutions).getCost(), 40.);
        Assert.assertEquals(2, Solutions.bestOf(solutions).getRoutes().size());
    }


    private void readDemandQuantitiesAsServices(VehicleRoutingProblem.Builder vrpBuilder) {
        BufferedReader reader = getBufferedReader("refuseCollectionExample_Quantities");
        String line;
        boolean firstLine = true;
        while ((line = readLine(reader)) != null) {
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
        close(reader);
    }

    private BufferedReader getBufferedReader(String s) {
        return new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(s)));
    }

    private void readDemandQuantitiesAsPickups(VehicleRoutingProblem.Builder vrpBuilder) {
        BufferedReader reader = getBufferedReader("refuseCollectionExample_Quantities");
        String line;
        boolean firstLine = true;
        while ((line = readLine(reader)) != null) {
            if (firstLine) {
                firstLine = false;
                continue;
            }
            String[] lineTokens = line.split(",");
			/*
			 * build service
			 */
            Pickup service = Pickup.Builder.newInstance(lineTokens[0]).addSizeDimension(0, Integer.parseInt(lineTokens[1]))
                .setLocation(Location.newInstance(lineTokens[0])).build();
			/*
			 * and add it to problem
			 */
            vrpBuilder.addJob(service);
        }
        close(reader);
    }

    private void readDemandQuantitiesAsDeliveries(VehicleRoutingProblem.Builder vrpBuilder) {
        BufferedReader reader = getBufferedReader("refuseCollectionExample_Quantities");
        String line;
        boolean firstLine = true;
        while ((line = readLine(reader)) != null) {
            if (firstLine) {
                firstLine = false;
                continue;
            }
            String[] lineTokens = line.split(",");
			/*
			 * build service
			 */
            Delivery service = (Delivery) Delivery.Builder.newInstance(lineTokens[0]).addSizeDimension(0, Integer.parseInt(lineTokens[1]))
                .setLocation(Location.newInstance(lineTokens[0])).build();
			/*
			 * and add it to problem
			 */
            vrpBuilder.addJob(service);
        }
        close(reader);
    }

    private static String readLine(BufferedReader reader) {
        String line = null;
        try {
            line = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return line;
    }

    private static void close(Reader reader) {
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void readDistances(VehicleRoutingTransportCostsMatrix.Builder matrixBuilder) {
        BufferedReader reader = getBufferedReader("refuseCollectionExample_Distances");
        String line;
        boolean firstLine = true;
        while ((line = readLine(reader)) != null) {
            if (firstLine) {
                firstLine = false;
                continue;
            }
            String[] lineTokens = line.split(",");
            matrixBuilder.addTransportDistance(lineTokens[0], lineTokens[1], Integer.parseInt(lineTokens[2]));
            matrixBuilder.addTransportTime(lineTokens[0], lineTokens[1], Integer.parseInt(lineTokens[2]));
        }
        close(reader);
    }


}
