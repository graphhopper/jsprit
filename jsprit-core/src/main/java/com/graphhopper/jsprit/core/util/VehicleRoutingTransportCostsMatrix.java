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
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl.VehicleCostParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


/**
 * CostMatrix that allows pre-compiled time and distance-matrices to be considered as {@link com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts}
 * in the {@link com.graphhopper.jsprit.core.problem.VehicleRoutingProblem}.
 * <p>Note that you can also use it with distance matrix only (or time matrix). But ones
 * you set a particular distance, this expects distance-entries for all relations. This counts also
 * for a particular time. If the method getTransportCosts(...) is then invoked for a relation, where no distance can be found, an
 * IllegalStateException will be thrown. Thus if you want to only use distances only, do not use addTransportTime(...).
 *
 * @author schroeder
 */
public class VehicleRoutingTransportCostsMatrix extends AbstractForwardVehicleRoutingTransportCosts {

    static class RelationKey {

        static RelationKey newKey(String from, String to) {
            return new RelationKey(from, to);
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


    /**
     * Builder that builds the matrix.
     *
     * @author schroeder
     */
    public static class Builder {
        private static Logger log = LoggerFactory.getLogger(Builder.class);

        private boolean isSymmetric;

        private Map<RelationKey, Double> distances = new HashMap<RelationKey, Double>();

        private Map<RelationKey, Double> times = new HashMap<RelationKey, Double>();

        private boolean distancesSet = false;

        private boolean timesSet = false;

        /**
         * Creates a new builder returning the matrix-builder.
         * <p>If you want to consider symmetric matrices, set isSymmetric to true.
         *
         * @param isSymmetric true if matrix is symmetric, false otherwise
         * @return builder
         */
        public static Builder newInstance(boolean isSymmetric) {
            return new Builder(isSymmetric);
        }

        private Builder(boolean isSymmetric) {
            this.isSymmetric = isSymmetric;
        }

        /**
         * Adds a transport-distance for a particular relation.
         *
         * @param from     from loactionId
         * @param to       to locationId
         * @param distance the distance to be added
         * @return builder
         */
        public Builder addTransportDistance(String from, String to, double distance) {
            RelationKey key = RelationKey.newKey(from, to);
            if (!distancesSet) distancesSet = true;
            if (distances.containsKey(key)) {
                log.warn("distance from " + from + " to " + to + " already exists. This overrides distance.");
            }
            distances.put(key, distance);
            if (isSymmetric) {
                RelationKey revKey = RelationKey.newKey(to, from);
                if (distances.containsKey(revKey)) distances.put(revKey, distance);
            }
            return this;
        }

        /**
         * Adds transport-time for a particular relation.
         *
         * @param from from locationId
         * @param to   to locationId
         * @param time the time to be added
         * @return builder
         */
        public Builder addTransportTime(String from, String to, double time) {
            RelationKey key = RelationKey.newKey(from, to);
            if (!timesSet) timesSet = true;
            if (times.containsKey(key)) {
                log.warn("transport-time from " + from + " to " + to + " already exists. This overrides times.");
            }
            times.put(key, time);
            if (isSymmetric) {
                RelationKey revKey = RelationKey.newKey(to, from);
                if (times.containsKey(revKey)) times.put(revKey, time);
            }
            return this;
        }

        /**
         * Builds the matrix.
         *
         * @return matrix
         */
        public VehicleRoutingTransportCostsMatrix build() {
            return new VehicleRoutingTransportCostsMatrix(this);
        }


    }

    private Map<RelationKey, Double> distances = new HashMap<RelationKey, Double>();

    private Map<RelationKey, Double> times = new HashMap<RelationKey, Double>();

    private boolean isSymmetric;

    private boolean timesSet;

    private boolean distancesSet;

    private VehicleRoutingTransportCostsMatrix(Builder builder) {
        this.isSymmetric = builder.isSymmetric;
        distances.putAll(builder.distances);
        times.putAll(builder.times);
        timesSet = builder.timesSet;
        distancesSet = builder.distancesSet;
    }


    @Override
    public double getTransportTime(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle) {
        return getTime(from.getId(), to.getId());
    }


    private double getTime(String fromId, String toId) {
        if (fromId.equals(toId)) return 0.0;
        if (!timesSet) return 0.0;
        RelationKey key = RelationKey.newKey(fromId, toId);
        if (!isSymmetric) {
            if (times.containsKey(key)) return times.get(key);
            else
                throw new IllegalStateException("time value for relation from " + fromId + " to " + toId + " does not exist");
        } else {
            Double time = times.get(key);
            if (time == null) {
                time = times.get(RelationKey.newKey(toId, fromId));
            }
            if (time != null) return time;
            else
                throw new IllegalStateException("time value for relation from " + fromId + " to " + toId + " does not exist");
        }
    }

    /**
     * Returns the distance fromId to toId.
     *
     * @param fromId from locationId
     * @param toId   to locationId
     * @return the distance from fromId to toId
     * @throws IllegalStateException if distance of fromId -> toId is not found
     */
    public double getDistance(String fromId, String toId) {
        if (fromId.equals(toId)) return 0.0;
        if (!distancesSet) return 0.0;
        RelationKey key = RelationKey.newKey(fromId, toId);
        if (!isSymmetric) {
            if (distances.containsKey(key)) return distances.get(key);
            else
                throw new IllegalStateException("distance value for relation from " + fromId + " to " + toId + " does not exist");
        } else {
            Double time = distances.get(key);
            if (time == null) {
                time = distances.get(RelationKey.newKey(toId, fromId));
            }
            if (time != null) return time;
            else
                throw new IllegalStateException("distance value for relation from " + fromId + " to " + toId + " does not exist");
        }
    }

    @Override
    public double getTransportCost(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle) {
        if (vehicle == null) return getDistance(from.getId(), to.getId());
        VehicleCostParams costParams = vehicle.getType().getVehicleCostParams();
        return costParams.perDistanceUnit * getDistance(from.getId(), to.getId()) + costParams.perTransportTimeUnit * getTime(from.getId(), to.getId());
    }

    @Override
    public double getDistance(Location from, Location to, double departureTime, Vehicle vehicle) {
        return getDistance(from.getId(), to.getId());
    }

}
