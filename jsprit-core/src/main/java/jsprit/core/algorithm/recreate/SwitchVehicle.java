package jsprit.core.algorithm.recreate;

import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.vehicle.Vehicle;

/**
 * Created by schroeder on 19/05/15.
 */
class SwitchVehicle implements Event {

    private VehicleRoute route;

    private Vehicle vehicle;

    private double departureTime;

    public SwitchVehicle(VehicleRoute route, Vehicle vehicle, double departureTime) {
        this.route = route;
        this.vehicle = vehicle;
        this.departureTime = departureTime;
    }

    public VehicleRoute getRoute() {
        return route;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public double getDepartureTime() {
        return departureTime;
    }
}
