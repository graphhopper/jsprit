/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
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
package basics.route;

import basics.Delivery;
import basics.route.TourActivity.JobActivity;

public class DeliveryActivity implements JobActivity<Delivery>{

	private Delivery delivery;
	
	private double arrTime;
	
	private double endTime;
	
	public DeliveryActivity(Delivery delivery) {
		super();
		this.delivery = delivery;
	}
	
	private DeliveryActivity(DeliveryActivity deliveryActivity){
		this.delivery=deliveryActivity.getJob();
		this.arrTime=deliveryActivity.getArrTime();
		this.endTime=deliveryActivity.getEndTime();
	}

	@Override
	public int getCapacityDemand() {
		return delivery.getCapacityDemand()*-1;
	}

	@Override
	public String getName() {
		return delivery.getType();
	}

	@Override
	public String getLocationId() {
		return delivery.getLocationId();
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
		return new DeliveryActivity(this);
	}

	@Override
	public Delivery getJob() {
		return delivery;
	}

}
