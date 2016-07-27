/*
 * Licensed to GraphHopper GmbH under one or more contributor
 * license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * GraphHopper GmbH licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.graphhopper.jsprit.core.problem.misc;

import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides context information about a particular job insertion.
 */
public class JobInsertionContext {

    private VehicleRoute route;

    private Job job;

    private Vehicle newVehicle;

    private Driver newDriver;

    private double newDepTime;

    private List<TourActivity> associatedActivities = new ArrayList<TourActivity>();

    private ActivityContext activityContext;

    private ActivityContext relatedActivityContext;

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
     * <p>
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
     * @param route      the existing route where the job needs to be inserted in
     * @param job        the job to be inserted
     * @param newVehicle the new vehicle that should operate the new route
     * @param newDriver  the new driver that should operate the new route
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
     * @param relatedActivityContext pickup context
     */
    public void setRelatedActivityContext(ActivityContext relatedActivityContext) {
        this.relatedActivityContext = relatedActivityContext;
    }

    /**
     * Returns pickup context. If no context available, returns null.
     *
     * @return pickup context
     */
    public ActivityContext getRelatedActivityContext() {
        return this.relatedActivityContext;
    }

    public void setActivityContext(ActivityContext activityContext){
        this.activityContext = activityContext;
    }

    public ActivityContext getActivityContext(){
        return this.activityContext;
    }
}
