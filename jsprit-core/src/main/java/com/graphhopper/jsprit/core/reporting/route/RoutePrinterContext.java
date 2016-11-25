package com.graphhopper.jsprit.core.reporting.route;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.reporting.PrinterContext;

public class RoutePrinterContext extends PrinterContext {

    private int routeNr;
    private VehicleRoute route;
    private TourActivity activity;
    private VehicleRoutingProblem problem;

    public RoutePrinterContext(int routeNr, VehicleRoute route, TourActivity activity, VehicleRoutingProblem problem) {
        super();
        this.routeNr = routeNr;
        this.route = route;
        this.activity = activity;
        this.problem = problem;
    }

    public int getRouteNr() {
        return routeNr;
    }

    public VehicleRoute getRoute() {
        return route;
    }

    public TourActivity getActivity() {
        return activity;
    }

    public void setActivity(TourActivity activity) {
        this.activity = activity;
    }

    public VehicleRoutingProblem getProblem() {
        return problem;
    }

}
