/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
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
package com.graphhopper.jsprit.core.algorithm;

import com.graphhopper.jsprit.core.IntegrationTest;
import com.graphhopper.jsprit.core.algorithm.box.GreedySchrimpfFactory;
import com.graphhopper.jsprit.core.algorithm.termination.IterationWithoutImprovementTermination;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.*;
import java.util.Collection;

import static org.junit.Assert.assertEquals;


public class RefuseCollectionWithCostsHigherThanTimesAndFiniteFleet_IT {

    static class RelationKey {

        static RelationKey newKey(String from, String to) {
            int fromInt = Integer.parseInt(from);
            int toInt = Integer.parseInt(to);
            if (fromInt < toInt) {
                return new RelationKey(from, to);
            } else {
                return new RelationKey(to, from);
            }
        }

        final String from;
        final String to;

        public RelationKey(String from, String to) {
            super();
            this.from = from;
            this.to = to;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((from == null) ? 0 : from.hashCode());
            result = prime * result + ((to == null) ? 0 : to.hashCode());
            return result;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            RelationKey other = (RelationKey) obj;
            if (from == null) {
                if (other.from != null)
                    return false;
            } else if (!from.equals(other.from))
                return false;
            if (to == null) {
                if (other.to != null)
                    return false;
            } else if (!to.equals(other.to))
                return false;
            return true;
        }
    }

    @Test
    @Category(IntegrationTest.class)
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
        vra.setPrematureAlgorithmTermination(new IterationWithoutImprovementTermination(100));
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        SolutionPrinter.print(vrp, Solutions.bestOf(solutions), SolutionPrinter.Print.VERBOSE);

        assertEquals(2. * 397., Solutions.bestOf(solutions).getCost(), 0.01);
        assertEquals(2, Solutions.bestOf(solutions).getRoutes().size());
    }


    private static void readDemandQuantities(VehicleRoutingProblem.Builder vrpBuilder) throws FileNotFoundException, IOException {
        BufferedReader reader = new BufferedReader(new FileReader(new File("src/test/resources/refuseCollectionExample_Quantities")));
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


    private static void readDistances(VehicleRoutingTransportCostsMatrix.Builder matrixBuilder) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(new File("src/test/resources/refuseCollectionExample_Distances")));
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
