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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

class SoftRouteConstraintManager implements SoftRouteConstraint {

    private Collection<SoftRouteConstraint> softConstraints = new ArrayList<SoftRouteConstraint>();

    public void addConstraint(SoftRouteConstraint constraint) {
        softConstraints.add(constraint);
    }

    Collection<SoftRouteConstraint> getConstraints() {
        return Collections.unmodifiableCollection(softConstraints);
    }

    @Override
    public double getCosts(JobInsertionContext insertionContext) {
        double sumCosts = 0.0;
        for (SoftRouteConstraint c : softConstraints) {
            sumCosts += c.getCosts(insertionContext);
        }
        return sumCosts;
    }

}
