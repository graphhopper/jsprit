package com.graphhopper.jsprit.core.reporting.route;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.reporting.PrinterContext;

/**
 * The context of the detailed route printer columns.
 *
 * <p>
 * This is a semi-mutable class: only the activity could be altered. Therefore
 * for each route a new instance should be created.
 * </p>
 *
 * @author balage
 *
 */
public class RoutePrinterContext implements PrinterContext {

    // The route id
    private int routeNr;
    // The route itself
    private VehicleRoute route;
    // The current activity
    private TourActivity activity;
    // The problem
    private VehicleRoutingProblem problem;

    /**
     * Constructor.
     *
     * @param routeNr
     *            route id
     * @param route
     *            the route
     * @param activity
     *            current activity
     * @param problem
     *            problem
     */
    public RoutePrinterContext(int routeNr, VehicleRoute route, TourActivity activity, VehicleRoutingProblem problem) {
        super();
        this.routeNr = routeNr;
        this.route = route;
        this.activity = activity;
        this.problem = problem;
    }

    /**
     * @return The route id.
     */
    public int getRouteNr() {
        return routeNr;
    }

    /**
     * @return The route itself.
     */
    public VehicleRoute getRoute() {
        return route;
    }

    /**
     * @return The current activity.
     */
    public TourActivity getActivity() {
        return activity;
    }

    /**
     * @param activity
     *            The current activity.
     */
    public void setActivity(TourActivity activity) {
        this.activity = activity;
    }

    /**
     * @return The problem.
     */
    public VehicleRoutingProblem getProblem() {
        return problem;
    }

}
