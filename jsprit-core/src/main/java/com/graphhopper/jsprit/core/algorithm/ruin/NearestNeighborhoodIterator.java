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

import com.graphhopper.jsprit.core.problem.job.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * Created by schroeder on 07/01/15.
 */
class NearestNeighborhoodIterator implements Iterator<Job> {

    private static Logger log = LoggerFactory.getLogger(NearestNeighborhoodIterator.class);

    private Iterator<ReferencedJob> jobIter;

    private int nJobs;

    private int jobCount = 0;

    public NearestNeighborhoodIterator(Iterator<ReferencedJob> jobIter, int nJobs) {
        super();
        this.jobIter = jobIter;
        this.nJobs = nJobs;
    }

    @Override
    public boolean hasNext() {
        if (jobCount < nJobs) {
            boolean hasNext = jobIter.hasNext();
//            if (!hasNext)
//                log.warn("more jobs are requested then iterator can iterate over. probably the number of neighbors memorized in JobNeighborhoods is too small");
            return hasNext;
        }
        return false;
    }

    @Override
    public Job next() {
        ReferencedJob next = jobIter.next();
        jobCount++;
        return next.getJob();
    }

    @Override
    public void remove() {
        jobIter.remove();
    }

}
