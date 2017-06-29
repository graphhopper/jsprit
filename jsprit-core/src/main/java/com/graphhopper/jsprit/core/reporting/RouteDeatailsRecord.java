package com.graphhopper.jsprit.core.reporting;

import com.graphhopper.jsprit.core.problem.SizeDimension;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.AbstractJob;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.JobActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

/**
 * The context of the detailed route printer columns.
 *
 * <p>
 * This is a imutable class.
 * </p>
 *
 * @author balage
 *
 */
public class RouteDeatailsRecord {

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
    public RouteDeatailsRecord(VehicleRoute route, TourActivity activity,
            VehicleRoutingProblem problem) {
        super();
        this.route = route;
        this.activity = activity;
        this.problem = problem;
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
     * @return The problem.
     */
    public VehicleRoutingProblem getProblem() {
        return problem;
    }

    public AbstractJob getJob() {
        return (getActivity() instanceof JobActivity) ? ((JobActivity) getActivity()).getJob() : null;
    }

    public SizeDimension calculateInitialLoad() {
        SizeDimension sd = SizeDimension.EMPTY;
        for (TourActivity a : getRoute().getActivities()) {
            sd = sd.add(a.getLoadChange());
        }
        sd = sd.getNegativeDimensions().abs();
        return sd;
    }

    /**
     * Returns the activity cost extracted from the context.
     *
     * @param context
     *            The context.
     * @return The activity cost.
     */
    double getActivityCost() {
        return getProblem().getActivityCosts().getActivityCost(getActivity(),
                getActivity().getArrTime(), getRoute().getDriver(), getRoute().getVehicle());
    }

    /**
     * Returns the transport cost extracted from the
     *
     * @param context
     *            The
     * @return The transport cost.
     */
    double getTransportCost(TourActivity prevAct) {
        return prevAct == null ? 0d
                : getProblem().getTransportCosts().getTransportCost(prevAct.getLocation(),
                        getActivity().getLocation(), getActivity().getArrTime(),
                        getRoute().getDriver(), getRoute().getVehicle());
    }

    /**
     * Returns the transport time extracted from the
     *
     * @param context
     *            The
     * @return The transpoert time.
     */
    double getTransportTime(TourActivity prevAct) {
        return prevAct == null ? 0d
                : getProblem().getTransportCosts().getTransportTime(prevAct.getLocation(),
                        getActivity().getLocation(), getActivity().getArrTime(),
                        getRoute().getDriver(), getRoute().getVehicle());
    }
}
