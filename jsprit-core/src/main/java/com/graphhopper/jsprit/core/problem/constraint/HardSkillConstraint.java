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

import com.graphhopper.jsprit.core.algorithm.state.InternalStates;
import com.graphhopper.jsprit.core.problem.Skills;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;

/**
 * SkillConstraint that ensures that only vehicles with according skills can serve route and job to be inserted.
 */
public class HardSkillConstraint implements HardRouteConstraint {

    private static final Skills defaultSkills = Skills.Builder.newInstance().build();

    private RouteAndActivityStateGetter states;

    public HardSkillConstraint(RouteAndActivityStateGetter states) {
        this.states = states;
    }

    @Override
    public boolean fulfilled(JobInsertionContext insertionContext) {
        for (String skill : insertionContext.getJob().getRequiredSkills().values()) {
            if (!insertionContext.getNewVehicle().getSkills().containsSkill(skill)) {
                return false;
            }
        }
        Skills requiredSkillsForRoute = states.getRouteState(insertionContext.getRoute(), InternalStates.SKILLS, Skills.class);
        if (requiredSkillsForRoute == null) requiredSkillsForRoute = defaultSkills;
        for (String skill : requiredSkillsForRoute.values()) {
            if (!insertionContext.getNewVehicle().getSkills().containsSkill(skill)) {
                return false;
            }
        }
        return true;
    }

}
