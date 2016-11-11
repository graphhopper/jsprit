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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

class SoftActivityConstraintManager implements SoftActivityConstraint {

    private Collection<SoftActivityConstraint> softConstraints = new ArrayList<SoftActivityConstraint>();

    public void addConstraint(SoftActivityConstraint constraint) {
        softConstraints.add(constraint);
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
