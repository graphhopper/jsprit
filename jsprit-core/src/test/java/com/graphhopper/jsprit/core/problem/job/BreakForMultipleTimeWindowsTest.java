package com.graphhopper.jsprit.core.problem.job;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class BreakForMultipleTimeWindowsTest {

    @Test
    public void breakCreatedWithDriverIdAndSkills() {
        final String serviceId = UUID.randomUUID().toString();
        final String skill = UUID.randomUUID().toString();
        final BreakForMultipleTimeWindows breakForMultipleTimeWindows =
            BreakForMultipleTimeWindows.Builder.newInstance(serviceId)
            .setLocation(Location.newInstance(-0.25, 0.25))
            .addTimeWindow(0, 60)
            .setServiceTime(20)
            .addRequiredSkill(skill)
            .setPriority(1)
            .build();

        assertTrue(breakForMultipleTimeWindows.getRequiredSkills().containsSkill(skill));
        assertEquals(Location.newInstance(-0.25, 0.25), breakForMultipleTimeWindows.getLocation());
        assertEquals(new TimeWindow(0, 60), breakForMultipleTimeWindows.getTimeWindow());
        assertEquals(20, breakForMultipleTimeWindows.getServiceDuration(), .0001);
    }
}
