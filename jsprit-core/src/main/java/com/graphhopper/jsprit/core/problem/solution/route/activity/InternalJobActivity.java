package com.graphhopper.jsprit.core.problem.solution.route.activity;

import com.graphhopper.jsprit.core.problem.SizeDimension;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.AbstractJob;

import java.util.Collection;

/**
 * Common ancesstor for job-based, internal activities
 *
 * @author balage
 */
public abstract class InternalJobActivity extends JobActivity implements InternalActivityMarker {

    public InternalJobActivity(AbstractJob job, String name, Location location,
                               double operationTime, SizeDimension capacity, Collection<TimeWindow> timeWindows) {
        super(job, name, location, operationTime, capacity, timeWindows);
    }

    public InternalJobActivity(JobActivity sourceActivity) {
        super(sourceActivity);
    }

}
