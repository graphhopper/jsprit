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

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Revised radial ruin strategy that operates on route activities instead of jobs.
 * <p>
 * Unlike {@link RuinRadial}, this strategy:
 * <ul>
 *   <li>Includes jobs without static locations (locationless jobs)</li>
 *   <li>Uses actual activity locations from routes rather than pre-computed job distances</li>
 *   <li>Computes distances dynamically at runtime</li>
 * </ul>
 * <p>
 * The trade-off is slightly higher runtime cost O(m·log(k)) vs O(k) per call,
 * where m is the number of activities in routes and k is the number of jobs to remove.
 * For typical VRP sizes, this overhead is negligible.
 *
 * @author schroeder
 */
public final class RuinRadialDynamic extends AbstractRuinStrategy {

    private static final Logger logger = LoggerFactory.getLogger(RuinRadialDynamic.class);

    private final VehicleRoutingTransportCosts transportCosts;

    private final int noJobsToMemorize;

    /**
     * Constructs RuinRadialRevised.
     *
     * @param vrp                the vehicle routing problem
     * @param fraction2beRemoved the share of jobs to be removed (relative to total jobs in vrp)
     */
    public RuinRadialDynamic(VehicleRoutingProblem vrp, double fraction2beRemoved) {
        super(vrp);
        this.transportCosts = vrp.getTransportCosts();
        this.noJobsToMemorize = (int) Math.ceil(vrp.getJobs().size() * fraction2beRemoved);
        this.ruinShareFactory = () -> noJobsToMemorize;
        logger.debug("initialise {}", this);
    }

    /**
     * Constructs RuinRadialRevised.
     *
     * @param vrp              the vehicle routing problem
     * @param noJobs2beRemoved the number of jobs to be removed
     */
    public RuinRadialDynamic(VehicleRoutingProblem vrp, int noJobs2beRemoved) {
        super(vrp);
        this.transportCosts = vrp.getTransportCosts();
        this.noJobsToMemorize = noJobs2beRemoved;
        this.ruinShareFactory = () -> noJobsToMemorize;
        logger.debug("initialise {}", this);
    }

    @Override
    public String toString() {
        return "[name=radialRuinRevised][noJobsToBeRemoved=" + noJobsToMemorize + "]";
    }

    /**
     * Ruins the collection of vehicleRoutes by removing jobs in spatial proximity.
     * <p>
     * Unlike the original RuinRadial, this method:
     * <ol>
     *   <li>Selects a random activity from routes (not from jobs)</li>
     *   <li>Finds nearest neighbor activities based on current route locations</li>
     *   <li>Removes the corresponding jobs from routes</li>
     * </ol>
     * This approach naturally includes locationless jobs since their activities
     * have locations assigned when inserted into routes.
     */
    @Override
    public Collection<Job> ruinRoutes(Collection<VehicleRoute> vehicleRoutes) {
        if (vehicleRoutes.isEmpty()) {
            return Collections.emptyList();
        }

        List<TourActivity.JobActivity> allJobActivities = collectJobActivities(vehicleRoutes);
        if (allJobActivities.isEmpty()) {
            return Collections.emptyList();
        }

        Collection<TourActivity.JobActivity> filteredActivities = filterActivities(allJobActivities);
        if (filteredActivities.isEmpty()) {
            return Collections.emptyList();
        }

        int nOfJobs2BeRemoved = Math.min(ruinShareFactory.createNumberToBeRemoved(), noJobsToMemorize);
        if (nOfJobs2BeRemoved == 0) {
            return Collections.emptyList();
        }
        double share = (double) nOfJobs2BeRemoved / (double) vrp.getJobs().size();
        int nOfActivities2BeRemoved = (int) Math.round(share * filteredActivities.size());
        if (nOfActivities2BeRemoved == 0) {
            return Collections.emptyList();
        }

        TourActivity.JobActivity seedActivity = selectRandomActivity(filteredActivities);
        return ruinRoutes(vehicleRoutes, seedActivity, nOfActivities2BeRemoved, allJobActivities);
    }

    private List<TourActivity.JobActivity> collectJobActivities(Collection<VehicleRoute> vehicleRoutes) {
        List<TourActivity.JobActivity> activities = new ArrayList<>();
        for (VehicleRoute route : vehicleRoutes) {
            for (TourActivity activity : route.getActivities()) {
                if (activity instanceof TourActivity.JobActivity) {
                    activities.add((TourActivity.JobActivity) activity);
                }
            }
        }
        return activities;
    }

    private Collection<TourActivity.JobActivity> filterActivities(List<TourActivity.JobActivity> activities) {
        List<TourActivity.JobActivity> filtered = new ArrayList<>();
        for (TourActivity.JobActivity activity : activities) {
            if (jobFilter.accept(activity.getJob())) {
                filtered.add(activity);
            }
        }
        return filtered.isEmpty() ? activities : filtered;
    }

    private TourActivity.JobActivity selectRandomActivity(Collection<TourActivity.JobActivity> activities) {
        int index = random.nextInt(activities.size());
        if (activities instanceof List) {
            return ((List<TourActivity.JobActivity>) activities).get(index);
        }
        Iterator<TourActivity.JobActivity> iterator = activities.iterator();
        for (int i = 0; i < index; i++) {
            iterator.next();
        }
        return iterator.next();
    }

    private Collection<Job> ruinRoutes(Collection<VehicleRoute> vehicleRoutes,
                                       TourActivity.JobActivity seedActivity,
                                       int nOfActivities2BeRemoved,
                                       List<TourActivity.JobActivity> allJobActivities) {
        List<Job> unassignedJobs = new ArrayList<>();
        Set<String> removedJobIds = new HashSet<>();

        Job seedJob = seedActivity.getJob();
        int removedActivities = 0;
        if (removeJob(seedJob, vehicleRoutes)) {
            unassignedJobs.add(seedJob);
            removedJobIds.add(seedJob.getId());
            removedActivities += seedJob.getActivities().size();
        }

        if (nOfActivities2BeRemoved <= 1) {
            return unassignedJobs;
        }

        Location seedLocation = seedActivity.getLocation();
        int nNeighbors = nOfActivities2BeRemoved - removedActivities;

        List<JobActivityDistance> nearestNeighbors = findNearestNeighbors(
                seedLocation, allJobActivities, removedJobIds, nNeighbors);

        for (JobActivityDistance neighbor : nearestNeighbors) {
            if (removedActivities >= nOfActivities2BeRemoved) {
                return unassignedJobs;
            }
            Job job = neighbor.activity.getJob();
            if (!removedJobIds.contains(job.getId())) {
                if (removeJob(job, vehicleRoutes)) {
                    unassignedJobs.add(job);
                    removedJobIds.add(job.getId());
                    removedActivities += job.getActivities().size();
                }
            }
        }

        return unassignedJobs;
    }

    private List<JobActivityDistance> findNearestNeighbors(Location seedLocation,
                                                           List<TourActivity.JobActivity> allActivities,
                                                           Set<String> excludeJobIds,
                                                           int k) {
        PriorityQueue<JobActivityDistance> maxHeap = new PriorityQueue<>(
                k + 1,
                (a, b) -> Double.compare(b.distance, a.distance)
        );

        for (TourActivity.JobActivity activity : allActivities) {
            if (excludeJobIds.contains(activity.getJob().getId())) {
                continue;
            }

            Location activityLocation = activity.getLocation();
            if (activityLocation == null) {
                continue;
            }

            double distance = transportCosts.getDistance(seedLocation, activityLocation, 0.0, null);

            if (maxHeap.size() < k) {
                maxHeap.offer(new JobActivityDistance(activity, distance));
            } else if (distance < maxHeap.peek().distance) {
                maxHeap.poll();
                maxHeap.offer(new JobActivityDistance(activity, distance));
            }
        }

        List<JobActivityDistance> result = new ArrayList<>(maxHeap);
        result.sort(Comparator.comparingDouble(a -> a.distance));
        return result;
    }

    private static class JobActivityDistance {
        final TourActivity.JobActivity activity;
        final double distance;

        JobActivityDistance(TourActivity.JobActivity activity, double distance) {
            this.activity = activity;
            this.distance = distance;
        }
    }
}
