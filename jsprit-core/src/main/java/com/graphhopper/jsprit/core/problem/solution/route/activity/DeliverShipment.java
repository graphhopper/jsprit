/*
 * Licensed to GraphHopper GmbH under one or more contributor
 * license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * GraphHopper GmbH licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
}
