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

public final class DeliverShipment extends AbstractActivity implements DeliveryActivity {

    private Shipment shipment;

    private double endTime;

    private double arrTime;

    private Capacity capacity;

    private double earliest = 0;

    private double latest = Double.MAX_VALUE;

    private double routeDistance;

    public DeliverShipment(Shipment shipment) {
        super();
        this.shipment = shipment;
        this.capacity = Capacity.invert(shipment.getSize());
    }

    private DeliverShipment(DeliverShipment deliveryShipmentActivity) {
        this.shipment = (Shipment) deliveryShipmentActivity.getJob();
        this.arrTime = deliveryShipmentActivity.getArrTime();
        this.endTime = deliveryShipmentActivity.getEndTime();
        this.capacity = deliveryShipmentActivity.getSize();
        setIndex(deliveryShipmentActivity.getIndex());
        this.earliest = deliveryShipmentActivity.getTheoreticalEarliestOperationStartTime();
        this.latest = deliveryShipmentActivity.getTheoreticalLatestOperationStartTime();
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
        return "deliverShipment";
    }

    @Override
    public Location getLocation() {
        return shipment.getDeliveryLocation();
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
        return shipment.getDeliveryServiceTime();
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
        return new DeliverShipment(this);
    }

    public String toString() {
        return "[type=" + getName() + "][locationId=" + getLocation().getId()
            + "][size=" + getSize().toString()
            + "][twStart=" + Activities.round(getTheoreticalEarliestOperationStartTime())
            + "][twEnd=" + Activities.round(getTheoreticalLatestOperationStartTime()) + "]";
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
