package jsprit.core.algorithm.state;

import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.ReverseActivityVisitor;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.solution.route.state.StateFactory;
import jsprit.core.problem.vehicle.Vehicle;

import java.util.Arrays;
import java.util.Collection;

public class UpdateVehicleDependentPracticalTimeWindows implements ReverseActivityVisitor, StateUpdater{

    public static interface VehiclesToUpdate {

        public Collection<Vehicle> get(VehicleRoute route);

    }

    private VehiclesToUpdate vehiclesToUpdate = new VehiclesToUpdate() {

        @Override
        public Collection<Vehicle> get(VehicleRoute route) {
            return Arrays.asList(route.getVehicle());
        }

    };

    private final StateManager stateManager;

    private final VehicleRoutingTransportCosts transportCosts;

    private VehicleRoute route;

    private double[] latest_arrTimes_at_prevAct;

    private String[] location_of_prevAct;

    private Collection<Vehicle> vehicles;

    public UpdateVehicleDependentPracticalTimeWindows(StateManager stateManager, VehicleRoutingTransportCosts tpCosts) {
        super();
        this.stateManager = stateManager;
        this.transportCosts = tpCosts;
        latest_arrTimes_at_prevAct = new double[stateManager.getMaxIndexOfVehicleTypeIdentifiers() + 1];
        location_of_prevAct = new String[stateManager.getMaxIndexOfVehicleTypeIdentifiers() + 1];
    }

    public void setVehiclesToUpdate(VehiclesToUpdate vehiclesToUpdate){
        this.vehiclesToUpdate = vehiclesToUpdate;
    }

    @Override
    public void begin(VehicleRoute route) {
        this.route = route;
        vehicles = vehiclesToUpdate.get(route);
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

