package jsprit.core.problem.constraint;

import jsprit.core.algorithm.state.InternalStates;
import jsprit.core.problem.Skills;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;

/**
 * SkillConstraint that ensures that only vehicles with according skills can serve route and job to be inserted.
 *
 */
public class HardSkillConstraint implements HardRouteStateLevelConstraint{

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
