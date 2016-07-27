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

import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;


/**
 * Calculator that calculates average distance between two jobs based on the input-transport costs.
 * <p>
 * <p>If the distance between two jobs cannot be calculated with input-transport costs, it tries the euclidean distance between these jobs.
 *
 * @author stefan schroeder
 */
public class AvgServiceDistance implements JobDistance {

    private VehicleRoutingTransportCosts costs;

    public AvgServiceDistance(VehicleRoutingTransportCosts costs) {
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
        double avgCost = 0.0;
        if (i instanceof Service && j instanceof Service) {
            if (i.equals(j)) {
                avgCost = 0.0;
            } else {
                Service s_i = (Service) i;
                Service s_j = (Service) j;
                avgCost = calcDist(s_i, s_j);
            }
        } else {
            throw new UnsupportedOperationException(
                "currently, this class just works services.");
        }
        return avgCost;
    }

    private double calcDist(Service s_i, Service s_j) {
        double distance;
        try {
            distance = costs.getTransportCost(s_i.getLocation(), s_j.getLocation(), 0.0, null, null);
            return distance;
        } catch (IllegalStateException e) {
            // now try the euclidean distance between these two services
        }
        EuclideanServiceDistance euclidean = new EuclideanServiceDistance();
        distance = euclidean.getDistance(s_i, s_j);
        return distance;
    }

}
