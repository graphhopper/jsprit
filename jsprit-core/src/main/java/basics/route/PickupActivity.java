/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package basics.route;

import basics.Pickup;
import basics.route.TourActivity.JobActivity;

public class PickupActivity implements JobActivity<Pickup>{
 
	private Pickup pickup;
	
	private double arrTime;
	
	private double depTime;
	
	public PickupActivity(Pickup pickup) {
		super();
		this.pickup = pickup;
	}
	
	private PickupActivity(PickupActivity pickupActivity){
		this.pickup=pickupActivity.getJob();
		this.arrTime=pickupActivity.getArrTime();
		this.depTime=pickupActivity.getEndTime();
	}

	@Override
	public String getName() {
		return pickup.getType();
	}

	@Override
	public String getLocationId() {
		return pickup.getLocationId();
	}

	@Override
	public double getTheoreticalEarliestOperationStartTime() {
		return pickup.getTimeWindow().getStart();
	}

	@Override
	public double getTheoreticalLatestOperationStartTime() {
		return pickup.getTimeWindow().getEnd();
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
		this.arrTime=arrTime;
	}

	@Override
	public void setEndTime(double endTime) {
		this.depTime=endTime;
	}

	@Override
	public TourActivity duplicate() {
		return new PickupActivity(this);
	}

	@Override
	public Pickup getJob() {
		return pickup;
	}

	@Override
	public int getCapacityDemand() {
		return pickup.getCapacityDemand();
	}

	
}
