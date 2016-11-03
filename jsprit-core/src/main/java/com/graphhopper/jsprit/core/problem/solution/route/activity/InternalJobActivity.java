package com.graphhopper.jsprit.core.problem.solution.route.activity;

import com.graphhopper.jsprit.core.problem.AbstractJob;
import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.Location;

/**
 * Common ancesstor for job-based, internal activities
 *
 * @author balage
 *
 */
public abstract class InternalJobActivity extends JobActivity implements InternalActivityMarker {

    public InternalJobActivity(AbstractJob job, String name, Location location, double operationTime, Capacity capacity) {
        super(job, name, location, operationTime, capacity);
    }

    public InternalJobActivity(JobActivity sourceActivity) {
        super(sourceActivity);
    }

}