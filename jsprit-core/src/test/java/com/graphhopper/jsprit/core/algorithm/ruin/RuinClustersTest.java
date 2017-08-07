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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import org.junit.Test;

import com.graphhopper.jsprit.core.algorithm.ruin.distance.DefaultJobDistance;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.ServiceJob;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.util.RandomNumberGeneration;

/**
 * Created by schroeder on 06/03/15.
 */
public class RuinClustersTest {

    @Test
    public void itShouldRuinTwoObviousClusters() {
        ServiceJob s0 = new ServiceJob.Builder("s0").setLocation(Location.newInstance(9, 0)).build();
        ServiceJob s1 = new ServiceJob.Builder("s1").setLocation(Location.newInstance(9, 1)).build();
        ServiceJob s2 = new ServiceJob.Builder("s2").setLocation(Location.newInstance(9, 10)).build();
        ServiceJob s3 = new ServiceJob.Builder("s3").setLocation(Location.newInstance(9, 9)).build();
        ServiceJob s4 = new ServiceJob.Builder("s4").setLocation(Location.newInstance(9, 16)).build();
        ServiceJob s5 = new ServiceJob.Builder("s5").setLocation(Location.newInstance(9, 17)).build();
        ServiceJob s6 = new ServiceJob.Builder("s6").setLocation(Location.newInstance(9, 15.5)).build();
        ServiceJob s7 = new ServiceJob.Builder("s7").setLocation(Location.newInstance(9, 30)).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s1).addJob(s2)
                        .addJob(s6).addJob(s7).addJob(s0).addJob(s3).addJob(s4).addJob(s5).addVehicle(v).build();

        VehicleRoute vr1 = VehicleRoute.Builder.newInstance(v).addService(s0).addService(s1).addService(s2).addService(s3).setJobActivityFactory(vrp.getJobActivityFactory()).build();
        VehicleRoute vr2 = VehicleRoute.Builder.newInstance(v)
                        .addService(s6).addService(s7).addService(s4).addService(s5).setJobActivityFactory(vrp.getJobActivityFactory()).build();

        JobNeighborhoods n = new JobNeighborhoodsFactory().createNeighborhoods(vrp, new DefaultJobDistance(vrp.getTransportCosts()));
        n.initialise();
        RuinClusters rc = new RuinClusters(vrp, 5, n);
        Random r = RandomNumberGeneration.newInstance();
        rc.setRandom(r);
        Collection<Job> ruined = rc.ruinRoutes(Arrays.asList(vr1, vr2));
        assertEquals(5, ruined.size());

    }


}
