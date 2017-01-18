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
import com.graphhopper.jsprit.core.problem.AbstractActivity;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
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
public final class RuinString extends AbstractRuinStrategy {

    private Logger logger = LoggerFactory.getLogger(RuinString.class);

    private VehicleRoutingProblem vrp;

    private JobNeighborhoods jobNeighborhoods;

    private int Kmin = 1;

    private int Kmax = 6;

    private int Lmin = 1;

    private int Lmax = 40;

    /**
     * Constructs RuinRadial.
     *
     * @param vrp
     * @param jobDistance i.e. a measure to define the distance between two jobs and whether they are located close or distant to eachother
     * @param Kmin
     * @param Kmax
     * @param Lmin
     * @param Lmax
     */
    public RuinString(VehicleRoutingProblem vrp, JobDistance jobDistance, int Kmin, int Kmax, int Lmin, int Lmax) {
        super(vrp);
        this.vrp = vrp;
        JobNeighborhoodsImplWithCapRestriction jobNeighborhoodsImpl = new JobNeighborhoodsImplWithCapRestriction(vrp, jobDistance, Kmax * Lmax);
        jobNeighborhoodsImpl.initialise();
        jobNeighborhoods = jobNeighborhoodsImpl;
        this.Kmin = Kmin;
        this.Kmax = Kmax;
        this.Lmin = Lmin;
        this.Lmax = Lmax;
        logger.debug("initialise {}", this);
    }

    public RuinString(VehicleRoutingProblem vrp, JobDistance jobDistance) {
        super(vrp);
        this.vrp = vrp;
        JobNeighborhoodsImplWithCapRestriction jobNeighborhoodsImpl = new JobNeighborhoodsImplWithCapRestriction(vrp, jobDistance, Kmax * Lmax);
        jobNeighborhoodsImpl.initialise();
        jobNeighborhoods = jobNeighborhoodsImpl;
        logger.debug("initialise {}", this);
    }

    public RuinString(VehicleRoutingProblem vrp, JobNeighborhoods jobNeighborhoods) {
        super(vrp);
        this.vrp = vrp;
        this.jobNeighborhoods = jobNeighborhoods;
        logger.debug("initialise {}", this);
    }

    @Override
    public String toString() {
        return "[name=splitRuin]";
    }

    /**
     * Ruins the collection of vehicleRoutes, i.e. removes a share of jobs. First, it selects a job randomly. Second, it identifies its neighborhood. And finally, it removes
     * the neighborhood plus the randomly selected job from the number of vehicleRoutes. All removed jobs are then returned as a collection.
     */
    @Override
    public Collection<Job> ruinRoutes(Collection<VehicleRoute> vehicleRoutes) {
        if (vehicleRoutes.isEmpty() || vrp.getJobs().isEmpty()) {
            return Collections.emptyList();
        }
        int noStrings = Kmin + random.nextInt((Kmax - Kmin));
        noStrings = Math.min(noStrings, vehicleRoutes.size());
        Set<Job> unassignedJobs = new HashSet<>();
        Set<VehicleRoute> ruinedRoutes = new HashSet<>();
        Job prevJob = RandomUtils.nextJob(vrp.getJobs().values(), random);
        Iterator<Job> neighborhoodIterator = jobNeighborhoods.getNearestNeighborsIterator(Kmax * Lmax, prevJob);
        while (neighborhoodIterator.hasNext() && ruinedRoutes.size() <= noStrings) {
            if (!unassignedJobs.contains(prevJob)) {
                VehicleRoute route = getRouteOf(prevJob, vehicleRoutes);
                if (route != null && !ruinedRoutes.contains(route)) {
                    if (random.nextDouble() < .5) {
                        ruinRouteWithStringRuin(route, prevJob, unassignedJobs);
                    } else {
                        ruinRouteWithSplitStringRuin(route, prevJob, unassignedJobs);
                    }
                    ruinedRoutes.add(route);
                }
            }
            prevJob = neighborhoodIterator.next();
        }
        return unassignedJobs;
    }

    private VehicleRoute getRouteOf(Job job, Collection<VehicleRoute> vehicleRoutes) {
        for (VehicleRoute route : vehicleRoutes) {
            if (route.getTourActivities().servesJob(job)) return route;
        }
        return null;
    }

    private void ruinRouteWithSplitStringRuin(VehicleRoute seedRoute, Job prevJob, Set<Job> unassignedJobs) {
        int noActivities = seedRoute.getActivities().size();
        int stringLength = Lmin + random.nextInt(Lmax - Lmin);
        stringLength = Math.min(stringLength, seedRoute.getActivities().size());

        int preservedSubstringLength = StringUtil.determineSubstringLength(stringLength, noActivities, random);

        List<AbstractActivity> acts = vrp.getActivities(prevJob);
        AbstractActivity randomSeedAct = RandomUtils.nextItem(acts, random);
        int seedIndex = 0;

        int index = 0;
        for (TourActivity act : seedRoute.getActivities()) {
            if (act.getIndex() == randomSeedAct.getIndex()) {
                seedIndex = index;
                break;
            }
            index++;
        }

        int totalStringLength = stringLength + preservedSubstringLength;
        List<Integer> stringBounds = StringUtil.getLowerBoundsOfAllStrings(totalStringLength, seedIndex, noActivities);
        if (stringBounds.isEmpty()) return;
        int lowerBound = RandomUtils.nextItem(stringBounds, random);

        List<Job> jobs2Remove = new ArrayList<>();
        int startIndexOfPreservedSubstring = random.nextInt(stringLength);
        int position = 0;
        int noStringsInPreservedSubstring = 0;
        boolean isPreservedSubstring = false;
        for (int i = lowerBound; i < (lowerBound + totalStringLength); i++) {
            if (position == startIndexOfPreservedSubstring) {
                isPreservedSubstring = true;
            }
            if (noStringsInPreservedSubstring >= preservedSubstringLength) {
                isPreservedSubstring = false;
            }
            if (!isPreservedSubstring) {
                TourActivity act = seedRoute.getActivities().get(i);
                if (act instanceof TourActivity.JobActivity) {
                    Job job = ((TourActivity.JobActivity) act).getJob();
                    if (vrp.getJobs().containsKey(job.getId())) {
                        jobs2Remove.add(job);
                    }
                }
            } else noStringsInPreservedSubstring++;
            position++;
        }
        for (Job job : jobs2Remove) {
            removeJob(job, seedRoute);
            unassignedJobs.add(job);
        }

    }


    private void ruinRouteWithStringRuin(VehicleRoute seedRoute, Job prevJob, Set<Job> unassignedJobs) {
        int stringLength = Lmin + random.nextInt(Lmax - Lmin);
        stringLength = Math.min(stringLength, seedRoute.getActivities().size());
        List<AbstractActivity> acts = vrp.getActivities(prevJob);
        AbstractActivity randomSeedAct = RandomUtils.nextItem(acts, random);
        int seedIndex = 0;
        int noActivities = seedRoute.getActivities().size();
        int index = 0;
        for (TourActivity act : seedRoute.getActivities()) {
            if (act.getIndex() == randomSeedAct.getIndex()) {
                seedIndex = index;
                break;
            }
            index++;
        }
        List<Integer> stringBounds = StringUtil.getLowerBoundsOfAllStrings(stringLength, seedIndex, noActivities);
        if (stringBounds.isEmpty()) return;
        int lowerBound = RandomUtils.nextItem(stringBounds, random);
        List<Job> jobs2Remove = new ArrayList<>();
        for (int i = lowerBound; i < (lowerBound + stringLength); i++) {
            TourActivity act = seedRoute.getActivities().get(i);
            if (act instanceof TourActivity.JobActivity) {
                Job job = ((TourActivity.JobActivity) act).getJob();
                if (vrp.getJobs().containsKey(job.getId())) {
                    jobs2Remove.add(job);
                }
            }
        }
        for (Job job : jobs2Remove) {
            removeJob(job, seedRoute);
            unassignedJobs.add(job);
        }

    }


}
