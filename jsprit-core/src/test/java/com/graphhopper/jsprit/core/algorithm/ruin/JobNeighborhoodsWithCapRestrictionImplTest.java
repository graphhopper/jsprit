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

import com.graphhopper.jsprit.core.algorithm.ruin.distance.EuclideanServiceDistance;
import com.graphhopper.jsprit.core.algorithm.ruin.distance.JobDistance;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Job Neighborhoods With Cap Restriction Impl Test")
class JobNeighborhoodsWithCapRestrictionImplTest {

    VehicleRoutingProblem vrp;

    JobDistance jobDistance;

    Service target;

    Service s2;

    Service s3;

    Service s4;

    Service s5;

    Service s6;

    Service s7;

    @BeforeEach
    void doBefore() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        target = Service.Builder.newInstance("s1").addSizeDimension(0, 1).setLocation(Location.newInstance(0, 5)).build();
        s2 = Service.Builder.newInstance("s2").addSizeDimension(0, 1).setLocation(Location.newInstance(0, 4)).build();
        s3 = Service.Builder.newInstance("s3").addSizeDimension(0, 1).setLocation(Location.newInstance(0, 3)).build();
        s4 = Service.Builder.newInstance("s4").addSizeDimension(0, 1).setLocation(Location.newInstance(0, 2)).build();
        s5 = Service.Builder.newInstance("s5").addSizeDimension(0, 1).setLocation(Location.newInstance(0, 6)).build();
        s6 = Service.Builder.newInstance("s6").addSizeDimension(0, 1).setLocation(Location.newInstance(0, 7)).build();
        s7 = Service.Builder.newInstance("s7").addSizeDimension(0, 1).setLocation(Location.newInstance(0, 8)).build();
        vrp = builder.addJob(target).addJob(s2).addJob(s3).addJob(s4).addJob(s5).addJob(s6).addJob(s7).build();
        jobDistance = new EuclideanServiceDistance();
    }

    @Test
    @DisplayName("When Requesting Neighborhood Of Target Job _ n Neighbors Should Be Two")
    void whenRequestingNeighborhoodOfTargetJob_nNeighborsShouldBeTwo() {
        JobNeighborhoodsImplWithCapRestriction jn = new JobNeighborhoodsImplWithCapRestriction(vrp, jobDistance, 2);
        jn.initialise();
        Iterator<Job> iter = jn.getNearestNeighborsIterator(2, target);
        List<Service> services = new ArrayList<Service>();
        while (iter.hasNext()) {
            services.add((Service) iter.next());
        }
        Assertions.assertEquals(2, services.size());
    }

    @Test
    @DisplayName("When Requesting Neighborhood Of Target Job _ s 2 Should Be Neighbor")
    void whenRequestingNeighborhoodOfTargetJob_s2ShouldBeNeighbor() {
        JobNeighborhoodsImplWithCapRestriction jn = new JobNeighborhoodsImplWithCapRestriction(vrp, jobDistance, 2);
        jn.initialise();
        Iterator<Job> iter = jn.getNearestNeighborsIterator(2, target);
        List<Service> services = new ArrayList<Service>();
        while (iter.hasNext()) {
            services.add((Service) iter.next());
        }
        Assertions.assertTrue(services.contains(s2));
    }

    @Test
    @DisplayName("When Requesting Neighborhood Of Target Job _ s 4 Should Be Neighbor")
    void whenRequestingNeighborhoodOfTargetJob_s4ShouldBeNeighbor() {
        JobNeighborhoodsImplWithCapRestriction jn = new JobNeighborhoodsImplWithCapRestriction(vrp, jobDistance, 2);
        jn.initialise();
        Iterator<Job> iter = jn.getNearestNeighborsIterator(2, target);
        List<Service> services = new ArrayList<Service>();
        while (iter.hasNext()) {
            services.add((Service) iter.next());
        }
        Assertions.assertTrue(services.contains(s5));
    }

    @Test
    @DisplayName("When Requesting Neighborhood Of Target Job _ size Should Be 4")
    void whenRequestingNeighborhoodOfTargetJob_sizeShouldBe4() {
        JobNeighborhoodsImplWithCapRestriction jn = new JobNeighborhoodsImplWithCapRestriction(vrp, jobDistance, 4);
        jn.initialise();
        Iterator<Job> iter = jn.getNearestNeighborsIterator(4, target);
        List<Service> services = new ArrayList<Service>();
        while (iter.hasNext()) {
            services.add((Service) iter.next());
        }
        Assertions.assertEquals(4, services.size());
    }

    @Test
    @DisplayName("When Requesting More Neighbors Than Existing _ it Should Return Max Neighbors")
    void whenRequestingMoreNeighborsThanExisting_itShouldReturnMaxNeighbors() {
        JobNeighborhoodsImplWithCapRestriction jn = new JobNeighborhoodsImplWithCapRestriction(vrp, jobDistance, 2);
        jn.initialise();
        Iterator<Job> iter = jn.getNearestNeighborsIterator(100, target);
        List<Service> services = new ArrayList<Service>();
        while (iter.hasNext()) {
            services.add((Service) iter.next());
        }
        Assertions.assertEquals(2, services.size());
    }
}
