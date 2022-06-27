package com.graphhopper.jsprit.core.problem.job;


import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.Skills;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;

import java.util.Collection;

public class RelativeBreak extends Service {

    final private int breakStartSec;
    final private int breakEndSec;
    final private int threshold;


    public RelativeBreak(RelativeBreak.Builder builder) {
        super(builder);
        this.breakStartSec = builder.breakStartSec;
        this.breakEndSec = builder.breakEndSec;
        this.threshold = builder.threshold;
    }

    public static class Builder extends Service.Builder {
        private int breakStartSec;
        private int breakEndSec;
        private int threshold;
        private String id;

        public static RelativeBreak.Builder newInstance(String id) {
            return new RelativeBreak.Builder(id);
        }

        public static RelativeBreak.Builder copyRelativeBreak (RelativeBreak relativeBreak) {
            return RelativeBreak.Builder.newInstance(relativeBreak.getId())
                .setBreakStartSec(relativeBreak.getBreakStartSec())
                .setBreakEndSec(relativeBreak.getBreakEndSec())
                .setThreshold(relativeBreak.getThreshold())
                .setLocation(relativeBreak.getLocation())
                .setServiceTime(relativeBreak.getServiceDuration())
                .addAllRequiredSkills(relativeBreak.getRequiredSkills())
                .setName(relativeBreak.getName())
                .setTimeWindow(relativeBreak.getTimeWindow());
        }

        Builder(String id) {
            super(id);
            this.id = id;
        }

        @Override
        protected RelativeBreak.Builder setType(String name) {
            super.setType(name);
            return this;
        }

        @Override
        public RelativeBreak.Builder setLocation(Location location) {
            super.setLocation(location);
            return this;
        }

        @Override
        public RelativeBreak.Builder setServiceTime(double serviceTime) {
            super.setServiceTime(serviceTime);
            return this;
        }

        @Override
        public RelativeBreak.Builder setUserData(Object userData) {
            super.setUserData(userData);
            return this;
        }

        @Override
        public RelativeBreak.Builder addSizeDimension(int dimensionIndex, int dimensionValue) {
            super.addSizeDimension(dimensionIndex, dimensionValue);
            return this;
        }

        @Override
        public RelativeBreak.Builder setTimeWindow(TimeWindow tw) {
            super.setTimeWindow(tw);
            return this;
        }

        @Override
        public RelativeBreak.Builder addTimeWindow(TimeWindow timeWindow) {
            super.addTimeWindow(timeWindow);
            return this;
        }

        @Override
        public RelativeBreak.Builder addTimeWindow(double earliest, double latest) {
            super.addTimeWindow(earliest, latest);
            return this;
        }

        @Override
        public RelativeBreak.Builder addAllTimeWindows(Collection collection) {
            super.addAllTimeWindows(collection);
            return this;
        }

        public RelativeBreak build() {
            if (this.location == null) {
                throw new IllegalArgumentException("The location of service " + this.id + " is missing.");
            } else {
                this.setType("break");
                this.capacity = this.capacityBuilder.build();
                this.skills = this.skillBuilder.build();
                return new RelativeBreak(this);
            }
        }

        @Override
        public RelativeBreak.Builder addRequiredSkill(String skill) {
            super.addRequiredSkill(skill);
            return this;
        }

        @Override
        public RelativeBreak.Builder setName(String name) {
            super.setName(name);
            return this;
        }

        @Override
        public RelativeBreak.Builder addAllRequiredSkills(Skills skills) {
            super.addAllRequiredSkills(skills);
            return this;
        }

        @Override
        public RelativeBreak.Builder addAllSizeDimensions(Capacity size) {
            super.addAllSizeDimensions(size);
            return this;
        }

        @Override
        public RelativeBreak.Builder setPriority(int priority) {
            super.setPriority(priority);
            return this;
        }

        @Override
        public RelativeBreak.Builder setMaxTimeInVehicle(double maxTimeInVehicle) {
            super.setMaxTimeInVehicle(maxTimeInVehicle);
            return this;
        }

        @Override
        public RelativeBreak.Builder setDriverId(String driverId) {
            super.setDriverId(driverId);
            return this;
        }

        @Override
        public RelativeBreak.Builder addAllRequiredSkills(Collection skills) {
            super.addAllRequiredSkills(skills);
            return this;
        }

        public Builder setBreakStartSec(int breakStartSec) {
            this.breakStartSec = breakStartSec;
            return this;
        }

        public Builder setBreakEndSec(int breakEndSec) {
            this.breakEndSec = breakEndSec;
            return this;
        }

        public Builder setThreshold(int threshold) {
            this.threshold = threshold;
            return this;
        }

        public int getBreakStartSec() {
            return breakStartSec;
        }

        public int getBreakEndSec() {
            return breakEndSec;
        }

        public int getThreshold() {
            return threshold;
        }

    }

    public int getBreakStartSec() {
        return breakStartSec;
    }

    public int getBreakEndSec() {
        return breakEndSec;
    }

    public int getThreshold() {
        return threshold;
    }

}
