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
package jsprit.core.algorithm;

import jsprit.core.IntegrationTest;
import jsprit.core.algorithm.box.Jsprit;
import jsprit.core.algorithm.box.SchrimpfFactory;
import jsprit.core.algorithm.termination.IterationWithoutImprovementTermination;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import jsprit.core.problem.job.Delivery;
import jsprit.core.problem.job.Pickup;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.reporting.SolutionPrinter;
import jsprit.core.reporting.SolutionPrinter.Print;
import jsprit.core.util.Solutions;
import jsprit.core.util.VehicleRoutingTransportCostsMatrix;
import jsprit.core.util.VehicleRoutingTransportCostsMatrix.Builder;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.*;
import java.util.Collection;

import static org.junit.Assert.assertEquals;


public class RefuseCollection_IT {


    @Test
    @Category(IntegrationTest.class)
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
        vrpBuilder.setFleetSize(FleetSize.INFINITE);
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

        SolutionPrinter.print(vrp, Solutions.bestOf(solutions), Print.VERBOSE);

        assertEquals(397.0, Solutions.bestOf(solutions).getCost(), 40.);
        assertEquals(2, Solutions.bestOf(solutions).getRoutes().size());
    }

    @Test
    @Category(IntegrationTest.class)
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
        vrpBuilder.setFleetSize(FleetSize.INFINITE);
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

        SolutionPrinter.print(vrp, Solutions.bestOf(solutions), Print.VERBOSE);

        assertEquals(397.0, Solutions.bestOf(solutions).getCost(), 40.);
        assertEquals(2, Solutions.bestOf(solutions).getRoutes().size());
    }

    @Test
    @Category(IntegrationTest.class)
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
        vrpBuilder.setFleetSize(FleetSize.INFINITE);
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

        SolutionPrinter.print(vrp, Solutions.bestOf(solutions), Print.VERBOSE);

        assertEquals(397.0, Solutions.bestOf(solutions).getCost(), 40.);
        assertEquals(2, Solutions.bestOf(solutions).getRoutes().size());
    }

    @Test
    @Category(IntegrationTest.class)
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
        vrpBuilder.setFleetSize(FleetSize.INFINITE);
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

        SolutionPrinter.print(vrp, Solutions.bestOf(solutions), Print.VERBOSE);

        assertEquals(397.0, Solutions.bestOf(solutions).getCost(), 40.);
        assertEquals(2, Solutions.bestOf(solutions).getRoutes().size());
    }


    private static void readDemandQuantitiesAsServices(VehicleRoutingProblem.Builder vrpBuilder) {
        BufferedReader reader = getBufferedReader("src/test/resources/refuseCollectionExample_Quantities");
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

    private static BufferedReader getBufferedReader(String s) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(new File(s)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return reader;
    }

    private static void readDemandQuantitiesAsPickups(VehicleRoutingProblem.Builder vrpBuilder) {
        BufferedReader reader = getBufferedReader("src/test/resources/refuseCollectionExample_Quantities");
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

    private static void readDemandQuantitiesAsDeliveries(VehicleRoutingProblem.Builder vrpBuilder) {
        BufferedReader reader = getBufferedReader("src/test/resources/refuseCollectionExample_Quantities");
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


    private static void readDistances(Builder matrixBuilder) {
        BufferedReader reader = getBufferedReader("src/test/resources/refuseCollectionExample_Distances");
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
