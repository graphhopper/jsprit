package com.graphhopper.jsprit.core.util;

import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tonirajkovski on 7/23/17.
 */
public class FailedConstraintInfo {

    public static class Builder<T extends FailedConstraintInfo> {

        private String failedConstraint;
        private String job;
        private String vehicle;
        private List<String> activities = new ArrayList<>();
        private int insertionIndex;

        public static FailedConstraintInfo.Builder newInstance() {
            return new FailedConstraintInfo.Builder();
        }

        /**
         * Builds the Failed Constraint info.
         *
         * @return {@link Service}
         * @throws IllegalArgumentException if neither locationId nor coordinate is set.
         */
        public T build() {
            if (failedConstraint == null) throw new IllegalArgumentException("failed constraint is missing");
            return (T) new FailedConstraintInfo(this);
        }

        public FailedConstraintInfo.Builder<T> setFailedConstraint(String failedConstraint) {
            this.failedConstraint = failedConstraint;
            return this;
        }

        public FailedConstraintInfo.Builder<T> loadInsertionContextData(JobInsertionContext insertionContext) {
            if (insertionContext != null) {
                this.job = insertionContext.getJob().getId();
                this.vehicle = insertionContext.getNewVehicle().getId();
                if (insertionContext.getActivityContext() != null) {
                    this.insertionIndex = insertionContext.getActivityContext().getInsertionIndex();
                }
                if (insertionContext.getRoute() != null && insertionContext.getRoute().getActivities() != null) {
                    for (TourActivity activity: insertionContext.getRoute().getTourActivities().getActivities()) {
                        if (activity instanceof TourActivity.JobActivity) {
                            activities.add(((TourActivity.JobActivity) activity).getJob().getId() + "-" + activity.getName());
                        }
                    }
                }
            }
            return this;
        }
    }

    private String failedConstraint;
    private String job;
    private String vehicle;
    private List<String> activities = new ArrayList<>();
    private int insertionIndex = -1;

    private FailedConstraintInfo(Builder<?> builder) {
        this.failedConstraint = builder.failedConstraint;
        this.job = builder.job;
        this.vehicle = builder.vehicle;
        this.activities = builder.activities;
        this.insertionIndex = builder.insertionIndex;
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

    public String getJob() {
        return job;
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
