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
import java.util.List;

import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.SizeDimension;
import com.graphhopper.jsprit.core.problem.Skills;
import com.graphhopper.jsprit.core.problem.job.CustomJob.BuilderBase.ActivityType;
import com.graphhopper.jsprit.core.problem.job.CustomJob.BuilderBase.BuilderActivityInfo;
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
 * @deprecated Use {@linkplain CustomJob} instead
 *
 *
 *             <p>
 *             <h1><em>Warning!</em></h1>
 *             </p>
 *
 *             <p>
 *             <strong>This class is deprecated and only available for backward
 *             compatibility and for easier migration.</strong>
 *             </p>
 *             <p>
 *             This class wraps a new CustomJob instance and delegates its
 *             values and the values from its sole activity. It is strongly
 *             recommended to switch to the {@linkplain CustomJob} and use one
 *             of the following functions of its builder to add the service
 *             activity:
 *
 *             <ul>
 *             <li>{@linkplain CustomJob.Builder#addService(Location)}</li>
 *             <li>
 *             {@linkplain CustomJob.Builder#addService(Location, double, TimeWindow)}
 *             </li>
 *             </ul>
 *
 *             or if you need more control on the activity, use the
 *             {@linkplain CustomJob.Builder#addActivity(BuilderActivityInfo)}
 *             function:
 *
 *             <pre>
 *    BuilderActivityInfo activityInfo = new BuilderActivityInfo(ActivityType.SERVICE, <i>location</i>);

        activityInfo.withName(<i>activity name</i>);
        activityInfo.withOperationTime(<i>serviceTime</i>);
        activityInfo.withSize((SizeDimension) <i>capacity</i>);
        activityInfo.withTimeWindows(<i>timeWindows</i>);
        activityInfo.withTimeWindow(<i>timeWindow</i>);

        CustomJob.Builder customJobBuilder = new CustomJob.Builder(<i>id</i>);
        customJobBuilder
            .addActivity(activityInfo)
            .addAllRequiredSkills(<i>skills<i>)
            .setName(<i>job name</i>)
            .setPriority(<i>priority</i>);

        job = customJobBuilder.build();
 *             </pre>
 *
 *             </p>
 *
 * @author schroeder
 * @author Balage
 *
 * @see {@linkplain CustomJob}
 * @see {@linkplain CustomJob.Builder}
 * @see {@linkplain CustomJob.BuilderBase.BuilderActivityInfo}
 *
 *
 * @author schroeder
 */
@Deprecated
public class Shipment extends AbstractJob {




    /**
     * Builder that builds the shipment.
     *
     * @deprecated Use {@linkplain CustomJob.Builder} instead
     * @author schroeder
     */
    @Deprecated
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

        public Object userData;

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
            pickupTimeWindows = new TimeWindowsImpl();
            pickupTimeWindows.add(pickupTimeWindow);
            deliveryTimeWindows = new TimeWindowsImpl();
            deliveryTimeWindows.add(deliveryTimeWindow);
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
         * Sets pickup location.
         *
         * @param pickupLocation
         *            pickup location
         * @return builder
         */
        public Builder setPickupLocation(Location pickupLocation) {
            this.pickupLocation_ = pickupLocation;
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
            if (serviceTime < 0.0) throw new IllegalArgumentException("serviceTime must not be < 0.0");
            this.pickupServiceTime = serviceTime;
            return this;
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
        public Builder setPickupTimeWindow(TimeWindow timeWindow) {
            if (timeWindow == null) throw new IllegalArgumentException("delivery time-window must not be null");
            this.pickupTimeWindow = timeWindow;
            this.pickupTimeWindows = new TimeWindowsImpl();
            this.pickupTimeWindows.add(timeWindow);
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
            if (deliveryServiceTime < 0.0) throw new IllegalArgumentException("deliveryServiceTime must not be < 0.0");
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
            if (timeWindow == null) throw new IllegalArgumentException("delivery time-window must not be null");
            this.deliveryTimeWindow = timeWindow;
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
            if (dimensionValue < 0) throw new IllegalArgumentException("capacity value cannot be negative");
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
            if(timeWindow == null) throw new IllegalArgumentException("time-window arg must not be null");
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

        public Builder addPickupTimeWindow(TimeWindow timeWindow) {
            if(timeWindow == null) throw new IllegalArgumentException("time-window arg must not be null");
            if(!pickupTimeWindowAdded){
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
         * Set priority to shipment. Only 1 (high) to 10 (low) are allowed.
         * <p>
         * Default is 2 = medium.
         *
         * @param priority
         * @return builder
         */
        public Builder setPriority(int priority) {
            if (priority < 1 || priority > 10)
                throw new IllegalArgumentException("incorrect priority. only 1 (very high) to 10 (very low) are allowed");
            this.priority = priority;
            return this;
        }


        /**
         * Builds the shipment.
         *
         * @return shipment
         * @throws IllegalArgumentException
         *             if neither pickup-location nor pickup-coord is set or if
         *             neither delivery-location nor delivery-coord is set
         */
        public Shipment build() {
            if (pickupLocation_ == null)
                throw new IllegalArgumentException("pickup location is missing");
            if (deliveryLocation_ == null)
                throw new IllegalArgumentException("delivery location is missing");
            capacity = capacityBuilder.build();
            skills = skillBuilder.build();
            return new Shipment(this);
        }
    }

    private CustomJob theRealJob;
    private PickupActivity theRealPickupActivity;
    private DeliveryActivity theRealDeliveryActivity;

    Shipment(Builder builder) {

        BuilderActivityInfo pickupActivityInfo = new BuilderActivityInfo(ActivityType.PICKUP,
                builder.pickupLocation_);

        pickupActivityInfo.withName(builder.name == null ? null : builder.name + ".pickup");
        pickupActivityInfo.withOperationTime(builder.pickupServiceTime);
        // Safe cast because SizeDimension is the only implementation of
        // Capacity
        pickupActivityInfo.withSize((SizeDimension) builder.capacity);
        pickupActivityInfo.withTimeWindows(builder.pickupTimeWindows.getTimeWindows());

        BuilderActivityInfo deliveryActivityInfo = new BuilderActivityInfo(ActivityType.DELIVERY,
                builder.deliveryLocation_);

        deliveryActivityInfo.withName(builder.name == null ? null : builder.name + ".delivery");
        deliveryActivityInfo.withOperationTime(builder.deliveryServiceTime);
        // Safe cast because SizeDimension is the only implementation of
        // Capacity
        deliveryActivityInfo.withSize((SizeDimension) builder.capacity);
        deliveryActivityInfo.withTimeWindows(builder.deliveryTimeWindows.getTimeWindows());

        CustomJob.Builder customJobBuilder = new CustomJob.Builder(builder.id);
        customJobBuilder
        .addActivity(pickupActivityInfo)
        .addActivity(deliveryActivityInfo)
        .addAllRequiredSkills(builder.skills)
        .setName(builder.name)
        .addUserData(builder.userData)
        .setPriority(builder.priority);
        theRealJob = customJobBuilder.build();

        theRealPickupActivity = (PickupActivity) theRealJob.getActivityList().getAll().get(0);
        theRealDeliveryActivity = (DeliveryActivity) theRealJob.getActivityList().getAll().get(1);
    }

    @Override
    public String getId() {
        return theRealJob.getId();
    }

    public Location getPickupLocation() {
        return theRealPickupActivity.getLocation();
    }

    /**
     * Returns the pickup service-time.
     * <p>
     * <p>By default service-time is 0.0.
     *
     * @return service-time
     */
    public double getPickupServiceTime() {
        return theRealPickupActivity.getOperationTime();
    }

    public Location getDeliveryLocation() {
        return theRealDeliveryActivity.getLocation();
    }

    /**
     * Returns service-time of delivery.
     *
     * @return service-time of delivery
     */
    public double getDeliveryServiceTime() {
        return theRealDeliveryActivity.getOperationTime();
    }

    /**
     * Returns the time-window of delivery.
     *
     * @return time-window of delivery
     */
    public TimeWindow getDeliveryTimeWindow() {
        return theRealDeliveryActivity.getTimeWindows().iterator().next();
    }

    public Collection<TimeWindow> getDeliveryTimeWindows() {
        return theRealDeliveryActivity.getTimeWindows();
    }

    /**
     * Returns the time-window of pickup.
     *
     * @return time-window of pickup
     */
    public TimeWindow getPickupTimeWindow() {
        return theRealPickupActivity.getTimeWindows().iterator().next();
    }

    public Collection<TimeWindow> getPickupTimeWindows() {
        return theRealPickupActivity.getTimeWindows();
    }

    @Override
    public SizeDimension getSize() {
        return theRealPickupActivity.getLoadSize();
    }

    @Override
    public Skills getRequiredSkills() {
        return theRealJob.getRequiredSkills();
    }

    @Override
    public String getName() {
        return theRealJob.getName();
    }

    /**
     * Get priority of shipment. Only 1 = high priority, 2 = medium and 3 = low are allowed.
     * <p>
     * Default is 2 = medium.
     *
     * @return priority
     */
    @Override
    public int getPriority() {
        return theRealJob.getPriority();
    }


    @Override
    protected void createActivities(JobBuilder<? extends AbstractJob, ?> jobBuilder) {
        // This is unused being a legacy implementation
    }

    // @Override
    // public int getIndex() {
    // return theRealJob.getIndex();
    // }

    @Override
    public Object getUserData() {
        return theRealJob.getUserData();
    }

    @Override
    public List<Location> getAllLocations() {
        return theRealJob.getAllLocations();
    }

    @Override
    public SizeDimension getSizeAtStart() {
        return theRealJob.getSizeAtStart();
    }

    @Override
    public SizeDimension getSizeAtEnd() {
        return theRealJob.getSizeAtEnd();
    }

    @Override
    public JobActivityList getActivityList() {
        return theRealJob.getActivityList();
    }

    @Override
    public Collection<TimeWindow> getTimeWindows() {
        return theRealJob.getTimeWindows();
    }

    @Override
    public String toString() {
        return theRealJob.toString();
    }

    // @Override
    // public void impl_setIndex(FriendlyHandshake handshake, int index) {
    // theRealJob.impl_setIndex(handshake, index);
    // }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        return result;
    }

    /**
     * Two services are equal if they have the same id.
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
        if (getId() == null) {
            if (other.getId() != null)
                return false;
        } else if (!getId().equals(other.getId()))
            return false;
        return true;
    }

    public CustomJob getTheRealJob() {
        return theRealJob;
    }

    public PickupActivity getTheRealPickupActivity() {
        return theRealPickupActivity;
    }

    public DeliveryActivity getTheRealDeliveryActivity() {
        return theRealDeliveryActivity;
    }
}
