package com.graphhopper.jsprit.core.problem.solution.route.activity;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.SizeDimension;

/**
 * Common ancestor for non-job-based, internal activities
 *
 * @author balage
 */
public abstract class InternalActivity extends AbstractActivity implements InternalActivityMarker {

    public InternalActivity(String name, Location location, SizeDimension capacity) {
        super(name, location, capacity);
    }

    public InternalActivity(InternalActivity sourceActivity) {
        super(sourceActivity);
    }

}
