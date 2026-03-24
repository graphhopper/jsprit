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
import com.graphhopper.jsprit.core.util.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Ruin strategy that removes clusters of jobs identified using Kruskal's MST algorithm.
 * <p>
 * Algorithm:
 * 1. Select target route (random or via job neighborhoods)
 * 2. Build MST of jobs in route using Kruskal's algorithm
 * 3. Cut longest edge to get 2 clusters
 * 4. Remove one cluster
 * 5. Repeat with neighboring routes until q jobs removed
 * <p>
 * Ranked #2 in Voigt (2025) "A review and ranking of operators in adaptive large
 * neighborhood search for vehicle routing problems."
 * <p>
 * Advantages over DBSCAN-based clustering:
 * - No parameters to tune (ε, minPts)
 * - Always produces exactly 2 clusters
 * - Never fails (no fallback needed)
 * - Deterministic given the distances
 */
public final class RuinKruskalClusters extends AbstractRuinStrategy {

    private static final Logger logger = LoggerFactory.getLogger(RuinKruskalClusters.class);

    private final VehicleRoutingProblem vrp;
    private final JobNeighborhoods jobNeighborhoods;
    private final KruskalClusterer clusterer;
    private boolean preferSmallerCluster = false;

    public RuinKruskalClusters(VehicleRoutingProblem vrp, int initialNumberJobsToRemove,
                               JobNeighborhoods jobNeighborhoods) {
        super(vrp);
        this.vrp = vrp;
        this.jobNeighborhoods = jobNeighborhoods;
        this.clusterer = new KruskalClusterer(vrp.getTransportCosts());
        setRuinShareFactory(() -> initialNumberJobsToRemove);
        logger.debug("initialise {}", this);
    }

    /**
     * If true, prefers removing the smaller of the two clusters.
     * If false, randomly chooses between clusters.
     */
    public void setPreferSmallerCluster(boolean preferSmallerCluster) {
        this.preferSmallerCluster = preferSmallerCluster;
    }

    @Override
    public Collection<Job> ruinRoutes(Collection<VehicleRoute> vehicleRoutes) {
        List<Job> unassignedJobs = new ArrayList<>();
        int nOfJobs2BeRemoved = getRuinShareFactory().createNumberToBeRemoved();
        ruin(vehicleRoutes, nOfJobs2BeRemoved, unassignedJobs);
        return unassignedJobs;
    }

    private void ruin(Collection<VehicleRoute> vehicleRoutes, int nOfJobs2BeRemoved, List<Job> unassignedJobs) {
        if (vrp.getJobs().values().isEmpty()) return;

        clusterer.setRandom(random);

        Map<Job, VehicleRoute> jobToRoute = mapJobsToRoutes(vehicleRoutes);
        int toRemove = nOfJobs2BeRemoved;

        Collection<Job> lastRemoved = new ArrayList<>();
        Set<VehicleRoute> ruinedRoutes = new HashSet<>();
        Set<Job> removedJobs = new HashSet<>();

        while (toRemove > 0) {
            VehicleRoute targetRoute = selectTargetRoute(jobToRoute, lastRemoved,
                    ruinedRoutes, removedJobs, nOfJobs2BeRemoved);

            if (targetRoute == null) {
                break;
            }

            // Get cluster using Kruskal MST
            List<Job> cluster = clusterer.getOneCluster(targetRoute, preferSmallerCluster);

            if (cluster.isEmpty()) {
                // Route too small for clustering, take all jobs
                cluster = new ArrayList<>(targetRoute.getTourActivities().getJobs());
            }

            lastRemoved.clear();

            // Remove entire cluster if overshoot is reasonable (<= 50% over target)
            boolean removeEntireCluster = cluster.size() <= toRemove * 1.5;

            for (Job job : cluster) {
                if (!removeEntireCluster && toRemove <= 0) break;

                if (removeJob(job, vehicleRoutes)) {
                    lastRemoved.add(job);
                    unassignedJobs.add(job);
                    removedJobs.add(job);
                    toRemove--;
                }
            }

            // Mark route as ruined after one cluster - spread ruin across routes
            ruinedRoutes.add(targetRoute);
        }
    }

    private VehicleRoute selectTargetRoute(Map<Job, VehicleRoute> jobToRoute,
                                           Collection<Job> lastRemoved, Set<VehicleRoute> ruinedRoutes,
                                           Set<Job> removedJobs, int totalToRemove) {

        VehicleRoute targetRoute = null;

        if (lastRemoved.isEmpty()) {
            // First iteration: select random job and its route
            Job randomJob = RandomUtils.nextJob(vrp.getJobs().values(), random);
            targetRoute = jobToRoute.get(randomJob);
        } else {
            // Subsequent iterations: find neighboring route
            Job seedJob = RandomUtils.nextJob(lastRemoved, random);
            Iterator<Job> neighborIterator = jobNeighborhoods.getNearestNeighborsIterator(
                    totalToRemove, seedJob);

            while (neighborIterator.hasNext()) {
                Job neighbor = neighborIterator.next();
                if (!removedJobs.contains(neighbor)) {
                    VehicleRoute neighborRoute = jobToRoute.get(neighbor);
                    if (neighborRoute != null && !ruinedRoutes.contains(neighborRoute)) {
                        targetRoute = neighborRoute;
                        break;
                    }
                }
            }
        }

        // Skip if already ruined or null
        if (targetRoute == null || ruinedRoutes.contains(targetRoute)) {
            return null;
        }

        return targetRoute;
    }

    private Map<Job, VehicleRoute> mapJobsToRoutes(Collection<VehicleRoute> vehicleRoutes) {
        Map<Job, VehicleRoute> map = new HashMap<>(vrp.getJobs().size());
        for (VehicleRoute route : vehicleRoutes) {
            for (Job job : route.getTourActivities().getJobs()) {
                map.put(job, route);
            }
        }
        return map;
    }

    @Override
    public String toString() {
        return "[name=kruskalClusterRuin]";
    }
}
