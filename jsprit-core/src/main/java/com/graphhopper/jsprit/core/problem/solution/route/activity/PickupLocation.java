package com.graphhopper.jsprit.core.problem.solution.route.activity;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.util.Coordinate;

import java.util.Collection;

public class PickupLocation {

    public static PickupLocation newInstance(Location location) {
        return new PickupLocation(location);
    }
    private Location pickupLocation_;

    private boolean pickupTimeWindowAdded = false;
    private TimeWindowsImpl pickupTimeWindows;

    /**
     * Constructs the PickupLocation
     *
     * @param location
     * @throw IllegalArgumentException if location is null
     */
    public PickupLocation(Location location) {
        super();
        if (location == null)
            throw new IllegalArgumentException("location cannot be null");
        this.pickupLocation_ = location;
        pickupTimeWindows = new TimeWindowsImpl();
        pickupTimeWindows.add(TimeWindow.newInstance(0.0, Double.MAX_VALUE));
    }

    public static class Builder {

        private Location location;

        private TimeWindowsImpl timeWindows;

        public Builder() {
            this.timeWindows = new TimeWindowsImpl();
        }

        public static PickupLocation.Builder newInstance() {
            return new PickupLocation.Builder();
        }


        public PickupLocation.Builder setLocation(Location location) {
            this.location = location;
            return this;
        }

        public PickupLocation.Builder addTimeWindow(TimeWindow timeWindow) {
            if (timeWindow == null) throw new IllegalArgumentException("The delivery time window must not be null.");
            this.timeWindows.add(timeWindow);
            return this;
        }

        public PickupLocation.Builder addTimeWindow(double earliest, double latest) {
            addTimeWindow(TimeWindow.newInstance(earliest, latest));
            return this;
        }

        public PickupLocation build() {
            if (location == null) {
                throw new IllegalArgumentException("locations cannot be null");
            }
            return new PickupLocation(this);
        }

    }

    private PickupLocation(PickupLocation.Builder builder) {
        this.pickupLocation_ = builder.location;
        this.pickupTimeWindows = builder.timeWindows;
        if (this.pickupTimeWindows == null) {
            this.pickupTimeWindows = new TimeWindowsImpl();
        }
        if (this.pickupTimeWindows.getTimeWindows().size() == 0) {
            this.pickupTimeWindows.add(TimeWindow.newInstance(0.0, Double.MAX_VALUE));
        }
    }

    /**
     * Sets pickup location.
     *
     * @param pickupLocation
     *            pickup location
     * @return builder
     */
    public PickupLocation setPickupLocation(Location pickupLocation) {
        this.pickupLocation_ = pickupLocation;
        return this;
    }


    public Location getLocation(){
        return this.pickupLocation_;
    }

    /**
     * Sets the timeWindow for the pickup, i.e. the time-period in which a pickup operation is
     * allowed to START.
     * <p>
     * <p>By default timeWindow is [0.0, Double.MAX_VALUE}
     *
     * @param timeWindow the time window within the pickup operation/activity can START
     * @return builder
     * @throws IllegalArgumentException if timeWindow is null
     */
    public PickupLocation setPickupTimeWindow(TimeWindow timeWindow) {
        if (timeWindow == null) throw new IllegalArgumentException("The pickup time window must not be null.");
        this.pickupTimeWindows = new TimeWindowsImpl();
        this.pickupTimeWindows.add(timeWindow);
        return this;
    }

    public TimeWindow getPickupTimeWindow() {
        return pickupTimeWindows.getTimeWindows().iterator().next();
    }


    public Collection<TimeWindow> getPickupTimeWindows() {
        return pickupTimeWindows.getTimeWindows();
    }

    public PickupLocation addPickupTimeWindow(TimeWindow timeWindow) {
        if (timeWindow == null) throw new IllegalArgumentException("The time window must not be null.");
        if(!pickupTimeWindowAdded){
            pickupTimeWindows = new TimeWindowsImpl();
            pickupTimeWindowAdded = true;
        }
        pickupTimeWindows.add(timeWindow);
        return this;
    }

}
