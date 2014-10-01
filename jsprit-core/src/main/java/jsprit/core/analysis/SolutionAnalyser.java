/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package jsprit.core.analysis;

import jsprit.core.algorithm.state.InternalStates;
import jsprit.core.algorithm.state.StateId;
import jsprit.core.algorithm.state.StateManager;
import jsprit.core.algorithm.state.StateUpdater;
import jsprit.core.problem.Capacity;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.ActivityVisitor;
import jsprit.core.problem.solution.route.activity.End;
import jsprit.core.problem.solution.route.activity.Start;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.util.ActivityTimeTracker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 */
public class SolutionAnalyser {

    public static interface DistanceCalculator {

        public double getDistance(String fromLocationId, String toLocationId);

    }

    private static class SumUpWaitingTimes implements StateUpdater, ActivityVisitor {

        private StateId waiting_time_id;

        private StateId transport_time_id;

        private StateId service_time_id;

        private StateManager stateManager;

        private ActivityTimeTracker.ActivityPolicy activityPolicy;

        private VehicleRoute route;

        double sum_waiting_time = 0.;

        double sum_transport_time = 0.;

        double sum_service_time = 0.;

        double prevActDeparture;

        private SumUpWaitingTimes(StateId waiting_time_id, StateId transport_time_id, StateId service_time_id, StateManager stateManager, ActivityTimeTracker.ActivityPolicy activityPolicy) {
            this.waiting_time_id = waiting_time_id;
            this.transport_time_id = transport_time_id;
            this.service_time_id = service_time_id;
            this.stateManager = stateManager;
            this.activityPolicy = activityPolicy;
        }

        @Override
        public void begin(VehicleRoute route) {
            this.route = route;
            sum_waiting_time = 0.;
            sum_transport_time = 0.;
            sum_service_time = 0.;
            prevActDeparture = route.getDepartureTime();
        }

        @Override
        public void visit(TourActivity activity) {
            //waiting time
            double waitAtAct = 0.;
            if(activityPolicy.equals(ActivityTimeTracker.ActivityPolicy.AS_SOON_AS_TIME_WINDOW_OPENS)){
                waitAtAct = Math.max(0,activity.getTheoreticalEarliestOperationStartTime() - activity.getArrTime());
            }
            sum_waiting_time += waitAtAct;
            //transport time
            double transportTime = activity.getArrTime() - prevActDeparture;
            sum_transport_time += transportTime;
            prevActDeparture = activity.getEndTime();
            //service time
            sum_service_time += activity.getOperationTime();
        }

        @Override
        public void finish() {
            sum_transport_time += route.getEnd().getArrTime() - prevActDeparture;
            stateManager.putRouteState(route,transport_time_id,sum_transport_time);
            stateManager.putRouteState(route,waiting_time_id,sum_waiting_time);
            stateManager.putRouteState(route,service_time_id,sum_service_time);
        }
    }

    private static class DistanceUpdater implements StateUpdater, ActivityVisitor {

        private StateId distance_id;

        private StateManager stateManager;

        private double sum_distance = 0.;

        private DistanceCalculator distanceCalculator;

        private TourActivity prevAct;

        private VehicleRoute route;

        private DistanceUpdater(StateId distance_id, StateManager stateManager, DistanceCalculator distanceCalculator) {
            this.distance_id = distance_id;
            this.stateManager = stateManager;
            this.distanceCalculator = distanceCalculator;
        }

        @Override
        public void begin(VehicleRoute route) {
            sum_distance = 0.;
            this.route = route;
            this.prevAct = route.getStart();
        }

        @Override
        public void visit(TourActivity activity) {
            double distance = distanceCalculator.getDistance(prevAct.getLocationId(),activity.getLocationId());
            sum_distance += distance;
            stateManager.putActivityState(activity,distance_id,sum_distance);
            prevAct = activity;
        }

        @Override
        public void finish() {
            double distance = distanceCalculator.getDistance(prevAct.getLocationId(), route.getEnd().getLocationId());
            sum_distance += distance;
            stateManager.putRouteState(route,distance_id,sum_distance);
        }
    }

    private VehicleRoutingProblem vrp;

    private List<VehicleRoute> routes;

    private StateManager stateManager;

    private DistanceCalculator distanceCalculator;

    private final StateId waiting_time_id;

    private final StateId transport_time_id;

    private final StateId service_time_id;

    private final StateId distance_id;

    private ActivityTimeTracker.ActivityPolicy activityPolicy;

    public SolutionAnalyser(VehicleRoutingProblem vrp, Collection<VehicleRoute> routes, StateManager stateManager, DistanceCalculator distanceCalculator) {
        this.vrp = vrp;
        this.routes = new ArrayList<VehicleRoute>(routes);
        this.stateManager = stateManager;
        this.distanceCalculator = distanceCalculator;
        waiting_time_id = stateManager.createStateId("waiting-time");
        transport_time_id = stateManager.createStateId("transport-time");
        service_time_id = stateManager.createStateId("service-time");
        distance_id = stateManager.createStateId("distance");
        activityPolicy = ActivityTimeTracker.ActivityPolicy.AS_SOON_AS_ARRIVED;
        if(stateManager.timeWindowUpdateIsActivated()){
            activityPolicy = ActivityTimeTracker.ActivityPolicy.AS_SOON_AS_TIME_WINDOW_OPENS;
        }
        stateManager.addStateUpdater(new SumUpWaitingTimes(waiting_time_id, transport_time_id, service_time_id, stateManager, activityPolicy));
        stateManager.addStateUpdater(new DistanceUpdater(distance_id,stateManager,distanceCalculator));
        refreshStates();
    }

    private void refreshStates(){
        stateManager.clear();
        stateManager.informInsertionStarts(routes,null);
    }
//
//
//    public void informRouteChanged(VehicleRoute route){
////            update(route);
//    }
//
//

    /**
     * @param route to get the load at beginning from
     * @return load at start location of specified route
     */
    public Capacity getLoadAtBeginning(VehicleRoute route){
        return stateManager.getRouteState(route, InternalStates.LOAD_AT_BEGINNING,Capacity.class);
    }

    /**
     * @param route to get the load at the end from
     * @return load at end location of specified route
     */
    public Capacity getLoadAtEnd(VehicleRoute route){
        return stateManager.getRouteState(route, InternalStates.LOAD_AT_END,Capacity.class);
    }

    /**
     * @param route to get max load from
     * @return max load of specified route, i.e. for each capacity dimension the max value.
     */
    public Capacity getMaxLoad(VehicleRoute route){
        return stateManager.getRouteState(route, InternalStates.MAXLOAD,Capacity.class);
    }

    /**
     *
     * @param activity to get the load from (after activity)
     * @return load right after the specified activity. If act is Start, it returns the load atBeginning of the specified
     * route. If act is End, it returns the load atEnd of specified route.
     * Returns null if no load can be found.
     */
    public Capacity getLoadRightAfterActivity(TourActivity activity, VehicleRoute route){
        if(activity instanceof Start) return getLoadAtBeginning(route);
        if(activity instanceof End) return getLoadAtEnd(route);
        return stateManager.getActivityState(activity, InternalStates.LOAD,Capacity.class);
    }

    /**
     * @param activity to get the load from (before activity)
     * @return load just before the specified activity. If act is Start, it returns the load atBeginning of the specified
     * route. If act is End, it returns the load atEnd of specified route.
     */
    public Capacity getLoadJustBeforeActivity(TourActivity activity, VehicleRoute route){
        if(activity instanceof Start) return getLoadAtBeginning(route);
        if(activity instanceof End) return getLoadAtEnd(route);
        Capacity afterAct = stateManager.getActivityState(activity, InternalStates.LOAD,Capacity.class);
        if(afterAct != null && activity.getSize() != null){
            return Capacity.subtract(afterAct, activity.getSize());
        }
        else if(afterAct != null) return afterAct;
        else return null;
    }
//
////        public Capacity getViolationAtBeginning(VehicleRoute route){
////            Capacity atBeginning = getLoadAtBeginning(route);
////            return Capacity.max(Capacity.Builder.newInstance().build(),Capacity.subtract(atBeginning,route.getVehicle().getType().getCapacityDimensions()));
////        }
////
////        public Capacity getViolationAtEnd(VehicleRoute route){
////            Capacity atEnd = getLoadAtEnd(route);
////            return Capacity.max(Capacity.Builder.newInstance().build(),Capacity.subtract(atEnd,route.getVehicle().getType().getCapacityDimensions()));
////        }
////
////        public Capacity getViolationRightAfterActivity(TourActivity activity, VehicleRoute route){
////            Capacity afterAct = getLoadRightAfterActivity(activity);
////            return Capacity.max(Capacity.Builder.newInstance().build(),Capacity.subtract(afterAct,route.getVehicle().getType().getCapacityDimensions()));
////        }

    /**
     * @param route to get the total operation time from
     * @return operation time of this route, i.e. endTime - startTime of specified route
     */
    public Double getOperationTime(VehicleRoute route){
        return route.getEnd().getArrTime() - route.getStart().getEndTime();
    }

    /**
     * @param route to get the total waiting time from
     * @return total waiting time of this route, i.e. sum of waiting times at activities.
     * Returns null if no waiting time value exists for the specified route
     */
    public Double getWaitingTime(VehicleRoute route){
        return stateManager.getRouteState(route, waiting_time_id, Double.class);
    }

    /**
     * @param route to get the total transport time from
     * @return total transport time of specified route. Returns null if no time value exists for the specified route.
     */
    public Double getTransportTime(VehicleRoute route){
        return stateManager.getRouteState(route, transport_time_id, Double.class);
    }

    /**
     * @param route to get the total service time from
     * @return total service time of specified route. Returns null if no time value exists for specified route.
     */
    public Double getServiceTime(VehicleRoute route){
        return stateManager.getRouteState(route, service_time_id, Double.class);
    }

    /**
     * @param activity to get the waiting from
     * @return waiting time at activity
     */
    public Double getWaitingTimeAtActivity(TourActivity activity, VehicleRoute route){
        double waitingTime = 0.;
        if(activityPolicy.equals(ActivityTimeTracker.ActivityPolicy.AS_SOON_AS_TIME_WINDOW_OPENS)){
            waitingTime = Math.max(0,activity.getTheoreticalEarliestOperationStartTime()-activity.getArrTime());
        }
        return waitingTime;
    }
//
//    public double getTimeTooLateAtActivity(TourActivity activity){
//
//    }
//
    /**
     * @param route to get the distance from
     * @return total distance of route
     */
    public Double getDistance(VehicleRoute route){
        return stateManager.getRouteState(route, distance_id, Double.class);
    }

    /**
     * @param activity at which is distance of the current route is measured
     * @return distance at activity
     */
    public double getDistanceAtActivity(TourActivity activity, VehicleRoute route){
        if(activity instanceof Start) return 0.;
        if(activity instanceof End) return getDistance(route);
        return stateManager.getActivityState(activity, distance_id, Double.class);
    }

//
//    /**
//     * @return total distance
//     */
//    public double getDistance(){
//
//    }
//
//    /**
//     * @return total operation time
//     */
//    public double getOperationTime(){
//
//    }
//
//    /**
//     * @return total waiting time
//     */
//    public double getWaitingTime(){
//
//    }
//
//    /**
//     * @return total transportation time
//     */
//    public double getTransportationTime(){
//
//    }

}

