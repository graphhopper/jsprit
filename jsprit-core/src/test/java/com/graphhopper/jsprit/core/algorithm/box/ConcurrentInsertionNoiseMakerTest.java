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
package com.graphhopper.jsprit.core.algorithm.box;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Insertion noise of the multi-threaded algorithm")
class ConcurrentInsertionNoiseMakerTest {

    private static final double MAX_COSTS = 1000, LEVEL = 0.1;

    private final VehicleRoutingProblem vrp = problem();

    private final List<TourActivity> acts = activities(vrp);

    @Test
    @DisplayName("the noise of a position does not depend on the order or the number of evaluations")
    void noiseIsAFunctionOfThePosition() {
        ConcurrentInsertionNoiseMaker noise = noiseMaker(1.0, 7);
        noise.informIterationStarts(1, vrp, Collections.emptyList());
        double first = noise.getCosts(null, acts.get(0), acts.get(1), acts.get(2), 0);
        // other positions in between, some of them repeatedly
        for (int i = 0; i < 50; i++) noise.getCosts(null, acts.get(i % 5), acts.get(5), acts.get((i + 1) % 5), 0);
        assertEquals(first, noise.getCosts(null, acts.get(0), acts.get(1), acts.get(2), 0), 0.0);
        assertTrue(first >= 0 && first < LEVEL * MAX_COSTS, "noise " + first);
        assertNotEquals(first, noise.getCosts(null, acts.get(0), acts.get(1), acts.get(3), 0), 0.0);
        assertNotEquals(first, noise.getCosts(null, acts.get(2), acts.get(1), acts.get(0), 0), 0.0);
    }

    @Test
    @DisplayName("a new iteration draws new noise, the same seed the same noise again")
    void noiseChangesWithTheIteration() {
        ConcurrentInsertionNoiseMaker noise = noiseMaker(1.0, 7);
        noise.informIterationStarts(1, vrp, Collections.emptyList());
        double iteration1 = noise.getCosts(null, acts.get(0), acts.get(1), acts.get(2), 0);
        noise.informIterationStarts(2, vrp, Collections.emptyList());
        double iteration2 = noise.getCosts(null, acts.get(0), acts.get(1), acts.get(2), 0);
        assertNotEquals(iteration1, iteration2, 0.0);

        ConcurrentInsertionNoiseMaker sameSeed = noiseMaker(1.0, 7);
        sameSeed.informIterationStarts(1, vrp, Collections.emptyList());
        assertEquals(iteration1, sameSeed.getCosts(null, acts.get(0), acts.get(1), acts.get(2), 0), 0.0);
    }

    @Test
    @DisplayName("without noise in an iteration every position costs nothing extra")
    void noNoiseOutsideNoisyIterations() {
        ConcurrentInsertionNoiseMaker noise = noiseMaker(0.0, 7);
        noise.informIterationStarts(1, vrp, Collections.emptyList());
        assertEquals(0.0, noise.getCosts(null, acts.get(0), acts.get(1), acts.get(2), 0), 0.0);
    }

    @Test
    @DisplayName("the noise spreads over the whole range")
    void noiseIsSpread() {
        ConcurrentInsertionNoiseMaker noise = noiseMaker(1.0, 3);
        noise.informIterationStarts(1, vrp, Collections.emptyList());
        double sum = 0, min = Double.MAX_VALUE, max = 0;
        int n = 0;
        for (TourActivity prev : acts) {
            for (TourActivity act : acts) {
                for (TourActivity next : acts) {
                    double v = noise.getCosts(null, prev, act, next, 0);
                    sum += v;
                    min = Math.min(min, v);
                    max = Math.max(max, v);
                    n++;
                }
            }
        }
        double range = LEVEL * MAX_COSTS;
        assertEquals(range / 2, sum / n, range * 0.05);
        assertTrue(min < range * 0.05 && max > range * 0.95, "min " + min + " max " + max);
    }

    private ConcurrentInsertionNoiseMaker noiseMaker(double probability, long seed) {
        ConcurrentInsertionNoiseMaker noise = new ConcurrentInsertionNoiseMaker(vrp, MAX_COSTS, LEVEL, probability);
        noise.setRandom(new Random(seed));
        return noise;
    }

    private static VehicleRoutingProblem problem() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance()
            .addVehicle(VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build());
        for (int i = 0; i < 12; i++) {
            builder.addJob(Service.Builder.newInstance("s" + i).setLocation(Location.newInstance(i, i)).build());
        }
        return builder.build();
    }

    private static List<TourActivity> activities(VehicleRoutingProblem vrp) {
        List<TourActivity> activities = new ArrayList<>();
        for (int i = 0; i < 12; i++) activities.addAll(vrp.getActivities(vrp.getJobs().get("s" + i)));
        return activities;
    }
}
