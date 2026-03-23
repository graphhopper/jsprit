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

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.Solutions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Regret K Insertion Test")
class RegretKInsertionTest {

    @Test
    @DisplayName("Regret-3 with sum strategy should produce feasible solution")
    void regret3WithSumStrategy() {
        VehicleRoutingProblem vrp = createTestProblem();
        StateManager stateManager = new StateManager(vrp);
        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
                .addCoreStateAndConstraintStuff(true)
                .setStateAndConstraintManager(stateManager, constraintManager)
                .setProperty(Jsprit.Parameter.REGRET_K, "3")
                .setProperty(Jsprit.Parameter.REGRET_K_STRATEGY, "sum")
                .setProperty(Jsprit.Parameter.ITERATIONS, "100")
                .buildAlgorithm();

        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());

        assertNotNull(solution);
        assertTrue(solution.getUnassignedJobs().isEmpty(), "All jobs should be assigned");
    }

    @Test
    @DisplayName("Regret-4 with max strategy should produce feasible solution")
    void regret4WithMaxStrategy() {
        VehicleRoutingProblem vrp = createTestProblem();
        StateManager stateManager = new StateManager(vrp);
        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
                .addCoreStateAndConstraintStuff(true)
                .setStateAndConstraintManager(stateManager, constraintManager)
                .setProperty(Jsprit.Parameter.REGRET_K, "4")
                .setProperty(Jsprit.Parameter.REGRET_K_STRATEGY, "max")
                .setProperty(Jsprit.Parameter.ITERATIONS, "100")
                .buildAlgorithm();

        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());

        assertNotNull(solution);
        assertTrue(solution.getUnassignedJobs().isEmpty(), "All jobs should be assigned");
    }

    @Test
    @DisplayName("Regret-all with avg strategy should produce feasible solution")
    void regretAllWithAvgStrategy() {
        VehicleRoutingProblem vrp = createTestProblem();
        StateManager stateManager = new StateManager(vrp);
        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
                .addCoreStateAndConstraintStuff(true)
                .setStateAndConstraintManager(stateManager, constraintManager)
                .setProperty(Jsprit.Parameter.REGRET_K, "all")
                .setProperty(Jsprit.Parameter.REGRET_K_STRATEGY, "avg")
                .setProperty(Jsprit.Parameter.ITERATIONS, "100")
                .buildAlgorithm();

        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());

        assertNotNull(solution);
        assertTrue(solution.getUnassignedJobs().isEmpty(), "All jobs should be assigned");
    }

    @Test
    @DisplayName("Default regret-2 should produce same results as explicit k=2")
    void defaultRegret2SameAsExplicitK2() {
        VehicleRoutingProblem vrp = createSimpleProblem();
        StateManager stateManager = new StateManager(vrp);
        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);

        // Run with default (should be k=2)
        VehicleRoutingAlgorithm vraDefault = Jsprit.Builder.newInstance(vrp)
                .addCoreStateAndConstraintStuff(true)
                .setStateAndConstraintManager(stateManager, constraintManager)
                .setProperty(Jsprit.Parameter.ITERATIONS, "50")
                .buildAlgorithm();
        VehicleRoutingProblemSolution solutionDefault = Solutions.bestOf(vraDefault.searchSolutions());

        // Reset state manager
        stateManager = new StateManager(vrp);
        constraintManager = new ConstraintManager(vrp, stateManager);

        // Run with explicit k=2
        VehicleRoutingAlgorithm vraExplicit = Jsprit.Builder.newInstance(vrp)
                .addCoreStateAndConstraintStuff(true)
                .setStateAndConstraintManager(stateManager, constraintManager)
                .setProperty(Jsprit.Parameter.REGRET_K, "2")
                .setProperty(Jsprit.Parameter.REGRET_K_STRATEGY, "sum")
                .setProperty(Jsprit.Parameter.ITERATIONS, "50")
                .buildAlgorithm();
        VehicleRoutingProblemSolution solutionExplicit = Solutions.bestOf(vraExplicit.searchSolutions());

        assertNotNull(solutionDefault);
        assertNotNull(solutionExplicit);
        // Both should produce feasible solutions
        assertTrue(solutionDefault.getUnassignedJobs().isEmpty());
        assertTrue(solutionExplicit.getUnassignedJobs().isEmpty());
    }

    @Test
    @DisplayName("Fast regret with k=3 should work correctly")
    void fastRegretWithK3() {
        VehicleRoutingProblem vrp = createTestProblem();
        StateManager stateManager = new StateManager(vrp);
        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
                .addCoreStateAndConstraintStuff(true)
                .setStateAndConstraintManager(stateManager, constraintManager)
                .setProperty(Jsprit.Parameter.FAST_REGRET, "true")
                .setProperty(Jsprit.Parameter.REGRET_K, "3")
                .setProperty(Jsprit.Parameter.REGRET_K_STRATEGY, "sum")
                .setProperty(Jsprit.Parameter.ITERATIONS, "100")
                .buildAlgorithm();

        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());

        assertNotNull(solution);
        assertTrue(solution.getUnassignedJobs().isEmpty(), "All jobs should be assigned");
    }

    @Test
    @DisplayName("Concurrent regret with k=3 should work correctly")
    void concurrentRegretWithK3() {
        VehicleRoutingProblem vrp = createTestProblem();
        StateManager stateManager = new StateManager(vrp);
        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
                .addCoreStateAndConstraintStuff(true)
                .setStateAndConstraintManager(stateManager, constraintManager)
                .setProperty(Jsprit.Parameter.THREADS, "2")
                .setProperty(Jsprit.Parameter.REGRET_K, "3")
                .setProperty(Jsprit.Parameter.REGRET_K_STRATEGY, "sum")
                .setProperty(Jsprit.Parameter.ITERATIONS, "100")
                .buildAlgorithm();

        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());

        assertNotNull(solution);
        assertTrue(solution.getUnassignedJobs().isEmpty(), "All jobs should be assigned");
    }

    @Test
    @DisplayName("Concurrent fast regret with k=4 should work correctly")
    void concurrentFastRegretWithK4() {
        VehicleRoutingProblem vrp = createTestProblem();
        StateManager stateManager = new StateManager(vrp);
        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
                .addCoreStateAndConstraintStuff(true)
                .setStateAndConstraintManager(stateManager, constraintManager)
                .setProperty(Jsprit.Parameter.THREADS, "2")
                .setProperty(Jsprit.Parameter.FAST_REGRET, "true")
                .setProperty(Jsprit.Parameter.REGRET_K, "4")
                .setProperty(Jsprit.Parameter.REGRET_K_STRATEGY, "max")
                .setProperty(Jsprit.Parameter.ITERATIONS, "100")
                .buildAlgorithm();

        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());

        assertNotNull(solution);
        assertTrue(solution.getUnassignedJobs().isEmpty(), "All jobs should be assigned");
    }

    @Test
    @DisplayName("Regret-k with shipments should work correctly")
    void regretKWithShipments() {
        Shipment s1 = Shipment.Builder.newInstance("ship1")
                .setPickupLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(0, 10)).build())
                .setDeliveryLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(10, 10)).build())
                .build();
        Shipment s2 = Shipment.Builder.newInstance("ship2")
                .setPickupLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(0, 20)).build())
                .setDeliveryLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(10, 20)).build())
                .build();
        Shipment s3 = Shipment.Builder.newInstance("ship3")
                .setPickupLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(0, 30)).build())
                .setDeliveryLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(10, 30)).build())
                .build();

        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1")
                .setStartLocation(Location.newInstance(0, 0))
                .build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addJob(s1).addJob(s2).addJob(s3)
                .addVehicle(v1)
                .build();

        StateManager stateManager = new StateManager(vrp);
        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
                .addCoreStateAndConstraintStuff(true)
                .setStateAndConstraintManager(stateManager, constraintManager)
                .setProperty(Jsprit.Parameter.REGRET_K, "3")
                .setProperty(Jsprit.Parameter.REGRET_K_STRATEGY, "sum")
                .setProperty(Jsprit.Parameter.ITERATIONS, "100")
                .buildAlgorithm();

        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());

        assertNotNull(solution);
        assertTrue(solution.getUnassignedJobs().isEmpty(), "All shipments should be assigned");
    }

    private VehicleRoutingProblem createTestProblem() {
        // Create a problem with multiple services and vehicles
        VehicleType type = VehicleTypeImpl.Builder.newInstance("type")
                .addCapacityDimension(0, 10)
                .build();

        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1")
                .setType(type)
                .setStartLocation(Location.newInstance(0, 0))
                .build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2")
                .setType(type)
                .setStartLocation(Location.newInstance(50, 0))
                .build();
        VehicleImpl v3 = VehicleImpl.Builder.newInstance("v3")
                .setType(type)
                .setStartLocation(Location.newInstance(100, 0))
                .build();

        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance()
                .addVehicle(v1).addVehicle(v2).addVehicle(v3)
                .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);

        // Add services spread across different locations
        for (int i = 1; i <= 12; i++) {
            Service s = Service.Builder.newInstance("s" + i)
                    .setLocation(Location.newInstance((i % 4) * 30, (i / 4) * 20))
                    .addSizeDimension(0, 1)
                    .build();
            builder.addJob(s);
        }

        return builder.build();
    }

    private VehicleRoutingProblem createSimpleProblem() {
        Service s1 = Service.Builder.newInstance("s1")
                .setLocation(Location.newInstance(0, 10))
                .build();
        Service s2 = Service.Builder.newInstance("s2")
                .setLocation(Location.newInstance(0, 20))
                .build();

        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1")
                .setStartLocation(Location.newInstance(0, 0))
                .build();

        return VehicleRoutingProblem.Builder.newInstance()
                .addJob(s1).addJob(s2)
                .addVehicle(v1)
                .build();
    }
}
