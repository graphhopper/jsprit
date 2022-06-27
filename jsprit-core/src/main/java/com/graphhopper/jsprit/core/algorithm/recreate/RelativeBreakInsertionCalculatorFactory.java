package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.problem.JobActivityFactory;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;

public class RelativeBreakInsertionCalculatorFactory implements JobInsertionCostsCalculatorFactory {
    public RelativeBreakInsertionCalculatorFactory() {
    }

    public JobInsertionCostsCalculator create(VehicleRoutingProblem vrp, ActivityInsertionCostsCalculator activityInsertionCostsCalculator, JobActivityFactory jobActivityFactory, ConstraintManager constraintManager) {
        return new RelativeBreakInsertionCalculator(vrp.getTransportCosts(), vrp.getActivityCosts(), activityInsertionCostsCalculator, constraintManager, jobActivityFactory);
    }
}
