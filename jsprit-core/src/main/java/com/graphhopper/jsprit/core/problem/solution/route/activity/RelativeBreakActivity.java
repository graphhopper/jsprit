//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.graphhopper.jsprit.core.problem.solution.route.activity;

import com.graphhopper.jsprit.core.problem.AbstractActivity;
import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.RelativeBreak;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity.JobActivity;

public class RelativeBreakActivity extends AbstractActivity implements JobActivity {
    public double arrTime;
    public double endTime;
    private Location location;
    private double duration;
    private final RelativeBreak aBreak;
    private double earliest = 0.0D;
    private double latest = 1.7976931348623157E308D;

    public double getArrTime() {
        return this.arrTime;
    }

    public void setArrTime(double arrTime) {
        this.arrTime = arrTime;
    }

    public double getEndTime() {
        return this.endTime;
    }

    public void setEndTime(double endTime) {
        this.endTime = endTime;
    }

    public static RelativeBreakActivity copyOf(RelativeBreakActivity breakActivity) {
        return new RelativeBreakActivity(breakActivity);
    }

    public static RelativeBreakActivity newInstance(RelativeBreakActivity aBreak) {
        return new RelativeBreakActivity(aBreak);
    }

    protected RelativeBreakActivity(RelativeBreak aBreak) {
        this.aBreak = aBreak;
        this.duration = aBreak.getServiceDuration();
        this.location = aBreak.getLocation();
    }

    protected RelativeBreakActivity(RelativeBreakActivity breakActivity) {
        super(breakActivity);
        this.aBreak = (RelativeBreak)breakActivity.getJob();
        this.arrTime = breakActivity.getArrTime();
        this.endTime = breakActivity.getEndTime();
        this.location = breakActivity.getLocation();
        this.setIndex(breakActivity.getIndex());
        this.earliest = breakActivity.getTheoreticalEarliestOperationStartTime();
        this.latest = breakActivity.getTheoreticalLatestOperationStartTime();
        this.duration = breakActivity.getOperationTime();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (this.getClass() != obj.getClass()) {
            return false;
        } else {
            RelativeBreakActivity other = (RelativeBreakActivity)obj;
            if (this.aBreak == null) {
                if (other.aBreak != null) {
                    return false;
                }
            } else if (!this.aBreak.equals(other.aBreak)) {
                return false;
            }

            return true;
        }
    }

    public double getTheoreticalEarliestOperationStartTime() {
        return this.earliest;
    }

    public double getTheoreticalLatestOperationStartTime() {
        return this.latest;
    }

    public double getOperationTime() {
        return this.duration;
    }

    public void setOperationTime(double duration) {
        this.duration = duration;
    }

    public Location getLocation() {
        return this.location;
    }

    public void setLocation(Location breakLocation) {
        this.location = breakLocation;
    }

    public Service getJob() {
        return this.aBreak;
    }

    public String toString() {
        return "[type=" + this.getName() + "][location=" + this.getLocation() + "][size=" + this.getSize().toString() + "][twStart=" + Activities.round(this.getTheoreticalEarliestOperationStartTime()) + "][twEnd=" + Activities.round(this.getTheoreticalLatestOperationStartTime()) + "]";
    }

    public void setTheoreticalEarliestOperationStartTime(double earliest) {
        this.earliest = earliest;
    }

    public void setTheoreticalLatestOperationStartTime(double latest) {
        this.latest = latest;
    }

    public String getName() {
        return this.aBreak.getType();
    }

    public TourActivity duplicate() {
        return new RelativeBreakActivity(this);
    }

    public Capacity getSize() {
        return this.aBreak.getSize();
    }
}
