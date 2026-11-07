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
import java.util.Collections;
import java.util.List;


class HardRouteLevelConstraintManager implements HardRouteConstraint {

    private final List<HardRouteConstraint> hardConstraints = new ArrayList<>();

    // live read-only view, created once: the calculators walk it per job and route
    private final List<HardRouteConstraint> view = Collections.unmodifiableList(hardConstraints);

    public void addConstraint(HardRouteConstraint constraint) {
        hardConstraints.add(constraint);
    }

    List<HardRouteConstraint> getConstraints() {
        return view;
    }

    @Override
    public boolean fulfilled(JobInsertionContext insertionContext) {
        for (int i = 0, n = hardConstraints.size(); i < n; i++) {
            if (!hardConstraints.get(i).fulfilled(insertionContext)) {
                return false;
            }
        }
        return true;
    }

}
