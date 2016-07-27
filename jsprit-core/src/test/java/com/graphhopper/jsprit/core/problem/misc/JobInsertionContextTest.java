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
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

public class JobInsertionContextTest {

    VehicleRoute route;

    Job job;

    Vehicle vehicle;

    Driver driver;

    double depTime;

    JobInsertionContext context;

    @Before
    public void doBefore() {
        route = mock(VehicleRoute.class);
        job = mock(Job.class);
        vehicle = mock(Vehicle.class);
        driver = mock(Driver.class);
        depTime = 0.;
        context = new JobInsertionContext(route, job, vehicle, driver, depTime);
    }

    @Test
    public void routeShouldBeAssigned() {
        assertEquals(route, context.getRoute());
    }

    @Test
    public void jobShouldBeAssigned() {
        assertEquals(job, context.getJob());
    }

    @Test
    public void vehicleShouldBeAssigned() {
        assertEquals(vehicle, context.getNewVehicle());
    }

    @Test
    public void driverShouldBeAssigned() {
        assertEquals(driver, context.getNewDriver());
    }

    @Test
    public void depTimeShouldBeAssigned() {
        assertEquals(0., context.getNewDepTime(), 0.001);
    }

    @Test
    public void relatedActivitiesShouldBeAssigned() {
        context.getAssociatedActivities().add(mock(TourActivity.class));
        context.getAssociatedActivities().add(mock(TourActivity.class));
        assertEquals(2, context.getAssociatedActivities().size());
    }

    @Test
    public void relatedActivityContextShouldBeAssigned() {
        context.setRelatedActivityContext(mock(ActivityContext.class));
        assertNotNull(context.getRelatedActivityContext());
    }

}
