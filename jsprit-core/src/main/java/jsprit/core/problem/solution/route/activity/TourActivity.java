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

import jsprit.core.problem.Capacity;
import jsprit.core.problem.HasIndex;
import jsprit.core.problem.Location;
import jsprit.core.problem.job.Job;

/**
 * Basic interface for tour-activities.
 * 
 * <p>A tour activity is the basic element of a tour, which is consequently a sequence of tour-activities.
 * 
 * @author schroeder
 *
 */
public interface TourActivity extends HasIndex {

	/**
	 * Basic interface of job-activies.
	 * 
	 * <p>A job activity is related to a {@link Job}.
	 * 
	 * @author schroeder
	 *
	 */
	public interface JobActivity extends TourActivity {
		
		/**
		 * Returns the job that is involved with this activity.
		 * 
		 * @return job
		 */
		public Job getJob();
		
	}
	
	/**
	 * Returns the name of this activity.
	 * 
	 * @return name
	 */
	public abstract String getName();
	
	/**
	 * Returns the activity's locationId.
	 * 
	 * @return locationId
     * @deprecated use location
	 */
    @Deprecated
	public abstract String getLocationId();

    /**
     * Returns location.
     *
     * @return location
     */
    public abstract Location getLocation();
	
	/**
	 * Returns the theoretical earliest operation start time, which is the time that is just allowed 
	 * (not earlier) to start this activity, that is for example <code>service.getTimeWindow().getStart()</code>.
	 * 
	 * @return earliest start time
	 */
	public abstract double getTheoreticalEarliestOperationStartTime();
	
	/**
	 * Returns the theoretical latest operation start time, which is the time that is just allowed 
	 * (not later) to start this activity, that is for example <code>service.getTimeWindow().getEnd()</code>.
	 * 
	 * 
	 * @return latest start time
	 */
	public abstract double getTheoreticalLatestOperationStartTime();

	/**
	 * Returns the operation-time this activity takes.
	 * 
	 * <p>Note that this is not necessarily the duration of this activity, but the 
	 * service time a pickup/delivery actually takes, that is for example <code>service.getServiceTime()</code>.
	 *  
	 * @return operation time
	 */
	public abstract double getOperationTime();
	
	/**
	 * Returns the arrival-time of this activity.
	 * 
	 * @return arrival time
	 */
	public abstract double getArrTime();
	
	/**
	 * Returns end-time of this activity.
	 * 
	 * @return end time
	 */
	public abstract double getEndTime();
	
	/**
	 * Sets the arrival time of that activity.
	 * 
	 * @param arrTime
	 */
	public abstract void setArrTime(double arrTime);
	
	/**
	 * Sets the end-time of this activity.
	 * 
	 * @param endTime
	 */
	public abstract void setEndTime(double endTime);
	
	/**
	 * Returns the capacity-demand of that activity, in terms of what needs to be loaded or unloaded at
	 * this activity.
	 * 
	 * @return capacity
	 */
	public abstract Capacity getSize();
	
	/**
	 * Makes a deep copy of this activity.
	 * 
	 * @return copied activity
	 */
	public abstract TourActivity duplicate();
		
}
