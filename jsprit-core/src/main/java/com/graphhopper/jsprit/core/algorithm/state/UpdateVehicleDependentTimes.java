package com.graphhopper.jsprit.core.algorithm.state;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.cost.*;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ActivityVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeKey;

import java.util.*;

/**
 * Created by hehuang on 4/10/17.
 */
public class UpdateVehicleDependentTimes implements StateUpdater, ActivityVisitor {

    static class State {

        Location prevLocation;

        double time;

        public State(Location prevLocation, double time) {
            this.prevLocation = prevLocation;
            this.time = time;
        }

        public Location getPrevLocation() {
            return prevLocation;
        }

        public double getTime() {
            return time;
        }
    }

    private final VehicleRoutingTransportCosts transportCosts;

    private final VehicleRoutingActivityCosts activityCosts;

    private final StateManager stateManager;

    private VehicleRoute route;

    private List<Vehicle> uniqueVehicles;

    private Map<VehicleTypeKey, State> states;

    public UpdateVehicleDependentTimes(VehicleRoutingTransportCosts transportCosts,
                                       VehicleRoutingActivityCosts activityCosts,
                                       StateManager stateManager,
                                       Collection<Vehicle> vehicles) {
        this.transportCosts = transportCosts;
        this.activityCosts = activityCosts;
        this.stateManager = stateManager;
        uniqueVehicles = getUniqueVehicles(vehicles);
    }

    private List<Vehicle> getUniqueVehicles(Collection<Vehicle> vehicles) {
        Set<VehicleTypeKey> types = new HashSet<>();
        List<Vehicle> uniqueVehicles = new ArrayList<>();
        for(Vehicle v : vehicles){
            if(!types.contains(v.getVehicleTypeIdentifier())){
                types.add(v.getVehicleTypeIdentifier());
                uniqueVehicles.add(v);
            }
        }
        return uniqueVehicles;
    }

    @Override
    public void begin(VehicleRoute route) {
        this.route = route;
        states = new HashMap<>();
        for (Vehicle v : uniqueVehicles) {
            State state = new State(v.getStartLocation(), v.getEarliestDeparture());
            states.put(v.getVehicleTypeIdentifier(), state);
        }
    }

    @Override
    public void visit(TourActivity activity) {
        for (Vehicle v : uniqueVehicles) {
            State old = states.get(v.getVehicleTypeIdentifier());
            double time = old.getTime();
            time += transportCosts.getTransportTime(old.getPrevLocation(), activity.getLocation(), time, null, v);

            double waiting = Math.max(activity.getTheoreticalEarliestOperationStartTime() - time, 0);
            stateManager.putInternalTypedActivityState(activity, v, InternalStates.WAITING, waiting);

            time += waiting + activityCosts.getActivityDuration(activity, time, null, v);
            stateManager.putInternalTypedActivityState(activity, v, InternalStates.END_TIME, time);
            states.put(v.getVehicleTypeIdentifier(),new State(activity.getLocation(),time));
        }
    }

    @Override
    public void finish() {
        for (Vehicle v : uniqueVehicles) {
            State old = states.get(v.getVehicleTypeIdentifier());
            double time = old.getTime();
            if (v.isReturnToDepot()) {
                time += transportCosts.getTransportTime(old.getPrevLocation(), v.getEndLocation(), time, null, v);
            }
            stateManager.putTypedInternalRouteState(route, v, InternalStates.END_TIME, time);
        }
    }
}
