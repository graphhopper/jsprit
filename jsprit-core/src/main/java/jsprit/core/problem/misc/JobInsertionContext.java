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
package jsprit.core.problem.misc;

import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.vehicle.Vehicle;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides context information about a particular job insertion.
 *
 */
public class JobInsertionContext {

    /**
     * Provides insertion context information about a particular activity.
     */
    public static class ActivityContext {

        private double arrivalTime;

        private double endTime;

        private int insertionIndex;

        /**
         * Returns arrival time at associated activity.
         *
         * @return arrival time
         */
        public double getArrivalTime() {
            return arrivalTime;
        }

        /**
         * Returns end time of associated activity.
         *
         * @return end time
         */
        public double getEndTime() {
            return endTime;
        }

        /**
         * Returns the insertion index of the associated vehicle.
         *
         * <p>The associated activity is not inserted yet. The actual insertion position is still to be evaluated.
         * Thus this insertion index is related to the potential insertion index which is the position before
         * the activity at this index in the existing route.
         *
         * if insertionIndex == 0, the associated activity will be inserted between start of vehicle and the first
         * activity in activity sequence.
         *
         * if insertionIndex == relatedRoute.getActivities().size(), the associated activity will be inserted between
         * the last activity in the activity sequence and the end of vehicle.
         *
         * @return insertion index
         */
        public int getInsertionIndex() {
            return insertionIndex;
        }

        /**
         * Sets arrivalTime of associated vehicle at activity.
         *
         * @param arrivalTime arrival time of associated vehicle at activity
         */
        public void setArrivalTime(double arrivalTime) {
            this.arrivalTime = arrivalTime;
        }

        /**
         * Sets end time of associated activity.
         *
         * @param endTime end time
         */
        public void setEndTime(double endTime) {
            this.endTime = endTime;
        }

        /**
         * Sets insertion index of associated activity.
         *
         * @param insertionIndex insertion index of associated activity
         */
        public void setInsertionIndex(int insertionIndex) {
            this.insertionIndex = insertionIndex;
        }
    }
	
	private VehicleRoute route;

	private Job job;

	private Vehicle newVehicle;

	private Driver newDriver;

	private double newDepTime;

    private List<TourActivity> associatedActivities = new ArrayList<TourActivity>();

    private ActivityContext pickupContext;
	
	/**
     * Returns the existing route where the .getJob() needs to be inserted in.
     *
	 * @return the route
	 */
	public VehicleRoute getRoute() {
		return route;
	}

	/**
     * Returns the job that needs to be inserted.
     *
	 * @return the job
	 */
	public Job getJob() {
		return job;
	}

	/**
     * Returns the vehicle that should operate the new route, i.e. route <code>this.getRoute()</code> + new job <code>this.getJob()</code>.
     *
	 * @return the newVehicle
	 */
	public Vehicle getNewVehicle() {
		return newVehicle;
	}

	/**
     * Returns the driver that should operate the new route, i.e. route <code>this.getRoute()</code> + new job <code>this.getJob()</code>.
     *
     * <p>Currently the driver is just a mock, it has no functions</p>
     *
	 * @return the newDriver
	 */
	public Driver getNewDriver() {
		return newDriver;
	}

	/**
     * Returns the new departure time at the new vehicle's start location.
     *
	 * @return the newDepTime
	 */
	public double getNewDepTime() {
		return newDepTime;
	}

    /**
     * Constructs the context.
     *
     * @param route the existing route where the job needs to be inserted in
     * @param job the job to be inserted
     * @param newVehicle the new vehicle that should operate the new route
     * @param newDriver the new driver that should operate the new route
     * @param newDepTime the new departure time at the new vehicle's start location
     */
	public JobInsertionContext(VehicleRoute route, Job job, Vehicle newVehicle, Driver newDriver, double newDepTime) {
		super();
		this.route = route;
		this.job = job;
		this.newVehicle = newVehicle;
		this.newDriver = newDriver;
		this.newDepTime = newDepTime;
	}

    public List<TourActivity> getAssociatedActivities() {
        return associatedActivities;
    }

    /**
     * Sets pickup context.
     *
     * @param pickupContext pickup context
     */
    public void setRelatedActivityContext(ActivityContext pickupContext){
        this.pickupContext = pickupContext;
    }

    /**
     * Returns pickup context. If no context available, returns null.
     *
     * @return pickup context
     */
    public ActivityContext getRelatedActivityContext(){
        return this.pickupContext;
    }
}
