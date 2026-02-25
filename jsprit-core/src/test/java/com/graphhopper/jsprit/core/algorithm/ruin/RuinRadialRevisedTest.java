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

import com.graphhopper.jsprit.core.algorithm.state.UpdateActivityPrevLocations;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.util.Coordinate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Ruin Radial Revised Test")
class RuinRadialRevisedTest {

    @Test
    @DisplayName("When routes are empty, should return empty collection")
    void whenRoutesAreEmpty_shouldReturnEmptyCollection() {
        VehicleImpl v = VehicleImpl.Builder.newInstance("v")
                .setStartLocation(Location.Builder.newInstance()
                        .setCoordinate(Coordinate.newInstance(0, 0)).build())
                .build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addVehicle(v)
                .build();

        RuinRadialDynamic ruinRadialDynamic = new RuinRadialDynamic(vrp, 1);

        Collection<Job> ruined = ruinRadialDynamic.ruinRoutes(Collections.emptyList());

        assertTrue(ruined.isEmpty());
    }

    @Test
    @DisplayName("When jobs have locations, should remove jobs in spatial proximity")
    void whenJobsHaveLocations_shouldRemoveJobsInSpatialProximity() {
        Service s1 = Service.Builder.newInstance("s1")
                .setLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(0, 0)).build())
                .build();
        Service s2 = Service.Builder.newInstance("s2")
                .setLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(1, 0)).build())
                .build();
        Service s3 = Service.Builder.newInstance("s3")
                .setLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(100, 100)).build())
                .build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v")
                .setStartLocation(Location.Builder.newInstance()
                        .setCoordinate(Coordinate.newInstance(0, 0)).build())
                .build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addJob(s1).addJob(s2).addJob(s3)
                .addVehicle(v)
                .build();

        RuinRadialDynamic ruinRadialDynamic = new RuinRadialDynamic(vrp, 2);
        ruinRadialDynamic.setRandom(new Random(42));

        VehicleRoute route = VehicleRoute.Builder.newInstance(v)
                .addService(s1).addService(s2).addService(s3)
                .setJobActivityFactory(vrp.getJobActivityFactory())
                .build();

        Collection<Job> ruined = ruinRadialDynamic.ruinRoutes(Collections.singletonList(route));

        assertEquals(2, ruined.size());
    }

    @Test
    @DisplayName("When jobs have no static location, should still be able to remove them")
    void whenJobsHaveNoStaticLocation_shouldStillBeAbleToRemoveThem() {
        Service s1 = Service.Builder.newInstance("s1").build();
        Service s2 = Service.Builder.newInstance("s2").build();
        Service s3 = Service.Builder.newInstance("s3").build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v")
                .setStartLocation(Location.Builder.newInstance()
                        .setCoordinate(Coordinate.newInstance(0, 0)).build())
                .build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addJob(s1).addJob(s2).addJob(s3)
                .addVehicle(v)
                .build();

        RuinRadialDynamic ruinRadialDynamic = new RuinRadialDynamic(vrp, 2);
        ruinRadialDynamic.setRandom(new Random(42));

        VehicleRoute route = VehicleRoute.Builder.newInstance(v)
                .addService(s1).addService(s2).addService(s3)
                .setJobActivityFactory(vrp.getJobActivityFactory())
                .build();

        // Run state updater to set activity locations (simulates what the algorithm does)
        new UpdateActivityPrevLocations().visit(route);

        Collection<Job> ruined = ruinRadialDynamic.ruinRoutes(Collections.singletonList(route));

        assertEquals(2, ruined.size());
    }

    @Test
    @DisplayName("When mix of jobs with and without locations, should handle all")
    void whenMixOfJobsWithAndWithoutLocations_shouldHandleAll() {
        Service s1 = Service.Builder.newInstance("s1")
                .setLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(0, 0)).build())
                .build();
        Service s2 = Service.Builder.newInstance("s2").build(); // No location
        Service s3 = Service.Builder.newInstance("s3")
                .setLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(10, 10)).build())
                .build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v")
                .setStartLocation(Location.Builder.newInstance()
                        .setCoordinate(Coordinate.newInstance(0, 0)).build())
                .build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addJob(s1).addJob(s2).addJob(s3)
                .addVehicle(v)
                .build();

        RuinRadialDynamic ruinRadialDynamic = new RuinRadialDynamic(vrp, 3);
        ruinRadialDynamic.setRandom(new Random(42));

        VehicleRoute route = VehicleRoute.Builder.newInstance(v)
                .addService(s1).addService(s2).addService(s3)
                .setJobActivityFactory(vrp.getJobActivityFactory())
                .build();

        // Run state updater to set activity locations (simulates what the algorithm does)
        new UpdateActivityPrevLocations().visit(route);

        Collection<Job> ruined = ruinRadialDynamic.ruinRoutes(Collections.singletonList(route));

        assertEquals(3, ruined.size());
    }

    @Test
    @DisplayName("Should respect ruinShareFactory settings")
    void shouldRespectRuinShareFactorySettings() {
        Service s1 = Service.Builder.newInstance("s1")
                .setLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(0, 0)).build())
                .build();
        Service s2 = Service.Builder.newInstance("s2")
                .setLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(1, 0)).build())
                .build();
        Service s3 = Service.Builder.newInstance("s3")
                .setLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(2, 0)).build())
                .build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v")
                .setStartLocation(Location.Builder.newInstance()
                        .setCoordinate(Coordinate.newInstance(0, 0)).build())
                .build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addJob(s1).addJob(s2).addJob(s3)
                .addVehicle(v)
                .build();

        RuinRadialDynamic ruinRadialDynamic = new RuinRadialDynamic(vrp, 10);
        ruinRadialDynamic.setRuinShareFactory(() -> 1);
        ruinRadialDynamic.setRandom(new Random(42));

        VehicleRoute route = VehicleRoute.Builder.newInstance(v)
                .addService(s1).addService(s2).addService(s3)
                .setJobActivityFactory(vrp.getJobActivityFactory())
                .build();

        Collection<Job> ruined = ruinRadialDynamic.ruinRoutes(Collections.singletonList(route));

        assertEquals(1, ruined.size());
    }

    @Test
    @DisplayName("Should work with multiple routes")
    void shouldWorkWithMultipleRoutes() {
        Service s1 = Service.Builder.newInstance("s1")
                .setLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(0, 0)).build())
                .build();
        Service s2 = Service.Builder.newInstance("s2")
                .setLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(1, 0)).build())
                .build();
        Service s3 = Service.Builder.newInstance("s3")
                .setLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(100, 100)).build())
                .build();
        Service s4 = Service.Builder.newInstance("s4")
                .setLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(101, 100)).build())
                .build();

        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1")
                .setStartLocation(Location.Builder.newInstance()
                        .setCoordinate(Coordinate.newInstance(0, 0)).build())
                .build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2")
                .setStartLocation(Location.Builder.newInstance()
                        .setCoordinate(Coordinate.newInstance(100, 100)).build())
                .build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addJob(s1).addJob(s2).addJob(s3).addJob(s4)
                .addVehicle(v1).addVehicle(v2)
                .build();

        RuinRadialDynamic ruinRadialDynamic = new RuinRadialDynamic(vrp, 2);
        ruinRadialDynamic.setRandom(new Random(42));

        VehicleRoute route1 = VehicleRoute.Builder.newInstance(v1)
                .addService(s1).addService(s2)
                .setJobActivityFactory(vrp.getJobActivityFactory())
                .build();
        VehicleRoute route2 = VehicleRoute.Builder.newInstance(v2)
                .addService(s3).addService(s4)
                .setJobActivityFactory(vrp.getJobActivityFactory())
                .build();

        Collection<Job> ruined = ruinRadialDynamic.ruinRoutes(Arrays.asList(route1, route2));

        assertEquals(2, ruined.size());
    }

    @Test
    @DisplayName("Fraction constructor should calculate correct number of jobs")
    void fractionConstructor_shouldCalculateCorrectNumberOfJobs() {
        Service s1 = Service.Builder.newInstance("s1")
                .setLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(0, 0)).build())
                .build();
        Service s2 = Service.Builder.newInstance("s2")
                .setLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(1, 0)).build())
                .build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v")
                .setStartLocation(Location.Builder.newInstance()
                        .setCoordinate(Coordinate.newInstance(0, 0)).build())
                .build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addJob(s1).addJob(s2)
                .addVehicle(v)
                .build();

        RuinRadialDynamic ruinRadialDynamic = new RuinRadialDynamic(vrp, 0.5);
        ruinRadialDynamic.setRandom(new Random(42));

        VehicleRoute route = VehicleRoute.Builder.newInstance(v)
                .addService(s1).addService(s2)
                .setJobActivityFactory(vrp.getJobActivityFactory())
                .build();

        Collection<Job> ruined = ruinRadialDynamic.ruinRoutes(Collections.singletonList(route));

        assertEquals(1, ruined.size());
    }

    @Test
    @DisplayName("toString should return descriptive string")
    void toStringShouldReturnDescriptiveString() {
        VehicleImpl v = VehicleImpl.Builder.newInstance("v")
                .setStartLocation(Location.Builder.newInstance()
                        .setCoordinate(Coordinate.newInstance(0, 0)).build())
                .build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addVehicle(v)
                .build();

        RuinRadialDynamic ruinRadialDynamic = new RuinRadialDynamic(vrp, 5);

        String result = ruinRadialDynamic.toString();

        assertTrue(result.contains("radialRuinRevised"));
        assertTrue(result.contains("5"));
    }
}
