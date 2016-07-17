package com.graphhopper.jsprit.core.algorithm.state;

import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ActivityVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.util.ActivityDistanceTracker;

public class UpdateActivityDistances implements ActivityVisitor, StateUpdater {
    
    private ActivityDistanceTracker distanceTracker;

    private VehicleRoute route;
    
    public UpdateActivityDistances(VehicleRoutingTransportCosts transportDistance) {
        super();
        distanceTracker = new ActivityDistanceTracker(transportDistance);
    }
    
    @Override
    public void begin(VehicleRoute route) {
        distanceTracker.begin(route);
        this.route = route;
        route.getStart().setRouteDistance(distanceTracker.getActDistance());
    }

    @Override
    public void visit(TourActivity activity) {
        distanceTracker.visit(activity);
        activity.setRouteDistance(distanceTracker.getActDistance());
    }

    @Override
    public void finish() {
        distanceTracker.finish();
        route.getEnd().setRouteDistance(distanceTracker.getActDistance());
    }

}
