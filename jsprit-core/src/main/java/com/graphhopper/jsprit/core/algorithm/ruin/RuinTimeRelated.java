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
package com.graphhopper.jsprit.core.algorithm.ruin;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.util.NoiseMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * @author stefan schroeder
 */

public final class RuinTimeRelated extends AbstractRuinStrategy {

    static class RelatednessToTourActivity {

        final double time;

        final TourActivity tourActivity;

        final VehicleRoute route;

        final double distance;

        public RelatednessToTourActivity(double relatednessToTarget, double distance, TourActivity tourActivity, VehicleRoute route) {
            this.time = relatednessToTarget;
            this.distance = distance;
            this.tourActivity = tourActivity;
            this.route = route;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(RuinTimeRelated.class);

    private final VehicleRoutingProblem vrp;

    private NoiseMaker noiseMaker = () -> 0;

    public void setNoiseMaker(NoiseMaker noiseMaker) {
        this.noiseMaker = noiseMaker;
    }

    public RuinTimeRelated(VehicleRoutingProblem vrp) {
        super(vrp);
        this.vrp = vrp;
        this.ruinShareFactory = () -> (int) Math.max(50, Math.round(vrp.getJobs().size() * 0.3));
        logger.debug("initialise {}", this);
    }

    /**
     * Removes a fraction of jobs from vehicleRoutes.
     * <p>
     * <p>The number of jobs is calculated as follows: Math.ceil(vrp.getJobs().values().size() * fractionOfAllNodes2beRuined).
     */
    @Override
    public Collection<Job> ruinRoutes(Collection<VehicleRoute> vehicleRoutes) {
        List<Job> unassignedJobs = new ArrayList<>();
        int totalActivities = 0;
        for (VehicleRoute route : vehicleRoutes) {
            totalActivities += route.getActivities().size();
        }
        if (totalActivities == 0) return unassignedJobs;
        int randomIndex = random.nextInt(totalActivities);
        int numActs = 0;
        TourActivity targetActivity = null;
        for (VehicleRoute route : vehicleRoutes) {
            if (numActs + route.getActivities().size() < randomIndex) {
                numActs += route.getActivities().size();
            } else {
                for (TourActivity activity : route.getActivities()) {
                    if (numActs == randomIndex) {
                        targetActivity = activity;
                        break;
                    }
                    numActs++;
                }
                if (targetActivity != null) {
                    break;
                }
            }
        }
        if (targetActivity == null) {
            return unassignedJobs;
        }
        List<RelatednessToTourActivity> neighborActivities = new ArrayList<>();
        long maxTime = 0;
        double maxDistance = 0;
        for (VehicleRoute route : vehicleRoutes) {
            for (TourActivity activity : route.getActivities()) {
                if (activity == targetActivity) continue;
                long absTime = Math.abs((long) targetActivity.getArrTime() - (long) activity.getArrTime());
                maxTime = Math.max(maxTime, absTime);
                double distance = Math.abs(vrp.getTransportCosts().getDistance(targetActivity.getLocation(), activity.getLocation(), 0, route.getVehicle()));
                maxDistance = Math.max(maxDistance, distance);
                neighborActivities.add(new RelatednessToTourActivity(absTime, distance, activity, route));
            }
        }
        final double maxT = maxTime;
        final double maxD = maxTime;
        final double timeI = 10;
        final double distanceI;
        double distanceInfluence = 1;
        if (random.nextDouble() < 0.5) {
            distanceI = 0;
        } else distanceI = distanceInfluence;
        neighborActivities.sort((o1, o2) -> {
            double rO1 = relatedness(o1, maxD, maxT, timeI, distanceI);
            double rO2 = relatedness(o2, maxD, maxT, timeI, distanceI);
            return Double.compare(rO1, rO2);
        });
        int toRemove = getRuinShareFactory().createNumberToBeRemoved();
        for (RelatednessToTourActivity neighborActivity : neighborActivities) {
            if (toRemove == 0) break;
            Job j = ((TourActivity.JobActivity) neighborActivity.tourActivity).getJob();
            if (removeJob(j, neighborActivity.route)) {
                unassignedJobs.add(j);
                toRemove--;
            }
        }
        return unassignedJobs;
    }

    private double relatedness(RelatednessToTourActivity o1, double maxDistance, double maxTime, double timeI, double distanceI) {
        return timeI * o1.time / maxTime + distanceI * o1.distance / maxDistance;
    }

    @Override
    public String toString() {
        return "[name=timeRelatedRuin]";
    }

}
