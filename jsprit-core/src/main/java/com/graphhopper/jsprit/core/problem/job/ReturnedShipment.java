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

import com.graphhopper.jsprit.core.problem.SizeDimension;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.solution.route.activity.DeliveryActivityNEW;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ExchangeActivityNEW;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupActivityNEW;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindowsImpl;


/**
 * Shipment is an implementation of Job and consists of a backhaul and exchange
 * and an delivery
 *
 * @author balage
 */
public final class ReturnedShipment extends Shipment {

    public static final String ACTIVITY_NAME_PICKUP = "pickup";
    public static final String ACTIVITY_NAME_DELIVERY = "delivery";
    public static final String ACTIVITY_NAME_BACKHAUL = "backhaul";

    /**
     * Builder that builds the shipment.
     *
     * @author schroeder
     */

    public static final class Builder extends Shipment.BuilderBase<ReturnedShipment, Builder> {

        private double backhaulServiceTime = 0.0;

        private Location backhaulLocation;

        protected TimeWindowsImpl backhaulTimeWindows = new TimeWindowsImpl();

        protected SizeDimension.Builder backhaulCapacityBuilder = SizeDimension.Builder.newInstance();

        /**
         * Returns new instance of this builder.
         *
         * @param id
         *            the id of the shipment which must be a unique identifier
         *            among all jobs
         * @return the builder
         */

        public Builder(String id) {
            super(id);
            backhaulTimeWindows = new TimeWindowsImpl();
        }

        /**
         * Sets backhaul location.
         *
         * @param backhaulLocation
         *            backhaul location
         * @return builder
         */

        public Builder setBackhaulLocation(Location backhaulLocation) {
            this.backhaulLocation = backhaulLocation;
            return this;
        }

        /**
         * Sets backhaulServiceTime.
         * <p>
         * <p>
         * ServiceTime is intended to be the time the implied activity takes at
         * the backhaul-location.
         *
         * @param serviceTime
         *            the service time / duration the backhaul of the associated
         *            shipment takes
         * @return builder
         * @throws IllegalArgumentException
         *             if servicTime < 0.0
         */

        public Builder setBackhaulServiceTime(double serviceTime) {
            if (serviceTime < 0.0) {
                throw new IllegalArgumentException("serviceTime must not be < 0.0");
            }
            backhaulServiceTime = serviceTime;
            return this;
        }

        /**
         * Sets the timeWindow for the backhaul, i.e. the time-period in which a
         * backhaul operation is allowed to START.
         * <p>
         * <p>
         * By default timeWindow is [0.0, Double.MAX_VALUE}
         *
         * @param timeWindow
         *            the time window within the backhaul operation/activity can
         *            START
         * @return builder
         * @throws IllegalArgumentException
         *             if timeWindow is null
         */

        public Builder setBackhaulTimeWindow(TimeWindow timeWindow) {
            if (timeWindow == null) {
                throw new IllegalArgumentException("backhaul time-window must not be null");
            }
            backhaulTimeWindows.clear();
            backhaulTimeWindows.add(timeWindow);
            return this;
        }




        public Builder addBackhaulTimeWindow(TimeWindow timeWindow) {
            if (timeWindow == null) {
                throw new IllegalArgumentException("time-window arg must not be null");
            }
            backhaulTimeWindows.add(timeWindow);
            return this;
        }


        public Builder addBackhaulTimeWindow(double earliest, double latest) {
            addBackhaulTimeWindow(TimeWindow.newInstance(earliest, latest));
            return this;
        }

        public Builder addBackhaulSizeDimension(int dimensionIndex, int dimensionValue) {
            if (dimensionValue < 0) {
                throw new IllegalArgumentException("capacity value cannot be negative");
            }
            backhaulCapacityBuilder.addDimension(dimensionIndex, dimensionValue);
            return this;
        }

        public Builder addAllBackhaulSizeDimensions(SizeDimension size) {
            for (int i = 0; i < size.getNuOfDimensions(); i++) {
                backhaulCapacityBuilder.addDimension(i, size.get(i));
            }
            return this;
        }

        @Override
        protected void validate() {
            super.validate();
            if (backhaulLocation == null) {
                backhaulLocation = getPickupLocation();
            }
            if (backhaulTimeWindows.isEmpty()) {
                backhaulTimeWindows.add(TimeWindow.ETERNITY);
            }
        }

        private double getBackhaulServiceTime() {
            return backhaulServiceTime;
        }

        private Location getBackhaulLocation() {
            return backhaulLocation;
        }

        private TimeWindowsImpl getBackhaulTimeWindows() {
            return backhaulTimeWindows;
        }

        private SizeDimension getBackhaulCapacity() {
            SizeDimension backhaulCapacity = backhaulCapacityBuilder.build();
            // If no capacity is specified, the backhaul capacity will be the
            // same as the picking one.
            if (backhaulCapacity.getNuOfDimensions() == 0) {
                backhaulCapacity = getCapacity();
            }
            return backhaulCapacity;
        }

        public static Builder newInstance(String id) {
            return new Builder(id);
        }

        @Override
        protected ReturnedShipment createInstance() {
            return new ReturnedShipment(this);
        }
    }



    ReturnedShipment(BuilderBase<? extends ReturnedShipment, ?> builder) {
        super(builder);
    }


    @Override
    protected void createActivities(JobBuilder<?, ?> builder) {
        Builder shipmentBuilder = (Builder) builder;
        JobActivityList list = new SequentialJobActivityList(this);
        list.addActivity(new PickupActivityNEW(this, ACTIVITY_NAME_PICKUP,
                        shipmentBuilder.getPickupLocation(),
                        shipmentBuilder.getPickupServiceTime(), shipmentBuilder.getCapacity(),
                        shipmentBuilder.getPickupTimeWindows().getTimeWindows()));
        list.addActivity(new ExchangeActivityNEW(this, ACTIVITY_NAME_DELIVERY,
                        shipmentBuilder.getDeliveryLocation(),
                        shipmentBuilder.getDeliveryServiceTime(),
                        shipmentBuilder.getBackhaulCapacity()
                                        .subtract(shipmentBuilder.getCapacity()),
                        shipmentBuilder.getDeliveryTimeWindows().getTimeWindows()));
        list.addActivity(new DeliveryActivityNEW(this, ACTIVITY_NAME_BACKHAUL,
                        shipmentBuilder.getBackhaulLocation(),
                        shipmentBuilder.getBackhaulServiceTime(),
                        shipmentBuilder.getBackhaulCapacity(),
                        shipmentBuilder.getBackhaulTimeWindows().getTimeWindows()));
        setActivities(list);
    }

    // TODO: RENAME WHEN SHIPMENT IS RETURNING THE SAME TYPE OF ACTIVIT
    public PickupActivityNEW getPickupActivityTO_BE_RENAMED_LATER() {
        return (PickupActivityNEW) getActivityList()
                        .findByType(ACTIVITY_NAME_PICKUP)
                        .get();
    }

    // TODO: RENAME WHEN SHIPMENT IS RETURNING THE SAME TYPE OF ACTIVIT
    public ExchangeActivityNEW getDeliveryActivityTO_BE_RENAMED_LATER() {
        return (ExchangeActivityNEW) getActivityList()
                        .findByType(ACTIVITY_NAME_DELIVERY)
                        .get();
    }

    public DeliveryActivityNEW getBackhaulActivityTO_BE_RENAMED_LATER() {
        return (DeliveryActivityNEW) getActivityList()
                        .findByType(ACTIVITY_NAME_BACKHAUL)
                        .get();
    }


}
