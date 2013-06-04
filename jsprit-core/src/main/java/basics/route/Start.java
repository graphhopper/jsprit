/*******************************************************************************
 * Copyright (c) 2011 Stefan Schroeder.
 * eMail: stefan.schroeder@kit.edu
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package basics.route;

import util.Coordinate;

public final class Start implements TourActivity {

	public final static String ACTIVITY_NAME = "start";
	
	public static int creation;
	
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
	
	

	public Coordinate getCoordinate() {
		return coordinate;
	}

	public void setCoordinate(Coordinate coordinate) {
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
		+ "][twStart=" + round(theoretical_earliestOperationStartTime)
		+ "][twEnd=" + round(theoretical_latestOperationStartTime) + "]";
	}
	
	private String round(double time) {
		if (time == Double.MAX_VALUE) {
			return "oo";
		}
		return "" + Math.round(time);
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

	

}
