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
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindows;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindowsImpl;
import com.graphhopper.jsprit.core.util.Coordinate;

import java.util.Collection;

/**
 * Service implementation of a job.
 * <p>
 * <p>A service distinguishes itself from a shipment such that it has only one location. Thus a service
 * is a single point in space (where a service-activity occurs).
 * <p>
 * <p>Note that two services are equal if they have the same id.
 *
 * @author schroeder
 */
public class Service extends AbstractJob {



    /**
     * Builder that builds a service.
     *
     * @author schroeder
     */
    public static class Builder<T extends Service> {




        /**
         * Returns a new instance of builder that builds a service.
         *
         * @param id the id of the service
         * @return the builder
         */
        public static Builder newInstance(String id) {
            return new Builder(id);
        }

        private String id;

        protected String locationId;

        private String type = "service";

        protected Coordinate coord;

        protected double serviceTime;

        protected Capacity.Builder capacityBuilder = Capacity.Builder.newInstance();

        protected Capacity capacity;

        protected Skills.Builder skillBuilder = Skills.Builder.newInstance();

        protected Skills skills;

        private String name = "no-name";

        protected Location location;

        protected TimeWindowsImpl timeWindows;

        private boolean twAdded = false;

        private int priority = 2;
        protected Object userData;

		protected double maxTimeInVehicle = Double.MAX_VALUE;

		Builder(String id){
			this.id = id;
			timeWindows = new TimeWindowsImpl();
			timeWindows.add(TimeWindow.newInstance(0.0, Double.MAX_VALUE));
		}

        /**
         * Protected method to set the type-name of the service.
         * <p>
         * <p>Currently there are {@link Service}, {@link Pickup} and {@link Delivery}.
         *
         * @param name the name of service
         * @return the builder
         */
        protected Builder<T> setType(String name) {
            this.type = name;
            return this;
        }

        /**
         * Sets location
         *
         * @param location location
         * @return builder
         */
        public Builder<T> setLocation(Location location) {
            this.location = location;
            return this;
        }

        /**
         * Sets the serviceTime of this service.
         * <p>
         * <p>It is understood as time that a service or its implied activity takes at the service-location, for instance
         * to unload goods.
         *
         * @param serviceTime the service time / duration of service to be set
         * @return builder
         * @throws IllegalArgumentException if serviceTime < 0
         */
        public Builder<T> setServiceTime(double serviceTime) {
            if (serviceTime < 0)
                throw new IllegalArgumentException("The service time of a service must be greater than or equal to zero.");
            this.serviceTime = serviceTime;
            return this;
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
        public Builder<T> setUserData(Object userData) {
            this.userData = userData;
            return this;
        }

        /**
         * Adds capacity dimension.
         *
         * @param dimensionIndex the dimension index of the capacity value
         * @param dimensionValue the capacity value
         * @return the builder
         * @throws IllegalArgumentException if dimensionValue < 0
         */
        public Builder<T> addSizeDimension(int dimensionIndex, int dimensionValue) {
            if (dimensionValue < 0) throw new IllegalArgumentException("The capacity value must not be negative.");
            capacityBuilder.addDimension(dimensionIndex, dimensionValue);
            return this;
        }

        public Builder<T> setTimeWindow(TimeWindow tw){
            if (tw == null) throw new IllegalArgumentException("The time window must not be null.");
            this.timeWindows = new TimeWindowsImpl();
            timeWindows.add(tw);
            return this;
        }

        public Builder<T> addTimeWindow(TimeWindow timeWindow) {
            if (timeWindow == null) throw new IllegalArgumentException("The time window must not be null.");
            if(!twAdded){
                timeWindows = new TimeWindowsImpl();
                twAdded = true;
            }
            timeWindows.add(timeWindow);
            return this;
        }

        public Builder<T> addTimeWindow(double earliest, double latest) {
            return addTimeWindow(TimeWindow.newInstance(earliest, latest));
        }

        public Builder<T> addAllTimeWindows(Collection<TimeWindow> timeWindows) {
            for (TimeWindow tw : timeWindows) addTimeWindow(tw);
            return this;
        }

        /**
         * Builds the service.
         *
         * @return {@link Service}
         * @throws IllegalArgumentException if neither locationId nor coordinate is set.
         */
        public T build() {
            if (location == null) throw new IllegalArgumentException("The location of service " + id + " is missing.");
            this.setType("service");
            capacity = capacityBuilder.build();
            skills = skillBuilder.build();
            return (T) new Service(this);
        }

        public Builder<T> addRequiredSkill(String skill) {
            skillBuilder.addSkill(skill);
            return this;
        }

        public Builder<T> setName(String name) {
            this.name = name;
            return this;
        }

        public Builder<T> addAllRequiredSkills(Skills skills){
            for(String s : skills.values()){
                skillBuilder.addSkill(s);
            }
            return this;
        }

        public Builder<T> addAllSizeDimensions(Capacity size){
            for(int i=0;i<size.getNuOfDimensions();i++){
                addSizeDimension(i, size.get(i));
            }
            return this;
        }

        /**
         * Set priority to service. Only 1 (very high) to 10 (very low) are allowed.
         * <p>
         * Default is 2.
         *
         * @param priority
         * @return builder
         */
        public Builder<T> setPriority(int priority) {
            if (priority < 1 || priority > 10)
                throw new IllegalArgumentException("The priority value is not valid. Only 1 (very high) to 10 (very low) are allowed.");
            this.priority = priority;
            return this;
        }

        public Builder<T> setMaxTimeInVehicle(double maxTimeInVehicle){
            throw new UnsupportedOperationException("The maximum time in vehicle is not yet supported for Pickups and Services (only for Deliveries and Shipments).");
//            if(maxTimeInVehicle < 0) throw new IllegalArgumentException("maxTimeInVehicle should be positive");
//            this.maxTimeInVehicle = maxTimeInVehicle;
//            return this;
        }
    }

    private final String id;

    private final String type;

    private final double serviceTime;

    private final Capacity size;

    private final Skills skills;

    private final String name;

    private final Location location;

    private final TimeWindows timeWindows;

    private final int priority;

    private final double maxTimeInVehicle;

    Service(Builder<?> builder) {
        setUserData(builder.userData);
        id = builder.id;
        serviceTime = builder.serviceTime;
        type = builder.type;
        size = builder.capacity;
        skills = builder.skills;
        name = builder.name;
        location = builder.location;
        timeWindows = builder.timeWindows;
        priority = builder.priority;
	    maxTimeInVehicle = builder.maxTimeInVehicle;
	}

    public Collection<TimeWindow> getTimeWindows(){
        return timeWindows.getTimeWindows();
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * Returns location.
     *
     * @return location
     */
    public Location getLocation() {
        return location;
    }


    /**
     * Returns the service-time/duration a service takes at service-location.
     *
     * @return service duration
     */
    public double getServiceDuration() {
        return serviceTime;
    }

    /**
     * Returns the time-window a service(-operation) is allowed to start.
     * It is recommended to use getTimeWindows() instead. If you still use this, it returns the first time window of getTimeWindows() collection.
     *
     * @return time window
     *
     */
    public TimeWindow getTimeWindow() {
        return timeWindows.getTimeWindows().iterator().next();
    }

    /**
     * @return the name
     */
    public String getType() {
        return type;
    }

    /**
     * Returns a string with the service's attributes.
     * <p>
     * <p>String is built as follows: [attr1=val1][attr2=val2]...
     */
    @Override
    public String toString() {
        return "[id=" + id + "][name=" + name + "][type=" + type + "][location=" + location
                + "][capacity=" + size + "][serviceTime=" + serviceTime + "][timeWindows="
                + timeWindows + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        Service other = (Service) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public Capacity getSize() {
        return size;
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
     * Get priority of service. Only 1 = high priority, 2 = medium and 3 = low are allowed.
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
    public double getMaxTimeInVehicle() {
        return this.maxTimeInVehicle;
    }

}
