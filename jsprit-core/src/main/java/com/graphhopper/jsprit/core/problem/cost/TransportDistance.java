package com.graphhopper.jsprit.core.problem.cost;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

/**
 * Created by schroeder on 23/12/14.
 */
public interface TransportDistance {

    public double getDistance(Location from, Location to, double departureTime, Vehicle vehicle);

}
