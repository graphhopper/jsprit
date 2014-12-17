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
package jsprit.core.util;



import jsprit.core.problem.Location;
import jsprit.core.problem.cost.AbstractForwardVehicleRoutingTransportCosts;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.vehicle.Vehicle;

/**
 * 
 * @author stefan schroeder
 * 
 */

public class GreatCircleCosts extends AbstractForwardVehicleRoutingTransportCosts {

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
     * @param detour
     */
    public void setDetour(double detour) {
        this.detour = detour;
    }

    private Locations locations;

    private DistanceUnit distanceUnit = DistanceUnit.Kilometer;

	public GreatCircleCosts(Locations locations) {
		super();
		this.locations = locations;
	}


    public GreatCircleCosts(Locations locations, DistanceUnit distanceUnit) {
        super();
        this.locations = locations;
        this.distanceUnit = distanceUnit;
    }

	@Override
	public double getTransportCost(Location from, Location to, double time,Driver driver, Vehicle vehicle) {
        double distance;
        try {
            distance = getDistance(from.getId(), to.getId());
        } catch (NullPointerException e) {
            throw new NullPointerException("cannot calculate euclidean distance. coordinates are missing. either add coordinates or use another transport-cost-calculator.");
        }
        double costs = distance;
        if(vehicle != null){
            if(vehicle.getType() != null){
                costs = distance * vehicle.getType().getVehicleCostParams().perDistanceUnit;
            }
        }
        return costs;
	}

	@Override
	public double getTransportTime(Location from, Location to, double time, Driver driver, Vehicle vehicle) {
		return getDistance(from.getId(), to.getId()) / speed;
	}

    public double getDistance(String fromId, String toId) {
		Coordinate fromCoordinate = locations.getCoord(fromId);
        Coordinate toCoordinate = locations.getCoord(toId);
        return GreatCircleDistanceCalculator.calculateDistance(fromCoordinate, toCoordinate, distanceUnit) * detour;
	}

}
