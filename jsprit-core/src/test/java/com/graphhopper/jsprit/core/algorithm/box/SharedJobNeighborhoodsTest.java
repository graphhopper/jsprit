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

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.ruin.JobNeighborhoods;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.util.Solutions;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SharedJobNeighborhoodsTest {

    private static class Counting implements JobNeighborhoods {
        final JobNeighborhoods delegate;
        final AtomicInteger initialised = new AtomicInteger(), asked = new AtomicInteger();

        Counting(JobNeighborhoods delegate) {
            this.delegate = delegate;
        }

        @Override
        public Iterator<Job> getNearestNeighborsIterator(int nNeighbors, Job neighborTo) {
            asked.incrementAndGet();
            return delegate.getNearestNeighborsIterator(nNeighbors, neighborTo);
        }

        @Override
        public void initialise() {
            initialised.incrementAndGet();
            delegate.initialise();
        }

        @Override
        public double getMaxDistance() {
            return delegate.getMaxDistance();
        }
    }

    private static VehicleRoutingProblem problem() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        builder.addVehicle(VehicleImpl.Builder.newInstance("v1").setStartLocation(Location.newInstance(0, 0)).build());
        builder.addVehicle(VehicleImpl.Builder.newInstance("v2").setStartLocation(Location.newInstance(50, 50)).build());
        Random coordinates = new Random(7);
        for (int i = 0; i < 40; i++)
            builder.addJob(Service.Builder.newInstance("s" + i).setLocation(Location.newInstance(coordinates.nextInt(100), coordinates.nextInt(100))).build());
        return builder.build();
    }

    private static VehicleRoutingProblemSolution solve(VehicleRoutingProblem vrp, JobNeighborhoods shared) {
        Jsprit.Builder builder = Jsprit.Builder.newInstance(vrp).setRandom(new Random(4711));
        if (shared != null) builder.setJobNeighborhoods(shared);
        VehicleRoutingAlgorithm algorithm = builder.buildAlgorithm();
        algorithm.setMaxIterations(200);
        return Solutions.bestOf(algorithm.searchSolutions());
    }

    @Test
    void sharedNeighborhoodsAreUsedAsTheyAreAndGiveTheSolutionOfTheAlgorithmsOwn() {
        VehicleRoutingProblem vrp = problem();
        Counting shared = new Counting(Jsprit.createJobNeighborhoods(vrp));

        VehicleRoutingProblemSolution own = solve(vrp, null);
        VehicleRoutingProblemSolution first = solve(vrp, shared);
        VehicleRoutingProblemSolution second = solve(vrp, shared);

        assertEquals(0, shared.initialised.get(), "handed in initialised, not initialised again");
        assertTrue(shared.asked.get() > 0, "the ruin operators read the shared neighborhoods");
        assertEquals(own.getCost(), first.getCost(), 1e-9);
        assertEquals(own.getCost(), second.getCost(), 1e-9);
        assertEquals(own.getRoutes().size(), first.getRoutes().size());
    }
}
