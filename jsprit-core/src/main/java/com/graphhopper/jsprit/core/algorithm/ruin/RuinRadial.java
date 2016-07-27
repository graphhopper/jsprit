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
import com.graphhopper.jsprit.core.util.RandomUtils;
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
public final class RuinRadial extends AbstractRuinStrategy {

    private Logger logger = LoggerFactory.getLogger(RuinRadial.class);

    private VehicleRoutingProblem vrp;

    private JobNeighborhoods jobNeighborhoods;

    private final int noJobsToMemorize;

    /**
     * Constructs RuinRadial.
     *
     * @param vrp
     * @param fraction2beRemoved i.e. the share of jobs to be removed (relative to the total number of jobs in vrp)
     * @param jobDistance        i.e. a measure to define the distance between two jobs and whether they are located close or distant to eachother
     */
    public RuinRadial(VehicleRoutingProblem vrp, double fraction2beRemoved, JobDistance jobDistance) {
        super(vrp);
        this.vrp = vrp;
        noJobsToMemorize = (int) Math.ceil(vrp.getJobs().values().size() * fraction2beRemoved);
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

    public RuinRadial(VehicleRoutingProblem vrp, int noJobs2beRemoved, JobDistance jobDistance) {
        super(vrp);
        this.vrp = vrp;
//		this.fractionOfAllNodes2beRuined = fraction2beRemoved;
        noJobsToMemorize = noJobs2beRemoved;
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

    public RuinRadial(VehicleRoutingProblem vrp, int noJobs2beRemoved, JobNeighborhoods neighborhoods) {
        super(vrp);
        this.vrp = vrp;
        noJobsToMemorize = noJobs2beRemoved;
        ruinShareFactory = new RuinShareFactory() {

            @Override
            public int createNumberToBeRemoved() {
                return noJobsToMemorize;
            }

        };
        jobNeighborhoods = neighborhoods;
        logger.debug("initialise {}", this);
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
        int nOfJobs2BeRemoved = Math.min(ruinShareFactory.createNumberToBeRemoved(), noJobsToMemorize);
        if (nOfJobs2BeRemoved == 0) {
            return Collections.emptyList();
        }
        Job randomJob = RandomUtils.nextJob(vrp.getJobs().values(), random);
        return ruinRoutes(vehicleRoutes, randomJob, nOfJobs2BeRemoved);
    }

    /**
     * Removes targetJob and its neighborhood and returns the removed jobs.
     *
     */
    private Collection<Job> ruinRoutes(Collection<VehicleRoute> vehicleRoutes, Job targetJob, int nOfJobs2BeRemoved) {
        List<Job> unassignedJobs = new ArrayList<Job>();
        int nNeighbors = nOfJobs2BeRemoved - 1;
        removeJob(targetJob, vehicleRoutes);
        unassignedJobs.add(targetJob);
        Iterator<Job> neighborhoodIterator = jobNeighborhoods.getNearestNeighborsIterator(nNeighbors, targetJob);
        while (neighborhoodIterator.hasNext()) {
            Job job = neighborhoodIterator.next();
            if (removeJob(job, vehicleRoutes)) {
                unassignedJobs.add(job);
            }
        }
        return unassignedJobs;
    }


}
