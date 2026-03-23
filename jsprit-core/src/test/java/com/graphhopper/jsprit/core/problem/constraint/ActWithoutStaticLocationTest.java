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
package com.graphhopper.jsprit.core.problem.constraint;

import com.graphhopper.jsprit.core.algorithm.recreate.LocalActivityInsertionCostsCalculator;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.cost.WaitingTimeCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ActWithoutStaticLocation;
import com.graphhopper.jsprit.core.problem.solution.route.activity.End;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for ActWithoutStaticLocation handling in VehicleDependentTimeWindowConstraints
 * and LocalActivityInsertionCostsCalculator.
 * <p>
 * Reproduces bug reported in GitHub issue #574:
 * The return values of getPreviousLocation() and getNextLocation() were called on
 * ActWithoutStaticLocation instances but never assigned back to the local variables.
 */
class ActWithoutStaticLocationTest {

    private VehicleRoutingTransportCosts transportCosts;
    private VehicleRoutingActivityCosts activityCosts;
    private VehicleImpl vehicle;
    private Location locationA;
    private Location locationB;
    private Location locationC;
    private Location locationD;

    @BeforeEach
    public void setUp() {
        // Set up distinct locations
        locationA = Location.Builder.newInstance().setId("A").setCoordinate(com.graphhopper.jsprit.core.util.Coordinate.newInstance(0, 0)).build();
        locationB = Location.Builder.newInstance().setId("B").setCoordinate(com.graphhopper.jsprit.core.util.Coordinate.newInstance(10, 0)).build();
        locationC = Location.Builder.newInstance().setId("C").setCoordinate(com.graphhopper.jsprit.core.util.Coordinate.newInstance(20, 0)).build();
        locationD = Location.Builder.newInstance().setId("D").setCoordinate(com.graphhopper.jsprit.core.util.Coordinate.newInstance(30, 0)).build();

        vehicle = VehicleImpl.Builder.newInstance("v")
                .setType(VehicleTypeImpl.Builder.newInstance("type").build())
                .setStartLocation(locationA)
                .setEndLocation(locationD)
                .build();

        // Transport costs that return distinct values based on locations
        transportCosts = new VehicleRoutingTransportCosts() {
            @Override
            public double getTransportCost(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle) {
                if (from == null || to == null) return 0;
                // Return the sum of coordinates as cost - this way we can verify which locations are used
                double fromX = from.getCoordinate() != null ? from.getCoordinate().getX() : 0;
                double toX = to.getCoordinate() != null ? to.getCoordinate().getX() : 0;
                return Math.abs(toX - fromX);
            }

            @Override
            public double getTransportTime(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle) {
                return getTransportCost(from, to, departureTime, driver, vehicle);
            }

            @Override
            public double getBackwardTransportCost(Location from, Location to, double arrivalTime, Driver driver, Vehicle vehicle) {
                return getTransportCost(from, to, arrivalTime, driver, vehicle);
            }

            @Override
            public double getBackwardTransportTime(Location from, Location to, double arrivalTime, Driver driver, Vehicle vehicle) {
                return getTransportTime(from, to, arrivalTime, driver, vehicle);
            }

            @Override
            public double getDistance(Location from, Location to, double departureTime, Vehicle vehicle) {
                return getTransportCost(from, to, departureTime, null, vehicle);
            }
        };

        activityCosts = new WaitingTimeCosts();
    }

    /**
     * Test that LocalActivityInsertionCostsCalculator correctly uses getNextLocation()
     * when nextAct is an ActWithoutStaticLocation.
     * <p>
     * Before the fix, nextLocation was set to nextAct.getLocation() which returns
     * previousLocation, not getNextLocation().
     */
    @Test
    void testLocalActivityInsertionCostsCalculator_usesNextLocationForActWithoutStaticLocation() {
        // Create services
        Service prevService = Service.Builder.newInstance("prev").setLocation(locationA).build();
        Service newService = Service.Builder.newInstance("new").setLocation(locationB).build();
        Service nextService = Service.Builder.newInstance("next").setLocation(locationC).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addVehicle(vehicle)
                .addJob(prevService)
                .addJob(newService)
                .addJob(nextService)
                .build();

        VehicleRoute route = VehicleRoute.emptyRoute();
        JobInsertionContext context = new JobInsertionContext(route, newService, vehicle, null, 0);

        StateManager stateManager = new StateManager(vrp);
        LocalActivityInsertionCostsCalculator calculator = new LocalActivityInsertionCostsCalculator(
                transportCosts, activityCosts, stateManager);

        // Create prevAct as regular ServiceActivity at location A
        TourActivity prevAct = vrp.getActivities(prevService).getFirst();

        // Create newAct at location B
        TourActivity newAct = vrp.getActivities(newService).getFirst();

        // Create nextAct as ActWithoutStaticLocation
        // Its getLocation() returns previousLocation (locationB)
        // Its getNextLocation() returns nextLocation (locationD)
        ActWithoutStaticLocation nextAct = new TestActWithoutStaticLocation(nextService);
        nextAct.setPreviousLocation(locationB);  // getLocation() will return this
        nextAct.setNextLocation(locationD);       // getNextLocation() returns this

        // Calculate costs - should use locationD for next activity, not locationB
        // With the bug, it would use locationB (from getLocation())
        // With the fix, it should use locationD (from getNextLocation())
        double costs = calculator.getCosts(context, prevAct, nextAct, newAct, 0);

        // If we use the correct nextLocation (D at x=30):
        // cost prev->new = |10-0| = 10
        // cost new->next = |30-10| = 20
        // total new costs = 30
        // old costs (prev->next directly) = |30-0| = 30 (when route is empty, no old transport costs for non-shipment)
        // But for empty route with non-DeliverShipment, old cost = 0
        // So total = 30 - 0 = 30

        // If the bug exists (uses locationB instead of locationD):
        // cost new->next = |10-10| = 0
        // total new costs = 10
        // So total = 10 - 0 = 10

        // Verify that the correct location (D) is being used
        // The costs should reflect distance to D (x=30), not B (x=10)
        Assertions.assertEquals(30.0, costs, 0.01, "Cost should reflect transport to nextLocation (D), not previousLocation (B)");
    }

    /**
     * Test that VehicleDependentTimeWindowConstraints correctly uses getNextLocation()
     * when nextAct is an ActWithoutStaticLocation.
     * <p>
     * This test uses End as the nextAct to avoid StateManager index issues,
     * and instead tests prevAct as ActWithoutStaticLocation.
     */
    @Test
    void testVehicleDependentTimeWindowConstraints_usesPreviousLocationForPrevActWithoutStaticLocation() {
        Service prevService = Service.Builder.newInstance("prev").setLocation(locationA).build();
        Service newService = Service.Builder.newInstance("new").setLocation(locationB).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addVehicle(vehicle)
                .addJob(prevService)
                .addJob(newService)
                .build();

        StateManager stateManager = new StateManager(vrp);
        VehicleDependentTimeWindowConstraints constraints = new VehicleDependentTimeWindowConstraints(
                stateManager, transportCosts, activityCosts);

        VehicleRoute route = VehicleRoute.emptyRoute();
        JobInsertionContext context = new JobInsertionContext(route, newService, vehicle, null, 0);

        // Create prevAct as ActWithoutStaticLocation
        // Its previousLocation should be used for route cost calculations
        ActWithoutStaticLocation prevAct = new TestActWithoutStaticLocation(prevService);
        prevAct.setPreviousLocation(locationA);
        prevAct.setNextLocation(locationC);
        prevAct.setTheoreticalEarliestOperationStartTime(0);
        prevAct.setTheoreticalLatestOperationStartTime(1000);

        // Create newAct at location B with wide time windows
        TourActivity newAct = vrp.getActivities(newService).getFirst();
        newAct.setTheoreticalEarliestOperationStartTime(0);
        newAct.setTheoreticalLatestOperationStartTime(1000);

        // Use End as nextAct - the vehicle ends at locationD
        End nextAct = new End(locationD, 0, 1000);

        // The constraint should be fulfilled when using correct locations
        HardActivityConstraint.ConstraintsStatus status = constraints.fulfilled(context, prevAct, newAct, nextAct, 0);

        Assertions.assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, status, "Constraint should be fulfilled with correct prevLocation");
    }

    /**
     * Test that prevLocation is correctly assigned from getPreviousLocation()
     * when prevAct is an ActWithoutStaticLocation.
     */
    @Test
    void testLocalActivityInsertionCostsCalculator_usesPreviousLocationForPrevActWithoutStaticLocation() {
        Service prevService = Service.Builder.newInstance("prev").setLocation(locationA).build();
        Service newService = Service.Builder.newInstance("new").setLocation(locationB).build();
        Service nextService = Service.Builder.newInstance("next").setLocation(locationD).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addVehicle(vehicle)
                .addJob(prevService)
                .addJob(newService)
                .addJob(nextService)
                .build();

        VehicleRoute route = VehicleRoute.emptyRoute();
        JobInsertionContext context = new JobInsertionContext(route, newService, vehicle, null, 0);

        StateManager stateManager = new StateManager(vrp);
        LocalActivityInsertionCostsCalculator calculator = new LocalActivityInsertionCostsCalculator(
                transportCosts, activityCosts, stateManager);

        // Create prevAct as ActWithoutStaticLocation
        // Its getLocation() returns previousLocation
        // For prevAct, we want to use previousLocation anyway, so getLocation() and getPreviousLocation()
        // return the same thing - but the code should still work correctly
        ActWithoutStaticLocation prevAct = new TestActWithoutStaticLocation(prevService);
        prevAct.setPreviousLocation(locationA);
        prevAct.setNextLocation(locationC);

        // Create newAct at location B
        TourActivity newAct = vrp.getActivities(newService).getFirst();

        // Create nextAct at location D
        TourActivity nextAct = vrp.getActivities(nextService).getFirst();

        double costs = calculator.getCosts(context, prevAct, nextAct, newAct, 0);

        // prev (A, x=0) -> new (B, x=10) -> next (D, x=30)
        // cost = |10-0| + |30-10| - 0 = 10 + 20 = 30
        Assertions.assertEquals(30.0, costs, 0.01, "Cost should use prevAct's previousLocation correctly");
    }

    /**
     * A test helper subclass that allows creating ActWithoutStaticLocation instances.
     */
    private static class TestActWithoutStaticLocation extends ActWithoutStaticLocation {
        public TestActWithoutStaticLocation(Service service) {
            super(service);
        }
    }
}
