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

        // Calculate savings once for all jobs - O(n) instead of O(k*n)
        Map<Job, Double> jobToSavings = new HashMap<>();
        NavigableMap<Double, Set<Job>> savingsToJobs = new TreeMap<>();
        Map<Job, VehicleRoute> jobToRoute = new HashMap<>();

        initializeSavings(vehicleRoutes, jobToSavings, savingsToJobs, jobToRoute);

        while (toRemove > 0) {
            Job worst = getWorstFromSortedMap(savingsToJobs, jobToSavings, tabu);
            if (worst == null) break;

            VehicleRoute affectedRoute = jobToRoute.get(worst);

            // Collect neighbors before removal for incremental update
            List<Job> neighborsToUpdate = getNeighborJobs(worst, affectedRoute);

            if (removeJob(worst, vehicleRoutes)) {
                unassignedJobs.add(worst);
                // Remove the job from our tracking structures
                removeJobFromSavingsMap(worst, jobToSavings, savingsToJobs);
                jobToRoute.remove(worst);

                // Incrementally update only the affected neighbors
                if (affectedRoute != null && !affectedRoute.isEmpty()) {
                    updateNeighborSavings(neighborsToUpdate, affectedRoute, jobToSavings, savingsToJobs);
                }
            } else {
                tabu.add(worst);
            }
            toRemove--;
        }
    }

    private void initializeSavings(Collection<VehicleRoute> vehicleRoutes,
                                   Map<Job, Double> jobToSavings,
                                   NavigableMap<Double, Set<Job>> savingsToJobs,
                                   Map<Job, VehicleRoute> jobToRoute) {
        for (VehicleRoute route : vehicleRoutes) {
            if (route.isEmpty()) continue;
            calculateSavingsForRoute(route, jobToSavings, savingsToJobs, jobToRoute);
        }
    }

    private Job getWorstFromSortedMap(NavigableMap<Double, Set<Job>> savingsToJobs,
                                      Map<Job, Double> jobToSavings,
                                      Set<Job> tabu) {
        // Iterate from highest savings to lowest - O(log n) to get highest entry
        while (!savingsToJobs.isEmpty()) {
            Map.Entry<Double, Set<Job>> highestEntry = savingsToJobs.lastEntry();
            if (highestEntry == null) return null;

            Set<Job> jobs = highestEntry.getValue();
            Iterator<Job> iterator = jobs.iterator();

            while (iterator.hasNext()) {
                Job job = iterator.next();

                if (tabu.contains(job) || !vrp.getJobs().containsKey(job.getId())) {
                    continue;
                }

                if (!jobFilter.accept(job)) {
                    continue;
                }

                return job;
            }

            // All jobs at this savings level are filtered out, try next level
            savingsToJobs.pollLastEntry();
        }
        return null;
    }

    private List<Job> getNeighborJobs(Job job, VehicleRoute route) {
        List<Job> neighbors = new ArrayList<>();
        if (route == null || route.isEmpty()) return neighbors;

        List<TourActivity> activities = route.getActivities();
        Set<Integer> jobActivityIndices = new HashSet<>();

        // Find all activity indices for this job
        for (int i = 0; i < activities.size(); i++) {
            TourActivity act = activities.get(i);
            if (act instanceof TourActivity.JobActivity) {
                if (((TourActivity.JobActivity) act).getJob().equals(job)) {
                    jobActivityIndices.add(i);
                }
            }
        }

        // Find neighbor jobs (activities adjacent to any of the job's activities)
        Set<Job> neighborSet = new HashSet<>();
        for (int idx : jobActivityIndices) {
            // Check activity before
            if (idx > 0) {
                TourActivity prevAct = activities.get(idx - 1);
                if (prevAct instanceof TourActivity.JobActivity) {
                    Job neighborJob = ((TourActivity.JobActivity) prevAct).getJob();
                    if (!neighborJob.equals(job)) {
                        neighborSet.add(neighborJob);
                    }
                }
            }
            // Check activity after
            if (idx < activities.size() - 1) {
                TourActivity nextAct = activities.get(idx + 1);
                if (nextAct instanceof TourActivity.JobActivity) {
                    Job neighborJob = ((TourActivity.JobActivity) nextAct).getJob();
                    if (!neighborJob.equals(job)) {
                        neighborSet.add(neighborJob);
                    }
                }
            }
        }

        neighbors.addAll(neighborSet);
        return neighbors;
    }

    private void removeJobFromSavingsMap(Job job, Map<Job, Double> jobToSavings,
                                         NavigableMap<Double, Set<Job>> savingsToJobs) {
        Double oldSavings = jobToSavings.remove(job);
        if (oldSavings != null) {
            Set<Job> jobsAtSavings = savingsToJobs.get(oldSavings);
            if (jobsAtSavings != null) {
                jobsAtSavings.remove(job);
                if (jobsAtSavings.isEmpty()) {
                    savingsToJobs.remove(oldSavings);
                }
            }
        }
    }

    private void updateNeighborSavings(List<Job> neighbors, VehicleRoute route,
                                       Map<Job, Double> jobToSavings,
                                       NavigableMap<Double, Set<Job>> savingsToJobs) {
        for (Job neighbor : neighbors) {
            // Remove old savings entry
            removeJobFromSavingsMap(neighbor, jobToSavings, savingsToJobs);

            // Recalculate savings for this job
            double newSavings = calculateSavingsForJob(neighbor, route);

            // Add new savings entry
            jobToSavings.put(neighbor, newSavings);
            savingsToJobs.computeIfAbsent(newSavings, k -> new HashSet<>()).add(neighbor);
        }
    }

    private double calculateSavingsForJob(Job job, VehicleRoute route) {
        double totalSavings = 0.0;
        List<TourActivity> activities = route.getActivities();

        for (int i = 0; i < activities.size(); i++) {
            TourActivity act = activities.get(i);
            if (!(act instanceof TourActivity.JobActivity)) continue;
            if (!((TourActivity.JobActivity) act).getJob().equals(job)) continue;

            TourActivity actBefore = (i == 0) ? route.getStart() : activities.get(i - 1);
            TourActivity actAfter = (i == activities.size() - 1) ? route.getEnd() : activities.get(i + 1);

            totalSavings += savings(route, actBefore, act, actAfter);
        }

        return totalSavings;
    }

    private void calculateSavingsForRoute(VehicleRoute route,
                                          Map<Job, Double> jobToSavings,
                                          NavigableMap<Double, Set<Job>> savingsToJobs,
                                          Map<Job, VehicleRoute> jobToRoute) {
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

            // Update savings tracking structures
            updateSavingsStructures(job, savings, route, jobToSavings, savingsToJobs, jobToRoute);

            actBefore = actToEval;
            actToEval = act;
        }

        // Process the last activity
        if (actToEval != null) {
            double savings = savings(route, actBefore, actToEval, route.getEnd());
            Job job = ((TourActivity.JobActivity) actToEval).getJob();

            updateSavingsStructures(job, savings, route, jobToSavings, savingsToJobs, jobToRoute);
        }
    }

    private void updateSavingsStructures(Job job, double additionalSavings, VehicleRoute route,
                                         Map<Job, Double> jobToSavings,
                                         NavigableMap<Double, Set<Job>> savingsToJobs,
                                         Map<Job, VehicleRoute> jobToRoute) {
        // Remove old entry from savingsToJobs if exists
        Double oldSavings = jobToSavings.get(job);
        if (oldSavings != null) {
            Set<Job> oldSet = savingsToJobs.get(oldSavings);
            if (oldSet != null) {
                oldSet.remove(job);
                if (oldSet.isEmpty()) {
                    savingsToJobs.remove(oldSavings);
                }
            }
        }

        // Calculate new total savings
        double newSavings = (oldSavings != null ? oldSavings : 0.0) + additionalSavings;

        // Update jobToSavings
        jobToSavings.put(job, newSavings);

        // Update savingsToJobs
        savingsToJobs.computeIfAbsent(newSavings, k -> new HashSet<>()).add(job);

        // Update jobToRoute
        jobToRoute.put(job, route);
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
