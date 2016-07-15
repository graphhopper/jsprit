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
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Shipment;

public final class PickupShipment extends AbstractActivity implements PickupActivity{

    private Shipment shipment;

    private double endTime;

    private double arrTime;

    public double readyTime;

    private double earliest = 0;

    private double latest = Double.MAX_VALUE;

    private double setup = 0;

    public PickupShipment(Shipment shipment) {
        super();
        this.shipment = shipment;
        this.setup = shipment.getPickupSetupTime();
    }

    private PickupShipment(PickupShipment pickupShipmentActivity) {
        this.shipment = (Shipment) pickupShipmentActivity.getJob();
        this.arrTime = pickupShipmentActivity.getArrTime();
        this.readyTime = pickupShipmentActivity.getReadyTime();
        this.endTime = pickupShipmentActivity.getEndTime();
        setIndex(pickupShipmentActivity.getIndex());
        this.earliest = pickupShipmentActivity.getTheoreticalEarliestOperationStartTime();
        this.latest = pickupShipmentActivity.getTheoreticalLatestOperationStartTime();
        this.setup = pickupShipmentActivity.getSetupTime();
    }

    @Override
    public Job getJob() {
        return shipment;
    }

    @Override
    public void setTheoreticalEarliestOperationStartTime(double earliest) {
        this.earliest = earliest;
    }

    @Override
    public void setTheoreticalLatestOperationStartTime(double latest) {
        this.latest = latest;
    }

    public void setSetupTime(double setup) {
    	this.setup = setup;
    }

    @Override
    public String getName() {
        return "pickupShipment";
    }

    @Override
    public Location getLocation() {
        return shipment.getPickupLocation();
    }

    @Override
    public double getTheoreticalEarliestOperationStartTime() {
        return earliest;
    }

    @Override
    public double getTheoreticalLatestOperationStartTime() {
        return latest;
    }

    public double getSetupTime() {
    	return setup;
    }

    @Override
    public double getOperationTime() {
        return shipment.getPickupServiceTime();
    }

    @Override
    public double getArrTime() {
        return arrTime;
    }

    @Override
    public double getEndTime() {
        return endTime;
    }

    @Override
    public void setArrTime(double arrTime) {
        this.arrTime = arrTime;
    }

    @Override
    public void setEndTime(double endTime) {
        this.endTime = endTime;
    }

    @Override
    public TourActivity duplicate() {
        return new PickupShipment(this);
    }

    public String toString() {
        return "[type=" + getName() + "][locationId=" + getLocation().getId()
            + "][size=" + getSize().toString()
            + "][twStart=" + Activities.round(getTheoreticalEarliestOperationStartTime())
            + "][twEnd=" + Activities.round(getTheoreticalLatestOperationStartTime())
            + "][Setup=" + Activities.round(getSetupTime()) + "]";
    }

    @Override
    public Capacity getSize() {
        return shipment.getSize();
    }

    @Override
    public double getReadyTime() {
        return readyTime;
    }

    @Override
    public void setReadyTime(double readyTime) {
        this.readyTime = readyTime;
    }


}
