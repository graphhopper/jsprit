package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.problem.JobActivityFactory;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;

public class BreakForMultipleTimeWindowsInsertionCalculatorFactory implements JobInsertionCostsCalculatorFactory {
    @Override
    public JobInsertionCostsCalculator create(VehicleRoutingProblem vrp, ActivityInsertionCostsCalculator activityInsertionCostsCalculator, JobActivityFactory jobActivityFactory, ConstraintManager constraintManager) {
        return new BreakForMultipleTimeWindowsInsertionCalculator(vrp.getTransportCosts(), vrp.getActivityCosts(), activityInsertionCostsCalculator, constraintManager, jobActivityFactory);
    }
}
