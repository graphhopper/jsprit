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

package com.graphhopper.jsprit.core.problem.solution.spec;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.job.Pickup;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SolutionSpecTest {

    @Test
    void testMaterializeServiceRoute() {
        // Create jobs
        Service s1 = Service.Builder.newInstance("s1")
                .setLocation(Location.newInstance(1, 1))
                .build();
        Service s2 = Service.Builder.newInstance("s2")
                .setLocation(Location.newInstance(2, 2))
                .build();

        // Create vehicle
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1")
                .setStartLocation(Location.newInstance(0, 0))
                .build();

        // Create spec
        SolutionSpec spec = SolutionSpec.of(
                RouteSpec.of("v1",
                        ActivitySpec.visit("s1"),
                        ActivitySpec.visit("s2")
                )
        );

        // Build VRP with spec
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addJob(s1)
                .addJob(s2)
                .addVehicle(v1)
                .setInitialRouteSpecs(spec.routes())
                .build();

        // Verify initial routes
        Collection<VehicleRoute> routes = vrp.getInitialVehicleRoutes();
        assertEquals(1, routes.size());

        VehicleRoute route = routes.iterator().next();
        assertEquals("v1", route.getVehicle().getId());
        assertEquals(2, route.getActivities().size());

        List<TourActivity> activities = route.getActivities();
        assertEquals("s1", ((TourActivity.JobActivity) activities.get(0)).getJob().getId());
        assertEquals("s2", ((TourActivity.JobActivity) activities.get(1)).getJob().getId());
    }

    @Test
    void testMaterializeShipmentRoute() {
        // Create shipment
        Shipment shipment = Shipment.Builder.newInstance("ship1")
                .setPickupLocation(Location.newInstance(1, 1))
                .setDeliveryLocation(Location.newInstance(5, 5))
                .build();

        // Create vehicle
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1")
                .setStartLocation(Location.newInstance(0, 0))
                .build();

        // Create spec with pickup and delivery
        SolutionSpec spec = SolutionSpec.of(
                RouteSpec.of("v1",
                        ActivitySpec.pickup("ship1"),
                        ActivitySpec.delivery("ship1")
                )
        );

        // Build VRP with spec
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addJob(shipment)
                .addVehicle(v1)
                .setInitialRouteSpecs(spec.routes())
                .build();

        // Verify initial routes
        Collection<VehicleRoute> routes = vrp.getInitialVehicleRoutes();
        assertEquals(1, routes.size());

        VehicleRoute route = routes.iterator().next();
        assertEquals(2, route.getActivities().size());

        List<TourActivity> activities = route.getActivities();
        assertEquals("ship1", ((TourActivity.JobActivity) activities.get(0)).getJob().getId());
        assertEquals("ship1", ((TourActivity.JobActivity) activities.get(1)).getJob().getId());
    }

    @Test
    void testMaterializeMultipleRoutes() {
        // Create jobs
        Service s1 = Service.Builder.newInstance("s1")
                .setLocation(Location.newInstance(1, 1))
                .build();
        Service s2 = Service.Builder.newInstance("s2")
                .setLocation(Location.newInstance(2, 2))
                .build();

        // Create vehicles
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1")
                .setStartLocation(Location.newInstance(0, 0))
                .build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2")
                .setStartLocation(Location.newInstance(10, 10))
                .build();

        // Create spec with two routes
        SolutionSpec spec = SolutionSpec.of(
                RouteSpec.of("v1", ActivitySpec.visit("s1")),
                RouteSpec.of("v2", ActivitySpec.visit("s2"))
        );

        // Build VRP with spec
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addJob(s1)
                .addJob(s2)
                .addVehicle(v1)
                .addVehicle(v2)
                .setInitialRouteSpecs(spec.routes())
                .build();

        // Verify initial routes
        Collection<VehicleRoute> routes = vrp.getInitialVehicleRoutes();
        assertEquals(2, routes.size());
    }

    @Test
    void testMaterializeWithPickupJob() {
        // Create pickup job (one-stop, adds capacity)
        Pickup pickup = Pickup.Builder.newInstance("p1")
                .setLocation(Location.newInstance(1, 1))
                .build();

        // Create vehicle
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1")
                .setStartLocation(Location.newInstance(0, 0))
                .build();

        // Create spec - use VISIT for one-stop jobs
        SolutionSpec spec = SolutionSpec.of(
                RouteSpec.of("v1", ActivitySpec.visit("p1"))
        );

        // Build VRP with spec
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addJob(pickup)
                .addVehicle(v1)
                .setInitialRouteSpecs(spec.routes())
                .build();

        // Verify initial routes
        Collection<VehicleRoute> routes = vrp.getInitialVehicleRoutes();
        assertEquals(1, routes.size());

        VehicleRoute route = routes.iterator().next();
        assertEquals(1, route.getActivities().size());
        assertEquals("p1", ((TourActivity.JobActivity) route.getActivities().get(0)).getJob().getId());
    }

    @Test
    void testMaterializeWithDeliveryJob() {
        // Create delivery job (one-stop, removes capacity)
        Delivery delivery = Delivery.Builder.newInstance("d1")
                .setLocation(Location.newInstance(1, 1))
                .build();

        // Create vehicle
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1")
                .setStartLocation(Location.newInstance(0, 0))
                .build();

        // Create spec - use VISIT for one-stop jobs
        SolutionSpec spec = SolutionSpec.of(
                RouteSpec.of("v1", ActivitySpec.visit("d1"))
        );

        // Build VRP with spec
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addJob(delivery)
                .addVehicle(v1)
                .setInitialRouteSpecs(spec.routes())
                .build();

        // Verify initial routes
        Collection<VehicleRoute> routes = vrp.getInitialVehicleRoutes();
        assertEquals(1, routes.size());

        VehicleRoute route = routes.iterator().next();
        assertEquals(1, route.getActivities().size());
        assertEquals("d1", ((TourActivity.JobActivity) route.getActivities().get(0)).getJob().getId());
    }

    @Test
    void testAddInitialRouteSpec() {
        // Create job
        Service s1 = Service.Builder.newInstance("s1")
                .setLocation(Location.newInstance(1, 1))
                .build();

        // Create vehicle
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1")
                .setStartLocation(Location.newInstance(0, 0))
                .build();

        // Build VRP with individual route spec
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addJob(s1)
                .addVehicle(v1)
                .addInitialRouteSpec(RouteSpec.of("v1", ActivitySpec.visit("s1")))
                .build();

        // Verify initial routes
        Collection<VehicleRoute> routes = vrp.getInitialVehicleRoutes();
        assertEquals(1, routes.size());
    }

    @Test
    void testMissingVehicleThrowsException() {
        Service s1 = Service.Builder.newInstance("s1")
                .setLocation(Location.newInstance(1, 1))
                .build();

        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1")
                .setStartLocation(Location.newInstance(0, 0))
                .build();

        // Spec references non-existent vehicle
        SolutionSpec spec = SolutionSpec.of(
                RouteSpec.of("v_unknown", ActivitySpec.visit("s1"))
        );

        assertThrows(IllegalArgumentException.class, () -> {
            VehicleRoutingProblem.Builder.newInstance()
                    .addJob(s1)
                    .addVehicle(v1)
                    .setInitialRouteSpecs(spec.routes())
                    .build();
        });
    }

    @Test
    void testMissingJobThrowsException() {
        Service s1 = Service.Builder.newInstance("s1")
                .setLocation(Location.newInstance(1, 1))
                .build();

        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1")
                .setStartLocation(Location.newInstance(0, 0))
                .build();

        // Spec references non-existent job
        SolutionSpec spec = SolutionSpec.of(
                RouteSpec.of("v1", ActivitySpec.visit("s_unknown"))
        );

        assertThrows(IllegalArgumentException.class, () -> {
            VehicleRoutingProblem.Builder.newInstance()
                    .addJob(s1)
                    .addVehicle(v1)
                    .setInitialRouteSpecs(spec.routes())
                    .build();
        });
    }

    @Test
    void testEmptySpec() {
        Service s1 = Service.Builder.newInstance("s1")
                .setLocation(Location.newInstance(1, 1))
                .build();

        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1")
                .setStartLocation(Location.newInstance(0, 0))
                .build();

        // Empty spec
        SolutionSpec spec = SolutionSpec.empty();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addJob(s1)
                .addVehicle(v1)
                .setInitialRouteSpecs(spec.routes())
                .build();

        // No initial routes
        Collection<VehicleRoute> routes = vrp.getInitialVehicleRoutes();
        assertTrue(routes.isEmpty());
    }

    @Test
    void testExtractSpecFromServiceSolution() {
        // Create jobs
        Service s1 = Service.Builder.newInstance("s1")
                .setLocation(Location.newInstance(1, 1))
                .build();
        Service s2 = Service.Builder.newInstance("s2")
                .setLocation(Location.newInstance(2, 2))
                .build();

        // Create vehicle
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1")
                .setStartLocation(Location.newInstance(0, 0))
                .build();

        // Build VRP
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addJob(s1)
                .addJob(s2)
                .addVehicle(v1)
                .build();

        // Create a solution manually
        VehicleRoute route = VehicleRoute.Builder.newInstance(v1)
                .setJobActivityFactory(vrp.getJobActivityFactory())
                .addService(s1)
                .addService(s2)
                .build();

        VehicleRoutingProblemSolution solution = new VehicleRoutingProblemSolution(
                List.of(route), new ArrayList<>(), 0.0);

        // Extract spec
        SolutionSpec spec = SolutionSpec.from(solution);

        // Verify
        assertEquals(1, spec.routes().size());
        RouteSpec routeSpec = spec.routes().get(0);
        assertEquals("v1", routeSpec.vehicleId());
        assertEquals(2, routeSpec.activities().size());
        assertEquals("s1", routeSpec.activities().get(0).jobId());
        assertEquals(ActivityType.VISIT, routeSpec.activities().get(0).type());
        assertEquals("s2", routeSpec.activities().get(1).jobId());
        assertEquals(ActivityType.VISIT, routeSpec.activities().get(1).type());
    }

    @Test
    void testExtractSpecFromShipmentSolution() {
        // Create shipment
        Shipment shipment = Shipment.Builder.newInstance("ship1")
                .setPickupLocation(Location.newInstance(1, 1))
                .setDeliveryLocation(Location.newInstance(5, 5))
                .build();

        // Create vehicle
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1")
                .setStartLocation(Location.newInstance(0, 0))
                .build();

        // Build VRP
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addJob(shipment)
                .addVehicle(v1)
                .build();

        // Create a solution manually
        VehicleRoute route = VehicleRoute.Builder.newInstance(v1)
                .setJobActivityFactory(vrp.getJobActivityFactory())
                .addPickup(shipment)
                .addDelivery(shipment)
                .build();

        VehicleRoutingProblemSolution solution = new VehicleRoutingProblemSolution(
                List.of(route), new ArrayList<>(), 0.0);

        // Extract spec
        SolutionSpec spec = SolutionSpec.from(solution);

        // Verify
        assertEquals(1, spec.routes().size());
        RouteSpec routeSpec = spec.routes().get(0);
        assertEquals("v1", routeSpec.vehicleId());
        assertEquals(2, routeSpec.activities().size());
        assertEquals("ship1", routeSpec.activities().get(0).jobId());
        assertEquals(ActivityType.PICKUP, routeSpec.activities().get(0).type());
        assertEquals("ship1", routeSpec.activities().get(1).jobId());
        assertEquals(ActivityType.DELIVERY, routeSpec.activities().get(1).type());
    }

    @Test
    void testRoundTripSpecToSolutionToSpec() {
        // Create jobs
        Service s1 = Service.Builder.newInstance("s1")
                .setLocation(Location.newInstance(1, 1))
                .build();
        Shipment ship1 = Shipment.Builder.newInstance("ship1")
                .setPickupLocation(Location.newInstance(2, 2))
                .setDeliveryLocation(Location.newInstance(3, 3))
                .build();

        // Create vehicle
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1")
                .setStartLocation(Location.newInstance(0, 0))
                .build();

        // Create original spec
        SolutionSpec originalSpec = SolutionSpec.of(
                RouteSpec.of("v1",
                        ActivitySpec.visit("s1"),
                        ActivitySpec.pickup("ship1"),
                        ActivitySpec.delivery("ship1")
                )
        );

        // Build VRP with spec
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addJob(s1)
                .addJob(ship1)
                .addVehicle(v1)
                .setInitialRouteSpecs(originalSpec.routes())
                .build();

        // Get initial routes as solution
        Collection<VehicleRoute> routes = vrp.getInitialVehicleRoutes();
        VehicleRoutingProblemSolution solution = new VehicleRoutingProblemSolution(
                routes, new ArrayList<>(), 0.0);

        // Extract spec back
        SolutionSpec extractedSpec = SolutionSpec.from(solution);

        // Verify round-trip
        assertEquals(1, extractedSpec.routes().size());
        RouteSpec routeSpec = extractedSpec.routes().get(0);
        assertEquals("v1", routeSpec.vehicleId());
        assertEquals(3, routeSpec.activities().size());

        assertEquals("s1", routeSpec.activities().get(0).jobId());
        assertEquals(ActivityType.VISIT, routeSpec.activities().get(0).type());
        assertEquals("ship1", routeSpec.activities().get(1).jobId());
        assertEquals(ActivityType.PICKUP, routeSpec.activities().get(1).type());
        assertEquals("ship1", routeSpec.activities().get(2).jobId());
        assertEquals(ActivityType.DELIVERY, routeSpec.activities().get(2).type());
    }
}
