package jsprit.core.algorithm.recreate;

import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.vehicle.Vehicle;

/**
 * Created by schroeder on 19/05/15.
 */
class InsertActivity implements Event {

    private VehicleRoute vehicleRoute;

    private Vehicle newVehicle;

    private TourActivity activity;

    private int index;

    public InsertActivity(VehicleRoute vehicleRoute, Vehicle newVehicle, TourActivity activity, int index) {
        this.vehicleRoute = vehicleRoute;
        this.newVehicle = newVehicle;
        this.activity = activity;
        this.index = index;
    }

    public Vehicle getNewVehicle() {
        return newVehicle;
    }

    public VehicleRoute getVehicleRoute() {
        return vehicleRoute;
    }

    public TourActivity getActivity() {
        return activity;
    }

    public int getIndex() {
        return index;
    }
}
