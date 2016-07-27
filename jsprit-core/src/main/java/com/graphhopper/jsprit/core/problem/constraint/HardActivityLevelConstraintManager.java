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
package com.graphhopper.jsprit.core.problem.constraint;

import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


class HardActivityLevelConstraintManager implements HardActivityConstraint {

    private Collection<HardActivityConstraint> criticalConstraints = new ArrayList<HardActivityConstraint>();

    private Collection<HardActivityConstraint> highPrioConstraints = new ArrayList<HardActivityConstraint>();

    private Collection<HardActivityConstraint> lowPrioConstraints = new ArrayList<HardActivityConstraint>();

    public void addConstraint(HardActivityConstraint constraint, ConstraintManager.Priority priority) {
        if (priority.equals(ConstraintManager.Priority.CRITICAL)) {
            criticalConstraints.add(constraint);
        } else if (priority.equals(ConstraintManager.Priority.HIGH)) {
            highPrioConstraints.add(constraint);
        } else {
            lowPrioConstraints.add(constraint);
        }
    }

    Collection<HardActivityConstraint> getCriticalConstraints() {
        return Collections.unmodifiableCollection(criticalConstraints);
    }

    Collection<HardActivityConstraint> getHighPrioConstraints() {
        return Collections.unmodifiableCollection(highPrioConstraints);
    }

    Collection<HardActivityConstraint> getLowPrioConstraints() {
        return Collections.unmodifiableCollection(lowPrioConstraints);
    }

    Collection<HardActivityConstraint> getAllConstraints() {
        List<HardActivityConstraint> c = new ArrayList<HardActivityConstraint>();
        c.addAll(criticalConstraints);
        c.addAll(highPrioConstraints);
        c.addAll(lowPrioConstraints);
        return Collections.unmodifiableCollection(c);
    }

    @Override
    public ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
        ConstraintsStatus notFulfilled = null;
        for (HardActivityConstraint c : criticalConstraints) {
            ConstraintsStatus status = c.fulfilled(iFacts, prevAct, newAct, nextAct, prevActDepTime);
            if (status.equals(ConstraintsStatus.NOT_FULFILLED_BREAK)) {
                return status;
            } else {
                if (status.equals(ConstraintsStatus.NOT_FULFILLED)) {
                    notFulfilled = status;
                }
            }
        }
        if (notFulfilled != null) return notFulfilled;

        for (HardActivityConstraint c : highPrioConstraints) {
            ConstraintsStatus status = c.fulfilled(iFacts, prevAct, newAct, nextAct, prevActDepTime);
            if (status.equals(ConstraintsStatus.NOT_FULFILLED_BREAK)) {
                return status;
            } else {
                if (status.equals(ConstraintsStatus.NOT_FULFILLED)) {
                    notFulfilled = status;
                }
            }
        }
        if (notFulfilled != null) return notFulfilled;

        for (HardActivityConstraint constraint : lowPrioConstraints) {
            ConstraintsStatus status = constraint.fulfilled(iFacts, prevAct, newAct, nextAct, prevActDepTime);
            if (status.equals(ConstraintsStatus.NOT_FULFILLED_BREAK) || status.equals(ConstraintsStatus.NOT_FULFILLED)) {
                return status;
            }
        }

        return ConstraintsStatus.FULFILLED;
    }

}
