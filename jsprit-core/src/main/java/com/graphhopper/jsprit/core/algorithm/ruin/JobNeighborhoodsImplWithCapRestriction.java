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

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.graphhopper.jsprit.core.algorithm.ruin.distance.JobDistance;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.util.StopWatch;

/**
 * Created by schroeder on 07/01/15.
 */
class JobNeighborhoodsImplWithCapRestriction implements JobNeighborhoods {

    private static Logger logger = LoggerFactory.getLogger(JobNeighborhoodsImpl.class);

    private VehicleRoutingProblem vrp;

    private Map<String, TreeSet<ReferencedJob>> distanceNodeTree = new HashMap<String, TreeSet<ReferencedJob>>();

    private JobDistance jobDistance;

    private int capacity;

    private double maxDistance = 0.;

    public JobNeighborhoodsImplWithCapRestriction(VehicleRoutingProblem vrp, JobDistance jobDistance, int capacity) {
        super();
        this.vrp = vrp;
        this.jobDistance = jobDistance;
        this.capacity = capacity;
        logger.debug("initialize {}", this);
    }

    @Override
    public Iterator<Job> getNearestNeighborsIterator(int nNeighbors, Job neighborTo) {

        TreeSet<ReferencedJob> tree = distanceNodeTree.get(neighborTo.getId());
        if (tree == null) return new Iterator<Job>() {

            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public Job next() {
                return null;
            }

            @Override
            public void remove() {

            }

        };
        Iterator<ReferencedJob> descendingIterator = tree.iterator();
        return new NearestNeighborhoodIterator(descendingIterator, nNeighbors);
    }

    @Override
    public void initialise() {
        logger.debug("calculates distances from EACH job to EACH job --> n^2={} calculations, but 'only' {} are cached.", Math.pow(vrp.getJobs().values().size(), 2), (vrp.getJobs().values().size() * capacity));
        if (capacity == 0) return;
        calculateDistancesFromJob2Job();
    }

    @Override
    public double getMaxDistance() {
        return maxDistance;
    }

    private void calculateDistancesFromJob2Job() {
        logger.debug("preprocess distances between locations ...");
        //ToDo
        /*
        1 -> 2,3,4,5,6
        2 -> 1,3,4,5,6
        3

         */
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        int nuOfDistancesStored = 0;
        for (Job i : vrp.getJobs().values()) {
            // Collections.sort(list, );
            TreeSet<ReferencedJob> treeSet = new TreeSet<ReferencedJob>(
                new Comparator<ReferencedJob>() {
                    @Override
                    public int compare(ReferencedJob o1, ReferencedJob o2) {
                        if (o1.getDistance() <= o2.getDistance()) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                });
            distanceNodeTree.put(i.getId(), treeSet);
            for (Job j : vrp.getJobs().values()) {
                if (i == j) continue;
                double distance = jobDistance.getDistance(i, j);
                if (distance > maxDistance) maxDistance = distance;
                ReferencedJob refNode = new ReferencedJob(j, distance);
                if (treeSet.size() < capacity) {
                    treeSet.add(refNode);
                    nuOfDistancesStored++;
                } else {
                    if (treeSet.last().getDistance() > distance) {
                        treeSet.pollLast();
                        treeSet.add(refNode);
                    }
                }
            }
            assert treeSet.size() <= capacity : "treeSet.size() is bigger than specified capacity";

        }
        stopWatch.stop();
        logger.debug("preprocessing comp-time: {}; nuOfDistances stored: {}; estimated memory: {}" +
            " bytes", stopWatch, nuOfDistancesStored, (distanceNodeTree.keySet().size() * 64 + nuOfDistancesStored * 92));
    }

    @Override
    public String toString() {
        return "[name=neighborhoodWithCapRestriction][capacity=" + capacity + "]";
    }

}
