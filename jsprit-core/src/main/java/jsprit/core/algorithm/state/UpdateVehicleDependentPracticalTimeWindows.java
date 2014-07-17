package jsprit.core.algorithm.state;

import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.ReverseActivityVisitor;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.solution.route.state.StateFactory;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleFleetManager;

import java.util.ArrayList;
import java.util.Collection;

public class UpdateVehicleDependentPracticalTimeWindows implements ReverseActivityVisitor, StateUpdater{

    private final StateManager stateManager;

    private final VehicleRoutingTransportCosts transportCosts;

    private final VehicleFleetManager fleetManager;

    private final boolean vehicleSwitchAllowed;

    private VehicleRoute route;

    private double[] latest_arrTimes_at_prevAct;

    private String[] location_of_prevAct;

    private Collection<Vehicle> vehicles;


    public UpdateVehicleDependentPracticalTimeWindows(StateManager stateManager, VehicleRoutingTransportCosts tpCosts, VehicleFleetManager fleetManager, boolean isVehicleSwitchAllowed) {
        super();
        this.stateManager = stateManager;
        this.transportCosts = tpCosts;
        this.fleetManager = fleetManager;
        this.vehicleSwitchAllowed=isVehicleSwitchAllowed;
        int maxVehicleTypeIndex = getMaxTypeIndex(fleetManager);
        latest_arrTimes_at_prevAct = new double[maxVehicleTypeIndex];
        location_of_prevAct = new String[maxVehicleTypeIndex];
    }

    private int getMaxTypeIndex(VehicleFleetManager fleetManager) {
        return 0;
    }

    @Override
    public void begin(VehicleRoute route) {
        this.route = route;
        vehicles = new ArrayList<Vehicle>();
        if(vehicleSwitchAllowed) vehicles.addAll(fleetManager.getAvailableVehicles(route.getVehicle()));
        vehicles.add(route.getVehicle());
        for(Vehicle vehicle : vehicles){
            latest_arrTimes_at_prevAct[vehicle.getVehicleTypeIdentifier().getIndex()] = vehicle.getLatestArrival();
            location_of_prevAct[vehicle.getVehicleTypeIdentifier().getIndex()] = vehicle.getEndLocationId();
        }
    }

    @Override
    public void visit(TourActivity activity) {
        for(Vehicle vehicle : vehicles){
            double latestArrTimeAtPrevAct = latest_arrTimes_at_prevAct[vehicle.getVehicleTypeIdentifier().getIndex()];
            String prevLocation = location_of_prevAct[vehicle.getVehicleTypeIdentifier().getIndex()];
            double potentialLatestArrivalTimeAtCurrAct = latestArrTimeAtPrevAct - transportCosts.getBackwardTransportTime(activity.getLocationId(), prevLocation,
                    latestArrTimeAtPrevAct, route.getDriver(), vehicle) - activity.getOperationTime();
            double latestArrivalTime = Math.min(activity.getTheoreticalLatestOperationStartTime(), potentialLatestArrivalTimeAtCurrAct);
            stateManager.putActivityState(activity, vehicle, StateFactory.LATEST_OPERATION_START_TIME, latestArrivalTime);
            latest_arrTimes_at_prevAct[vehicle.getVehicleTypeIdentifier().getIndex()] = latestArrivalTime;
            location_of_prevAct[vehicle.getVehicleTypeIdentifier().getIndex()] = activity.getLocationId();
        }
    }

    @Override
    public void finish() {}
}

