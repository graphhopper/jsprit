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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class SoftRouteConstraintManager implements SoftRouteConstraint {

    private final List<SoftRouteConstraint> softConstraints = new ArrayList<>();

    /** "Route:" + simple class name per constraint, computed once; the breakdown used to concatenate it per job and route. */
    private final List<String> breakdownNames = new ArrayList<>();

    private final Collection<SoftRouteConstraint> view = Collections.unmodifiableCollection(softConstraints);

    public void addConstraint(SoftRouteConstraint constraint) {
        softConstraints.add(constraint);
        breakdownNames.add("Route:" + constraint.getClass().getSimpleName());
    }

    Collection<SoftRouteConstraint> getConstraints() {
        return view;
    }

    @Override
    public double getCosts(JobInsertionContext insertionContext) {
        double sumCosts = 0.0;
        for (int i = 0, n = softConstraints.size(); i < n; i++) {
            sumCosts += softConstraints.get(i).getCosts(insertionContext);
        }
        return sumCosts;
    }

    /** Every constraint's costs for the route of the context, by name, zero costs left out. */
    InsertionCostBreakdown getCostsBreakdown(JobInsertionContext insertionContext) {
        InsertionCostBreakdown breakdown = new InsertionCostBreakdown();
        for (int i = 0, n = softConstraints.size(); i < n; i++) {
            double cost = softConstraints.get(i).getCosts(insertionContext);
            if (cost != 0) {
                breakdown.add(breakdownNames.get(i), cost);
            }
        }
        return breakdown;
    }

}
