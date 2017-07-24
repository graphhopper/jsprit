package com.graphhopper.jsprit.core.util;

import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tonirajkovski on 7/23/17.
 */
public class FailedConstraintInfo {
    private String failedConstraint;
    private String job;
    private String vehicle;
    private List<String> activities = new ArrayList<>();
    private int insertionIndex;

    public FailedConstraintInfo(String failedConstraint, JobInsertionContext jobInsertionContext) {
        this.failedConstraint = failedConstraint;
        if (jobInsertionContext != null) {
            this.job = jobInsertionContext.getJob().getId();
            this.vehicle = jobInsertionContext.getNewVehicle().getId();
            if (jobInsertionContext.getActivityContext() != null) {
                this.insertionIndex = jobInsertionContext.getActivityContext().getInsertionIndex();
            }
            if (jobInsertionContext.getRoute() != null && jobInsertionContext.getRoute().getActivities() != null) {
                for (TourActivity activity: jobInsertionContext.getRoute().getTourActivities().getActivities()) {
                    if (activity instanceof TourActivity.JobActivity) {
                        activities.add(((TourActivity.JobActivity) activity).getJob().getId() + "-" + activity.getName());
                    }
                }
            }
        }
    }

    public String getFailedConstraint() {
        return failedConstraint;
    }

    public String getVehicle() {
        return vehicle;
    }

    public int getInsertionIndex() {
        return insertionIndex;
    }

    public List<String> getActivities() {
        return activities;
    }

    public String toString() {
        StringBuilder route = new StringBuilder();
        route.append(vehicle).append(" [ ");
        for (String activity: activities) {
            route.append(activity).append(" ");
        }
        route.append("]");
        return String.format("Constraint '%s' failed for job insertion of job '%s' on position '%d' on route '%s'",
            failedConstraint,
            job,
            insertionIndex,
            route
        );
    }
}
