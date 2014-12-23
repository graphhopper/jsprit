package jsprit.core.problem.cost;

import jsprit.core.problem.Location;

/**
 * Created by schroeder on 23/12/14.
 */
public interface TransportDistance {

    public double getDistance(Location from, Location to);

}
