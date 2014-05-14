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
package jsprit.core.problem.solution.route.activity;

import jsprit.core.problem.Capacity;
import jsprit.core.util.Coordinate;

public final class Start implements TourActivity {

	public final static String ACTIVITY_NAME = "start";
	
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
	
	private Coordinate coordinate;
	
	private double theoretical_earliestOperationStartTime;
	
	private double theoretical_latestOperationStartTime;

	private double endTime;

	private double arrTime; 

	public Start(String locationId, double theoreticalStart, double theoreticalEnd) {
		super();
		this.locationId = locationId;
		this.theoretical_earliestOperationStartTime = theoreticalStart;
		this.theoretical_latestOperationStartTime = theoreticalEnd;
		this.endTime = theoreticalStart;
		
	}

	private Start(Start start) {
		this.locationId = start.getLocationId();
		theoretical_earliestOperationStartTime = start.getTheoreticalEarliestOperationStartTime();
		theoretical_latestOperationStartTime = start.getTheoreticalLatestOperationStartTime();
		endTime = start.getEndTime();
	}

	@Deprecated
	Coordinate getCoordinate() {
		return coordinate;
	}

	@Deprecated
	void setCoordinate(Coordinate coordinate) {
		this.coordinate = coordinate;
	}

	public double getTheoreticalEarliestOperationStartTime() {
		return theoretical_earliestOperationStartTime;
	}

	public void setLocationId(String locationId) {
		this.locationId = locationId;
	}

	public double getTheoreticalLatestOperationStartTime() {
		return theoretical_latestOperationStartTime;
	}
	
	public void setTheoreticalEarliestOperationStartTime(double time) {
		this.theoretical_earliestOperationStartTime=time;
	}

	public void setTheoreticalLatestOperationStartTime(double time) {
		this.theoretical_latestOperationStartTime=time;
	}

	@Override
	public String getLocationId() {
		return locationId;
	}

	@Override
	public double getOperationTime() {
		return 0.0;
	}

	@Override
	public String toString() {
		return "[type="+getName()+"][locationId=" + getLocationId() 
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
	public int getCapacityDemand() {
		return 0;
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
