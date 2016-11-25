package com.graphhopper.jsprit.core.reporting.route;

import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

public interface CostAndTimeExtractor {

    default double getActivityCost(RoutePrinterContext context) {
        return context.getProblem().getActivityCosts().getActivityCost(context.getActivity(),
                        context.getActivity().getArrTime(), context.getRoute().getDriver(), context.getRoute().getVehicle());
    }

    default double getTransportCost(RoutePrinterContext context, TourActivity prevAct) {
        return prevAct == null ? 0d
                        : context.getProblem().getTransportCosts().getTransportCost(prevAct.getLocation(),
                                        context.getActivity().getLocation(),
                                        context.getActivity().getArrTime(), context.getRoute().getDriver(),
                                        context.getRoute().getVehicle());
    }

    default double getTransportTime(RoutePrinterContext context, TourActivity prevAct) {
        return prevAct == null ? 0d
                        : context.getProblem().getTransportCosts().getTransportTime(prevAct.getLocation(),
                                        context.getActivity().getLocation(),
                                        context.getActivity().getArrTime(), context.getRoute().getDriver(),
                                        context.getRoute().getVehicle());
    }

}
