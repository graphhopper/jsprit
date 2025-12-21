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

import com.graphhopper.jsprit.core.problem.AbstractJob;
import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.Skills;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupLocation;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupLocationsImpl;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindowsImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Shipment is an implementation of Job and consists of a pickup and a delivery of something.
 * <p>
 * <p>It distinguishes itself from {@link Service} as two locations are involved a pickup where usually
 * something is loaded to the transport unit and a delivery where something is unloaded.
 * <p>
 * <p>By default serviceTimes of both pickup and delivery is 0.0 and timeWindows of both is [0.0, Double.MAX_VALUE],
 * <p>
 * <p>A shipment can be built with a builder. You can get an instance of the builder by coding <code>Shipment.Builder.newInstance(...)</code>.
 * This way you can specify the shipment. Once you build the shipment, it is immutable, i.e. fields/attributes cannot be changed anymore and
 * you can only 'get' the specified values.
 * <p>
 * <p>Note that two shipments are equal if they have the same id.
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

        private Capacity.Builder capacityBuilder = Capacity.Builder.newInstance();

        private Capacity capacity;

        private Skills.Builder skillBuilder = Skills.Builder.newInstance();

        private Skills skills;

        private String name = "no-name";

        protected PickupLocationsImpl pickupLocations;

        private Location deliveryLocation_;

        protected TimeWindowsImpl deliveryTimeWindows;

        private boolean deliveryTimeWindowAdded = false;

        private int priority = 2;

        public Object userData;

        public double maxTimeInVehicle = Double.MAX_VALUE;

        private Activity pickup;

        private Activity delivery;

        /**
         * Returns new instance of this builder.
         *
         * @param id the id of the shipment which must be a unique identifier among all jobs
         * @return the builder
         */
        public static Builder newInstance(String id) {
            return new Builder(id);
        }

        Builder(String id) {
            if (id == null) throw new IllegalArgumentException("id must not be null");
            this.id = id;
            //pickupTimeWindows = new TimeWindowsImpl();
            //pickupTimeWindows.add(TimeWindow.newInstance(0.0, Double.MAX_VALUE));
            deliveryTimeWindows = new TimeWindowsImpl();
            deliveryTimeWindows.add(TimeWindow.newInstance(0.0, Double.MAX_VALUE));
        }

        /**
         * Sets user specific domain data associated with the object.
         *
         * <p>
         * The user data is a black box for the framework, it only stores it,
         * but never interacts with it in any way.
         * </p>
         *
         * @param userData
         *            any object holding the domain specific user data
         *            associated with the object.
         * @return builder
         */
        public Builder setUserData(Object userData) {
            this.userData = userData;
            return this;
        }

        /**
         * Sets pickup locations.
         *
         * @param pickupLocation
         *            pickup location
         * @return builder
         */
        public Builder setPickupLocations(PickupLocationsImpl pickupLocation) {
            this.pickupLocations = pickupLocation;
            return this;
        }


        /**
         * Sets pickupServiceTime.
         * <p>
         * <p>ServiceTime is intended to be the time the implied activity takes at the pickup-location.
         *
         * @param serviceTime the service time / duration the pickup of the associated shipment takes
         * @return builder
         * @throws IllegalArgumentException if servicTime < 0.0
         */
        public Builder setPickupServiceTime(double serviceTime) {
            if (serviceTime < 0.0)
                throw new IllegalArgumentException("The service time of a shipment must not be < 0.0.");
            this.pickupServiceTime = serviceTime;
            return this;
        }


        /**
         * Sets the pickupLocation for the pickup
         * <p>
         *
         * @param pickupLocation the pickupLocation of pickup
         * @return builder
         * @throws IllegalArgumentException if pickupLocation is null
         */
        public Builder setPickupLocation(PickupLocation pickupLocation) {
            if (pickupLocation == null) throw new IllegalArgumentException("The pickup location must not be null.");
            // Replace the collection (this is the "set" behavior)
            this.pickupLocations = new PickupLocationsImpl();
            this.pickupLocations.add(pickupLocation);
            return this;
        }


        /**
         * Sets delivery location.
         *
         * @param deliveryLocation delivery location
         * @return builder
         */
        public Builder setDeliveryLocation(Location deliveryLocation) {
            this.deliveryLocation_ = deliveryLocation;
            return this;
        }

        /**
         * Sets the delivery service-time.
         * <p>
         * <p>ServiceTime is intended to be the time the implied activity takes at the delivery-location.
         *
         * @param deliveryServiceTime the service time / duration of shipment's delivery
         * @return builder
         * @throws IllegalArgumentException if serviceTime < 0.0
         */
        public Builder setDeliveryServiceTime(double deliveryServiceTime) {
            if (deliveryServiceTime < 0.0)
                throw new IllegalArgumentException("The service time of a delivery must not be < 0.0.");
            this.deliveryServiceTime = deliveryServiceTime;
            return this;
        }

        /**
         * Sets the timeWindow for the delivery, i.e. the time-period in which a delivery operation is
         * allowed to start.
         * <p>
         * <p>By default timeWindow is [0.0, Double.MAX_VALUE}
         *
         * @param timeWindow the time window within the associated delivery is allowed to START
         * @return builder
         * @throws IllegalArgumentException if timeWindow is null
         */
        public Builder setDeliveryTimeWindow(TimeWindow timeWindow) {
            if (timeWindow == null) throw new IllegalArgumentException("The delivery time window must not be null.");
            this.deliveryTimeWindows = new TimeWindowsImpl();
            this.deliveryTimeWindows.add(timeWindow);
            return this;
        }

        /**
         * Adds capacity dimension.
         *
         * @param dimensionIndex the dimension index of the corresponding capacity value
         * @param dimensionValue the capacity value
         * @return builder
         * @throws IllegalArgumentException if dimVal < 0
         */
        public Builder addSizeDimension(int dimensionIndex, int dimensionValue) {
            if (dimensionValue < 0)
                throw new IllegalArgumentException("The capacity value must not be negative, but is " + dimensionValue + ".");
            capacityBuilder.addDimension(dimensionIndex, dimensionValue);
            return this;
        }

        public Builder addAllSizeDimensions(Capacity size) {
            for (int i = 0; i < size.getNuOfDimensions(); i++) {
                addSizeDimension(i, size.get(i));
            }
            return this;
        }


        /**
         * Builds the shipment.
         *
         * @return shipment
         * @throws IllegalArgumentException if neither pickup-location nor pickup-coord is set or if neither delivery-location nor delivery-coord
         *                               is set
         */
        public Shipment build() {
            if (pickupLocations == null || pickupLocations.getPickupLocations().size() == 0)
                throw new IllegalArgumentException("At least one pickup location must be provided.");
            if (deliveryLocation_ == null) throw new IllegalArgumentException("The delivery location is missing.");
            capacity = capacityBuilder.build();
            skills = skillBuilder.build();
            pickup =
                    new Activity.Builder(pickupLocations, Activity.Type.PICKUP)
                            .setServiceTime(pickupServiceTime).build();
            delivery = new Activity.Builder(deliveryLocation_, Activity.Type.DELIVERY).setServiceTime(deliveryServiceTime).setTimeWindows(deliveryTimeWindows.getTimeWindows()).build();
            return new Shipment(this);
        }


        public Builder addRequiredSkill(String skill) {
            skillBuilder.addSkill(skill);
            return this;
        }

        public Builder addAllRequiredSkills(Collection<String> skills) {
            skillBuilder.addAllSkills(skills);
            return this;
        }

        public Builder addAllRequiredSkills(Skills skills) {
            addAllRequiredSkills(skills.values());
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder addDeliveryTimeWindow(TimeWindow timeWindow) {
            if (timeWindow == null) throw new IllegalArgumentException("The time window must not be null.");
            if(!deliveryTimeWindowAdded){
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

        public Builder addAllDeliveryTimeWindows(Collection<TimeWindow> timeWindow) {
            for (TimeWindow tw : timeWindow) addDeliveryTimeWindow(tw);
            return this;
        }

        public Builder addPickupLocation(PickupLocation pickupLocation) {
            if (pickupLocation == null) throw new IllegalArgumentException("The pickup location must not be null.");
            // Lazy initialization: create only if null (no flag needed)
            if (pickupLocations == null) {
                pickupLocations = new PickupLocationsImpl();
            }
            pickupLocations.add(pickupLocation);
            return this;
        }

        public Builder addPickupLocation(Location location) {
            return addPickupLocation(PickupLocation.newInstance(location));
        }

        public Builder addAllPickupLocations(Collection<PickupLocation> pickupLocation) {
            for (PickupLocation pl : pickupLocation) addPickupLocation(pl);
            return this;
        }

        /**
         * Set priority to shipment. Only 1 (high) to 10 (low) are allowed.
         * <p>
         * Default is 2.
         *
         * @param priority
         * @return builder
         */
        public Builder setPriority(int priority) {
            if (priority < 1 || priority > 10)
                throw new IllegalArgumentException("The priority value is not valid. Only 1 (very high) to 10 (very low) are allowed.");
            this.priority = priority;
            return this;
        }

        /**
         * Sets maximal time the job can be in vehicle.
         *
         * @param maxTimeInVehicle
         * @return
         */
        public Builder setMaxTimeInVehicle(double maxTimeInVehicle){
            if (maxTimeInVehicle < 0)
                throw new IllegalArgumentException("The maximum time in vehicle must be positive.");
            this.maxTimeInVehicle = maxTimeInVehicle;
            return this;
        }
    }

    private final String id;

    private final double pickupServiceTime;

    private final double deliveryServiceTime;

    private final Capacity capacity;

    private final Skills skills;

    private final String name;

    private final PickupLocationsImpl pickupLocations;

    private final Location deliveryLocation_;

    private final TimeWindowsImpl deliveryTimeWindows;

    private final int priority;

    private final double maxTimeInVehicle;

    private List<Activity> activities = new ArrayList<>();

    Shipment(Builder builder) {
        setUserData(builder.userData);
        this.id = builder.id;
        this.pickupServiceTime = builder.pickupServiceTime;
        this.deliveryServiceTime = builder.deliveryServiceTime;
        this.capacity = builder.capacity;
        this.skills = builder.skills;
        this.name = builder.name;
        this.pickupLocations = builder.pickupLocations;
        this.deliveryLocation_ = builder.deliveryLocation_;
        this.deliveryTimeWindows = builder.deliveryTimeWindows;
        this.priority = builder.priority;
        this.maxTimeInVehicle = builder.maxTimeInVehicle;
        activities.add(builder.pickup);
        activities.add(builder.delivery);
        activities = Collections.unmodifiableList(activities);
    }

    @Override
    public String getId() {
        return id;
    }

    public Collection<PickupLocation> getPickupLocations() {
        return pickupLocations.getPickupLocations();
    }

    public void setPickupLocation(Location location)
    {
        pickupLocations.clear();
        pickupLocations.add(PickupLocation.Builder.newInstance().setLocation(location).build());
    }

    /**
     * Returns the pickup service-time.
     * <p>
     * <p>By default service-time is 0.0.
     *
     * @return service-time
     */
    public double getPickupServiceTime() {
        return pickupServiceTime;
    }

    public Location getDeliveryLocation() {
        return deliveryLocation_;
    }

    /**
     * Returns service-time of delivery.
     *
     * @return service-time of delivery
     */
    public double getDeliveryServiceTime() {
        return deliveryServiceTime;
    }

    /**
     * Returns the time-window of delivery.
     *
     * @return time-window of delivery
     */
    public TimeWindow getDeliveryTimeWindow() {
        return deliveryTimeWindows.getTimeWindows().iterator().next();
    }

    public Collection<TimeWindow> getDeliveryTimeWindows() {
        return deliveryTimeWindows.getTimeWindows();
    }


    /**
    public Collection<TimeWindow> getPickupTimeWindows() {
        return pickupLocations.getPickupLocations().stream().flatMap(pickupLocation -> {
            return pickupLocation.getPickupTimeWindows().stream();
        }).collect(Collectors.toList());
    }*/


    /**
     * Returns a string with the shipment's attributes.
     * <p>
     * <p>String is built as follows: [attr1=val1][attr2=val2]...
     */
    @Override
    public String toString() {
        return "[id=" + id + "][name=" + name + "][pickupLocations=" + pickupLocations
                + "][deliveryLocation=" + deliveryLocation_ + "][capacity=" + capacity
                + "][pickupServiceTime=" + pickupServiceTime + "][deliveryServiceTime="
                + deliveryServiceTime + "][pickupTimeWindows=" + getPickupLocations().stream().findFirst().get().getPickupTimeWindows()
                + "][deliveryTimeWindows=" + deliveryTimeWindows + "]";
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
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Shipment other = (Shipment) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public Capacity getSize() {
        return capacity;
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
     * Get priority of shipment. Only 1 (high) to 10 (low) are allowed.
     * <p>
     * Default is 2.
     *
     * @return priority
     */
    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public double getMaxTimeInVehicle() {
        return maxTimeInVehicle;
    }

    @Override
    public List<Activity> getActivities() {
        return activities;
    }

    public Type getJobType() {
        return Type.SHIPMENT;
    }
}
