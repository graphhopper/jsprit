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

import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.Skills;
import com.graphhopper.jsprit.core.problem.solution.route.activity.DeliverShipmentDEPRECATED;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupShipmentDEPRECATED;
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
    public static class Builder {

        private String id;

        private double pickupServiceTime = 0.0;

        private double deliveryServiceTime = 0.0;

        private TimeWindow deliveryTimeWindow = TimeWindow.newInstance(0.0, Double.MAX_VALUE);

        private TimeWindow pickupTimeWindow = TimeWindow.newInstance(0.0, Double.MAX_VALUE);

        private Capacity.Builder capacityBuilder = Capacity.Builder.newInstance();

        private Capacity capacity;

        private Skills.Builder skillBuilder = Skills.Builder.newInstance();

        private Skills skills;

        private String name = "no-name";

        private Location pickupLocation_;

        private Location deliveryLocation_;

        protected TimeWindowsImpl deliveryTimeWindows;

        private boolean deliveryTimeWindowAdded = false;

        private boolean pickupTimeWindowAdded = false;

        private TimeWindowsImpl pickupTimeWindows;

        private int priority = 2;

        /**
         * Returns new instance of this builder.
         *
         * @param id
         *            the id of the shipment which must be a unique identifier
         *            among all jobs
         * @return the builder
         */
        public static Builder newInstance(String id) {
            return new Builder(id);
        }

        Builder(String id) {
            if (id == null) {
                throw new IllegalArgumentException("id must not be null");
            }
            this.id = id;
            pickupTimeWindows = new TimeWindowsImpl();
            pickupTimeWindows.add(pickupTimeWindow);
            deliveryTimeWindows = new TimeWindowsImpl();
            deliveryTimeWindows.add(deliveryTimeWindow);
        }

        /**
         * Sets pickup location.
         *
         * @param pickupLocation
         *            pickup location
         * @return builder
         */
        public Builder setPickupLocation(Location pickupLocation) {
            pickupLocation_ = pickupLocation;
            return this;
        }

        /**
         * Sets pickupServiceTime.
         * <p>
         * <p>
         * ServiceTime is intended to be the time the implied activity takes at
         * the pickup-location.
         *
         * @param serviceTime
         *            the service time / duration the pickup of the associated
         *            shipment takes
         * @return builder
         * @throws IllegalArgumentException
         *             if servicTime < 0.0
         */
        public Builder setPickupServiceTime(double serviceTime) {
            if (serviceTime < 0.0) {
                throw new IllegalArgumentException("serviceTime must not be < 0.0");
            }
            pickupServiceTime = serviceTime;
            return this;
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
        public Builder setPickupTimeWindow(TimeWindow timeWindow) {
            if (timeWindow == null) {
                throw new IllegalArgumentException("delivery time-window must not be null");
            }
            pickupTimeWindow = timeWindow;
            pickupTimeWindows = new TimeWindowsImpl();
            pickupTimeWindows.add(timeWindow);
            return this;
        }



        /**
         * Sets delivery location.
         *
         * @param deliveryLocation
         *            delivery location
         * @return builder
         */
        public Builder setDeliveryLocation(Location deliveryLocation) {
            deliveryLocation_ = deliveryLocation;
            return this;
        }

        /**
         * Sets the delivery service-time.
         * <p>
         * <p>
         * ServiceTime is intended to be the time the implied activity takes at
         * the delivery-location.
         *
         * @param deliveryServiceTime
         *            the service time / duration of shipment's delivery
         * @return builder
         * @throws IllegalArgumentException
         *             if serviceTime < 0.0
         */
        public Builder setDeliveryServiceTime(double deliveryServiceTime) {
            if (deliveryServiceTime < 0.0) {
                throw new IllegalArgumentException("deliveryServiceTime must not be < 0.0");
            }
            this.deliveryServiceTime = deliveryServiceTime;
            return this;
        }

        /**
         * Sets the timeWindow for the delivery, i.e. the time-period in which a
         * delivery operation is allowed to start.
         * <p>
         * <p>
         * By default timeWindow is [0.0, Double.MAX_VALUE}
         *
         * @param timeWindow
         *            the time window within the associated delivery is allowed
         *            to START
         * @return builder
         * @throws IllegalArgumentException
         *             if timeWindow is null
         */
        public Builder setDeliveryTimeWindow(TimeWindow timeWindow) {
            if (timeWindow == null) {
                throw new IllegalArgumentException("delivery time-window must not be null");
            }
            deliveryTimeWindow = timeWindow;
            deliveryTimeWindows = new TimeWindowsImpl();
            deliveryTimeWindows.add(timeWindow);
            return this;
        }

        /**
         * Adds capacity dimension.
         *
         * @param dimensionIndex
         *            the dimension index of the corresponding capacity value
         * @param dimensionValue
         *            the capacity value
         * @return builder
         * @throws IllegalArgumentException
         *             if dimVal < 0
         */
        public Builder addSizeDimension(int dimensionIndex, int dimensionValue) {
            if (dimensionValue < 0) {
                throw new IllegalArgumentException("capacity value cannot be negative");
            }
            capacityBuilder.addDimension(dimensionIndex, dimensionValue);
            return this;
        }

        public Builder addRequiredSkill(String skill) {
            skillBuilder.addSkill(skill);
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder addDeliveryTimeWindow(TimeWindow timeWindow) {
            if (timeWindow == null) {
                throw new IllegalArgumentException("time-window arg must not be null");
            }
            if (!deliveryTimeWindowAdded) {
                deliveryTimeWindows = new TimeWindowsImpl();
                deliveryTimeWindowAdded = true;
            }
            deliveryTimeWindows.add(timeWindow);
            return this;
        }

        public Builder addDeliveryTimeWindow(double earliest, double latest) {
            addDeliveryTimeWindow(TimeWindow.newInstance(earliest, latest));
            return this;
        }

        public Builder addPickupTimeWindow(TimeWindow timeWindow) {
            if (timeWindow == null) {
                throw new IllegalArgumentException("time-window arg must not be null");
            }
            if (!pickupTimeWindowAdded) {
                pickupTimeWindows = new TimeWindowsImpl();
                pickupTimeWindowAdded = true;
            }
            pickupTimeWindows.add(timeWindow);
            return this;
        }

        public Builder addPickupTimeWindow(double earliest, double latest) {
            return addPickupTimeWindow(TimeWindow.newInstance(earliest, latest));
        }

        /**
         * Set priority to shipment. Only 1 = high priority, 2 = medium and 3 =
         * low are allowed.
         * <p>
         * Default is 2 = medium.
         *
         * @param priority
         * @return builder
         */
        public Builder setPriority(int priority) {
            if (priority < 1 || priority > 3) {
                throw new IllegalArgumentException("incorrect priority. only 1 = high, 2 = medium and 3 = low is allowed");
            }
            this.priority = priority;
            return this;
        }


        /**
         * Builds a shipment.
         *
         * <p>
         * The implementation of the builder <b>may</b> call the function {@linkplain #preProcess()} prior creating the
         * instant and <b>MUST</b> call the {@linkplain #postProcess(Service)} method after the instance is constructed:
         *
         * <pre>
         *    &#64;Override
         *    public {@link Shipment} build() {
         *        [...]
         *        preProcess();
         *        Shipment shipment= new Service(this);
         *        postProcess(shipment);
         *        return shipment;
         *    }
         * </pre>
         *
         * </p>
         *
         * @return shipment
         * @throws IllegalArgumentException
         *             if neither pickup-location nor pickup-coord is set or if neither delivery-location nor
         *             delivery-coord is set
         */
        public Shipment build() {
            if (pickupLocation_ == null) {
                throw new IllegalArgumentException("pickup location is missing");
            }
            if (deliveryLocation_ == null) {
                throw new IllegalArgumentException("delivery location is missing");
            }
            preProcess();
            Shipment shipment = new Shipment(this);
            postProcess(shipment);
            return shipment;
        }

        protected void preProcess() {
            capacity = capacityBuilder.build();
            skills = skillBuilder.build();
        }

        protected <T extends Shipment> void postProcess(T shipment) {
            shipment.createActivities(this);
        }


        // ---- Refactor test

        public String getId() {
            return id;
        }

        public double getPickupServiceTime() {
            return pickupServiceTime;
        }

        public double getDeliveryServiceTime() {
            return deliveryServiceTime;
        }

        public TimeWindow getDeliveryTimeWindow() {
            return deliveryTimeWindow;
        }

        public TimeWindow getPickupTimeWindow() {
            return pickupTimeWindow;
        }

        public Capacity.Builder getCapacityBuilder() {
            return capacityBuilder;
        }

        public Capacity getCapacity() {
            return capacity;
        }

        public Skills.Builder getSkillBuilder() {
            return skillBuilder;
        }

        public Skills getSkills() {
            return skills;
        }

        public String getName() {
            return name;
        }

        public Location getPickupLocation() {
            return pickupLocation_;
        }

        public Location getDeliveryLocation() {
            return deliveryLocation_;
        }

        public TimeWindowsImpl getDeliveryTimeWindows() {
            return deliveryTimeWindows;
        }

        public boolean isDeliveryTimeWindowAdded() {
            return deliveryTimeWindowAdded;
        }

        public boolean isPickupTimeWindowAdded() {
            return pickupTimeWindowAdded;
        }

        public TimeWindowsImpl getPickupTimeWindows() {
            return pickupTimeWindows;
        }

        public int getPriority() {
            return priority;
        }



    }

    private final String id;

    private final Skills skills;

    private final String name;

    private final int priority;

    Shipment(Builder builder) {
        id = builder.id;
        skills = builder.skills;
        name = builder.name;
        priority = builder.priority;
    }


    @Override
    protected void createActivities() {
    }

    protected void createActivities(Builder builder) {
        JobActivityList list = new SequentialJobActivityList(this);
        // TODO - Balage1551
        //      list.addActivity(new PickupActivityNEW(this, "pickup", getPickupLocation(), getPickupServiceTime(), getSize()));
        //      list.addActivity(new PickupActivityNEW(this, "delivery", getDeliveryLocation(), getDeliveryServiceTime(), getSize()));
        list.addActivity(new PickupShipmentDEPRECATED(this, builder));
        list.addActivity(new DeliverShipmentDEPRECATED(this, builder));
        setActivities(list);
    }

    @Override
    public String getId() {
        return id;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    /**
     * Two shipments are equal if they have the same id.
     *
     * @return true if shipments are equal (have the same id)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Shipment other = (Shipment) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }


    @Override
    public Skills getRequiredSkills() {
        return skills;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Get priority of shipment. Only 1 = high priority, 2 = medium and 3 = low
     * are allowed.
     * <p>
     * Default is 2 = medium.
     *
     * @return priority
     */
    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public Location getStartLocation() {
        return getPickupActivity().getLocation();
    }


    @Override
    public Location getEndLocation() {
        return getDeliveryActivity().getLocation();
    }

    public PickupShipmentDEPRECATED getPickupActivity() {
        return (PickupShipmentDEPRECATED) getActivityList().findByType(PickupShipmentDEPRECATED.NAME)
                        .get();
    }

    public DeliverShipmentDEPRECATED getDeliveryActivity() {
        return (DeliverShipmentDEPRECATED) getActivityList().findByType(DeliverShipmentDEPRECATED.NAME)
                        .get();
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
    public Capacity getSize() {
        return getPickupActivity().getSize();
    }

}
