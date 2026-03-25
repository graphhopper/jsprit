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
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AffectedJobTracker Test")
class AffectedJobTrackerTest {

    private VehicleRoutingProblem vrp;
    private AffectedJobTracker tracker;
    private Service job1, job2, job3;
    private VehicleRoute route1, route2, route3;

    @BeforeEach
    void setUp() {
        job1 = Service.Builder.newInstance("job1").setLocation(Location.newInstance(0, 10)).build();
        job2 = Service.Builder.newInstance("job2").setLocation(Location.newInstance(0, 20)).build();
        job3 = Service.Builder.newInstance("job3").setLocation(Location.newInstance(0, 30)).build();

        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(Location.newInstance(0, 15)).build();
        VehicleImpl v3 = VehicleImpl.Builder.newInstance("v3").setStartLocation(Location.newInstance(0, 25)).build();

        vrp = VehicleRoutingProblem.Builder.newInstance()
            .addJob(job1).addJob(job2).addJob(job3)
            .addVehicle(v1).addVehicle(v2).addVehicle(v3)
            .build();

        // Create routes
        route1 = VehicleRoute.Builder.newInstance(v1).build();
        route2 = VehicleRoute.Builder.newInstance(v2).build();
        route3 = VehicleRoute.Builder.newInstance(v3).build();

        tracker = new AffectedJobTracker(vrp);
    }

    @Test
    @DisplayName("Should track jobs with route as best")
    void shouldTrackJobsWithRouteAsBest() {
        // Simulate: job1 has route1 as best, route2 as second best
        BoundedInsertionQueue queue1 = new BoundedInsertionQueue();
        queue1.addOrReplace(new InsertionData(10, 0, 0, null, null), route1);
        queue1.addOrReplace(new InsertionData(20, 0, 0, null, null), route2);

        tracker.updateJobTracking(job1, queue1);

        Set<Job> affected = tracker.getAffectedJobs(route1);
        assertTrue(affected.contains(job1));

        affected = tracker.getAffectedJobs(route2);
        assertTrue(affected.contains(job1));

        // route3 was not in job1's top-2
        affected = tracker.getAffectedJobs(route3);
        assertFalse(affected.contains(job1));
    }

    @Test
    @DisplayName("Should track multiple jobs per route")
    void shouldTrackMultipleJobsPerRoute() {
        // job1: best=route1, 2nd=route2
        BoundedInsertionQueue queue1 = new BoundedInsertionQueue();
        queue1.addOrReplace(new InsertionData(10, 0, 0, null, null), route1);
        queue1.addOrReplace(new InsertionData(20, 0, 0, null, null), route2);
        tracker.updateJobTracking(job1, queue1);

        // job2: best=route1, 2nd=route3
        BoundedInsertionQueue queue2 = new BoundedInsertionQueue();
        queue2.addOrReplace(new InsertionData(15, 0, 0, null, null), route1);
        queue2.addOrReplace(new InsertionData(25, 0, 0, null, null), route3);
        tracker.updateJobTracking(job2, queue2);

        // route1 is best for both jobs
        Set<Job> affected = tracker.getAffectedJobs(route1);
        assertEquals(2, affected.size());
        assertTrue(affected.contains(job1));
        assertTrue(affected.contains(job2));

        // route2 is only in job1's top-2
        affected = tracker.getAffectedJobs(route2);
        assertEquals(1, affected.size());
        assertTrue(affected.contains(job1));

        // route3 is only in job2's top-2
        affected = tracker.getAffectedJobs(route3);
        assertEquals(1, affected.size());
        assertTrue(affected.contains(job2));
    }

    @Test
    @DisplayName("Should update tracking when job's top-2 changes")
    void shouldUpdateTrackingWhenTopTwoChanges() {
        // Initial: job1 has route1 as best, route2 as second
        BoundedInsertionQueue queue1 = new BoundedInsertionQueue();
        queue1.addOrReplace(new InsertionData(10, 0, 0, null, null), route1);
        queue1.addOrReplace(new InsertionData(20, 0, 0, null, null), route2);
        tracker.updateJobTracking(job1, queue1);

        assertTrue(tracker.getAffectedJobs(route1).contains(job1));
        assertTrue(tracker.getAffectedJobs(route2).contains(job1));
        assertFalse(tracker.getAffectedJobs(route3).contains(job1));

        // Update: now job1 has route3 as best, route1 as second
        queue1.addOrReplace(new InsertionData(5, 0, 0, null, null), route3);
        tracker.updateJobTracking(job1, queue1);

        // route3 should now be in affected jobs
        assertTrue(tracker.getAffectedJobs(route3).contains(job1));
        // route1 should still be affected (now as 2nd best)
        assertTrue(tracker.getAffectedJobs(route1).contains(job1));
        // route2 is no longer in top-2
        assertFalse(tracker.getAffectedJobs(route2).contains(job1));
    }

    @Test
    @DisplayName("Should remove job from tracking")
    void shouldRemoveJobFromTracking() {
        BoundedInsertionQueue queue1 = new BoundedInsertionQueue();
        queue1.addOrReplace(new InsertionData(10, 0, 0, null, null), route1);
        queue1.addOrReplace(new InsertionData(20, 0, 0, null, null), route2);
        tracker.updateJobTracking(job1, queue1);

        assertTrue(tracker.getAffectedJobs(route1).contains(job1));

        tracker.removeJob(job1);

        assertFalse(tracker.getAffectedJobs(route1).contains(job1));
        assertFalse(tracker.getAffectedJobs(route2).contains(job1));
    }

    @Test
    @DisplayName("Should track spatial neighborhood")
    void shouldTrackSpatialNeighborhood() {
        tracker.registerSpatialNeighborhood(job1, Arrays.asList(route1, route2));
        tracker.registerSpatialNeighborhood(job2, Arrays.asList(route2, route3));

        // Set up top-2 for job1: best=route1, 2nd=route2
        BoundedInsertionQueue queue1 = new BoundedInsertionQueue();
        queue1.addOrReplace(new InsertionData(10, 0, 0, null, null), route1);
        queue1.addOrReplace(new InsertionData(20, 0, 0, null, null), route2);
        tracker.updateJobTracking(job1, queue1);

        // Set up top-2 for job2: best=route2, 2nd=route3
        BoundedInsertionQueue queue2 = new BoundedInsertionQueue();
        queue2.addOrReplace(new InsertionData(15, 0, 0, null, null), route2);
        queue2.addOrReplace(new InsertionData(25, 0, 0, null, null), route3);
        tracker.updateJobTracking(job2, queue2);

        // For route1 modification:
        // - job1 is affected (route1 is best)
        // - job2 is NOT in neighborhood, so not in cheap check either
        Set<Job> affected = tracker.getAffectedJobs(route1);
        Set<Job> cheapCheck = tracker.getJobsNeedingCheapCheck(route1, affected);

        assertTrue(affected.contains(job1));
        assertFalse(affected.contains(job2));
        assertFalse(cheapCheck.contains(job2)); // job2's neighborhood doesn't include route1
    }

    @Test
    @DisplayName("Should identify jobs needing cheap check")
    void shouldIdentifyJobsNeedingCheapCheck() {
        // Both jobs have route1 in neighborhood
        tracker.registerSpatialNeighborhood(job1, Arrays.asList(route1, route2));
        tracker.registerSpatialNeighborhood(job2, Arrays.asList(route1, route3));

        // job1: best=route1, 2nd=route2 (route1 in top-2)
        BoundedInsertionQueue queue1 = new BoundedInsertionQueue();
        queue1.addOrReplace(new InsertionData(10, 0, 0, null, null), route1);
        queue1.addOrReplace(new InsertionData(20, 0, 0, null, null), route2);
        tracker.updateJobTracking(job1, queue1);

        // job2: best=route3, 2nd=... (route1 NOT in top-2, but in neighborhood)
        BoundedInsertionQueue queue2 = new BoundedInsertionQueue();
        queue2.addOrReplace(new InsertionData(15, 0, 0, null, null), route3);
        tracker.updateJobTracking(job2, queue2);

        Set<Job> affected = tracker.getAffectedJobs(route1);
        Set<Job> cheapCheck = tracker.getJobsNeedingCheapCheck(route1, affected);

        // job1 is affected (route1 in top-2)
        assertTrue(affected.contains(job1));
        assertFalse(cheapCheck.contains(job1));

        // job2 is in neighborhood but not top-2, needs cheap check
        assertFalse(affected.contains(job2));
        assertTrue(cheapCheck.contains(job2));
    }

    @Test
    @DisplayName("Should check competitiveness with lower bound")
    void shouldCheckCompetitivenessWithLowerBound() {
        // job1 at (0, 10), route1 starts at (0, 0), route3 starts at (0, 25)
        BoundedInsertionQueue queue1 = new BoundedInsertionQueue();
        queue1.addOrReplace(new InsertionData(10, 0, 0, null, null), route1);  // best
        queue1.addOrReplace(new InsertionData(100, 0, 0, null, null), route2); // 2nd best (far)
        tracker.updateJobTracking(job1, queue1);

        // route3 is far from job1 (distance ~15), 2nd best cost is 100
        // Lower bound (15) < 2nd best (100), so could be competitive
        assertTrue(tracker.couldBeCompetitive(job1, route3));

        // Now with a closer 2nd best
        queue1.addOrReplace(new InsertionData(12, 0, 0, null, null), route2);
        tracker.updateJobTracking(job1, queue1);

        // route3 is far from job1, lower bound (~15) > 2nd best (12)
        // This depends on exact distance calculation
    }

    @Test
    @DisplayName("Should clear all tracking data")
    void shouldClearAllTrackingData() {
        BoundedInsertionQueue queue1 = new BoundedInsertionQueue();
        queue1.addOrReplace(new InsertionData(10, 0, 0, null, null), route1);
        tracker.updateJobTracking(job1, queue1);
        tracker.registerSpatialNeighborhood(job1, Arrays.asList(route1, route2));

        assertTrue(tracker.getAffectedJobs(route1).contains(job1));

        tracker.clear();

        assertTrue(tracker.getAffectedJobs(route1).isEmpty());
    }

    @Test
    @DisplayName("Should handle empty queue gracefully")
    void shouldHandleEmptyQueueGracefully() {
        BoundedInsertionQueue emptyQueue = new BoundedInsertionQueue();
        tracker.updateJobTracking(job1, emptyQueue);

        assertTrue(tracker.getAffectedJobs(route1).isEmpty());
    }

    @Test
    @DisplayName("Should handle single entry queue")
    void shouldHandleSingleEntryQueue() {
        BoundedInsertionQueue queue = new BoundedInsertionQueue();
        queue.addOrReplace(new InsertionData(10, 0, 0, null, null), route1);
        tracker.updateJobTracking(job1, queue);

        assertTrue(tracker.getAffectedJobs(route1).contains(job1));
        assertFalse(tracker.getAffectedJobs(route2).contains(job1));

        // With only one option, any route could be competitive
        assertTrue(tracker.couldBeCompetitive(job1, route2));
    }
}
