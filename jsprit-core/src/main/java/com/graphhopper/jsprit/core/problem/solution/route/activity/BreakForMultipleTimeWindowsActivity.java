package com.graphhopper.jsprit.core.problem.solution.route.activity;

import com.graphhopper.jsprit.core.problem.AbstractActivity;
import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.BreakForMultipleTimeWindows;
import com.graphhopper.jsprit.core.problem.job.Service;

public class BreakForMultipleTimeWindowsActivity extends AbstractActivity implements TourActivity.JobActivity {
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

    public static BreakForMultipleTimeWindowsActivity copyOf(BreakForMultipleTimeWindowsActivity breakActivity) {
        return new BreakForMultipleTimeWindowsActivity(breakActivity);
    }

    public static BreakForMultipleTimeWindowsActivity newInstance(BreakForMultipleTimeWindows aBreak) {
        return new BreakForMultipleTimeWindowsActivity(aBreak);
    }

    private final BreakForMultipleTimeWindows aBreak;

    private double earliest = 0;

    private double latest = Double.MAX_VALUE;

    protected BreakForMultipleTimeWindowsActivity(BreakForMultipleTimeWindows aBreak) {
        this.aBreak = aBreak;
        this.duration = aBreak.getServiceDuration();
        this.location = aBreak.getLocation();
    }

    protected BreakForMultipleTimeWindowsActivity(BreakForMultipleTimeWindowsActivity breakActivity) {
        super(breakActivity);
        this.aBreak = (BreakForMultipleTimeWindows) breakActivity.getJob();
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
        BreakForMultipleTimeWindowsActivity other = (BreakForMultipleTimeWindowsActivity) obj;
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
        return new BreakForMultipleTimeWindowsActivity(this);
    }

    @Override
    public Capacity getSize() {
        return aBreak.getSize();
    }


}
