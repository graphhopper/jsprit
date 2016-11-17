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

package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

/**
 * Created by schroeder on 15/10/15.
 */
public class DefaultScorer implements ScoringFunction {

    private VehicleRoutingProblem vrp;

    private double timeWindowParam = -0.5;

    private double depotDistanceParam = +0.1;

    private double minTimeWindowScore = -100000;

    public DefaultScorer(VehicleRoutingProblem vrp) {
        this.vrp = vrp;
    }

    public void setTimeWindowParam(double tw_param) {
        timeWindowParam = tw_param;
    }

    public void setDepotDistanceParam(double depotDistance_param) {
        depotDistanceParam = depotDistance_param;
    }

    @Override
    public double score(InsertionData best, Job job) {
        Vehicle selectedVehicle = best.getSelectedVehicle();
        double maxFromStart = job.getAllLocations().stream()
            .mapToDouble(l -> getDistance(selectedVehicle.getStartLocation(), l))
            .max()
            .orElse(0d);
        double maxToEnd = job.getAllLocations().stream()
            .mapToDouble(l -> getDistance(selectedVehicle.getEndLocation(), l))
            .max()
            .orElse(0d);
        double maxDepotDistance = Math.max(maxFromStart, maxToEnd);
        double minTimeToOperate = job.getTimeWindows().stream()
            .mapToDouble(tw -> tw.getEnd() - tw.getStart())
            .min()
            .orElse(0d);
        return Math.max(timeWindowParam * minTimeToOperate, minTimeWindowScore) + depotDistanceParam * maxDepotDistance;
    }
//
//        double score;
//        if (job instanceofx Service) {
//            score = scoreService(best, job);
//        } else if (job instanceofx Shipment) {
//            score = scoreShipment(best, job);
//        } else {
//            throw new IllegalStateException("not supported");
//        }
//        System.out.format("OLD SCORE: %6.2f   NEW SCORE: %6.2f    PASS: %s\n", score, calculateScore(best, job),
//                (score == calculateScore(best, job) ? "true" : "false"));
//        return score;
//    }
//
//
//    private double scoreShipment(InsertionData best, Job job) {
//        Shipment shipment = (Shipment) job;
//        double maxDepotDistance_1 = Math.max(
//                getDistance(best.getSelectedVehicle().getStartLocation(), shipment.getPickupLocation()),
//                getDistance(best.getSelectedVehicle().getStartLocation(), shipment.getDeliveryLocation())
//                );
//        double maxDepotDistance_2 = Math.max(
//                getDistance(best.getSelectedVehicle().getEndLocation(), shipment.getPickupLocation()),
//                getDistance(best.getSelectedVehicle().getEndLocation(), shipment.getDeliveryLocation())
//                );
//        double maxDepotDistance = Math.max(maxDepotDistance_1, maxDepotDistance_2);
//        double minTimeToOperate = Math.min(shipment.getPickupTimeWindow().getEnd() - shipment.getPickupTimeWindow().getStart(),
//                shipment.getDeliveryTimeWindow().getEnd() - shipment.getDeliveryTimeWindow().getStart());
//        return Math.max(timeWindowParam * minTimeToOperate, minTimeWindowScore) + depotDistanceParam * maxDepotDistance;
//    }
//
//    private double scoreService(InsertionData best, Job job) {
//        Location location = ((Service) job).getLocation();
//        double maxDepotDistance = 0;
//        if (location != null) {
//            maxDepotDistance = Math.max(
//                    getDistance(best.getSelectedVehicle().getStartLocation(), location),
//                    getDistance(best.getSelectedVehicle().getEndLocation(), location)
//                    );
//        }
//        return Math.max(timeWindowParam * (((Service) job).getTimeWindow().getEnd() - ((Service) job).getTimeWindow().getStart()), minTimeWindowScore) +
//                depotDistanceParam * maxDepotDistance;
//    }
//

    private double getDistance(Location loc1, Location loc2) {
        return vrp.getTransportCosts().getTransportCost(loc1, loc2, 0., null, null);
    }

    @Override
    public String toString() {
        return "[name=defaultScorer][twParam=" + timeWindowParam + "][depotDistanceParam=" + depotDistanceParam + "]";
    }
}
