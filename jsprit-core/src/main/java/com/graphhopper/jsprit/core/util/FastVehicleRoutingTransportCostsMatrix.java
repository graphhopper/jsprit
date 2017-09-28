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
package com.graphhopper.jsprit.core.util;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.cost.AbstractForwardVehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;


/**
 * CostMatrix that allows pre-compiled time and distance-matrices to be considered as {@link com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts}
 * in the {@link com.graphhopper.jsprit.core.problem.VehicleRoutingProblem}.
 * <p>Note that you can also use it with distance matrix only (or time matrix).
 *
 * @author schroeder
 */
public class FastVehicleRoutingTransportCostsMatrix extends AbstractForwardVehicleRoutingTransportCosts {

    /**
     * Builder that builds the matrix.
     *
     * @author schroeder
     */
    public static class Builder {

        private boolean isSymmetric;

        private double[][][] matrix;

        private final int noLocations;

        /**
         * Creates a new builder returning the matrix-builder.
         * <p>If you want to consider symmetric matrices, set isSymmetric to true.
         *
         * @param isSymmetric true if matrix is symmetric, false otherwise
         * @return builder
         */
        public static Builder newInstance(int noLocations, boolean isSymmetric) {
            return new Builder(noLocations, isSymmetric);
        }

        private Builder(int noLocations, boolean isSymmetric) {
            this.isSymmetric = isSymmetric;
            matrix = new double[noLocations][noLocations][2];
            this.noLocations = noLocations;
        }

        /**
         * Adds a transport-distance for a particular relation.
         *
         * @param fromIndex from location index
         * @param toIndex   to location index
         * @param distance  the distance to be added
         * @return builder
         */
        public Builder addTransportDistance(int fromIndex, int toIndex, double distance) {
            add(fromIndex, toIndex, 0, distance);
            return this;
        }

        private void add(int fromIndex, int toIndex, int indicatorIndex, double value) {
            if (isSymmetric) {
                if (fromIndex < toIndex) matrix[fromIndex][toIndex][indicatorIndex] = value;
                else matrix[toIndex][fromIndex][indicatorIndex] = value;
            } else matrix[fromIndex][toIndex][indicatorIndex] = value;
        }

        /**
         * Adds transport-time for a particular relation.
         *
         * @param fromIndex from location index
         * @param toIndex   to location index
         * @param time      the time to be added
         * @return builder
         */
        public Builder addTransportTime(int fromIndex, int toIndex, double time) {
            add(fromIndex, toIndex, 1, time);
            return this;
        }

        public Builder addTransportTimeAndDistance(int fromIndex, int toIndex, double time, double distance) {
            addTransportTime(fromIndex, toIndex, time);
            addTransportDistance(fromIndex, toIndex, distance);
            return this;
        }
        /**
         * Builds the matrix.
         *
         * @return matrix
         */
        public FastVehicleRoutingTransportCostsMatrix build() {
            return new FastVehicleRoutingTransportCostsMatrix(this);
        }


    }

    private final boolean isSymmetric;

    private final double[][][] matrix;

    private int noLocations;

    private FastVehicleRoutingTransportCostsMatrix(Builder builder) {
        this.isSymmetric = builder.isSymmetric;
        matrix = builder.matrix;
        noLocations = builder.noLocations;
    }

    /**
     * First dim is from, second to and third indicates whether it is a distance value (index=0) or time value (index=1).
     *
     * @return
     */
    public double[][][] getMatrix() {
        return matrix;
    }

    @Override
    public double getTransportTime(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle) {
        if (from.getIndex() < 0 || to.getIndex() < 0)
            throw new IllegalArgumentException("index of from " + from + " to " + to + " < 0 ");
        int timeIndex = 1;
        return get(from.getIndex(), to.getIndex(), timeIndex);
    }

    private double get(int from, int to, int indicatorIndex) {
        double value;
        if (isSymmetric) {
            if (from < to) value = matrix[from][to][indicatorIndex];
            else value = matrix[to][from][indicatorIndex];
        } else {
            value = matrix[from][to][indicatorIndex];
        }
        return value;
    }

    /**
     * Returns the distance from to to.
     *
     * @param fromIndex from location index
     * @param toIndex   to location index
     * @return the distance
     */
    public double getDistance(int fromIndex, int toIndex) {
        int distanceIndex = 0;
        return get(fromIndex, toIndex, distanceIndex);
    }

    @Override
    public double getDistance(Location from, Location to, double departureTime, Vehicle vehicle) {
        return getDistance(from.getIndex(), to.getIndex());
    }

    @Override
    public double getTransportCost(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle) {
        if (from.getIndex() < 0 || to.getIndex() < 0)
            throw new IllegalArgumentException("index of from " + from + " to " + to + " < 0 ");
        if (vehicle == null) return getDistance(from.getIndex(), to.getIndex());
        VehicleTypeImpl.VehicleCostParams costParams = vehicle.getType().getVehicleCostParams();
        return costParams.perDistanceUnit * getDistance(from.getIndex(), to.getIndex()) + costParams.perTransportTimeUnit * getTransportTime(from, to, departureTime, driver, vehicle);
    }

    public int getNoLocations() {
        return noLocations;
    }


}
