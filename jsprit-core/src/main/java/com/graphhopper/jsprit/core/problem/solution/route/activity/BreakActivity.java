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
import com.graphhopper.jsprit.core.problem.job.Break;
import com.graphhopper.jsprit.core.problem.job.Service;

public class BreakActivity extends AbstractActivity implements TourActivity.JobActivity {

    public static int counter = 0;

    public double arrTime;

    public double endTime;

    private Location location;

    private double duration;

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

    public static BreakActivity copyOf(BreakActivity breakActivity) {
        return new BreakActivity(breakActivity);
    }

    public static BreakActivity newInstance(Break aBreak) {
        return new BreakActivity(aBreak);
    }

    private final Break aBreak;

    private double earliest = 0;

    private double latest = Double.MAX_VALUE;

    protected BreakActivity(Break aBreak) {
        counter++;
        this.aBreak = aBreak;
        this.duration = aBreak.getServiceDuration();
    }

    protected BreakActivity(BreakActivity breakActivity) {
        counter++;
        this.aBreak = (Break) breakActivity.getJob();
        this.arrTime = breakActivity.getArrTime();
        this.endTime = breakActivity.getEndTime();
        this.location = breakActivity.getLocation();
        setIndex(breakActivity.getIndex());
        this.earliest = breakActivity.getTheoreticalEarliestOperationStartTime();
        this.latest = breakActivity.getTheoreticalLatestOperationStartTime();
        this.duration = breakActivity.getOperationTime();
    }


    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((aBreak == null) ? 0 : aBreak.hashCode());
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
        BreakActivity other = (BreakActivity) obj;
        if (aBreak == null) {
            if (other.aBreak != null)
                return false;
        } else if (!aBreak.equals(other.aBreak))
            return false;
        return true;
    }

    public double getTheoreticalEarliestOperationStartTime() {
        return earliest;
    }

    public double getTheoreticalLatestOperationStartTime() {
        return latest;
    }

    @Override
    public double getOperationTime() {
        return duration;
    }

    public void setOperationTime(double duration){
        this.duration = duration;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    public void setLocation(Location breakLocation) {
        this.location = breakLocation;
    }

    @Override
    public Service getJob() {
        return aBreak;
    }


    @Override
    public String toString() {
        return "[type=" + getName() + "][location=" + getLocation()
            + "][size=" + getSize().toString()
            + "][twStart=" + Activities.round(getTheoreticalEarliestOperationStartTime())
            + "][twEnd=" + Activities.round(getTheoreticalLatestOperationStartTime()) + "]";
    }

    @Override
    public void setTheoreticalEarliestOperationStartTime(double earliest) {
        this.earliest = earliest;
    }

    @Override
    public void setTheoreticalLatestOperationStartTime(double latest) {
        this.latest = latest;
    }

    @Override
    public String getName() {
        return aBreak.getType();
    }

    @Override
    public TourActivity duplicate() {
        return new BreakActivity(this);
    }

    @Override
    public Capacity getSize() {
        return aBreak.getSize();
    }


}
