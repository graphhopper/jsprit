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

import com.graphhopper.jsprit.core.algorithm.ruin.distance.JobDistance;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 * RuinStrategy that ruins the neighborhood of a randomly selected job. The size and the structure of the neighborhood is defined by
 * the share of jobs to be removed and the distance between jobs (where distance not necessarily mean Euclidean distance but an arbitrary
 * measure).
 *
 * @author stefan
 */
@Deprecated
public final class RuinRadialMultipleCenters extends AbstractRuinStrategy {

    private Logger logger = LoggerFactory.getLogger(RuinRadialMultipleCenters.class);

    private VehicleRoutingProblem vrp;

    private JobNeighborhoods jobNeighborhoods;

    private final int noJobsToMemorize;

    private int noCenters = 1;

    public RuinRadialMultipleCenters(VehicleRoutingProblem vrp, int neighborhoodSize, JobDistance jobDistance) {
        super(vrp);
        this.vrp = vrp;
        noJobsToMemorize = neighborhoodSize;
        ruinShareFactory = new RuinShareFactory() {

            @Override
            public int createNumberToBeRemoved() {
                return noJobsToMemorize;
            }

        };
        JobNeighborhoodsImplWithCapRestriction jobNeighborhoodsImpl = new JobNeighborhoodsImplWithCapRestriction(vrp, jobDistance, noJobsToMemorize);
        jobNeighborhoodsImpl.initialise();
        jobNeighborhoods = jobNeighborhoodsImpl;
        logger.debug("initialise {}", this);
    }

    public void setNumberOfRuinCenters(int noCenters) {
        this.noCenters = noCenters;
    }

    @Override
    public String toString() {
        return "[name=radialRuin][noJobsToBeRemoved=" + noJobsToMemorize + "]";
    }

    /**
     * Ruins the collection of vehicleRoutes, i.e. removes a share of jobs. First, it selects a job randomly. Second, it identifies its neighborhood. And finally, it removes
     * the neighborhood plus the randomly selected job from the number of vehicleRoutes. All removed jobs are then returned as a collection.
     */
    @Override
    public Collection<Job> ruinRoutes(Collection<VehicleRoute> vehicleRoutes) {
        if (vehicleRoutes.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Job> available = new HashSet<Job>(vrp.getJobs().values());
        Collection<Job> ruined = new ArrayList<Job>();
        for (int center = 0; center < noCenters; center++) {
            int nOfJobs2BeRemoved = ruinShareFactory.createNumberToBeRemoved();
            if (nOfJobs2BeRemoved == 0) {
                return Collections.emptyList();
            }
            Job randomJob = pickRandomJob(available);
            if (randomJob != null) {
                ruined.addAll(ruinRoutes_(vehicleRoutes, randomJob, nOfJobs2BeRemoved, available));
            }
        }
        return ruined;
    }

    private Collection<Job> ruinRoutes_(Collection<VehicleRoute> vehicleRoutes, Job targetJob, int nOfJobs2BeRemoved, Set<Job> available) {
        List<Job> unassignedJobs = new ArrayList<Job>();
        int nNeighbors = nOfJobs2BeRemoved - 1;
        removeJob(targetJob, vehicleRoutes);
        unassignedJobs.add(targetJob);
        Iterator<Job> neighborhoodIterator = jobNeighborhoods.getNearestNeighborsIterator(nNeighbors, targetJob);
        while (neighborhoodIterator.hasNext()) {
            Job job = neighborhoodIterator.next();
            if (available != null) available.remove(job);
            if (removeJob(job, vehicleRoutes)) {
                unassignedJobs.add(job);
            }
        }
        return unassignedJobs;
    }

    private Job pickRandomJob(Set<Job> available) {
        int randomIndex = random.nextInt(available.size());
        int i = 0;
        for (Job j : available) {
            if (i >= randomIndex) {
                return j;
            } else i++;
        }
        return null;
    }

}
