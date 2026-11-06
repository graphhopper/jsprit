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

import com.graphhopper.jsprit.core.algorithm.recreate.InsertionCostBreakdown;

import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Collections;

class SoftActivityConstraintManager implements SoftActivityConstraint {

    private final List<SoftActivityConstraint> softConstraints = new ArrayList<>();

    /** "Activity:" + simple class name per constraint, computed once; the breakdown used to concatenate it per position. */
    private final List<String> breakdownNames = new ArrayList<>();

    public void addConstraint(SoftActivityConstraint constraint) {
        softConstraints.add(constraint);
        breakdownNames.add("Activity:" + constraint.getClass().getSimpleName());
    }

    int size() {
        return softConstraints.size();
    }

    /**
     * Evaluates every constraint once (in registration order, exactly like {@link #getCosts}) and writes the
     * single values into {@code perConstraint}; returns the same total as the breakdown's getTotal did
     * (zero costs are skipped in the sum, as the breakdown skipped them).
     */
    double getCosts(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime, double[] perConstraint) {
        double total = 0.0;
        for (int i = 0; i < softConstraints.size(); i++) {
            double cost = softConstraints.get(i).getCosts(iFacts, prevAct, newAct, nextAct, prevActDepTime);
            perConstraint[i] = cost;
            if (cost != 0) total += cost;
        }
        return total;
    }

    /** The breakdown of values recorded by {@link #getCosts(JobInsertionContext, TourActivity, TourActivity, TourActivity, double, double[])}. */
    InsertionCostBreakdown breakdownOf(double[] perConstraint) {
        InsertionCostBreakdown breakdown = new InsertionCostBreakdown();
        for (int i = 0; i < softConstraints.size(); i++) {
            if (perConstraint[i] != 0) breakdown.add(breakdownNames.get(i), perConstraint[i]);
        }
        return breakdown;
    }

    Collection<SoftActivityConstraint> getConstraints() {
        return Collections.unmodifiableCollection(softConstraints);
    }

    @Override
    public double getCosts(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
        double sumCosts = 0.0;
        for (SoftActivityConstraint c : softConstraints) {
            sumCosts += c.getCosts(iFacts, prevAct, newAct, nextAct, prevActDepTime);
        }
        return sumCosts;
    }

}
