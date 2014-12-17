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

public final class Start extends AbstractActivity implements TourActivity {

	public final static String ACTIVITY_NAME = "start";

    @Deprecated
	public static int creation;
	
	private final static Capacity capacity = Capacity.Builder.newInstance().build();
	
	public static Start newInstance(String locationId, double theoreticalStart, double theoreticalEnd){
		creation++;
		return new Start(locationId,theoreticalStart,theoreticalEnd);
	}
	
	public static Start copyOf(Start start){
		return new Start(start);
	}
	
	private String locationId;
	
	private double theoretical_earliestOperationStartTime;
	
	private double theoretical_latestOperationStartTime;

	private double endTime;

	private double arrTime;

    private Location location;

    @Deprecated
	public Start(String locationId, double theoreticalStart, double theoreticalEnd) {
		super();
		if(locationId != null) this.location = Location.Builder.newInstance().setId(locationId).build();
		this.theoretical_earliestOperationStartTime = theoreticalStart;
		this.theoretical_latestOperationStartTime = theoreticalEnd;
		this.endTime = theoreticalStart;
        setIndex(-1);
	}

    public Start(Location location, double theoreticalStart, double theoreticalEnd) {
        super();
        this.location = location;
        this.theoretical_earliestOperationStartTime = theoreticalStart;
        this.theoretical_latestOperationStartTime = theoreticalEnd;
        this.endTime = theoreticalStart;
        setIndex(-1);
    }

	private Start(Start start) {
		this.location = start.getLocation();
		theoretical_earliestOperationStartTime = start.getTheoreticalEarliestOperationStartTime();
		theoretical_latestOperationStartTime = start.getTheoreticalLatestOperationStartTime();
		endTime = start.getEndTime();
        setIndex(-1);
	}

	public double getTheoreticalEarliestOperationStartTime() {
		return theoretical_earliestOperationStartTime;
	}

    @Deprecated
	public void setLocationId(String locationId) {
		if(locationId == null) return;
        this.location = Location.Builder.newInstance().setId(locationId).build();
	}

    public void setLocation(Location location) { this.location = location; };

	public double getTheoreticalLatestOperationStartTime() {
		return theoretical_latestOperationStartTime;
	}
	
	public void setTheoreticalEarliestOperationStartTime(double time) {
		this.theoretical_earliestOperationStartTime=time;
	}

	public void setTheoreticalLatestOperationStartTime(double time) {
		this.theoretical_latestOperationStartTime=time;
	}

    @Deprecated
	@Override
	public String getLocationId() {
		if(location == null) return null;
        return location.getId();
	}

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
	public double getOperationTime() {
		return 0.0;
	}

	@Override
	public String toString() {
		return "[type="+getName()+"][location=" + location
		+ "][twStart=" + Activities.round(theoretical_earliestOperationStartTime)
		+ "][twEnd=" + Activities.round(theoretical_latestOperationStartTime) + "]";
	}
	
	@Override
	public String getName() {
		return "start";
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
		return new Start(this);
	}

	@Override
	public Capacity getSize() {
		return capacity;
	}

}
