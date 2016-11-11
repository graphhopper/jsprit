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

import java.util.List;

import org.junit.Test;

import com.graphhopper.jsprit.core.distance.EuclideanDistanceCalculator;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.util.DefaultCosts;


/**
 * Created by schroeder on 06/03/15.
 */
public class DBSCANClustererTest {

    @Test
    public void itShouldReturnOneClusterOfSizeTwo() {
        Service s1 = new Service.Builder("s1").setLocation(Location.newInstance(1, 1)).build();
        Service s2 = new Service.Builder("s2").setLocation(Location.newInstance(10, 10)).build();
        Service s3 = new Service.Builder("s3").setLocation(Location.newInstance(9, 9)).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoute r = VehicleRoute.Builder.newInstance(v).addService(s1).addService(s2).addService(s3).build();

        DBSCANClusterer c = new DBSCANClusterer(
                        new DefaultCosts(EuclideanDistanceCalculator.getInstance()));
        c.setEpsDistance(3);
        List<Job> cluster = c.getRandomCluster(r);
        assertEquals(2, cluster.size());
    }

    @Test
    public void itShouldReturnOneCluster() {
        Service s1 = new Service.Builder("s1").setLocation(Location.newInstance(1, 1)).build();
        Service s2 = new Service.Builder("s2").setLocation(Location.newInstance(10, 10)).build();
        Service s3 = new Service.Builder("s3").setLocation(Location.newInstance(9, 9)).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoute r = VehicleRoute.Builder.newInstance(v).addService(s1).addService(s2).addService(s3).build();

        DBSCANClusterer c = new DBSCANClusterer(new DefaultCosts(EuclideanDistanceCalculator.getInstance()));
        c.setEpsDistance(3);
        List<List<Job>> cluster = c.getClusters(r);
        assertEquals(1, cluster.size());

    }

    @Test
    public void itShouldReturnTwoClusters() {
        Service s0 = new Service.Builder("s0").setLocation(Location.newInstance(9, 0)).build();
        Service s1 = new Service.Builder("s1").setLocation(Location.newInstance(9, 1)).build();
        Service s2 = new Service.Builder("s2").setLocation(Location.newInstance(9, 10)).build();
        Service s3 = new Service.Builder("s3").setLocation(Location.newInstance(9, 9)).build();
        Service s4 = new Service.Builder("s4").setLocation(Location.newInstance(9, 16)).build();
        Service s5 = new Service.Builder("s5").setLocation(Location.newInstance(9, 17)).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoute r = VehicleRoute.Builder.newInstance(v).addService(s1).addService(s2).addService(s3)
                        .addService(s0).addService(s4).addService(s5).build();

        DBSCANClusterer c = new DBSCANClusterer(
                        new DefaultCosts(EuclideanDistanceCalculator.getInstance()));
        c.setMinPts(1);
        c.setEpsDistance(2);
        List<List<Job>> cluster = c.getClusters(r);
        assertEquals(3, cluster.size());

    }
}
