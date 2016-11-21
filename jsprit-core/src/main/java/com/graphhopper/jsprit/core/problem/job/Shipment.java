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

import java.util.Collection;

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
 * <p>
 * It distinguishes itself from {@link Service} as two locations are involved a
 * pickup where usually something is loaded to the transport unit and a delivery
 * where something is unloaded.
 * <p>
 * <p>
 * By default serviceTimes of both pickup and delivery is 0.0 and timeWindows of
 * both is [0.0, Double.MAX_VALUE],
 * <p>
 * <p>
 * A shipment can be built with a builder. You can get an instance of the
 * builder by coding <code>Shipment.Builder.newInstance(...)</code>. This way
 * you can specify the shipment. Once you build the shipment, it is immutable,
 * i.e. fields/attributes cannot be changed anymore and you can only 'get' the
 * specified values.
 * <p>
 * <p>
 * Note that two shipments are equal if they have the same id.
 *
 * @author schroeder
 */
public class Shipment extends AbstractJob {

    /**
     * Builder that builds the shipment.
     *
     * @author schroeder
     */
    public static abstract class BuilderBase<T extends Shipment, B extends BuilderBase<T, B>>
    extends JobBuilder<T, B> {

        private double pickupServiceTime = 0.0;

        private double deliveryServiceTime = 0.0;

        private Location pickupLocation;

        private Location deliveryLocation;

        protected TimeWindowsImpl deliveryTimeWindows = new TimeWindowsImpl();

        private TimeWindowsImpl pickupTimeWindows = new TimeWindowsImpl();

        /**
         * Returns new instance of this builder.
         *
         * @param id the id of the shipment which must be a unique identifier
         *           among all jobs
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
            if (serviceTime < 0.0) {
                throw new IllegalArgumentException("serviceTime must not be < 0.0");
            }
            pickupServiceTime = serviceTime;
            return (B) this;
        }

        /**
         * Sets the timeWindow for the pickup, i.e. the time-period in which a
         * pickup operation is allowed to START.
         * <p>
         * <p>
         * By default timeWindow is [0.0, Double.MAX_VALUE}
         *
         * @param timeWindow the time window within the pickup operation/activity can
         *                   START
         * @return builder
         * @throws IllegalArgumentException if timeWindow is null
         */
        @SuppressWarnings("unchecked")
        public B setPickupTimeWindow(TimeWindow timeWindow) {
            if (timeWindow == null) {
                throw new IllegalArgumentException("pickup time-window must not be null");
            }
            pickupTimeWindows.clear();
            pickupTimeWindows.add(timeWindow);
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
            if (deliveryServiceTime < 0.0) {
                throw new IllegalArgumentException("deliveryServiceTime must not be < 0.0");
            }
            this.deliveryServiceTime = deliveryServiceTime;
            return (B) this;
        }

        /**
         * Sets the timeWindow for the delivery, i.e. the time-period in which a
         * delivery operation is allowed to start.
         * <p>
         * <p>
         * By default timeWindow is [0.0, Double.MAX_VALUE}
         *
         * @param timeWindow the time window within the associated delivery is allowed
         *                   to START
         * @return builder
         * @throws IllegalArgumentException if timeWindow is null
         */
        @SuppressWarnings("unchecked")
        public B setDeliveryTimeWindow(TimeWindow timeWindow) {
            if (timeWindow == null) {
                throw new IllegalArgumentException("delivery time-window must not be null");
            }
            deliveryTimeWindows.clear();
            deliveryTimeWindows.add(timeWindow);
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B addDeliveryTimeWindow(TimeWindow timeWindow) {
            if (timeWindow == null) {
                throw new IllegalArgumentException("time-window arg must not be null");
            }
            deliveryTimeWindows.add(timeWindow);
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B addDeliveryTimeWindow(double earliest, double latest) {
            addDeliveryTimeWindow(TimeWindow.newInstance(earliest, latest));
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B addPickupTimeWindow(TimeWindow timeWindow) {
            if (timeWindow == null) {
                throw new IllegalArgumentException("time-window arg must not be null");
            }
            pickupTimeWindows.add(timeWindow);
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B addPickupTimeWindow(double earliest, double latest) {
            addPickupTimeWindow(TimeWindow.newInstance(earliest, latest));
            return (B) this;
        }

        @Override
        protected void validate() {
            if (pickupLocation == null) {
                throw new IllegalArgumentException("pickup location is missing");
            }
            if (deliveryLocation == null) {
                throw new IllegalArgumentException("delivery location is missing");
            }
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

    public static final class Builder extends BuilderBase<Shipment, Builder> {

        public static Builder newInstance(String id) {
            return new Builder(id);
        }

        public Builder(String id) {
            super(id);
        }

        @Override
        protected Shipment createInstance() {
            return new Shipment(this);
        }

    }


    Shipment(BuilderBase<? extends Shipment, ?> builder) {
        super(builder);
    }


    @Override
    protected void createActivities(JobBuilder<?, ?> builder) {
        Builder shipmentBuilder = (Builder) builder;
        JobActivityList list = new SequentialJobActivityList(this);
        list.addActivity(new PickupActivity(this, "pickupShipment",
                        shipmentBuilder.getPickupLocation(),
                        shipmentBuilder.getPickupServiceTime(), shipmentBuilder.getCapacity(),
                        shipmentBuilder.getPickupTimeWindows().getTimeWindows()));
        list.addActivity(new DeliveryActivity(this, "deliverShipment",
                        shipmentBuilder.getDeliveryLocation(),
                        shipmentBuilder.getDeliveryServiceTime(),
                        shipmentBuilder.getCapacity().invert(),
                        shipmentBuilder.getDeliveryTimeWindows().getTimeWindows()));

        setActivities(list);
    }

    public PickupActivity getPickupActivity() {
        return (PickupActivity) getActivityList().findByType("pickupShipment").get();
    }

    public DeliveryActivity getDeliveryActivity() {
        return (DeliveryActivity) getActivityList().findByType("deliverShipment").get();
    }

    // =================== DEPRECATED GETTERS

    @Deprecated
    public Location getPickupLocation() {
        return getPickupActivity().getLocation();
    }

    /**
     * Returns the pickup service-time.
     * <p>
     * <p>
     * By default service-time is 0.0.
     *
     * @return service-time
     */
    @Deprecated
    public double getPickupServiceTime() {
        return getPickupActivity().getOperationTime();
    }

    @Deprecated
    public Location getDeliveryLocation() {
        return getDeliveryActivity().getLocation();
    }

    /**
     * Returns service-time of delivery.
     *
     * @return service-time of delivery
     */
    @Deprecated
    public double getDeliveryServiceTime() {
        return getDeliveryActivity().getOperationTime();
    }

    /**
     * Returns the time-window of delivery.
     *
     * @return time-window of delivery
     */
    @Deprecated
    public TimeWindow getDeliveryTimeWindow() {
        return getDeliveryTimeWindows().iterator().next();
    }

    @Deprecated
    public Collection<TimeWindow> getDeliveryTimeWindows() {
        return getDeliveryActivity().getTimeWindows();
    }

    /**
     * Returns the time-window of pickup.
     *
     * @return time-window of pickup
     */
    @Deprecated
    public TimeWindow getPickupTimeWindow() {
        return getPickupTimeWindows().iterator().next();
    }

    @Deprecated
    public Collection<TimeWindow> getPickupTimeWindows() {
        return getPickupActivity().getTimeWindows();
    }

    @Override
    @Deprecated
    public SizeDimension getSize() {
        return getPickupActivity().getLoadChange();
    }

}
