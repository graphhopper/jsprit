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
import com.graphhopper.jsprit.core.problem.driver.DriverImpl;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.util.NoiseMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 * Ruin strategy that ruins current solution randomly. I.e.
 * customer are removed randomly from current solution.
 *
 * @author stefan schroeder
 */

public final class RuinWorst extends AbstractRuinStrategy {

    private static Logger logger = LoggerFactory.getLogger(RuinWorst.class);

    private VehicleRoutingProblem vrp;

    private NoiseMaker noiseMaker = () -> 0;

    public void setNoiseMaker(NoiseMaker noiseMaker) {
        this.noiseMaker = noiseMaker;
    }

    public RuinWorst(VehicleRoutingProblem vrp, final int initialNumberJobsToRemove) {
        super(vrp);
        this.vrp = vrp;
        setRuinShareFactory(() -> initialNumberJobsToRemove);
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
        int nOfJobs2BeRemoved = getRuinShareFactory().createNumberToBeRemoved();
        ruin(vehicleRoutes, nOfJobs2BeRemoved, unassignedJobs);
        return unassignedJobs;
    }

    private void ruin(Collection<VehicleRoute> vehicleRoutes, int nOfJobs2BeRemoved, List<Job> unassignedJobs) {
        int toRemove = nOfJobs2BeRemoved;
        Set<Job> tabu = new HashSet<>();
        while (toRemove > 0) {
            Job worst = getWorst(vehicleRoutes, tabu);
            if (worst == null) break;
            if (removeJob(worst, vehicleRoutes)) {
                unassignedJobs.add(worst);
            } else {
                tabu.add(worst);
            }
            toRemove--;
        }
    }

    private Job getWorst(Collection<VehicleRoute> routes, Set<Job> tabu) {
        if (routes.isEmpty()) return null;

        Job worst = null;
        double bestSavings = Double.MIN_VALUE;
        Map<Job, Double> savingsMap = new HashMap<>();

        // Calculate savings for all jobs across all routes
        for (VehicleRoute route : routes) {
            if (route.isEmpty()) continue;

            calculateSavingsForRoute(route, savingsMap);
        }

        // If no jobs have been evaluated, return null
        if (savingsMap.isEmpty()) return null;

        // Find the job with highest savings (worst job to keep)
        for (Map.Entry<Job, Double> entry : savingsMap.entrySet()) {
            Job job = entry.getKey();
            double savings = entry.getValue();

            if (tabu.contains(job) || !vrp.getJobs().containsKey(job.getId())) {
                continue;
            }

            // Skip jobs that don't pass the filter
            if (!jobFilter.accept(job)) {
                continue;
            }

            if (savings > bestSavings) {
                bestSavings = savings;
                worst = job;
            }
        }

        return worst;
    }

    private void calculateSavingsForRoute(VehicleRoute route, Map<Job, Double> savingsMap) {
        if (route.isEmpty()) return;

        TourActivity actBefore = route.getStart();
        TourActivity actToEval = null;

        for (TourActivity act : route.getActivities()) {
            if (!(act instanceof TourActivity.JobActivity)) {
                continue;
            }

            if (actToEval == null) {
                actToEval = act;
                continue;
            }

            double savings = savings(route, actBefore, actToEval, act);
            Job job = ((TourActivity.JobActivity) actToEval).getJob();

            // Add to savings map
            savingsMap.merge(job, savings, Double::sum);

            actBefore = actToEval;
            actToEval = act;
        }

        // Process the last activity
        if (actToEval != null) {
            double savings = savings(route, actBefore, actToEval, route.getEnd());
            Job job = ((TourActivity.JobActivity) actToEval).getJob();

            // Add to savings map
            savingsMap.merge(job, savings, Double::sum);
        }
    }

    private double savings(VehicleRoute route, TourActivity actBefore, TourActivity actToEval, TourActivity act) {
        double savings = c(actBefore, actToEval, route.getVehicle()) + c(actToEval, act, route.getVehicle()) - c(actBefore, act, route.getVehicle());
        return Math.max(0, savings + noiseMaker.makeNoise());
    }

    private double c(TourActivity from, TourActivity to, Vehicle vehicle) {
        return vrp.getTransportCosts().getTransportCost(from.getLocation(), to.getLocation(), from.getEndTime(), DriverImpl.noDriver(), vehicle);
    }

    @Override
    public String toString() {
        return "[name=worstRuin]";
    }

}
