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

import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.FiniteFleetManagerFactory;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleFleetManager;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RegretInsertionFast Affected Job Tracking Test")
class RegretInsertionFastAffectedJobTest {

    @Test
    @DisplayName("Should produce same results with and without affected-job tracking")
    void shouldProduceSameResultsWithAndWithoutTracking() {
        // Create a moderate-sized problem
        List<Service> services = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            services.add(Service.Builder.newInstance("s" + i)
                .setLocation(Location.newInstance(i * 5, (i % 5) * 10))
                .build());
        }

        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1")
            .setStartLocation(Location.newInstance(0, 0)).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2")
            .setStartLocation(Location.newInstance(50, 0)).build();
        VehicleImpl v3 = VehicleImpl.Builder.newInstance("v3")
            .setStartLocation(Location.newInstance(100, 0)).build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance()
            .addVehicle(v1).addVehicle(v2).addVehicle(v3);
        for (Service s : services) {
            vrpBuilder.addJob(s);
        }
        VehicleRoutingProblem vrp = vrpBuilder.build();

        // Run with affected-job tracking ENABLED
        double costWithTracking = runInsertion(vrp, true);

        // Run with affected-job tracking DISABLED
        double costWithoutTracking = runInsertion(vrp, false);

        // Results should be identical (same algorithm, just different optimization path)
        assertEquals(costWithoutTracking, costWithTracking, 0.001,
            "Affected-job tracking should produce identical results");
    }

    @Test
    @DisplayName("Should handle single vehicle correctly")
    void shouldHandleSingleVehicleCorrectly() {
        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(0, 10)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(0, 20)).build();
        Service s3 = Service.Builder.newInstance("s3").setLocation(Location.newInstance(0, 30)).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v")
            .setStartLocation(Location.newInstance(0, 0)).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
            .addJob(s1).addJob(s2).addJob(s3)
            .addVehicle(v).build();

        StateManager stateManager = new StateManager(vrp);
        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);
        VehicleFleetManager fm = new FiniteFleetManagerFactory(vrp.getVehicles()).createFleetManager();

        ArrayList<com.graphhopper.jsprit.core.algorithm.recreate.listener.InsertionListener> iListeners = new ArrayList<>();
        JobInsertionCostsCalculator calculator = new JobInsertionCostsCalculatorBuilder(
                iListeners, new ArrayList<>())
            .setLocalLevel(true)
            .setConstraintManager(constraintManager)
            .setStateManager(stateManager)
            .setVehicleRoutingProblem(vrp)
            .setVehicleFleetManager(fm)
            .setAllowVehicleSwitch(true)
            .build();

        RegretInsertionFast insertion = new RegretInsertionFast(calculator, vrp, fm);
        insertion.setAffectedJobTrackingEnabled(true);
        for (var l : iListeners) insertion.addListener(l);

        Collection<VehicleRoute> routes = new ArrayList<>();
        stateManager.informInsertionStarts(routes, null);
        Collection<Job> badJobs = insertion.insertJobs(routes, vrp.getJobs().values());

        assertEquals(0, badJobs.size());
        assertEquals(1, routes.size());
        assertEquals(3, routes.iterator().next().getActivities().size());
    }

    @Test
    @DisplayName("Should handle multiple vehicles correctly")
    void shouldHandleMultipleVehiclesCorrectly() {
        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(0, 10)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(100, 10)).build();

        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1")
            .setStartLocation(Location.newInstance(0, 0)).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2")
            .setStartLocation(Location.newInstance(100, 0)).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
            .addJob(s1).addJob(s2)
            .addVehicle(v1).addVehicle(v2)
            .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
            .build();

        // Run with tracking enabled
        double costWithTracking = runInsertion(vrp, true);

        // Run with tracking disabled
        double costWithoutTracking = runInsertion(vrp, false);

        assertEquals(costWithoutTracking, costWithTracking, 0.001);
    }

    @Test
    @DisplayName("Should work with regret-3")
    void shouldWorkWithRegret3() {
        List<Service> services = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            services.add(Service.Builder.newInstance("s" + i)
                .setLocation(Location.newInstance(i * 10, 0))
                .build());
        }

        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1")
            .setStartLocation(Location.newInstance(0, 0)).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2")
            .setStartLocation(Location.newInstance(50, 0)).build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance()
            .addVehicle(v1).addVehicle(v2);
        for (Service s : services) {
            vrpBuilder.addJob(s);
        }
        VehicleRoutingProblem vrp = vrpBuilder.build();

        StateManager stateManager = new StateManager(vrp);
        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);
        VehicleFleetManager fm = new FiniteFleetManagerFactory(vrp.getVehicles()).createFleetManager();

        ArrayList<com.graphhopper.jsprit.core.algorithm.recreate.listener.InsertionListener> iListeners = new ArrayList<>();
        JobInsertionCostsCalculator calculator = new JobInsertionCostsCalculatorBuilder(
                iListeners, new ArrayList<>())
            .setLocalLevel(true)
            .setConstraintManager(constraintManager)
            .setStateManager(stateManager)
            .setVehicleRoutingProblem(vrp)
            .setVehicleFleetManager(fm)
            .setAllowVehicleSwitch(true)
            .build();

        RegretInsertionFast insertion = new RegretInsertionFast(calculator, vrp, fm);
        insertion.setRegretK(3);
        insertion.setAffectedJobTrackingEnabled(true);
        for (var l : iListeners) insertion.addListener(l);

        Collection<VehicleRoute> routes = new ArrayList<>();
        stateManager.informInsertionStarts(routes, null);
        Collection<Job> badJobs = insertion.insertJobs(routes, vrp.getJobs().values());

        assertEquals(0, badJobs.size());
        // All jobs should be inserted
        int totalJobs = 0;
        for (VehicleRoute r : routes) {
            totalJobs += r.getTourActivities().getJobs().size();
        }
        assertEquals(10, totalJobs);
    }

    @Test
    @DisplayName("Should respect setAffectedJobTrackingEnabled flag")
    void shouldRespectTrackingEnabledFlag() {
        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(0, 10)).build();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v")
            .setStartLocation(Location.newInstance(0, 0)).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
            .addJob(s1).addVehicle(v).build();

        StateManager stateManager = new StateManager(vrp);
        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);
        VehicleFleetManager fm = new FiniteFleetManagerFactory(vrp.getVehicles()).createFleetManager();

        ArrayList<com.graphhopper.jsprit.core.algorithm.recreate.listener.InsertionListener> iListeners = new ArrayList<>();
        JobInsertionCostsCalculator calculator = new JobInsertionCostsCalculatorBuilder(
                iListeners, new ArrayList<>())
            .setLocalLevel(true)
            .setConstraintManager(constraintManager)
            .setStateManager(stateManager)
            .setVehicleRoutingProblem(vrp)
            .setVehicleFleetManager(fm)
            .setAllowVehicleSwitch(true)
            .build();

        RegretInsertionFast insertion = new RegretInsertionFast(calculator, vrp, fm);

        // Default should be enabled
        assertTrue(insertion.isAffectedJobTrackingEnabled());

        insertion.setAffectedJobTrackingEnabled(false);
        assertFalse(insertion.isAffectedJobTrackingEnabled());

        insertion.setAffectedJobTrackingEnabled(true);
        assertTrue(insertion.isAffectedJobTrackingEnabled());
    }

    private double runInsertion(VehicleRoutingProblem vrp, boolean trackingEnabled) {
        StateManager stateManager = new StateManager(vrp);
        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);
        VehicleFleetManager fm = new FiniteFleetManagerFactory(vrp.getVehicles()).createFleetManager();

        ArrayList<com.graphhopper.jsprit.core.algorithm.recreate.listener.InsertionListener> iListeners = new ArrayList<>();
        JobInsertionCostsCalculator calculator = new JobInsertionCostsCalculatorBuilder(
                iListeners, new ArrayList<>())
            .setLocalLevel(true)
            .setConstraintManager(constraintManager)
            .setStateManager(stateManager)
            .setVehicleRoutingProblem(vrp)
            .setVehicleFleetManager(fm)
            .setAllowVehicleSwitch(true)
            .build();

        RegretInsertionFast insertion = new RegretInsertionFast(calculator, vrp, fm);
        insertion.setAffectedJobTrackingEnabled(trackingEnabled);
        for (var l : iListeners) insertion.addListener(l);

        Collection<VehicleRoute> routes = new ArrayList<>();
        stateManager.informInsertionStarts(routes, null);
        insertion.insertJobs(routes, vrp.getJobs().values());

        // Calculate total cost
        double totalCost = 0;
        for (VehicleRoute r : routes) {
            for (int i = 0; i < r.getActivities().size(); i++) {
                if (i == 0) {
                    totalCost += distance(r.getStart().getLocation(), r.getActivities().get(i).getLocation());
                } else {
                    totalCost += distance(r.getActivities().get(i - 1).getLocation(), r.getActivities().get(i).getLocation());
                }
            }
            if (!r.getActivities().isEmpty()) {
                totalCost += distance(r.getActivities().get(r.getActivities().size() - 1).getLocation(), r.getEnd().getLocation());
            }
        }
        return totalCost;
    }

    private double distance(Location l1, Location l2) {
        if (l1.getCoordinate() == null || l2.getCoordinate() == null) return 0;
        double dx = l1.getCoordinate().getX() - l2.getCoordinate().getX();
        double dy = l1.getCoordinate().getY() - l2.getCoordinate().getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
}
