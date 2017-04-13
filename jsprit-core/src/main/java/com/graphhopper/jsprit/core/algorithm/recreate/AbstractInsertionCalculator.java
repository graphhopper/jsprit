/*
 * Licensed to GraphHopper GmbH under one or more contributor
 * license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * GraphHopper GmbH licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.constraint.HardActivityConstraint;
import com.graphhopper.jsprit.core.problem.constraint.HardActivityConstraint.ConstraintsStatus;
import com.graphhopper.jsprit.core.problem.constraint.HardRouteConstraint;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by schroeder on 06/02/17.
 */
abstract class AbstractInsertionCalculator implements JobInsertionCostsCalculator {

    InsertionData checkRouteContraints(JobInsertionContext insertionContext, ConstraintManager constraintManager) {
        for (HardRouteConstraint hardRouteConstraint : constraintManager.getHardRouteConstraints()) {
            if (!hardRouteConstraint.fulfilled(insertionContext)) {
                InsertionData emptyInsertionData = new InsertionData.NoInsertionFound();
                emptyInsertionData.addFailedConstrainName(hardRouteConstraint.getClass().getSimpleName());
                return emptyInsertionData;
            }
        }
        return null;
    }

    ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime, Collection<String> failedActivityConstraints, ConstraintManager constraintManager) {
        ConstraintsStatus notFulfilled = null;
        List<String> failed = new ArrayList<>();
        for (HardActivityConstraint c : constraintManager.getCriticalHardActivityConstraints()) {
            ConstraintsStatus status = c.fulfilled(iFacts, prevAct, newAct, nextAct, prevActDepTime);
            if (status.equals(ConstraintsStatus.NOT_FULFILLED_BREAK)) {
                failedActivityConstraints.add(c.getClass().getSimpleName());
                return status;
            } else {
                if (status.equals(ConstraintsStatus.NOT_FULFILLED)) {
                    failed.add(c.getClass().getSimpleName());
                    notFulfilled = status;
                }
            }
        }
        if (notFulfilled != null) {
            failedActivityConstraints.addAll(failed);
            return notFulfilled;
        }

        for (HardActivityConstraint c : constraintManager.getHighPrioHardActivityConstraints()) {
            ConstraintsStatus status = c.fulfilled(iFacts, prevAct, newAct, nextAct, prevActDepTime);
            if (status.equals(ConstraintsStatus.NOT_FULFILLED_BREAK)) {
                failedActivityConstraints.add(c.getClass().getSimpleName());
                return status;
            } else {
                if (status.equals(ConstraintsStatus.NOT_FULFILLED)) {
                    failed.add(c.getClass().getSimpleName());
                    notFulfilled = status;
                }
            }
        }
        if (notFulfilled != null) {
            failedActivityConstraints.addAll(failed);
            return notFulfilled;
        }

        for (HardActivityConstraint constraint : constraintManager.getLowPrioHardActivityConstraints()) {
            ConstraintsStatus status = constraint.fulfilled(iFacts, prevAct, newAct, nextAct, prevActDepTime);
            if (status.equals(ConstraintsStatus.NOT_FULFILLED_BREAK) || status.equals(ConstraintsStatus.NOT_FULFILLED)) {
                failedActivityConstraints.add(constraint.getClass().getSimpleName());
                return status;
            }
        }
        return ConstraintsStatus.FULFILLED;
    }

}
