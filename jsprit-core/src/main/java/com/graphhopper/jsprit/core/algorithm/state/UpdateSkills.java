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

package com.graphhopper.jsprit.core.algorithm.state;

import com.graphhopper.jsprit.core.problem.Skills;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ActivityVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

/**
 * Update to update required skills on route
 */
public class UpdateSkills implements StateUpdater, ActivityVisitor {

    private Skills.Builder skillBuilder;

    private StateManager statesManager;

    private VehicleRoute route;

    public UpdateSkills(StateManager statesManager) {
        this.statesManager = statesManager;
    }

    @Override
    public void begin(VehicleRoute route) {
        this.route = route;
        skillBuilder = Skills.Builder.newInstance();
    }

    @Override
    public void visit(TourActivity activity) {
        if (activity instanceof TourActivity.JobActivity) {
            Skills skills = ((TourActivity.JobActivity) activity).getJob().getRequiredSkills();
            skillBuilder.addAllSkills(skills.values());
        }
    }

    @Override
    public void finish() {
        Skills skills = skillBuilder.build();
        statesManager.putTypedInternalRouteState(route, InternalStates.SKILLS, skills);
    }
}
