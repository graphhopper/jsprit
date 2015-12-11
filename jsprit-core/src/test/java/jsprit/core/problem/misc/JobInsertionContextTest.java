/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package jsprit.core.problem.misc;


import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.vehicle.Vehicle;
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
