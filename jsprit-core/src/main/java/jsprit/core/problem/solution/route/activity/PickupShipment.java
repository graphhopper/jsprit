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
package jsprit.core.problem.solution.route.activity;

import jsprit.core.problem.AbstractActivity;
import jsprit.core.problem.Capacity;
import jsprit.core.problem.Location;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Shipment;

public final class PickupShipment extends AbstractActivity implements PickupActivity{

    private Shipment shipment;

    private double endTime;

    private double arrTime;

    private double earliest = 0;

    private double latest = Double.MAX_VALUE;

    public PickupShipment(Shipment shipment) {
        super();
        this.shipment = shipment;
    }

    @Deprecated
    public PickupShipment(PickupShipment pickupShipmentActivity) {
        this.shipment = (Shipment) pickupShipmentActivity.getJob();
        this.arrTime = pickupShipmentActivity.getArrTime();
        this.endTime = pickupShipmentActivity.getEndTime();
        setIndex(pickupShipmentActivity.getIndex());
        this.earliest = pickupShipmentActivity.getTheoreticalEarliestOperationStartTime();
        this.latest = pickupShipmentActivity.getTheoreticalLatestOperationStartTime();
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

    @Override
    public String getName() {
        return "pickupShipment";
    }

    @Override
    public String getLocationId() {
        return shipment.getPickupLocation().getId();
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
        return "[type=" + getName() + "][locationId=" + getLocationId()
            + "][size=" + getSize().toString()
            + "][twStart=" + Activities.round(getTheoreticalEarliestOperationStartTime())
            + "][twEnd=" + Activities.round(getTheoreticalLatestOperationStartTime()) + "]";
    }

    @Override
    public Capacity getSize() {
        return shipment.getSize();
    }


}
