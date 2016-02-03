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
