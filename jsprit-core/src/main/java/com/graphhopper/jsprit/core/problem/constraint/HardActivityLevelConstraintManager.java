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


/**
 * The hard activity constraints in their three tiers, see {@link ConstraintManager.Priority} for the evaluation protocol.
 * This is the only implementation of that protocol; the insertion calculators call
 * {@link #fulfilled(JobInsertionContext, TourActivity, TourActivity, TourActivity, double, Collection, List)} to also
 * learn which constraints failed.
 */
class HardActivityLevelConstraintManager implements HardActivityConstraint {

    private final List<HardActivityConstraint> criticalConstraints = new ArrayList<>();

    private final List<HardActivityConstraint> highPrioConstraints = new ArrayList<>();

    private final List<HardActivityConstraint> lowPrioConstraints = new ArrayList<>();

    // live read-only views, created once: a fresh wrapper per call was an allocation per insertion position
    private final List<HardActivityConstraint> criticalView = Collections.unmodifiableList(criticalConstraints);

    private final List<HardActivityConstraint> highPrioView = Collections.unmodifiableList(highPrioConstraints);

    private final List<HardActivityConstraint> lowPrioView = Collections.unmodifiableList(lowPrioConstraints);

    private int numActivityConstraints = 0;

    public void addConstraint(HardActivityConstraint constraint, ConstraintManager.Priority priority) {
        if (priority == ConstraintManager.Priority.CRITICAL) {
            criticalConstraints.add(constraint);
        } else if (priority == ConstraintManager.Priority.HIGH) {
            highPrioConstraints.add(constraint);
        } else {
            lowPrioConstraints.add(constraint);
        }
        numActivityConstraints++;
    }

    boolean hasHardActivityConstraints() {
        return numActivityConstraints != 0;
    }

    List<HardActivityConstraint> getCriticalConstraints() {
        return criticalView;
    }

    List<HardActivityConstraint> getHighPrioConstraints() {
        return highPrioView;
    }

    List<HardActivityConstraint> getLowPrioConstraints() {
        return lowPrioView;
    }

    Collection<HardActivityConstraint> getAllConstraints() {
        List<HardActivityConstraint> c = new ArrayList<>();
        c.addAll(criticalConstraints);
        c.addAll(highPrioConstraints);
        c.addAll(lowPrioConstraints);
        return Collections.unmodifiableCollection(c);
    }

    @Override
    public ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
        return fulfilled(iFacts, prevAct, newAct, nextAct, prevActDepTime, null, null);
    }

    /**
     * Evaluates the tiers as described at {@link ConstraintManager.Priority} and, if {@code failedConstraints} is given,
     * records the constraints that made the position infeasible: all constraints of the deciding tier that returned
     * NOT_FULFILLED, or only the one that returned NOT_FULFILLED_BREAK.
     *
     * @param failedConstraints sink for the failed constraints, null when the caller does not care
     * @param failedInTier      scratch list reused across positions (the per-tier failures before the tier's verdict is
     *                          known); may be null, then one is allocated when needed
     */
    ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime,
                                Collection<HardConstraint> failedConstraints, List<HardConstraint> failedInTier) {
        if (numActivityConstraints == 0) return ConstraintsStatus.FULFILLED;
        if (failedConstraints != null) {
            if (failedInTier == null) failedInTier = new ArrayList<>();
            else if (!failedInTier.isEmpty()) failedInTier.clear();
        }
        ConstraintsStatus status = fulfilledInTier(criticalConstraints, iFacts, prevAct, newAct, nextAct, prevActDepTime, failedConstraints, failedInTier);
        if (status != ConstraintsStatus.FULFILLED) return status;
        status = fulfilledInTier(highPrioConstraints, iFacts, prevAct, newAct, nextAct, prevActDepTime, failedConstraints, failedInTier);
        if (status != ConstraintsStatus.FULFILLED) return status;
        // the low tier stops at the first failure
        for (int i = 0, n = lowPrioConstraints.size(); i < n; i++) {
            HardActivityConstraint c = lowPrioConstraints.get(i);
            status = c.fulfilled(iFacts, prevAct, newAct, nextAct, prevActDepTime);
            if (status != ConstraintsStatus.FULFILLED) {
                if (failedConstraints != null) failedConstraints.add(c);
                return status;
            }
        }
        return ConstraintsStatus.FULFILLED;
    }

    /**
     * One of the two upper tiers: every constraint is asked, NOT_FULFILLED_BREAK ends the evaluation at once (the
     * NOT_FULFILLED collected before it in this tier are dropped), otherwise the tier fails with NOT_FULFILLED if any
     * constraint did. Indexed loops: no iterator per position.
     */
    private static ConstraintsStatus fulfilledInTier(List<HardActivityConstraint> tier, JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime,
                                                     Collection<HardConstraint> failedConstraints, List<HardConstraint> failedInTier) {
        ConstraintsStatus notFulfilled = null;
        for (int i = 0, n = tier.size(); i < n; i++) {
            HardActivityConstraint c = tier.get(i);
            ConstraintsStatus status = c.fulfilled(iFacts, prevAct, newAct, nextAct, prevActDepTime);
            if (status == ConstraintsStatus.NOT_FULFILLED_BREAK) {
                if (failedConstraints != null) failedConstraints.add(c);
                return status;
            }
            if (status == ConstraintsStatus.NOT_FULFILLED) {
                if (failedConstraints != null) failedInTier.add(c);
                notFulfilled = status;
            }
        }
        if (notFulfilled != null) {
            if (failedConstraints != null) failedConstraints.addAll(failedInTier);
            return notFulfilled;
        }
        return ConstraintsStatus.FULFILLED;
    }

}
