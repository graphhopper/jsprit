package com.graphhopper.jsprit.core.reporting;

import java.util.HashMap;
import java.util.Map;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.BreakActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.JobActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

public class VehicleSummaryRecord {
    private Vehicle vehicle;
    private Driver driver;
    private int routeNr;
    private long start;
    private long end;
    private int activityCount = 0;
    private Map<String, Integer> activityCountByType = new HashMap<>();
    private long travelDuration;
    private long operationDuration;
    private long breakDuration;
    private long travelDistance;

    public VehicleSummaryRecord(VehicleRoute route, VehicleRoutingProblem problem) {
        routeNr = route.getId();
        vehicle = route.getVehicle();
        driver = route.getDriver();
        start = (long) route.getStart().getEndTime();
        end = (long) route.getEnd().getArrTime();

        TourActivity prevAct = route.getStart();
        for (TourActivity act : route.getActivities()) {
            if (act instanceof BreakActivity) {
                breakDuration += act.getOperationTime();
            } else
                if (act instanceof JobActivity) {
                    JobActivity jobAct = (JobActivity) act;
                    activityCount++;
                    String type = jobAct.getType();
                    if (!activityCountByType.containsKey(type)) {
                        activityCountByType.put(type, 0);
                    }
                    activityCountByType.put(type, activityCountByType.get(type) + 1);
                    operationDuration += jobAct.getOperationTime();
                    travelDuration += problem.getTransportCosts().getTransportTime(prevAct.getLocation(),
                            act.getLocation(),act.getArrTime(), route.getDriver(),
                            route.getVehicle());
                }
            prevAct = act;
        }
    }

    public String getVehicleId() {
        return vehicle.getId();
    }

    public String getDriverId() {
        return driver.getId();
    }

    public int getRouteNr() {
        return routeNr;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public int getActivityCount() {
        return activityCount;
    }

    public Map<String, Integer> getActivityCountByType() {
        return activityCountByType;
    }

    public long getTravelDuration() {
        return travelDuration;
    }

    public long getOperationDuration() {
        return operationDuration;
    }

    public long getShiftDuration() {
        return vehicle.getLatestArrival() == Double.MAX_VALUE ? getRouteDuration()
                : (long) (vehicle.getLatestArrival() - vehicle.getEarliestDeparture());
    }

    public long getRouteDuration() {
        return end - start;
    }

    public long getTravelDistance() {
        return travelDistance;
    }

    public long getBreakDuration() {
        return breakDuration;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public Driver getDriver() {
        return driver;
    }

    @Override
    public String toString() {
        return "VehicleStatisticsContext [vehicleId=" + vehicle.getId() + ", driver=" + driver.getId() + ", routeNr=" + routeNr
                + ", start=" + start + ", end=" + end + ", activityCount=" + activityCount + ", activityCountByType="
                + activityCountByType + ", travelDuration=" + travelDuration + ", operationDuration=" + operationDuration
                + ", totalDuration=" + getRouteDuration() + ", travelDistance=" + travelDistance + ", breakDuration="
                + breakDuration + "]";
    }

    public Long getActiveDuration() {
        return getTravelDuration() + getOperationDuration();
    }

    public Long getIdleDuration() {
        return getRouteDuration() - (getActiveDuration() + getBreakDuration());
    }

}