package com.graphhopper.jsprit.core.pando.constraints;

import com.graphhopper.jsprit.core.problem.constraint.HardRouteConstraint;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;

public class DepotCapacityRouteLevelConstraint implements HardRouteConstraint {
    @Override
    public boolean fulfilled(JobInsertionContext insertionContext) {
        return true;
    }
}
