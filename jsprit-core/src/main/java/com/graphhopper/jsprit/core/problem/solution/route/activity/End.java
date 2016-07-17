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
package com.graphhopper.jsprit.core.problem.solution.route.activity;

import com.graphhopper.jsprit.core.problem.AbstractActivity;
import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.Location;

public final class End extends AbstractActivity implements TourActivity {

    public static End newInstance(String locationId, double earliestArrival, double latestArrival) {
        return new End(locationId, earliestArrival, latestArrival);
    }

    public static End copyOf(End end) {
        return new End(end);
    }

    private final static Capacity capacity = Capacity.Builder.newInstance().build();


    private double endTime = -1;


    private double theoretical_earliestOperationStartTime;

    private double theoretical_latestOperationStartTime;

    private double arrTime;

    private double routeDistance;

    private Location location;

    public void setTheoreticalEarliestOperationStartTime(double theoreticalEarliestOperationStartTime) {
        theoretical_earliestOperationStartTime = theoreticalEarliestOperationStartTime;
    }

    public void setTheoreticalLatestOperationStartTime(double theoreticalLatestOperationStartTime) {
        theoretical_latestOperationStartTime = theoreticalLatestOperationStartTime;
    }

    public End(Location location, double theoreticalStart, double theoreticalEnd) {
        super();
        this.location = location;
        theoretical_earliestOperationStartTime = theoreticalStart;
        theoretical_latestOperationStartTime = theoreticalEnd;
        endTime = theoreticalEnd;
        setIndex(-2);
    }

    public End(String locationId, double theoreticalStart, double theoreticalEnd) {
        super();
        if (locationId != null) this.location = Location.Builder.newInstance().setId(locationId).build();
        theoretical_earliestOperationStartTime = theoreticalStart;
        theoretical_latestOperationStartTime = theoreticalEnd;
        endTime = theoreticalEnd;
        setIndex(-2);
    }

    public End(End end) {
        this.location = end.getLocation();
//		this.locationId = end.getLocation().getId();
        theoretical_earliestOperationStartTime = end.getTheoreticalEarliestOperationStartTime();
        theoretical_latestOperationStartTime = end.getTheoreticalLatestOperationStartTime();
        arrTime = end.getArrTime();
        endTime = end.getEndTime();
        setIndex(-2);
    }

    public double getTheoreticalEarliestOperationStartTime() {
        return theoretical_earliestOperationStartTime;
    }

    public double getTheoreticalLatestOperationStartTime() {
        return theoretical_latestOperationStartTime;
    }

    public double getEndTime() {
        return endTime;
    }

    public void setEndTime(double endTime) {
        this.endTime = endTime;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public double getOperationTime() {
        return 0.0;
    }


    @Override
    public String toString() {
        return "[type=" + getName() + "][location=" + location
            + "][twStart=" + Activities.round(theoretical_earliestOperationStartTime)
            + "][twEnd=" + Activities.round(theoretical_latestOperationStartTime) + "]";
    }

    @Override
    public String getName() {
        return "end";
    }

    @Override
    public double getArrTime() {
        return this.arrTime;
    }

    @Override
    public void setArrTime(double arrTime) {
        this.arrTime = arrTime;

    }

    @Override
    public TourActivity duplicate() {
        return new End(this);
    }

    @Override
    public Capacity getSize() {
        return capacity;
    }

    @Override
    public double getRouteDistance() {
        return routeDistance;
    }

    @Override
    public void setRouteDistance(double routeDistance) {
        this.routeDistance = routeDistance;
    }


}
