package jsprit.core.problem.constraint;

import jsprit.core.problem.Skills;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;
import jsprit.core.problem.solution.route.state.StateFactory;

/**
 * SkillConstraint that ensures that only vehicles with according skills can serve route and job to be inserted.
 *
 */
public class HardSkillConstraint implements HardRouteStateLevelConstraint{

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
        Skills requiredSkillsForRoute = states.getRouteState(insertionContext.getRoute(), StateFactory.SKILLS, Skills.class);
        for(String skill : requiredSkillsForRoute.values()){
            if(!insertionContext.getNewVehicle().getSkills().containsSkill(skill)){
                return false;
            }
        }
        return true;
    }

}
