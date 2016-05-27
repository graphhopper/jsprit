package com.graphhopper.jsprit.core.util;

import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ActivityVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

public class ActivityDistanceTracker implements ActivityVisitor{

    private double distanceAtPrevAct;

    private final VehicleRoutingTransportCosts transportDistance;

    private VehicleRoute route;

    private TourActivity prevAct = null;

    public ActivityDistanceTracker(VehicleRoutingTransportCosts transportDistance) {
        super();
        this.transportDistance = transportDistance;
    }
    
    public double getActDistance() {
        return distanceAtPrevAct;
    }
    
    @Override
    public void begin(VehicleRoute route) {
        prevAct = route.getStart();
        distanceAtPrevAct = 0;
        this.route = route;
    }

    @Override
    public void visit(TourActivity activity) {
        distanceAtPrevAct += transportDistance.getDistance(prevAct.getLocation(), activity.getLocation());
    }

    @Override
    public void finish() {
        distanceAtPrevAct += transportDistance.getDistance(prevAct.getLocation(), route.getEnd().getLocation());
    }

}
