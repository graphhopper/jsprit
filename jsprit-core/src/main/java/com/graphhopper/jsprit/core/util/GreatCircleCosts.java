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
package com.graphhopper.jsprit.core.util;


import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.cost.AbstractForwardVehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.cost.TransportDistance;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

/**
 * @author stefan schroeder
 */

public class GreatCircleCosts extends AbstractForwardVehicleRoutingTransportCosts implements TransportDistance {

    private double speed = 1.;

    private double detour = 1.;

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    /**
     * Sets the detour factor.
     * <p>
     * The distance is calculated by the great circle distance * detour factor.
     * </p>
     *
     * @param detour
     */
    public void setDetour(double detour) {
        this.detour = detour;
    }

    private DistanceUnit distanceUnit = DistanceUnit.Kilometer;

   public GreatCircleCosts() {
        super();
    }

    public GreatCircleCosts(DistanceUnit distanceUnit) {
        super();
        this.distanceUnit = distanceUnit;
    }


    @Override
    public double getTransportCost(Location from, Location to, double time, Driver driver, Vehicle vehicle) {
        double distance;
        try {
            distance = calculateDistance(from, to);
        } catch (NullPointerException e) {
            throw new NullPointerException("cannot calculate euclidean distance. coordinates are missing. either add coordinates or use another transport-cost-calculator.");
        }
        double costs = distance;
        if (vehicle != null) {
            if (vehicle.getType() != null) {
                costs = distance * vehicle.getType().getVehicleCostParams().perDistanceUnit;
            }
        }
        return costs;
    }

    private double calculateDistance(Location fromLocation, Location toLocation) {
        Coordinate from = null;
        Coordinate to = null;
        if (fromLocation.getCoordinate() != null && toLocation.getCoordinate() != null) {
            from = fromLocation.getCoordinate();
            to = toLocation.getCoordinate();
        }
        if (from == null || to == null) throw new NullPointerException("either from or to location is null");
        return GreatCircleDistanceCalculator.calculateDistance(from, to, distanceUnit) * detour;
    }

    @Override
    public double getTransportTime(Location from, Location to, double time, Driver driver, Vehicle vehicle) {
    	double timeShift = 0;
        double coef = 1.0;
        if(vehicle != null)
        	coef = vehicle.getCoefSetupTime();
    	if(from != to && from.getIndex() >= 0 && to.getIndex() >=0){
    		timeShift = to.getSetupTime() * coef;
    	}
        return timeShift + calculateDistance(from, to) / speed;
    }

    @Override
    public double getDistance(Location from, Location to) {
        return calculateDistance(from, to);
    }
}
