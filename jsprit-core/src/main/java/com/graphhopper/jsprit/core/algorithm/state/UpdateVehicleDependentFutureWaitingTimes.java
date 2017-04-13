package com.graphhopper.jsprit.core.algorithm.state;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.BreakActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ReverseActivityVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeKey;

import java.util.*;

/**
 * Created by hehuang on 4/10/17.
 */
public class UpdateVehicleDependentFutureWaitingTimes implements ReverseActivityVisitor, StateUpdater {

    private final StateManager stateManager;

    private List<Vehicle> uniqueVehicles;

    private Map<VehicleTypeKey, Double> states;

    public UpdateVehicleDependentFutureWaitingTimes(StateManager stateManager,
                                                    Collection<Vehicle> vehicles) {
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
        states = new HashMap<>();
        for (Vehicle v : uniqueVehicles) {
            states.put(v.getVehicleTypeIdentifier(), 0.);
        }
    }

    @Override
    public void visit(TourActivity activity) {
        for (Vehicle v : uniqueVehicles) {
            double futureWaiting = states.get(v.getVehicleTypeIdentifier());
            stateManager.putInternalTypedActivityState(activity, v, InternalStates.FUTURE_WAITING, futureWaiting);
            if(!(activity instanceof BreakActivity)) {
                futureWaiting += stateManager.getActivityState(activity, v, InternalStates.WAITING, Double.class);
            }
            states.put(v.getVehicleTypeIdentifier(), futureWaiting);
        }
    }

    @Override
    public void finish() {

    }
}
