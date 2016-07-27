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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * Ruin strategy that ruins current solution randomly. I.e.
 * customer are removed randomly from current solution.
 *
 * @author stefan schroeder
 */

public final class RuinRandom extends AbstractRuinStrategy {

    private Logger logger = LoggerFactory.getLogger(RuinRandom.class);

    private VehicleRoutingProblem vrp;

    private double fractionOfAllNodes2beRuined;

    /**
     * Constructs ruinRandom.
     *
     * @param vrp
     * @param fraction which is the fraction of total c
     */
    public RuinRandom(VehicleRoutingProblem vrp, double fraction) {
        super(vrp);
        this.vrp = vrp;
        this.fractionOfAllNodes2beRuined = fraction;
        setRuinShareFactory(new RuinShareFactory() {
            @Override
            public int createNumberToBeRemoved() {
                return selectNuOfJobs2BeRemoved();
            }
        });
        logger.debug("initialise {}", this);
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
        ArrayList<Job> availableJobs = new ArrayList<Job>(vrp.getJobs().values());
        Collections.shuffle(availableJobs, random);
        int removed = 0;
        for (Job job : availableJobs) {
            if (removed == nOfJobs2BeRemoved) break;
            if (removeJob(job, vehicleRoutes)) {
                unassignedJobs.add(job);
            }
            removed++;
        }
    }

    @Override
    public String toString() {
        return "[name=randomRuin][noJobsToBeRemoved=" + selectNuOfJobs2BeRemoved() + "]";
    }

    private int selectNuOfJobs2BeRemoved() {
        return (int) Math.ceil(vrp.getJobs().values().size() * fractionOfAllNodes2beRuined);
    }


}
