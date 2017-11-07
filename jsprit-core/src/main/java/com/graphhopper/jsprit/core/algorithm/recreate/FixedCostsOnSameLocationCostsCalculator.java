package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;

public class FixedCostsOnSameLocationCostsCalculator extends LocalActivityInsertionCostsCalculator  {
    private final double activityDurationOnSameLocation;

    public FixedCostsOnSameLocationCostsCalculator(VehicleRoutingTransportCosts routingCosts,
                                                   VehicleRoutingActivityCosts actCosts,
                                                   RouteAndActivityStateGetter stateManager,
                                                   double activityDurationOnSameLocation) {
        super(routingCosts, actCosts, stateManager);
        this.activityDurationOnSameLocation = activityDurationOnSameLocation;
    }

    @Override
    public double getCosts(JobInsertionContext iFacts, TourActivity prevAct, TourActivity nextAct, TourActivity newAct, double depTimeAtPrevAct) {
        return prevAct.getLocation().getCoordinate().equals(nextAct.getLocation().getCoordinate()) ? activityDurationOnSameLocation : super.getCosts(iFacts, prevAct, nextAct, newAct, depTimeAtPrevAct);
    }
}
