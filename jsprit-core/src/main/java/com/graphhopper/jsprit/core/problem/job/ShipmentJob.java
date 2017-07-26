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
package com.graphhopper.jsprit.core.problem.job;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.SizeDimension;
import com.graphhopper.jsprit.core.problem.solution.route.activity.DeliveryActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindowsImpl;


/**
 * Shipment is an implementation of Job and consists of a pickup and a delivery
 * of something.
 * <p>
 * <h3>Warning!</h3>
 * <p>
 * This class and are here for convenience. Most of the time using the
 * {@linkplain CustomJob} is a better choice. Note that this class may most
 * likely be deprecated and be removed in the future.
 * </p>
 *
 * @author schroeder
 * @author Balage
 *
 * @see {@linkplain CustomJob.BuilderBase}
 */
public class ShipmentJob extends AbstractJob {

    /**
     * Name of the pickup activity in the shipment.
     */
    public static final String DELIVERY_ACTIVITY_NAME = "deliverShipment";
    /**
     * Name of the delivery activity in the shipment.
     */
    public static final String PICKUP_ACTIVITY_NAME = "pickupShipment";

    /**
     * Builder that builds the shipment.
     *
     * @author schroeder
     * @author Balage
     */
    protected static abstract class BuilderBase<T extends ShipmentJob, B extends BuilderBase<T, B>>
    extends JobBuilder<T, B> {

        private double pickupServiceTime = 0.0;

        private double deliveryServiceTime = 0.0;

        private Location pickupLocation;

        private Location deliveryLocation;

        private TimeWindowsImpl deliveryTimeWindows = new TimeWindowsImpl();

        private TimeWindowsImpl pickupTimeWindows = new TimeWindowsImpl();

        /**
         * Constructor.
         *
         * @param id
         *            the id of the shipment which must be a unique identifier
         *            among all jobs
         * @return the builder
         */

        public BuilderBase(String id) {
            super(id);
            pickupTimeWindows = new TimeWindowsImpl();
            deliveryTimeWindows = new TimeWindowsImpl();
        }

        /**
         * Sets pickup location.
         *
         * @param pickupLocation pickup location
         * @return builder
         */
        @SuppressWarnings("unchecked")
        public B setPickupLocation(Location pickupLocation) {
            this.pickupLocation = pickupLocation;
            return (B) this;
        }

        /**
         * Sets pickupServiceTime.
         * <p>
         * <p>
         * ServiceTime is intended to be the time the implied activity takes at
         * the pickup-location.
         *
         * @param serviceTime the service time / duration the pickup of the associated
         *                    shipment takes
         * @return builder
         * @throws IllegalArgumentException if servicTime < 0.0
         */
        @SuppressWarnings("unchecked")
        public B setPickupServiceTime(double serviceTime) {
            if (serviceTime < 0.0)
                throw new IllegalArgumentException("serviceTime must not be < 0.0");
            pickupServiceTime = serviceTime;
            return (B) this;
        }



        /**
         * Sets delivery location.
         *
         * @param deliveryLocation delivery location
         * @return builder
         */
        @SuppressWarnings("unchecked")
        public B setDeliveryLocation(Location deliveryLocation) {
            this.deliveryLocation = deliveryLocation;
            return (B) this;
        }

        /**
         * Sets the delivery service-time.
         * <p>
         * <p>
         * ServiceTime is intended to be the time the implied activity takes at
         * the delivery-location.
         *
         * @param deliveryServiceTime the service time / duration of shipment's delivery
         * @return builder
         * @throws IllegalArgumentException if serviceTime < 0.0
         */
        @SuppressWarnings("unchecked")
        public B setDeliveryServiceTime(double deliveryServiceTime) {
            if (deliveryServiceTime < 0.0)
                throw new IllegalArgumentException("deliveryServiceTime must not be < 0.0");
            this.deliveryServiceTime = deliveryServiceTime;
            return (B) this;
        }

        /**
         * Sets a single time window.
         * <p>
         * This method clears any previously set time windows. Use
         * {@linkplain #addTimeWindow(TimeWindow)} to add an additional one,
         * instead of replacing the already set ones.
         * </p>
         *
         * @param timeWindow
         *            the time window within the associated delivery is allowed
         *            to start
         * @return builder
         * @throws IllegalArgumentException
         *             if timeWindow is null
         */
        @SuppressWarnings("unchecked")
        public B setDeliveryTimeWindow(TimeWindow timeWindow) {
            if (timeWindow == null)
                throw new IllegalArgumentException("delivery time-window must not be null");
            deliveryTimeWindows.clear();
            deliveryTimeWindows.add(timeWindow);
            return (B) this;
        }

        /**
         * Adds a single time window to the delivery activity.
         *
         * @param timeWindow
         *            The time window to set.
         * @return the builder
         * @throws IllegalArgumentException
         *             If the time window is null.
         */
        @SuppressWarnings("unchecked")
        public B addDeliveryTimeWindow(TimeWindow timeWindow) {
            if (timeWindow == null)
                throw new IllegalArgumentException("time-window arg must not be null");
            deliveryTimeWindows.add(timeWindow);
            return (B) this;
        }

        /**
         * Constructs and adds a time window to the delivery activity.
         *
         * @param earliest
         *            The earliest start.
         * @param latest
         *            The latest start.
         * @return the builder
         */
        @SuppressWarnings("unchecked")
        public B addDeliveryTimeWindow(double earliest, double latest) {
            addDeliveryTimeWindow(TimeWindow.newInstance(earliest, latest));
            return (B) this;
        }

        /**
         * Sets the timeWindow for the pickup, i.e. the time-period in which a
         * pickup operation is allowed to START.
         * <p>
         * <p>
         * By default timeWindow is [0.0, Double.MAX_VALUE}
         *
         * @param timeWindow
         *            the time window within the pickup operation/activity can
         *            START
         * @return builder
         * @throws IllegalArgumentException
         *             if timeWindow is null
         */
        @SuppressWarnings("unchecked")
        public B setPickupTimeWindow(TimeWindow timeWindow) {
            if (timeWindow == null)
                throw new IllegalArgumentException("pickup time-window must not be null");
            pickupTimeWindows.clear();
            pickupTimeWindows.add(timeWindow);
            return (B) this;
        }

        /**
         * Adds a single time window to the pickup activity.
         *
         * @param timeWindow
         *            The time window to set.
         * @return the builder
         * @throws IllegalArgumentException
         *             If the time window is null.
         */
        @SuppressWarnings("unchecked")
        public B addPickupTimeWindow(TimeWindow timeWindow) {
            if (timeWindow == null)
                throw new IllegalArgumentException("time-window arg must not be null");
            pickupTimeWindows.add(timeWindow);
            return (B) this;
        }

        /**
         * Constructs and adds a time window to the pickup activity.
         *
         * @param earliest
         *            The earliest start.
         * @param latest
         *            The latest start.
         * @return the builder
         */
        @SuppressWarnings("unchecked")
        public B addPickupTimeWindow(double earliest, double latest) {
            addPickupTimeWindow(TimeWindow.newInstance(earliest, latest));
            return (B) this;
        }

        @Override
        protected void validate() {
            if (pickupLocation == null)
                throw new IllegalArgumentException("pickup location is missing");
            if (deliveryLocation == null)
                throw new IllegalArgumentException("delivery location is missing");
            if (pickupTimeWindows.isEmpty()) {
                pickupTimeWindows.add(TimeWindow.ETERNITY);
            }
            if (deliveryTimeWindows.isEmpty()) {
                deliveryTimeWindows.add(TimeWindow.ETERNITY);
            }
        }

        // ---- Refactor test

        public double getPickupServiceTime() {
            return pickupServiceTime;
        }

        public double getDeliveryServiceTime() {
            return deliveryServiceTime;
        }

        public Location getPickupLocation() {
            return pickupLocation;
        }

        public Location getDeliveryLocation() {
            return deliveryLocation;
        }

        public TimeWindowsImpl getDeliveryTimeWindows() {
            return deliveryTimeWindows;
        }

        public TimeWindowsImpl getPickupTimeWindows() {
            return pickupTimeWindows;
        }

    }

    /**
     * The builder for {@linkplain ShipmentJob}.
     *
     * <h3>Warning!</h3>
     * <p>
     * This class and are here for convenience. Most of the time using the
     * {@linkplain CustomJob} is a better choice. Note that this class may most
     * likely be deprecated and be removed in the future.
     * </p>
     *
     * @author Balage
     */
    public static final class Builder extends BuilderBase<ShipmentJob, Builder> {

        /**
         * Constructor.
         *
         * @param id
         *            The unique id.
         */
        public Builder(String id) {
            super(id);
        }

        @Override
        protected ShipmentJob createInstance() {
            return new ShipmentJob(this);
        }

    }


    private ShipmentJob(BuilderBase<? extends ShipmentJob, ?> builder) {
        super(builder);
    }


    @Override
    protected void createActivities(JobBuilder<?, ?> builder) {
        Builder shipmentBuilder = (Builder) builder;
        JobActivityList list = new SequentialJobActivityList(this);
        list.addActivity(new PickupActivity(this, PICKUP_ACTIVITY_NAME,
                shipmentBuilder.getPickupLocation(),
                shipmentBuilder.getPickupServiceTime(), shipmentBuilder.getCapacity(),
                shipmentBuilder.getPickupTimeWindows().getTimeWindows()));
        list.addActivity(new DeliveryActivity(this, DELIVERY_ACTIVITY_NAME,
                shipmentBuilder.getDeliveryLocation(),
                shipmentBuilder.getDeliveryServiceTime(),
                shipmentBuilder.getCapacity().invert(),
                shipmentBuilder.getDeliveryTimeWindows().getTimeWindows()));

        setActivities(list);
    }

    /**
     * @return The pickup activity.
     */
    public PickupActivity getPickupActivity() {
        return (PickupActivity) getActivityList().findByType(PICKUP_ACTIVITY_NAME).get();
    }

    /**
     * @return The delivery activity.
     */
    public DeliveryActivity getDeliveryActivity() {
        return (DeliveryActivity) getActivityList().findByType(DELIVERY_ACTIVITY_NAME).get();
    }


    @Override
    @Deprecated
    public SizeDimension getSize() {
        return getPickupActivity().getLoadChange();
    }

}
