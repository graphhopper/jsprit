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

public final class DeliverShipment extends AbstractActivity implements DeliveryActivity{

	private Shipment shipment;
	
	private double endTime;
	
	private double arrTime;
	
	private Capacity capacity;
	
	public DeliverShipment(Shipment shipment) {
		super();
		this.shipment = shipment;
		this.capacity = Capacity.invert(shipment.getSize());
	}

    @Deprecated
	public DeliverShipment(DeliverShipment deliveryShipmentActivity) {
		this.shipment = (Shipment) deliveryShipmentActivity.getJob();
		this.arrTime = deliveryShipmentActivity.getArrTime();
		this.endTime = deliveryShipmentActivity.getEndTime();
		this.capacity = deliveryShipmentActivity.getSize();
        setIndex(deliveryShipmentActivity.getIndex());
	}

	@Override
	public Job getJob() {
		return shipment;
	}

	@Override
	public String getName() {
		return "deliverShipment";
	}

	@Override
	public String getLocationId() {
		return shipment.getDeliveryLocation().getId();
	}

    @Override
    public Location getLocation() {
        return shipment.getDeliveryLocation();
    }

    @Override
	public double getTheoreticalEarliestOperationStartTime() {
		return shipment.getDeliveryTimeWindow().getStart();
	}

	@Override
	public double getTheoreticalLatestOperationStartTime() {
		return shipment.getDeliveryTimeWindow().getEnd();
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
		this.arrTime=arrTime;
	}

	@Override
	public void setEndTime(double endTime) {
		this.endTime=endTime;
	}

	@Override
	public TourActivity duplicate() {
		return new DeliverShipment(this);
	}
	
	public String toString() {
		return "[type="+getName()+"][locationId=" + getLocationId() 
		+ "][size=" + getSize().toString()
		+ "][twStart=" + Activities.round(getTheoreticalEarliestOperationStartTime())
		+ "][twEnd=" + Activities.round(getTheoreticalLatestOperationStartTime()) + "]";
	}

	@Override
	public Capacity getSize() {
		return capacity;
	}
}
