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
import com.graphhopper.jsprit.core.problem.job.Pickup;
import com.graphhopper.jsprit.core.problem.job.Service;

public final class PickupService extends AbstractActivity implements PickupActivity {

    private Service pickup;

    private double arrTime;

    private double depTime;

    private double theoreticalEarliest = 0;

    private double theoreticalLatest = Double.MAX_VALUE;

    private double softEarliest = 0.;

    private double softLatest = Double.MAX_VALUE;

    public PickupService(Pickup pickup) {
        super();
        this.pickup = pickup;
    }

    public PickupService(Service service) {
        this.pickup = service;
    }

    private PickupService(PickupService pickupActivity) {
        this.pickup = pickupActivity.getJob();
        this.arrTime = pickupActivity.getArrTime();
        this.depTime = pickupActivity.getEndTime();
        setIndex(pickupActivity.getIndex());
        this.theoreticalEarliest = pickupActivity.getTheoreticalEarliestOperationStartTime();
        this.theoreticalLatest = pickupActivity.getTheoreticalLatestOperationStartTime();
        this.softEarliest = pickupActivity.getSoftLowerBoundOperationStartTime();
        this.softLatest = pickupActivity.getSoftUpperBoundOperationStartTime();
    }

    @Override
    public String getName() {
        return pickup.getType();
    }

    @Override
    public Location getLocation() {
        return pickup.getLocation();
    }

    @Override
    public double getTheoreticalEarliestOperationStartTime() {
        return theoreticalEarliest;
    }

    @Override
    public double getTheoreticalLatestOperationStartTime() {
        return theoreticalLatest;
    }

    @Override
    public void setTheoreticalEarliestOperationStartTime(double earliest) {
        theoreticalEarliest = earliest;
        if(this.softEarliest < earliest)
        	this.softEarliest = earliest;
    }

    @Override
    public void setTheoreticalLatestOperationStartTime(double latest) {
        theoreticalLatest = latest;
        if(this.softLatest > latest)
        	this.softLatest = latest;
    }

    public void setSoftEarliestoperationStartTime(double earliest) {
    	this.softEarliest = earliest;
    	if(this.theoreticalEarliest > earliest)
    		this.theoreticalEarliest = earliest;
    }

    public void setSoftLatestOperationStartTime(double latest) {
    	this.softLatest = latest;
    	if(this.theoreticalLatest < latest)
    		this.theoreticalLatest = latest;
    }

    @Override
    public double getOperationTime() {
        return pickup.getServiceDuration();
    }

    @Override
    public double getArrTime() {
        return arrTime;
    }

    @Override
    public double getEndTime() {
        return depTime;
    }

    @Override
    public void setArrTime(double arrTime) {
        this.arrTime = arrTime;
    }

    @Override
    public void setEndTime(double endTime) {
        this.depTime = endTime;
    }

    @Override
    public TourActivity duplicate() {
        return new PickupService(this);
    }

    @Override
    public Service getJob() {
        return pickup;
    }

    public String toString() {
        return "[type=" + getName() + "][locationId=" + getLocation().getId()
            + "][size=" + getSize().toString()
            + "][twStart=" + Activities.round(getTheoreticalEarliestOperationStartTime())
            + "][twEnd=" + Activities.round(getTheoreticalLatestOperationStartTime()) + "]";
    }

    @Override
    public Capacity getSize() {
        return pickup.getSize();
    }

	@Override
	public double getSoftLowerBoundOperationStartTime() {
		return softEarliest;
	}

	@Override
	public double getSoftUpperBoundOperationStartTime() {
		return softLatest;
	}
}
