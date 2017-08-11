package com.graphhopper.jsprit.core.util;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.misc.ActivityContext;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ServiceActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivities;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

/**
 * Created by tonirajkovski on 7/24/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class FailedConstraintInfoTest {

    @Mock
    private Job job;
    @Mock
    private Vehicle vehicle;
    @Mock
    private ActivityContext activityContext;
    @Mock
    private JobInsertionContext jobInsertionContext;

    @Test
    public void shouldLoadJobInsertionContextWhenNull() {
        // given
        FailedConstraintInfo.Builder builder = FailedConstraintInfo.Builder.newInstance().setFailedConstraint("c1");

        //when
        FailedConstraintInfo failedConstraintInfo = builder.loadInsertionContextData(null).build();

        //then
        Assert.assertNull(failedConstraintInfo.getVehicle());
        Assert.assertNull(failedConstraintInfo.getJob());
        Assert.assertEquals(0, failedConstraintInfo.getInsertionIndex());
        // just assert that there will be no exception because of null values and will produce some value
        Assert.assertNotNull(failedConstraintInfo.toString());
    }

    @Test
    public void shouldLoadJobInsertionContextWhenNotNull() {
        // given
        FailedConstraintInfo.Builder builder = FailedConstraintInfo.Builder.newInstance().setFailedConstraint("c1");

        List<TourActivity> activities = new ArrayList<>();
        activities.add(ServiceActivity.newInstance(Service.Builder.newInstance("testService").setLocation(Location.newInstance(1, 1)).build()));

        when(job.getId()).thenReturn("job");
        when(vehicle.getId()).thenReturn("vehicle");
        when(activityContext.getInsertionIndex()).thenReturn(2);

        when(jobInsertionContext.getJob()).thenReturn(job);
        when(jobInsertionContext.getActivityContext()).thenReturn(activityContext);
        when(jobInsertionContext.getNewVehicle()).thenReturn(vehicle);

        //when
        FailedConstraintInfo failedConstraintInfo = builder.loadInsertionContextData(jobInsertionContext).build();

        //then
        Assert.assertEquals("job", failedConstraintInfo.getJob());
        Assert.assertEquals(2, failedConstraintInfo.getInsertionIndex());
        Assert.assertEquals("vehicle", failedConstraintInfo.getVehicle());
    }

}
