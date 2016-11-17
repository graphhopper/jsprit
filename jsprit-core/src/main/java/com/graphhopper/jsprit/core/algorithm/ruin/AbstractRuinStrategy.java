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


import com.graphhopper.jsprit.core.algorithm.ruin.listener.RuinListener;
import com.graphhopper.jsprit.core.algorithm.ruin.listener.RuinListeners;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.util.RandomNumberGeneration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Random;

public abstract class AbstractRuinStrategy implements RuinStrategy {

    private final static Logger logger = LoggerFactory.getLogger(AbstractRuinStrategy.class);

    private RuinListeners ruinListeners;

    protected Random random = RandomNumberGeneration.getRandom();

    protected VehicleRoutingProblem vrp;

    public void setRandom(Random random) {
        this.random = random;
    }

    protected RuinShareFactory ruinShareFactory;

    public void setRuinShareFactory(RuinShareFactory ruinShareFactory) {
        this.ruinShareFactory = ruinShareFactory;
    }

    public RuinShareFactory getRuinShareFactory() {
        return ruinShareFactory;
    }

    protected AbstractRuinStrategy(VehicleRoutingProblem vrp) {
        this.vrp = vrp;
        ruinListeners = new RuinListeners();
    }

    @Override
    public Collection<Job> ruin(Collection<VehicleRoute> vehicleRoutes) {
        ruinListeners.ruinStarts(vehicleRoutes);
        Collection<Job> unassigned = ruinRoutes(vehicleRoutes);
        logger.trace("ruin: [ruined={}]", unassigned.size());
        ruinListeners.ruinEnds(vehicleRoutes, unassigned);
        return unassigned;
    }

    public abstract Collection<Job> ruinRoutes(Collection<VehicleRoute> vehicleRoutes);

    @Override
    public void addListener(RuinListener ruinListener) {
        ruinListeners.addListener(ruinListener);
    }

    @Override
    public void removeListener(RuinListener ruinListener) {
        ruinListeners.removeListener(ruinListener);
    }

    @Override
    public Collection<RuinListener> getListeners() {
        return ruinListeners.getListeners();
    }

    protected boolean removeJob(Job job, Collection<VehicleRoute> vehicleRoutes) {
        if (jobIsInitial(job)) return false;
        for (VehicleRoute route : vehicleRoutes) {
            if (removeJob(job, route)) {
                return true;
            }
        }
        return false;
    }

    private boolean jobIsInitial(Job job) {
        return !vrp.getJobs().containsKey(job.getId()); //for initial jobs (being not contained in problem
    }

    protected boolean removeJob(Job job, VehicleRoute route) {
        if (jobIsInitial(job)) return false;
        boolean removed = route.getTourActivities().removeJob(job);
        if (removed) {
            logger.trace("ruin: {}", job.getId());
            ruinListeners.removed(job, route);
            return true;
        }
        return false;
    }
}
