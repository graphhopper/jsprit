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

    private double softEarliest = 0.;

    private double softLatest = Double.MAX_VALUE;


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
        this.softEarliest = serviceActivity.getSoftLowerBoundOperationStartTime();
        this.softLatest = serviceActivity.getSoftUpperBoundOperationStartTime();
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
        if(this.softEarliest < earliest)
            this.softEarliest = earliest;
    }

    @Override
    public void setTheoreticalLatestOperationStartTime(double latest) {
        theoreticalLatest = latest;
        if(this.softLatest > latest)
            this.softLatest = latest;
    }

    public void setSoftEarliestoperationStartTime(double earliest) {
        this.softEarliest = earliest;
        if(this.theoreticalEarliest > earliest)
    	    this.theoreticalEarliest = earliest;
    }

    public void setSoftLatestOperationStartTime(double latest) {
        this.softLatest = latest;
        if(this.theoreticalLatest < latest)
    	    this.theoreticalLatest = latest;
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

	@Override
	public double getSoftLowerBoundOperationStartTime() {
        return softEarliest;
	}

	@Override
	public double getSoftUpperBoundOperationStartTime() {
        return softLatest;
	}

}
