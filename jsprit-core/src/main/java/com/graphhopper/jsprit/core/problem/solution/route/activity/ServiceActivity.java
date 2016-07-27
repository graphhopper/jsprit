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
package com.graphhopper.jsprit.core.problem.solution.route.activity;

import com.graphhopper.jsprit.core.problem.AbstractActivity;
import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.Service;

public class ServiceActivity extends AbstractActivity implements TourActivity.JobActivity {

    public double arrTime;

    public double endTime;

    private double theoreticalEarliest;

    private double theoreticalLatest;

    /**
     * @return the arrTime
     */
    public double getArrTime() {
        return arrTime;
    }

    /**
     * @param arrTime the arrTime to set
     */
    public void setArrTime(double arrTime) {
        this.arrTime = arrTime;
    }

    /**
     * @return the endTime
     */
    public double getEndTime() {
        return endTime;
    }

    /**
     * @param endTime the endTime to set
     */
    public void setEndTime(double endTime) {
        this.endTime = endTime;
    }

    public static ServiceActivity copyOf(ServiceActivity serviceActivity) {
        return new ServiceActivity(serviceActivity);
    }

    public static ServiceActivity newInstance(Service service) {
        return new ServiceActivity(service);
    }


    private final Service service;

    protected ServiceActivity(Service service) {
        this.service = service;
    }

    protected ServiceActivity(ServiceActivity serviceActivity) {
        this.service = serviceActivity.getJob();
        this.arrTime = serviceActivity.getArrTime();
        this.endTime = serviceActivity.getEndTime();
        setIndex(serviceActivity.getIndex());
        this.theoreticalEarliest = serviceActivity.getTheoreticalEarliestOperationStartTime();
        this.theoreticalLatest = serviceActivity.getTheoreticalLatestOperationStartTime();
    }


    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((service == null) ? 0 : service.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ServiceActivity other = (ServiceActivity) obj;
        if (service == null) {
            if (other.service != null)
                return false;
        } else if (!service.equals(other.service))
            return false;
        return true;
    }

    public double getTheoreticalEarliestOperationStartTime() {
        return theoreticalEarliest;
    }

    public double getTheoreticalLatestOperationStartTime() {
        return theoreticalLatest;
    }

    @Override
    public void setTheoreticalEarliestOperationStartTime(double earliest) {
        theoreticalEarliest = earliest;
    }

    @Override
    public void setTheoreticalLatestOperationStartTime(double latest) {
        theoreticalLatest = latest;
    }

    @Override
    public double getOperationTime() {
        return service.getServiceDuration();
    }

    @Override
    public Location getLocation() {
        return service.getLocation();
    }


    @Override
    public Service getJob() {
        return service;
    }


    @Override
    public String toString() {
        return "[type=" + getName() + "][locationId=" + getLocation().getId()
            + "][size=" + getSize().toString()
            + "][twStart=" + Activities.round(getTheoreticalEarliestOperationStartTime())
            + "][twEnd=" + Activities.round(getTheoreticalLatestOperationStartTime()) + "]";
    }

    @Override
    public String getName() {
        return service.getType();
    }

    @Override
    public TourActivity duplicate() {
        return new ServiceActivity(this);
    }

    @Override
    public Capacity getSize() {
        return service.getSize();
    }


}
