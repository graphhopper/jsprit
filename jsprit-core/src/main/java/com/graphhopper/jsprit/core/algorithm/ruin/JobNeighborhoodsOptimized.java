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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.graphhopper.jsprit.core.algorithm.ruin.distance.JobDistance;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.util.StopWatch;

/**
 * Created by schroeder on 07/01/15.
 */
class JobNeighborhoodsOptimized implements JobNeighborhoods {

    static class ArrayIterator implements Iterator<Job> {

        private final int noItems;

        private final int[] itemArray;

        private final Job[] jobs;

        private int index = 0;

        public ArrayIterator(int noItems, int[] itemArray, Job[] jobs) {
            this.noItems = noItems;
            this.itemArray = itemArray;
            this.jobs = jobs;
        }

        @Override
        public boolean hasNext() {
            if (index < noItems && index < itemArray.length)
                return true;
            return false;
        }

        @Override
        public Job next() {
            Job job = jobs[itemArray[index]];
            index++;
            return job;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private static Logger logger = LoggerFactory.getLogger(JobNeighborhoodsOptimized.class);

    private VehicleRoutingProblem vrp;

    private Map<Job, Integer> jobIndexMapping;

    private int[][] neighbors;

    private Job[] jobs;

    private JobDistance jobDistance;

    private int capacity;

    private double maxDistance = 0.;

    public JobNeighborhoodsOptimized(VehicleRoutingProblem vrp, JobDistance jobDistance, int capacity) {
        super();
        this.vrp = vrp;
        this.jobDistance = jobDistance;
        this.capacity = capacity;
        neighbors = new int[vrp.getJobsInclusiveInitialJobsInRoutes().size() + 1][capacity];
        jobs = new Job[vrp.getJobsInclusiveInitialJobsInRoutes().size() + 1];
        jobIndexMapping = new HashMap<>();
        for (Job job : vrp.getJobsInclusiveInitialJobsInRoutes().values()) {
            jobIndexMapping.put(job, jobIndexMapping.size());
        }
        logger.debug("initialize {}", this);
    }

    @Override
    public Iterator<Job> getNearestNeighborsIterator(int nNeighbors, Job neighborTo) {
        int[] neighbors = this.neighbors[jobIndexMapping.get(neighborTo)];
        return new ArrayIterator(nNeighbors, neighbors, jobs);
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
        logger.debug("pre-process distances between locations ...");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        for (Job job_i : vrp.getJobsInclusiveInitialJobsInRoutes().values()) {
            jobs[jobIndexMapping.get(job_i)] = job_i;
            List<ReferencedJob> jobList = new ArrayList<>(vrp.getJobsInclusiveInitialJobsInRoutes().values().size());
            for (Job job_j : vrp.getJobsInclusiveInitialJobsInRoutes().values()) {
                if (job_i == job_j) {
                    continue;
                }
                double distance = jobDistance.getDistance(job_i, job_j);
                if (distance > maxDistance) {
                    maxDistance = distance;
                }
                ReferencedJob referencedJob = new ReferencedJob(job_j, distance);
                jobList.add(referencedJob);
            }
            Collections.sort(jobList, getComparator());
            int[] jobIndices = new int[capacity];
            for (int index = 0; index < capacity; index++) {
                jobIndices[index] = jobIndexMapping.get(jobList.get(index).getJob());
            }
            neighbors[jobIndexMapping.get(job_i)] = jobIndices;
        }
        stopWatch.stop();
        logger.debug("pre-processing comp-time: {}", stopWatch);
    }

    private Comparator<ReferencedJob> getComparator() {
        return (o1, o2) -> {
            if (o1.getDistance() < o2.getDistance())
                return -1;
            else if (o1.getDistance() > o2.getDistance())
                return 1;
            else return 0;
        };
    }

    @Override
    public String toString() {
        return "[name=neighborhoodWithCapRestriction][capacity=" + capacity + "]";
    }

}
