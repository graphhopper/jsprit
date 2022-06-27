//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.algorithm.recreate.InsertionData.NoInsertionFound;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.Location.Builder;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.constraint.HardActivityConstraint;
import com.graphhopper.jsprit.core.problem.constraint.HardActivityConstraint.ConstraintsStatus;
import com.graphhopper.jsprit.core.problem.constraint.HardRouteConstraint;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.activity.BreakForMultipleTimeWindowsActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.RelativeBreakActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

abstract class AbstractInsertionCalculator implements JobInsertionCostsCalculator {
    AbstractInsertionCalculator() {
    }

    InsertionData checkRouteContraints(JobInsertionContext insertionContext, ConstraintManager constraintManager) {
        Iterator var3 = constraintManager.getHardRouteConstraints().iterator();

        HardRouteConstraint hardRouteConstraint;
        do {
            if (!var3.hasNext()) {
                return null;
            }

            hardRouteConstraint = (HardRouteConstraint)var3.next();
        } while(hardRouteConstraint.fulfilled(insertionContext));

        InsertionData emptyInsertionData = new NoInsertionFound();
        emptyInsertionData.addFailedConstrainName(hardRouteConstraint.getClass().getSimpleName());
        return emptyInsertionData;
    }

    ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime, Collection<String> failedActivityConstraints, ConstraintManager constraintManager) {
        ConstraintsStatus notFulfilled = null;
        List<String> failed = new ArrayList();
        Iterator var11 = constraintManager.getCriticalHardActivityConstraints().iterator();

        HardActivityConstraint constraint;
        ConstraintsStatus status;
        while(var11.hasNext()) {
            constraint = (HardActivityConstraint)var11.next();
            status = constraint.fulfilled(iFacts, prevAct, newAct, nextAct, prevActDepTime);
            if (status.equals(ConstraintsStatus.NOT_FULFILLED_BREAK)) {
                failedActivityConstraints.add(constraint.getClass().getSimpleName());
                return status;
            }

            if (status.equals(ConstraintsStatus.NOT_FULFILLED)) {
                failed.add(constraint.getClass().getSimpleName());
                notFulfilled = status;
            }
        }

        if (notFulfilled != null) {
            failedActivityConstraints.addAll(failed);
            return notFulfilled;
        } else {
            var11 = constraintManager.getHighPrioHardActivityConstraints().iterator();

            while(var11.hasNext()) {
                constraint = (HardActivityConstraint)var11.next();
                status = constraint.fulfilled(iFacts, prevAct, newAct, nextAct, prevActDepTime);


                //debug - should be removed after
                if(prevAct instanceof RelativeBreakActivity && !status.equals(ConstraintsStatus.FULFILLED)) {
                    constraint.fulfilled(iFacts, prevAct, newAct, nextAct, prevActDepTime);
                    constraint.fulfilled(iFacts, prevAct, newAct, nextAct, prevActDepTime);
                    constraint.fulfilled(iFacts, prevAct, newAct, nextAct, prevActDepTime);
                    constraint.fulfilled(iFacts, prevAct, newAct, nextAct, prevActDepTime);
                    constraint.fulfilled(iFacts, prevAct, newAct, nextAct, prevActDepTime);
                }

                if (status.equals(ConstraintsStatus.NOT_FULFILLED_BREAK)) {
                    failedActivityConstraints.add(constraint.getClass().getSimpleName());
                    return status;
                }

                if (status.equals(ConstraintsStatus.NOT_FULFILLED)) {
                    failed.add(constraint.getClass().getSimpleName());
                    notFulfilled = status;
                }
            }

            if (notFulfilled != null) {
                failedActivityConstraints.addAll(failed);
                return notFulfilled;
            } else {
                var11 = constraintManager.getLowPrioHardActivityConstraints().iterator();

                do {
                    if (!var11.hasNext()) {
                        return ConstraintsStatus.FULFILLED;
                    }

                    constraint = (HardActivityConstraint)var11.next();
                    status = constraint.fulfilled(iFacts, prevAct, newAct, nextAct, prevActDepTime);
                } while(!status.equals(ConstraintsStatus.NOT_FULFILLED_BREAK) && !status.equals(ConstraintsStatus.NOT_FULFILLED));

                failedActivityConstraints.add(constraint.getClass().getSimpleName());
                return status;
            }
        }
    }

    protected TourActivity getBreakCopyWithUpdatedLocation(Location location, TourActivity activity) {
        BreakForMultipleTimeWindowsActivity breakForMultipleTimeWindowsActivity = (BreakForMultipleTimeWindowsActivity)activity.duplicate();
        breakForMultipleTimeWindowsActivity.setLocation(Builder.newInstance().setId(breakForMultipleTimeWindowsActivity.getJob().getLocation().getId()).setCoordinate(location.getCoordinate()).build());
        return breakForMultipleTimeWindowsActivity;
    }

    protected TourActivity getRelativeBreakCopyWithUpdatedLocation(Location location, TourActivity activity) {
        RelativeBreakActivity relativeBreakActivity = (RelativeBreakActivity)activity.duplicate();
        relativeBreakActivity.setLocation(Builder.newInstance().setId(relativeBreakActivity.getJob().getLocation().getId()).setCoordinate(location.getCoordinate()).build());
        return relativeBreakActivity;
    }
}
