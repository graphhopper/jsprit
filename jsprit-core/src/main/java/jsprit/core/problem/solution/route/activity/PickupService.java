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
import jsprit.core.problem.job.Pickup;
import jsprit.core.problem.job.Service;

import java.util.ArrayList;
import java.util.List;

public final class PickupService extends AbstractActivity implements PickupActivity{
	
	private Service pickup;
	
	private double arrTime;
	
	private double depTime;

	private double theoreticalEarliest;

	private double theoreticalLatest;

	private List<TimeWindow> timeWindows;
	
	public PickupService(Pickup pickup) {
		super();
		this.pickup = pickup;
		timeWindows = new ArrayList<TimeWindow>(pickup.getTimeWindows(0.));
	}
	
	public PickupService(Service service){
		this.pickup = service;
	}
	
	private PickupService(PickupService pickupActivity){
		this.pickup=pickupActivity.getJob();
		this.arrTime=pickupActivity.getArrTime();
		this.depTime=pickupActivity.getEndTime();
		this.theoreticalEarliest = pickupActivity.getTheoreticalEarliestOperationStartTime();
		this.theoreticalLatest = pickupActivity.getTheoreticalLatestOperationStartTime();
        setIndex(pickupActivity.getIndex());
		timeWindows = new ArrayList<TimeWindow>(pickup.getTimeWindows(0.));
	}

	@Override
	public void setTheoreticalEarliestOperationStartTime(double earliest) {
		this.theoreticalEarliest = earliest;
	}

	@Override
	public void setTheoreticalLatestOperationStartTime(double latest) {
		this.theoreticalLatest = latest;
	}

	@Override
	public List<TimeWindow> getTimeWindows() {
		return timeWindows;
	}

	@Override
	public String getName() {
		return pickup.getType();
	}

	@Override
	public String getLocationId() {
		return pickup.getLocation().getId();
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
		return new PickupService(this);
	}

	@Override
	public Service getJob() {
		return pickup;
	}

	public String toString() {
		return "[type="+getName()+"][locationId=" + getLocationId() 
		+ "][size=" + getSize().toString()
		+ "][twStart=" + Activities.round(getTheoreticalEarliestOperationStartTime())
		+ "][twEnd=" + Activities.round(getTheoreticalLatestOperationStartTime()) + "]";
	}

	@Override
	public Capacity getSize() {
		return pickup.getSize();
	}

}
