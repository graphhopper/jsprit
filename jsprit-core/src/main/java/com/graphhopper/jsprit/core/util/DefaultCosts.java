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
/**
 *
 */
package com.graphhopper.jsprit.core.util;

import com.graphhopper.jsprit.core.distance.DistanceCalculator;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.cost.AbstractForwardVehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.cost.TransportDistance;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;


/**
 * @author stefan schroeder
 */
public class DefaultCosts extends AbstractForwardVehicleRoutingTransportCosts implements TransportDistance {

    private double detourFactor = 1.0;

    private DistanceCalculator distanceCalculator;

    private Locations coordinateConverter;

    public DefaultCosts(DistanceCalculator distanceCalculator) {
        super();
        this.distanceCalculator = distanceCalculator;
    }

    public Locations getPredefinedLocations() {
        return coordinateConverter;
    }

    public DefaultCosts withCoordinateConverter(Locations coordinateConverter) {
        this.coordinateConverter = coordinateConverter;
        return this;
    }

    public double getDetourFactor() {
        return detourFactor;
    }

    public DefaultCosts withDetourFactor(double detourFactor) {
        this.detourFactor = detourFactor;
        return this;
    }

    @Override
    public String toString() {
        return "[name=" + distanceCalculator.getName() + "]";
    }

    @Override
    public double getTransportCost(Location from, Location to, double time, Driver driver, Vehicle vehicle) {
        double distance;
        try {
            distance = calculateDistance(from, to);
        } catch (NullPointerException e) {
            throw new NullPointerException("cannot calculate euclidean distance. coordinates are missing. either add coordinates or use another transport-cost-calculator.");
        }
        double costs = distance;
        if (vehicle != null) {
            if (vehicle.getType() != null) {
                costs = distance * vehicle.getType().getVehicleCostParams().perDistanceUnit;
            }
        }
        return costs;
    }

    private double calculateDistance(Location fromLocation, Location toLocation) {
        Coordinate from = getCoordinate(fromLocation);
        Coordinate to = getCoordinate(toLocation);

        return distanceCalculator.calculateDistance(from, to) * detourFactor;
    }


    protected Coordinate getCoordinate(Location location) {
        if (location.getCoordinate() != null) {
            return location.getCoordinate();
        } else if (coordinateConverter != null) {
            return coordinateConverter.getCoord(location.getId());
        }
        throw new NullPointerException("Coordinates are missing in " + location);
    }

    @Override
    public double getTransportTime(Location from, Location to, double time, Driver driver, Vehicle vehicle) {
        double distance = calculateDistance(from, to);
        if (vehicle != null) {
            if (vehicle.getType() != null) {
                distance = distance / vehicle.getType().getAvgVelocity();
            }
        }

        return distance;
    }

    @Override
    public double getDistance(Location from, Location to, double departureTime, Vehicle vehicle) {
        return calculateDistance(from, to);
    }
}
