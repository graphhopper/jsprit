package com.graphhopper.jsprit.core.algorithm.objectivefunction;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.BreakActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

/**
 * A cost calculator component for penaltizing activities in break time.
 * <p>
 * This cost calculator returns a value proportional value to the length of the
 * break.
 * </p>
 * <p>
 * This is a route level component.
 * </p>
 *
 * @author balage
 *
 */
public class MissedBreak extends RouteLevelSolutionCostComponent {

    public static final String COMPONENT_ID = "MissedBreak";

    public MissedBreak() {
        super(COMPONENT_ID);
    }

    @Override
    protected double calculateRouteLevelCost(VehicleRoutingProblem problem, VehicleRoute route) {
        for (TourActivity act : route.getActivities()) {
            if (act instanceof BreakActivity)
                return 0d;
        }
        if (route.getVehicle().getBreak() != null) {
            if (route.getEnd().getArrTime() > route.getVehicle().getBreak().getActivity().getBreakTimeWindow()
                    .getEnd())
                return 4 * (getMaxCosts() * 2 + route.getVehicle().getBreak().getActivity().getOperationTime()
                        * route.getVehicle().getType().getVehicleCostParams().perServiceTimeUnit);
        }
        return 0d;
    }

}
