package com.graphhopper.jsprit.core.algorithm.utils;

import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.job.RelativeBreak;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

public class RelativeBreaksUtils {

    public static double getRouteStartTime(VehicleRoute route, VehicleRoutingTransportCosts transportCosts) {
        if(route.getActivities().isEmpty())
            return route.getVehicle().getEarliestDeparture();

        TourActivity firstAct = route.getActivities().get(0);
        double drivingTimeToFirstActivity = transportCosts.getTransportTime(route.getStart().getLocation(), firstAct.getLocation(), firstAct.getArrTime()-firstAct.getOperationTime(), route.getDriver(), route.getVehicle());
        return firstAct.getEndTime() - firstAct.getOperationTime() - drivingTimeToFirstActivity;
    }

    public static double getRouteLength(VehicleRoute route, VehicleRoutingTransportCosts transportCosts) {
        return route.getEnd().getArrTime() - getRouteStartTime(route, transportCosts);
    }

    public static boolean minRouteLengthAchieved(VehicleRoute route, RelativeBreak aBreak, VehicleRoutingTransportCosts transportCosts) {
        double routeLength = getRouteLength(route, transportCosts);
        int threshold = aBreak.getThreshold();
        return routeLength >= threshold;
    }

    public static TimeWindow getUpdatedTimeWindowForRelativeBreak(VehicleRoute route, RelativeBreak aBreak, VehicleRoutingTransportCosts transportCosts) {
        double routeStartTime = RelativeBreaksUtils.getRouteStartTime(route, transportCosts);
        double breakTwStart =  routeStartTime + aBreak.getBreakStartSec();
        double breakTwEnd = routeStartTime + aBreak.getBreakEndSec();

        if(breakTwStart > breakTwEnd)
            return new TimeWindow(breakTwEnd, breakTwStart);

        return new TimeWindow(breakTwStart, breakTwEnd);
    }

}
