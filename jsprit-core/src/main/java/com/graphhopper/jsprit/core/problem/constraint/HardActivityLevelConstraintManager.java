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
 * The hard activity constraints in their three tiers, see {@link ConstraintManager.Priority} for the protocol. This is
 * the only implementation of that protocol; the insertion calculators call
 * {@link #fulfilled(JobInsertionContext, TourActivity, TourActivity, TourActivity, double, Collection)} to also learn
 * which constraints failed.
 */
class HardActivityLevelConstraintManager implements HardActivityConstraint {

    private final List<HardActivityConstraint> criticalConstraints = new ArrayList<>();

    private final List<HardActivityConstraint> highPrioConstraints = new ArrayList<>();

    private final List<HardActivityConstraint> lowPrioConstraints = new ArrayList<>();

    // live read-only views, created once: a fresh wrapper per call was an allocation per insertion position
    private final List<HardActivityConstraint> criticalView = Collections.unmodifiableList(criticalConstraints);

    private final List<HardActivityConstraint> highPrioView = Collections.unmodifiableList(highPrioConstraints);

    private final List<HardActivityConstraint> lowPrioView = Collections.unmodifiableList(lowPrioConstraints);

    /** the three tiers as arrays for the per-position loop, registration order within a tier; rebuilt on every add */
    private HardActivityConstraint[][] tiers = new HardActivityConstraint[][]{{}, {}, {}};

    private int numActivityConstraints = 0;

    public void addConstraint(HardActivityConstraint constraint, ConstraintManager.Priority priority) {
        if (priority == ConstraintManager.Priority.CRITICAL) {
            criticalConstraints.add(constraint);
        } else if (priority == ConstraintManager.Priority.HIGH) {
            highPrioConstraints.add(constraint);
        } else {
            lowPrioConstraints.add(constraint);
        }
        tiers = new HardActivityConstraint[][]{criticalConstraints.toArray(new HardActivityConstraint[0]),
            highPrioConstraints.toArray(new HardActivityConstraint[0]), lowPrioConstraints.toArray(new HardActivityConstraint[0])};
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
        return fulfilled(iFacts, prevAct, newAct, nextAct, prevActDepTime, null);
    }

    /**
     * The protocol of {@link ConstraintManager.Priority}: tier by tier, CRITICAL first; within a tier the constraints
     * are asked in registration order, after the first NOT_FULFILLED only those that
     * {@link HardActivityConstraint#canPruneRemainingPositions()} are still asked, a NOT_FULFILLED_BREAK ends the check
     * at once, and a tier with a failure is the last one asked. If {@code failedConstraints} is given, every constraint
     * that answered NOT_FULFILLED or NOT_FULFILLED_BREAK is recorded there.
     */
    ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime,
                                Collection<HardConstraint> failedConstraints) {
        HardActivityConstraint[][] tiers = this.tiers;
        for (int t = 0; t < tiers.length; t++) {
            HardActivityConstraint[] tier = tiers[t];
            ConstraintsStatus verdict = ConstraintsStatus.FULFILLED;
            for (int i = 0; i < tier.length; i++) {
                HardActivityConstraint c = tier[i];
                if (verdict != ConstraintsStatus.FULFILLED && !c.canPruneRemainingPositions()) continue;
                ConstraintsStatus status = c.fulfilled(iFacts, prevAct, newAct, nextAct, prevActDepTime);
                if (status == ConstraintsStatus.FULFILLED) continue;
                if (failedConstraints != null) failedConstraints.add(c);
                if (status == ConstraintsStatus.NOT_FULFILLED_BREAK) return status;
                verdict = status;
            }
            if (verdict != ConstraintsStatus.FULFILLED) return verdict;
        }
        return ConstraintsStatus.FULFILLED;
    }

}
