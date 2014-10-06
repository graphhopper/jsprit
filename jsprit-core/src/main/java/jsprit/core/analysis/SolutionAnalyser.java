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
import jsprit.core.problem.constraint.ConstraintManager;
import jsprit.core.problem.constraint.HardActivityConstraint;
import jsprit.core.problem.constraint.HardRouteConstraint;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.ActivityVisitor;
import jsprit.core.problem.solution.route.activity.End;
import jsprit.core.problem.solution.route.activity.Start;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.util.ActivityTimeTracker;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class SolutionAnalyser {




    public static interface DistanceCalculator {

        public double getDistance(String fromLocationId, String toLocationId);

    }

//    public static class ViolatedRouteConstraints {
//
//        private List<HardRouteConstraint> hardRouteConstraints = new ArrayList<HardRouteConstraint>();
//
//        public List<HardRouteConstraint> getHardRouteConstraints() {
//            return hardRouteConstraints;
//        }
//
//    }
//
//    public static class ViolatedActivityConstraints {
//
//        private List<HardActivityConstraint> hardConstraints = new ArrayList<HardActivityConstraint>();
//
//        public List<HardActivityConstraint> getHardActivityConstraints() {
//            return hardConstraints;
//        }
//
//    }

    private static class SumUpWaitingTimes implements StateUpdater, ActivityVisitor {

        private StateId waiting_time_id;

        private StateId transport_time_id;

        private StateId service_time_id;

        private StateId too_late_id;

        private StateManager stateManager;

        private ActivityTimeTracker.ActivityPolicy activityPolicy;

        private VehicleRoute route;

        double sum_waiting_time = 0.;

        double sum_transport_time = 0.;

        double sum_service_time = 0.;

        double sum_too_late = 0.;

        double prevActDeparture;

        private SumUpWaitingTimes(StateId waiting_time_id, StateId transport_time_id, StateId service_time_id, StateId too_late_id, StateManager stateManager, ActivityTimeTracker.ActivityPolicy activityPolicy) {
            this.waiting_time_id = waiting_time_id;
            this.transport_time_id = transport_time_id;
            this.service_time_id = service_time_id;
            this.too_late_id = too_late_id;
            this.stateManager = stateManager;
            this.activityPolicy = activityPolicy;
        }

        @Override
        public void begin(VehicleRoute route) {
            this.route = route;
            sum_waiting_time = 0.;
            sum_transport_time = 0.;
            sum_service_time = 0.;
            sum_too_late = 0.;
            prevActDeparture = route.getDepartureTime();
        }

        @Override
        public void visit(TourActivity activity) {
            //waiting time & toolate
            double waitAtAct = 0.;
            double tooLate = 0.;
            if(activityPolicy.equals(ActivityTimeTracker.ActivityPolicy.AS_SOON_AS_TIME_WINDOW_OPENS)){
                waitAtAct = Math.max(0,activity.getTheoreticalEarliestOperationStartTime() - activity.getArrTime());
                tooLate = Math.max(0,activity.getArrTime() - activity.getTheoreticalLatestOperationStartTime());
            }
            sum_waiting_time += waitAtAct;
            sum_too_late += tooLate;
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
            stateManager.putRouteState(route,too_late_id,sum_too_late);
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

//    private static class ConstraintViolationUpdater implements StateUpdater, ActivityVisitor {
//
//        private ConstraintManager constraintManager;
//
//        private StateManager stateManager;
//
//        private StateId violated_route_constraints_id;
//
//        private VehicleRoute route;
//
//        private List<HardRouteConstraint> hardRouteConstraints;
//
//        private Set<String> examinedJobs = new HashSet<String>();
//
////        ViolatedRouteConstraints violatedRouteConstraints = new ViolatedRouteConstraints();
//
//        private ConstraintViolationUpdater(ConstraintManager constraintManager, StateManager stateManager, StateId violated_route_constraints_id) {
//            this.constraintManager = constraintManager;
//            this.stateManager = stateManager;
//            this.violated_route_constraints_id = violated_route_constraints_id;
//            hardRouteConstraints = getHardRouteConstraints(constraintManager);
//        }
//
//        private List<HardRouteConstraint> getHardRouteConstraints(ConstraintManager constraintManager) {
//            List<HardRouteConstraint> constraints = new ArrayList<HardRouteConstraint>();
//            for(Constraint c : constraintManager.getConstraints()){
//                if(c instanceof HardRouteConstraint){
//                    constraints.add((HardRouteConstraint) c);
//                }
//            }
//            return constraints;
//        }
//
//        @Override
//        public void begin(VehicleRoute route) {
//            this.route = route;
//            examinedJobs.clear();
////            violatedRouteConstraints.getHardRouteConstraints().clear();
//        }
//
//        @Override
//        public void visit(TourActivity activity) {
//            //hard route constraints
//            if(activity instanceof TourActivity.JobActivity){
//                Job job = ((TourActivity.JobActivity) activity).getJob();
//                if(!examinedJobs.contains(job.getId())){
//                    examinedJobs.add(job.getId());
//                    JobInsertionContext iContext = new JobInsertionContext(route,job,route.getVehicle(),route.getDriver(),route.getDepartureTime());
//                    for(HardRouteConstraint hardRouteConstraint : hardRouteConstraints){
//                        boolean fulfilled = hardRouteConstraint.fulfilled(iContext);
//                        if(!fulfilled) violatedRouteConstraints.getHardRouteConstraints().add(hardRouteConstraint);
//                    }
//                }
//            }
//
//        }
//
//        @Override
//        public void finish() {
//            stateManager.putRouteState(route,violated_route_constraints_id,violatedRouteConstraints);
//        }
//    }

    private VehicleRoutingProblem vrp;

    private List<VehicleRoute> routes;

    private StateManager stateManager;

    private ConstraintManager constraintManager;

    private DistanceCalculator distanceCalculator;

    private final StateId waiting_time_id;

    private final StateId transport_time_id;

    private final StateId service_time_id;

    private final StateId distance_id;

    private final StateId too_late_id;

//    private final StateId violated_constraint_id;

    private ActivityTimeTracker.ActivityPolicy activityPolicy;

    private List<HardRouteConstraint> hardRouteConstraints;

    private List<HardActivityConstraint> hardActivityConstraints;

    private final VehicleRoutingProblemSolution working_solution;

//    private final VehicleRoutingProblemSolution original_solution;

    public SolutionAnalyser(VehicleRoutingProblem vrp, VehicleRoutingProblemSolution solution, DistanceCalculator distanceCalculator) {
        this.vrp = vrp;
//        this.original_solution = VehicleRoutingProblemSolution.copyOf(solution);
        this.working_solution = solution;
        this.routes = new ArrayList<VehicleRoute>(working_solution.getRoutes());
        this.stateManager = new StateManager(vrp);
        this.stateManager.updateTimeWindowStates();
        this.stateManager.updateLoadStates();
        this.stateManager.updateSkillStates();
        this.constraintManager = new ConstraintManager(vrp,stateManager);
        this.distanceCalculator = distanceCalculator;
        waiting_time_id = stateManager.createStateId("waiting-time");
        transport_time_id = stateManager.createStateId("transport-time");
        service_time_id = stateManager.createStateId("service-time");
        distance_id = stateManager.createStateId("distance");
        too_late_id = stateManager.createStateId("too-late");
//        violated_constraint_id = stateManager.createStateId("violated-constraints");
        activityPolicy = ActivityTimeTracker.ActivityPolicy.AS_SOON_AS_TIME_WINDOW_OPENS;
//        if(stateManager.timeWindowUpdateIsActivated()){
//            activityPolicy = ActivityTimeTracker.ActivityPolicy.AS_SOON_AS_TIME_WINDOW_OPENS;
//        }
        stateManager.addStateUpdater(new SumUpWaitingTimes(waiting_time_id, transport_time_id, service_time_id,too_late_id , stateManager, activityPolicy));
        stateManager.addStateUpdater(new DistanceUpdater(distance_id,stateManager,distanceCalculator));
//        hardRouteConstraints = getHardRouteConstraints(constraintManager);
//        hardActivityConstraints = getHardActivityConstrains(constraintManager);
        refreshStates();
    }

//    private List<HardActivityConstraint> getHardActivityConstrains(ConstraintManager constraintManager) {
//        List<HardActivityConstraint> constraints = new ArrayList<HardActivityConstraint>();
//        for(Constraint c : constraintManager.getConstraints()){
//            if(c instanceof HardActivityConstraint){
//                constraints.add((HardActivityConstraint) c);
//            }
//        }
//        return constraints;
//    }

    private void refreshStates(){
        stateManager.clear();
        stateManager.informInsertionStarts(routes,null);
//        checkConstraintViolation();
    }

//    private void checkConstraintViolation() {
//        for(VehicleRoute route : routes){
//            Set<HardRouteConstraint> unique = new HashSet<HardRouteConstraint>();
//            Set<String> examinedJobIds = new HashSet<String>();
//            TourActivity actBeforePrevAct = route.getStart();
//            TourActivity prevAct = null;
//            JobInsertionContext prevJobInsertionContext = null;
//            for(TourActivity act : route.getActivities()){
//                if(prevAct == null){
//                    prevAct = act;
//                    prevJobInsertionContext = createContext(route,act);
//                    continue;
//                }
//                //examine hard route constraints
//                if(!examinedJobIds.contains(prevJobInsertionContext.getJob().getId())){
//                    examinedJobIds.add(prevJobInsertionContext.getJob().getId());
//                    for(HardRouteConstraint hardRouteConstraint : hardRouteConstraints){
//                        boolean fulfilled = hardRouteConstraint.fulfilled(prevJobInsertionContext);
//                        if(!fulfilled) unique.add(hardRouteConstraint);
//                    }
//                }
//
//                //exmaine hard activity constraints
//                examineActivityConstraints(prevJobInsertionContext, actBeforePrevAct, prevAct, act);
//
//                actBeforePrevAct = prevAct;
//                prevAct = act;
//                prevJobInsertionContext = createContext(route,act);
//            }
//            examineActivityConstraints(prevJobInsertionContext,actBeforePrevAct,prevAct,route.getEnd());
//
////            ViolatedRouteConstraints violatedRouteConstraints = new ViolatedRouteConstraints();
////            violatedRouteConstraints.getHardRouteConstraints().addAll(unique);
////            stateManager.putRouteState(route, violated_constraint_id,violatedRouteConstraints);
//        }
//    }

//    private void examineActivityConstraints(JobInsertionContext prevJobInsertionContext, TourActivity actBeforePrevAct, TourActivity prevAct, TourActivity act) {
//        ViolatedActivityConstraints violatedActivityConstraints = new ViolatedActivityConstraints();
//        for(HardActivityConstraint activityConstraint : hardActivityConstraints){
//            HardActivityConstraint.ConstraintsStatus status =
//                    activityConstraint.fulfilled(prevJobInsertionContext,actBeforePrevAct,prevAct,act,actBeforePrevAct.getEndTime());
//            if(!status.equals(HardActivityConstraint.ConstraintsStatus.FULFILLED)){
//                violatedActivityConstraints.getHardActivityConstraints().add(activityConstraint);
//            }
//        }
//        stateManager.putActivityState(prevAct, violated_constraint_id,violatedActivityConstraints);
//    }

//    private JobInsertionContext createContext(VehicleRoute route, TourActivity act) {
//        if(act instanceof TourActivity.JobActivity){
//            return new JobInsertionContext(route,((TourActivity.JobActivity) act).getJob(),route.getVehicle(),route.getDriver(),route.getDepartureTime());
//        }
//        throw new IllegalStateException("act not a job activity. this should not be currently");
//    }
//
//    private List<HardRouteConstraint> getHardRouteConstraints(ConstraintManager constraintManager) {
//        List<HardRouteConstraint> constraints = new ArrayList<HardRouteConstraint>();
//        for(Constraint c : constraintManager.getConstraints()){
//            if(c instanceof HardRouteConstraint){
//                constraints.add((HardRouteConstraint) c);
//            }
//        }
//        return constraints;
//    }

//
//
//    public void informRouteChanged(VehicleRoute route){
//        update(route);
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
        return stateManager.getActivityState(activity, InternalStates.LOAD, Capacity.class);
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

    /**
     * @param route to get the capacity violation from
     * @return the capacity violation on this route, i.e. maxLoad - vehicleCapacity
     */
    public Capacity getCapacityViolation(VehicleRoute route){
        Capacity maxLoad = getMaxLoad(route);
        return Capacity.max(Capacity.Builder.newInstance().build(),Capacity.subtract(maxLoad,route.getVehicle().getType().getCapacityDimensions()));
    }

    /**
     * @param route to get the capacity violation from (at beginning of the route)
     * @return violation, i.e. all dimensions and their corresponding violation. For example, if vehicle has two capacity
     * dimension with dimIndex=0 and dimIndex=1 and dimIndex=1 is violated by 4 units then this method returns
     * [[dimIndex=0][dimValue=0][dimIndex=1][dimValue=4]]
     */
    public Capacity getCapacityViolationAtBeginning(VehicleRoute route){
        Capacity atBeginning = getLoadAtBeginning(route);
        return Capacity.max(Capacity.Builder.newInstance().build(),Capacity.subtract(atBeginning,route.getVehicle().getType().getCapacityDimensions()));
    }

    /**
     * @param route to get the capacity violation from (at end of the route)
     * @return violation, i.e. all dimensions and their corresponding violation. For example, if vehicle has two capacity
     * dimension with dimIndex=0 and dimIndex=1 and dimIndex=1 is violated by 4 units then this method returns
     * [[dimIndex=0][dimValue=0][dimIndex=1][dimValue=4]]
     */
    public Capacity getCapacityViolationAtEnd(VehicleRoute route){
        Capacity atEnd = getLoadAtEnd(route);
        return Capacity.max(Capacity.Builder.newInstance().build(),Capacity.subtract(atEnd,route.getVehicle().getType().getCapacityDimensions()));
    }


    /**
     * @param route to get the capacity violation from (at activity of the route)
     * @return violation, i.e. all dimensions and their corresponding violation. For example, if vehicle has two capacity
     * dimension with dimIndex=0 and dimIndex=1 and dimIndex=1 is violated by 4 units then this method returns
     * [[dimIndex=0][dimValue=0][dimIndex=1][dimValue=4]]
     */
    public Capacity getCapacityViolationAfterActivity(TourActivity activity, VehicleRoute route){
        Capacity afterAct = getLoadRightAfterActivity(activity,route);
        return Capacity.max(Capacity.Builder.newInstance().build(),Capacity.subtract(afterAct,route.getVehicle().getType().getCapacityDimensions()));
    }

    /**
     * @param route to get the time window violation from
     * @return time violation of route, i.e. sum of individual activity time window violations.
     */
    public Double getTimeWindowViolation(VehicleRoute route){
        return stateManager.getRouteState(route,too_late_id,Double.class);
    }

    /**
     * @param activity to get the time window violation from
     * @param route where activity needs to be part of
     * @return time violation of activity
     */
    public Double getTimeWindowViolationAtActivity(TourActivity activity, VehicleRoute route){
        return Math.max(0,activity.getArrTime()-activity.getTheoreticalLatestOperationStartTime());
    }

    public Boolean skillConstraintIsViolated(VehicleRoute route){
        return null;
    }

    public Boolean skillConstraintIsViolatedAtActivity(TourActivity activity, VehicleRoute route){
        return null;
    }

    public Boolean backhaulConstraintIsViolated(VehicleRoute route){
        return null;
    }

    public Boolean shipmentConstraintIsViolated(VehicleRoute route){
        return null;
    }

    public Boolean shipmentConstraintIsViolatedAtActivity(TourActivity activity, VehicleRoute route){
        return null;
    }



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
     * @param route to get the transport costs from
     * @return total variable transport costs of route, i.e. sum of transport costs specified by
     * vrp.getTransportCosts().getTransportCost(fromId,toId,...)
     */
    public Double getVariableTransportCosts(VehicleRoute route){
        return stateManager.getRouteState(route,InternalStates.COSTS,Double.class);
    }

    /**
     * @param route to get the fixed costs from
     * @return fixed costs of route, i.e. fixed costs of employed vehicle on this route.
     */
    public Double getFixedCosts(VehicleRoute route){
        return route.getVehicle().getType().getVehicleCostParams().fix;
    }

//    public ViolatedRouteConstraints getViolatedHardConstraints(VehicleRoute route){
//        return stateManager.getRouteState(route, violated_constraint_id,ViolatedRouteConstraints.class);
//    }

//    public Map<SoftRouteConstraint,Double> getViolatedSoftConstraints(VehicleRoute route){
//        return null;
//    }

//    public ViolatedActivityConstraints getViolatedHardConstraintsAtActivity(TourActivity activity, VehicleRoute route){
//        if(activity instanceof Start) return new ViolatedActivityConstraints();
//        if(activity instanceof End) return new ViolatedActivityConstraints();
//        return stateManager.getActivityState(activity,violated_constraint_id,ViolatedActivityConstraints.class);
//    }

//    public Map<SoftRouteConstraint,Double> getViolatedSoftConstraint(VehicleRoute route){
//        return null;
//    }

    /**
     * @param activity to get the variable transport costs from
     * @param route where the activity should be part of
     * @return variable transport costs at activity, i.e. sum of transport costs from start of route to the specified activity
     * If activity is start, it returns 0.. If it is end, it returns .getVariableTransportCosts(route).
     */
    public Double getVariableTransportCostsAtActivity(TourActivity activity, VehicleRoute route){
        if(activity instanceof Start) return 0.;
        if(activity instanceof End) return getVariableTransportCosts(route);
        return stateManager.getActivityState(activity,InternalStates.COSTS,Double.class);
    }

    /**
     * @param activity to get the waiting from
     * @param route where activity should be part of
     * @return waiting time at activity
     */
    public Double getWaitingTimeAtActivity(TourActivity activity, VehicleRoute route){
        double waitingTime = 0.;
        if(activityPolicy.equals(ActivityTimeTracker.ActivityPolicy.AS_SOON_AS_TIME_WINDOW_OPENS)){
            waitingTime = Math.max(0,activity.getTheoreticalEarliestOperationStartTime()-activity.getArrTime());
        }
        return waitingTime;
    }

    /**
     * @param activity to get the late arrival times from
     * @return time too late
     */
    public Double getLateArrivalTimesAtActivity(TourActivity activity, VehicleRoute route){
        double tooLate = 0.;
        if(activityPolicy.equals(ActivityTimeTracker.ActivityPolicy.AS_SOON_AS_TIME_WINDOW_OPENS)){
            tooLate = Math.max(0,activity.getArrTime()-activity.getTheoreticalLatestOperationStartTime());
        }
        return tooLate;
    }

    /**
     * @param route to get the late arrival times from
     * @return time too late, i.e. sum of late time of activities
     */
    public Double getLateArrivalTimes(VehicleRoute route){
        return stateManager.getRouteState(route,too_late_id,Double.class);
    }

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
    public Double getDistanceAtActivity(TourActivity activity, VehicleRoute route){
        if(activity instanceof Start) return 0.;
        if(activity instanceof End) return getDistance(route);
        return stateManager.getActivityState(activity, distance_id, Double.class);
    }




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

