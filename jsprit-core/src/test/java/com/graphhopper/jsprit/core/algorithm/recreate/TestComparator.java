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

package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by schroeder on 24/05/16.
 */
public class TestComparator {

    @Test
    public void test(){
        Service s = Service.Builder.newInstance("1").setLocation(Location.newInstance("loc"))
            .setPriority(1).build();
        Service s2 = Service.Builder.newInstance("2").setLocation(Location.newInstance("loc"))
            .setPriority(2).build();
        Service s3 = Service.Builder.newInstance("3").setLocation(Location.newInstance("loc"))
            .setPriority(3).build();
        Service s4 = Service.Builder.newInstance("4").setLocation(Location.newInstance("loc"))
            .setPriority(1).build();
        List<Job> jobs = new ArrayList<Job>();
        jobs.add(s2);
        jobs.add(s3);
        jobs.add(s4);
        jobs.add(s);
        Collections.sort(jobs, new Comparator<Job>() {
            @Override
            public int compare(Job o1, Job o2) {
                return o1.getPriority() - o2.getPriority();
            }
        });

    }
}
