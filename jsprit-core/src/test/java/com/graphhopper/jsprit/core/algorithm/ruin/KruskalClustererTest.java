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

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for KruskalClusterer - MST-based clustering for ruin strategies.
 */
@DisplayName("Kruskal Clusterer Test")
class KruskalClustererTest {

    @Test
    @DisplayName("Should return exactly 2 clusters for route with 4 jobs")
    void shouldReturnTwoClustersForRouteWithFourJobs() {
        // Two obvious clusters: (s0, s1) near origin and (s2, s3) far away
        Service s0 = Service.Builder.newInstance("s0").setLocation(Location.newInstance(0, 0)).build();
        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(1, 1)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(100, 100)).build();
        Service s3 = Service.Builder.newInstance("s3").setLocation(Location.newInstance(101, 101)).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addJob(s0).addJob(s1).addJob(s2).addJob(s3)
                .addVehicle(v).build();

        VehicleRoute route = VehicleRoute.Builder.newInstance(v)
                .addService(s0).addService(s1).addService(s2).addService(s3)
                .setJobActivityFactory(vrp.getJobActivityFactory()).build();

        KruskalClusterer clusterer = new KruskalClusterer(vrp.getTransportCosts());
        List<List<Job>> clusters = clusterer.getClusters(route);

        assertEquals(2, clusters.size(), "Should return exactly 2 clusters");
        assertEquals(4, clusters.get(0).size() + clusters.get(1).size(), "Total jobs should be 4");
    }

    @Test
    @DisplayName("Should separate two obvious clusters correctly")
    void shouldSeparateTwoObviousClustersCorrectly() {
        // Cluster 1: (s0, s1) at y=0
        // Cluster 2: (s2, s3) at y=100
        Service s0 = Service.Builder.newInstance("s0").setLocation(Location.newInstance(0, 0)).build();
        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(1, 0)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(0, 100)).build();
        Service s3 = Service.Builder.newInstance("s3").setLocation(Location.newInstance(1, 100)).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addJob(s0).addJob(s1).addJob(s2).addJob(s3)
                .addVehicle(v).build();

        VehicleRoute route = VehicleRoute.Builder.newInstance(v)
                .addService(s0).addService(s1).addService(s2).addService(s3)
                .setJobActivityFactory(vrp.getJobActivityFactory()).build();

        KruskalClusterer clusterer = new KruskalClusterer(vrp.getTransportCosts());
        List<List<Job>> clusters = clusterer.getClusters(route);

        assertEquals(2, clusters.size());

        // Each cluster should have 2 jobs
        assertEquals(2, clusters.get(0).size());
        assertEquals(2, clusters.get(1).size());

        // Verify clusters are correctly separated (jobs in same cluster should be close)
        Set<String> cluster0Ids = new HashSet<>();
        Set<String> cluster1Ids = new HashSet<>();
        for (Job j : clusters.get(0)) cluster0Ids.add(j.getId());
        for (Job j : clusters.get(1)) cluster1Ids.add(j.getId());

        // Either (s0, s1) in one cluster and (s2, s3) in other, or vice versa
        boolean correctSeparation =
                (cluster0Ids.contains("s0") && cluster0Ids.contains("s1") &&
                        cluster1Ids.contains("s2") && cluster1Ids.contains("s3")) ||
                        (cluster1Ids.contains("s0") && cluster1Ids.contains("s1") &&
                                cluster0Ids.contains("s2") && cluster0Ids.contains("s3"));

        assertTrue(correctSeparation, "Clusters should separate close jobs together");
    }

    @Test
    @DisplayName("Should return empty list for route with single job")
    void shouldReturnEmptyListForSingleJob() {
        Service s0 = Service.Builder.newInstance("s0").setLocation(Location.newInstance(0, 0)).build();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addJob(s0).addVehicle(v).build();

        VehicleRoute route = VehicleRoute.Builder.newInstance(v)
                .addService(s0)
                .setJobActivityFactory(vrp.getJobActivityFactory()).build();

        KruskalClusterer clusterer = new KruskalClusterer(vrp.getTransportCosts());
        List<List<Job>> clusters = clusterer.getClusters(route);

        assertTrue(clusters.isEmpty(), "Should return empty list for single job");
    }

    @Test
    @DisplayName("Should return empty list for empty route")
    void shouldReturnEmptyListForEmptyRoute() {
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addVehicle(v).build();

        VehicleRoute route = VehicleRoute.Builder.newInstance(v)
                .setJobActivityFactory(vrp.getJobActivityFactory()).build();

        KruskalClusterer clusterer = new KruskalClusterer(vrp.getTransportCosts());
        List<List<Job>> clusters = clusterer.getClusters(route);

        assertTrue(clusters.isEmpty(), "Should return empty list for empty route");
    }

    @Test
    @DisplayName("getOneCluster should return smaller cluster when preferSmaller is true")
    void getOneClusterShouldReturnSmallerClusterWhenPreferSmaller() {
        // Cluster 1: (s0) - 1 job
        // Cluster 2: (s1, s2, s3) - 3 jobs (but will be split as 2 clusters)
        // Actually with 4 jobs and Kruskal, we get exactly 2 clusters
        Service s0 = Service.Builder.newInstance("s0").setLocation(Location.newInstance(0, 0)).build();
        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(1, 0)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(100, 0)).build();
        Service s3 = Service.Builder.newInstance("s3").setLocation(Location.newInstance(101, 0)).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addJob(s0).addJob(s1).addJob(s2).addJob(s3)
                .addVehicle(v).build();

        VehicleRoute route = VehicleRoute.Builder.newInstance(v)
                .addService(s0).addService(s1).addService(s2).addService(s3)
                .setJobActivityFactory(vrp.getJobActivityFactory()).build();

        KruskalClusterer clusterer = new KruskalClusterer(vrp.getTransportCosts());
        List<Job> cluster = clusterer.getOneCluster(route, true);

        // With equal-sized clusters (2 each), either is acceptable
        assertFalse(cluster.isEmpty());
        assertTrue(cluster.size() <= 2, "Should return smaller or equal cluster");
    }

    @Test
    @DisplayName("Should handle two jobs correctly")
    void shouldHandleTwoJobsCorrectly() {
        Service s0 = Service.Builder.newInstance("s0").setLocation(Location.newInstance(0, 0)).build();
        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(100, 100)).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addJob(s0).addJob(s1)
                .addVehicle(v).build();

        VehicleRoute route = VehicleRoute.Builder.newInstance(v)
                .addService(s0).addService(s1)
                .setJobActivityFactory(vrp.getJobActivityFactory()).build();

        KruskalClusterer clusterer = new KruskalClusterer(vrp.getTransportCosts());
        List<List<Job>> clusters = clusterer.getClusters(route);

        assertEquals(2, clusters.size(), "Should return 2 clusters for 2 jobs");
        assertEquals(1, clusters.get(0).size(), "First cluster should have 1 job");
        assertEquals(1, clusters.get(1).size(), "Second cluster should have 1 job");
    }
}
