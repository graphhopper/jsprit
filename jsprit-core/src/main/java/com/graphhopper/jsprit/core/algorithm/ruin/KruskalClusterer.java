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

import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.util.RandomNumberGeneration;

import java.util.*;

/**
 * Clusters jobs in a route using Kruskal's Minimum Spanning Tree algorithm.
 * <p>
 * Algorithm:
 * 1. Build complete distance graph between all jobs in route
 * 2. Run Kruskal's algorithm to build MST
 * 3. Remove longest edge in MST
 * 4. Result: exactly 2 clusters (connected components)
 * <p>
 * Ranked #2 in Voigt (2025) "A review and ranking of operators in adaptive large
 * neighborhood search for vehicle routing problems."
 */
public class KruskalClusterer {

    private static class Edge implements Comparable<Edge> {
        final int from;
        final int to;
        final double distance;

        Edge(int from, int to, double distance) {
            this.from = from;
            this.to = to;
            this.distance = distance;
        }

        @Override
        public int compareTo(Edge other) {
            return Double.compare(this.distance, other.distance);
        }
    }

    private static class UnionFind {
        private final int[] parent;
        private final int[] rank;

        UnionFind(int n) {
            parent = new int[n];
            rank = new int[n];
            for (int i = 0; i < n; i++) {
                parent[i] = i;
                rank[i] = 0;
            }
        }

        int find(int x) {
            if (parent[x] != x) {
                parent[x] = find(parent[x]); // path compression
            }
            return parent[x];
        }

        void union(int x, int y) {
            int px = find(x);
            int py = find(y);
            if (px == py) return;

            // union by rank
            if (rank[px] < rank[py]) {
                parent[px] = py;
            } else if (rank[px] > rank[py]) {
                parent[py] = px;
            } else {
                parent[py] = px;
                rank[px]++;
            }
        }
    }

    private final VehicleRoutingTransportCosts costs;
    private Random random = RandomNumberGeneration.getRandom();

    public KruskalClusterer(VehicleRoutingTransportCosts costs) {
        this.costs = costs;
    }

    public void setRandom(Random random) {
        this.random = random;
    }

    /**
     * Returns two clusters from the route by cutting the longest MST edge.
     *
     * @param route the route to cluster
     * @return list containing exactly 2 clusters, or empty list if route has < 2 jobs
     */
    public List<List<Job>> getClusters(VehicleRoute route) {
        List<Job> jobs = new ArrayList<>(route.getTourActivities().getJobs());
        // Sort by job ID for deterministic behavior (Set has no guaranteed order)
        jobs.sort(Comparator.comparing(Job::getId));
        int n = jobs.size();

        if (n < 2) {
            return Collections.emptyList();
        }

        // Build location map for jobs (handle jobs with multiple activities)
        Map<Job, TourActivity> jobToActivity = new HashMap<>();
        for (TourActivity act : route.getActivities()) {
            if (act instanceof TourActivity.JobActivity) {
                Job job = ((TourActivity.JobActivity) act).getJob();
                if (!jobToActivity.containsKey(job)) {
                    jobToActivity.put(job, act);
                }
            }
        }

        // Build complete graph with all edges
        List<Edge> edges = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                TourActivity act1 = jobToActivity.get(jobs.get(i));
                TourActivity act2 = jobToActivity.get(jobs.get(j));
                if (act1 != null && act2 != null) {
                    double dist = costs.getDistance(
                            act1.getLocation(), act2.getLocation(), 0, route.getVehicle());
                    edges.add(new Edge(i, j, dist));
                }
            }
        }

        if (edges.isEmpty()) {
            return Collections.emptyList();
        }

        // Sort edges by distance (ascending)
        Collections.sort(edges);

        // Kruskal's algorithm to build MST
        UnionFind uf = new UnionFind(n);
        List<Edge> mstEdges = new ArrayList<>();

        for (Edge e : edges) {
            if (uf.find(e.from) != uf.find(e.to)) {
                uf.union(e.from, e.to);
                mstEdges.add(e);
                if (mstEdges.size() == n - 1) {
                    break; // MST complete
                }
            }
        }

        if (mstEdges.isEmpty()) {
            return Collections.emptyList();
        }

        // Find and remove longest edge
        Edge longestEdge = Collections.max(mstEdges, Comparator.comparingDouble(e -> e.distance));
        mstEdges.remove(longestEdge);

        // Build adjacency list from remaining MST edges
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }
        for (Edge e : mstEdges) {
            adj.get(e.from).add(e.to);
            adj.get(e.to).add(e.from);
        }

        // BFS to find first cluster (starting from one side of removed edge)
        Set<Integer> cluster1Indices = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();
        queue.add(longestEdge.from);
        cluster1Indices.add(longestEdge.from);

        while (!queue.isEmpty()) {
            int node = queue.poll();
            for (int neighbor : adj.get(node)) {
                if (!cluster1Indices.contains(neighbor)) {
                    cluster1Indices.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        // Build cluster lists
        List<Job> cluster1 = new ArrayList<>();
        List<Job> cluster2 = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            if (cluster1Indices.contains(i)) {
                cluster1.add(jobs.get(i));
            } else {
                cluster2.add(jobs.get(i));
            }
        }

        List<List<Job>> result = new ArrayList<>();
        result.add(cluster1);
        result.add(cluster2);
        return result;
    }

    /**
     * Returns one cluster (randomly chosen or smaller) from the route.
     *
     * @param route         the route to cluster
     * @param preferSmaller if true, returns smaller cluster; if false, random
     * @return one cluster of jobs
     */
    public List<Job> getOneCluster(VehicleRoute route, boolean preferSmaller) {
        List<List<Job>> clusters = getClusters(route);

        if (clusters.isEmpty()) {
            return Collections.emptyList();
        }

        if (clusters.size() == 1) {
            return clusters.get(0);
        }

        List<Job> cluster1 = clusters.get(0);
        List<Job> cluster2 = clusters.get(1);

        if (preferSmaller) {
            return cluster1.size() <= cluster2.size() ? cluster1 : cluster2;
        } else {
            return random.nextBoolean() ? cluster1 : cluster2;
        }
    }
}
