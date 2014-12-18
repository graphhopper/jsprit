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
import jsprit.core.problem.job.Delivery;

public final class DeliverService extends AbstractActivity implements DeliveryActivity{
	
	private Delivery delivery;
	
	private Capacity capacity;
	
	private double arrTime;
	
	private double endTime;
	
	public DeliverService(Delivery delivery) {
		super();
		this.delivery = delivery;
		capacity = Capacity.invert(delivery.getSize());
	}
	
	private DeliverService(DeliverService deliveryActivity){
		this.delivery=deliveryActivity.getJob();
		this.arrTime=deliveryActivity.getArrTime();
		this.endTime=deliveryActivity.getEndTime();
		capacity = deliveryActivity.getSize();
        setIndex(deliveryActivity.getIndex());
	}

	@Override
	public String getName() {
		return delivery.getType();
	}

	@Override
	public String getLocationId() {
		return delivery.getLocation().getId();
	}

    @Override
    public Location getLocation() {
        return delivery.getLocation();
    }

    @Override
	public double getTheoreticalEarliestOperationStartTime() {
		return delivery.getTimeWindow().getStart();
	}

	@Override
	public double getTheoreticalLatestOperationStartTime() {
		return delivery.getTimeWindow().getEnd();
	}

	@Override
	public double getOperationTime() {
		return delivery.getServiceDuration();
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
		return new DeliverService(this);
	}

	@Override
	public Delivery getJob() {
		return delivery;
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
