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
import jsprit.core.problem.job.Break;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.route.activity.TourActivity.JobActivity;

public class BreakActivity extends AbstractActivity implements JobActivity {

    public static int counter = 0;

    public double arrTime;

    public double endTime;

    private Location location;

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

    protected BreakActivity(Break aBreak) {
        counter++;
        this.aBreak = aBreak;
    }

    protected BreakActivity(BreakActivity breakActivity) {
        counter++;
        this.aBreak = (Break) breakActivity.getJob();
        this.arrTime = breakActivity.getArrTime();
        this.endTime = breakActivity.getEndTime();
        this.location = breakActivity.getLocation();
        setIndex(breakActivity.getIndex());
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
        return aBreak.getTimeWindow().getStart();
    }

    public double getTheoreticalLatestOperationStartTime() {
        return aBreak.getTimeWindow().getEnd();
    }

    @Override
    public double getOperationTime() {
        return aBreak.getServiceDuration();
    }

    @Override
    public String getLocationId() {
        return aBreak.getLocation().getId();
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
