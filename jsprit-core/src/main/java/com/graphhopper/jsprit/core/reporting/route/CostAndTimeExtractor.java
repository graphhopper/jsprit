package com.graphhopper.jsprit.core.reporting.route;

import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

/**
 * Utility interface for extracting cost and time values from problem.
 *
 * @author balage
 *
 */
public interface CostAndTimeExtractor {

    /**
     * Returns the activity cost extracted from the context.
     *
     * @param context
     *            The context.
     * @return The activity cost.
     */
    default double getActivityCost(RoutePrinterContext context) {
        return context.getProblem().getActivityCosts().getActivityCost(context.getActivity(),
                        context.getActivity().getArrTime(), context.getRoute().getDriver(), context.getRoute().getVehicle());
    }

    /**
     * Returns the transport cost extracted from the context.
     *
     * @param context
     *            The context.
     * @return The transport cost.
     */
    default double getTransportCost(RoutePrinterContext context, TourActivity prevAct) {
        return prevAct == null ? 0d
                        : context.getProblem().getTransportCosts().getTransportCost(prevAct.getLocation(),
                                        context.getActivity().getLocation(),
                                        context.getActivity().getArrTime(), context.getRoute().getDriver(),
                                        context.getRoute().getVehicle());
    }

    /**
     * Returns the transport time extracted from the context.
     *
     * @param context
     *            The context.
     * @return The transpoert time.
     */
    default double getTransportTime(RoutePrinterContext context, TourActivity prevAct) {
        return prevAct == null ? 0d
                        : context.getProblem().getTransportCosts().getTransportTime(prevAct.getLocation(),
                                        context.getActivity().getLocation(),
                                        context.getActivity().getArrTime(), context.getRoute().getDriver(),
                                        context.getRoute().getVehicle());
    }

}
