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
package com.graphhopper.jsprit.core.problem.misc;

import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

@DisplayName("Job Insertion Context Test")
class JobInsertionContextTest {

    VehicleRoute route;

    Job job;

    Vehicle vehicle;

    Driver driver;

    double depTime;

    JobInsertionContext context;

    @BeforeEach
    void doBefore() {
        route = mock(VehicleRoute.class);
        job = mock(Job.class);
        vehicle = mock(Vehicle.class);
        driver = mock(Driver.class);
        depTime = 0.;
        context = new JobInsertionContext(route, job, vehicle, driver, depTime);
    }

    @Test
    @DisplayName("Route Should Be Assigned")
    void routeShouldBeAssigned() {
        Assertions.assertEquals(route, context.getRoute());
    }

    @Test
    @DisplayName("Job Should Be Assigned")
    void jobShouldBeAssigned() {
        Assertions.assertEquals(job, context.getJob());
    }

    @Test
    @DisplayName("Vehicle Should Be Assigned")
    void vehicleShouldBeAssigned() {
        Assertions.assertEquals(vehicle, context.getNewVehicle());
    }

    @Test
    @DisplayName("Driver Should Be Assigned")
    void driverShouldBeAssigned() {
        Assertions.assertEquals(driver, context.getNewDriver());
    }

    @Test
    @DisplayName("Dep Time Should Be Assigned")
    void depTimeShouldBeAssigned() {
        Assertions.assertEquals(0., context.getNewDepTime(), 0.001);
    }

    @Test
    @DisplayName("Related Activities Should Be Assigned")
    void relatedActivitiesShouldBeAssigned() {
        context.getAssociatedActivities().add(mock(TourActivity.class));
        context.getAssociatedActivities().add(mock(TourActivity.class));
        Assertions.assertEquals(2, context.getAssociatedActivities().size());
    }

    @Test
    @DisplayName("Related Activity Context Should Be Assigned")
    void relatedActivityContextShouldBeAssigned() {
        context.setRelatedActivityContext(mock(ActivityContext.class));
        assertNotNull(context.getRelatedActivityContext());
    }
}
