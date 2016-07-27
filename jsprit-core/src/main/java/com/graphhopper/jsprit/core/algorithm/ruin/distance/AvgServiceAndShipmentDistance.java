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
package com.graphhopper.jsprit.core.algorithm.ruin.distance;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.util.EuclideanDistanceCalculator;


/**
 * Calculator that calculates average distance between two jobs based on the input-transport costs.
 * <p>
 * <p>If the distance between two jobs cannot be calculated with input-transport costs, it tries the euclidean distance between these jobs.
 *
 * @author stefan schroeder
 */
public class AvgServiceAndShipmentDistance implements JobDistance {

    private VehicleRoutingTransportCosts costs;

    public AvgServiceAndShipmentDistance(VehicleRoutingTransportCosts costs) {
        super();
        this.costs = costs;

    }

    /**
     * Calculates and returns the average distance between two jobs based on the input-transport costs.
     * <p>
     * <p>If the distance between two jobs cannot be calculated with input-transport costs, it tries the euclidean distance between these jobs.
     */
    @Override
    public double getDistance(Job i, Job j) {
        if (i.equals(j)) return 0.0;

        if (i instanceof Service && j instanceof Service) {
            return calcDist((Service) i, (Service) j);
        } else if (i instanceof Service && j instanceof Shipment) {
            return calcDist((Service) i, (Shipment) j);
        } else if (i instanceof Shipment && j instanceof Service) {
            return calcDist((Service) j, (Shipment) i);
        } else if (i instanceof Shipment && j instanceof Shipment) {
            return calcDist((Shipment) i, (Shipment) j);
        } else {
            throw new IllegalStateException("this supports only shipments or services");
        }
    }

    private double calcDist(Service i, Service j) {
        return calcDist(i.getLocation(), j.getLocation());
    }

    private double calcDist(Service i, Shipment j) {
        double c_ij1 = calcDist(i.getLocation(), j.getPickupLocation());
        double c_ij2 = calcDist(i.getLocation(), j.getDeliveryLocation());
        return (c_ij1 + c_ij2) / 2.0;
    }

    private double calcDist(Shipment i, Shipment j) {
        double c_i1j1 = calcDist(i.getPickupLocation(), j.getPickupLocation());
        double c_i1j2 = calcDist(i.getPickupLocation(), j.getDeliveryLocation());
        double c_i2j1 = calcDist(i.getDeliveryLocation(), j.getPickupLocation());
        double c_i2j2 = calcDist(i.getDeliveryLocation(), j.getDeliveryLocation());
        return (c_i1j1 + c_i1j2 + c_i2j1 + c_i2j2) / 4.0;
    }

    private double calcDist(Location location_i, Location location_j) {
        try {
            return costs.getTransportCost(location_i, location_j, 0.0, null, null);
        } catch (IllegalStateException e) {
            // now try the euclidean distance between these two services
        }
        return EuclideanDistanceCalculator.calculateDistance(location_i.getCoordinate(), location_j.getCoordinate());
    }
}
