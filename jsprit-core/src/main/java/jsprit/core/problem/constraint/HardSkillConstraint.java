/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package jsprit.core.problem.constraint;

import jsprit.core.algorithm.state.InternalStates;
import jsprit.core.problem.Skills;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;

/**
 * SkillConstraint that ensures that only vehicles with according skills can serve route and job to be inserted.
 *
 */
public class HardSkillConstraint implements HardRouteConstraint {

    private static final Skills defaultSkills = Skills.Builder.newInstance().build();

    private RouteAndActivityStateGetter states;

    public HardSkillConstraint(RouteAndActivityStateGetter states) {
        this.states = states;
    }

    @Override
    public boolean fulfilled(JobInsertionContext insertionContext) {
        for(String skill : insertionContext.getJob().getRequiredSkills().values()){
            if(!insertionContext.getNewVehicle().getSkills().containsSkill(skill)){
                return false;
            }
        }
        Skills requiredSkillsForRoute = states.getRouteState(insertionContext.getRoute(), InternalStates.SKILLS, Skills.class);
        if(requiredSkillsForRoute == null) requiredSkillsForRoute = defaultSkills;
        for(String skill : requiredSkillsForRoute.values()){
            if(!insertionContext.getNewVehicle().getSkills().containsSkill(skill)){
                return false;
            }
        }
        return true;
    }

}
