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

import com.graphhopper.jsprit.core.algorithm.listener.IterationStartsListener;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.util.RandomUtils;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 * Ruin strategy that ruins current solution randomly. I.e.
 * customer are removed randomly from current solution.
 *
 * @author stefan schroeder
 */

public final class RuinClusters extends AbstractRuinStrategy implements IterationStartsListener {

    @Override
    public void informIterationStarts(int i, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
        minPts = 1 + random.nextInt(2);
        epsFactor = 0.5 + random.nextDouble();
    }

    public static class JobActivityWrapper implements Clusterable {

        private TourActivity.JobActivity jobActivity;

        public JobActivityWrapper(TourActivity.JobActivity jobActivity) {
            this.jobActivity = jobActivity;
        }

        @Override
        public double[] getPoint() {
            return new double[]{jobActivity.getLocation().getCoordinate().getX(), jobActivity.getLocation().getCoordinate().getY()};
        }

        public TourActivity.JobActivity getActivity() {
            return jobActivity;
        }
    }

    private Logger logger = LoggerFactory.getLogger(RuinClusters.class);

    private VehicleRoutingProblem vrp;


    private JobNeighborhoods jobNeighborhoods;

    private int noClusters = 2;

    private int minPts = 1;

    private double epsFactor = 0.8;

    public RuinClusters(VehicleRoutingProblem vrp, final int initialNumberJobsToRemove, JobNeighborhoods jobNeighborhoods) {
        super(vrp);
        this.vrp = vrp;
        setRuinShareFactory(new RuinShareFactory() {
            @Override
            public int createNumberToBeRemoved() {
                return initialNumberJobsToRemove;
            }
        });
        this.jobNeighborhoods = jobNeighborhoods;
        logger.debug("initialise {}", this);
    }

    public void setNoClusters(int noClusters) {
        this.noClusters = noClusters;
    }

    /**
     * Removes a fraction of jobs from vehicleRoutes.
     * <p>
     * <p>The number of jobs is calculated as follows: Math.ceil(vrp.getJobs().values().size() * fractionOfAllNodes2beRuined).
     */
    @Override
    public Collection<Job> ruinRoutes(Collection<VehicleRoute> vehicleRoutes) {
        List<Job> unassignedJobs = new ArrayList<Job>();
        int nOfJobs2BeRemoved = getRuinShareFactory().createNumberToBeRemoved();
        ruin(vehicleRoutes, nOfJobs2BeRemoved, unassignedJobs);
        return unassignedJobs;
    }

    private void ruin(Collection<VehicleRoute> vehicleRoutes, int nOfJobs2BeRemoved, List<Job> unassignedJobs) {
        if (vrp.getJobs().values().size() == 0) return;
        Map<Job, VehicleRoute> mappedRoutes = map(vehicleRoutes);
        int toRemove = nOfJobs2BeRemoved;

        Collection<Job> lastRemoved = new ArrayList<Job>();
        Set<VehicleRoute> ruined = new HashSet<VehicleRoute>();
        Set<Job> removed = new HashSet<Job>();
        Set<VehicleRoute> cycleCandidates = new HashSet<VehicleRoute>();
        while (toRemove > 0) {
            Job target;
            VehicleRoute targetRoute = null;
            if (lastRemoved.isEmpty()) {
                target = RandomUtils.nextJob(vrp.getJobs().values(), random);
                targetRoute = mappedRoutes.get(target);
            } else {
                target = RandomUtils.nextJob(lastRemoved, random);
                Iterator<Job> neighborIterator = jobNeighborhoods.getNearestNeighborsIterator(nOfJobs2BeRemoved, target);
                while (neighborIterator.hasNext()) {
                    Job j = neighborIterator.next();
                    if (!removed.contains(j) && !ruined.contains(mappedRoutes.get(j))) {
                        targetRoute = mappedRoutes.get(j);
                        break;
                    }
                }
                lastRemoved.clear();
            }
            if (targetRoute == null) break;
            if (cycleCandidates.contains(targetRoute)) break;
            if (ruined.contains(targetRoute)) {
                cycleCandidates.add(targetRoute);
                break;
            }
            DBSCANClusterer dbscan = new DBSCANClusterer(vrp.getTransportCosts());
            dbscan.setRandom(random);
            dbscan.setMinPts(minPts);
            dbscan.setEpsFactor(epsFactor);
            List<Job> cluster = dbscan.getRandomCluster(targetRoute);
            for (Job j : cluster) {
                if (toRemove == 0) break;
                if (removeJob(j, vehicleRoutes)) {
                    lastRemoved.add(j);
                    unassignedJobs.add(j);
                }
                toRemove--;
            }
            ruined.add(targetRoute);
        }
    }

    private List<JobActivityWrapper> wrap(List<TourActivity> activities) {
        List<JobActivityWrapper> wl = new ArrayList<JobActivityWrapper>();
        for (TourActivity act : activities) {
            wl.add(new JobActivityWrapper((TourActivity.JobActivity) act));
        }
        return wl;
    }

    private Map<Job, VehicleRoute> map(Collection<VehicleRoute> vehicleRoutes) {
        Map<Job, VehicleRoute> map = new HashMap<Job, VehicleRoute>(vrp.getJobs().size());
        for (VehicleRoute r : vehicleRoutes) {
            for (Job j : r.getTourActivities().getJobs()) {
                map.put(j, r);
            }
        }
        return map;
    }

    @Override
    public String toString() {
        return "[name=clusterRuin]";
    }

}
