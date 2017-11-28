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
package com.graphhopper.jsprit.core.problem.vehicle;

import com.graphhopper.jsprit.core.problem.AbstractVehicle;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.Skills;
import com.graphhopper.jsprit.core.problem.job.Break;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of {@link Vehicle}.
 *
 * @author stefan schroeder
 */

public class VehicleImpl extends AbstractVehicle {


    /**
     * Extension of {@link VehicleImpl} representing an unspecified vehicle with the id 'noVehicle'
     * (to avoid null).
     *
     * @author schroeder
     */
    public static class NoVehicle extends AbstractVehicle {

        private String id = "noVehicle";

        private VehicleType type = VehicleTypeImpl.Builder.newInstance("noType").build();

        public NoVehicle() {
        }

        @Override
        public double getEarliestDeparture() {
            return 0;
        }

        @Override
        public double getLatestArrival() {
            return 0;
        }

        @Override
        public VehicleType getType() {
            return type;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public boolean isReturnToDepot() {
            return false;
        }

        @Override
        public Location getStartLocation() {
            return null;
        }

        @Override
        public Location getEndLocation() {
            return null;
        }

        @Override
        public Skills getSkills() {
            return null;
        }


        @Override
        public Break getBreak() {
            return null;
        }

    }

    /**
     * Builder that builds the vehicle.
     * <p>
     * <p>By default, earliestDepartureTime is 0.0, latestDepartureTime is Double.MAX_VALUE,
     * it returns to the depot and its {@link VehicleType} is the DefaultType with typeId equal to 'default'
     * and a capacity of 0.
     *
     * @author stefan
     */
    public static class Builder {

        static final Logger log = LoggerFactory.getLogger(Builder.class.getName());

        private String id;

        private double earliestStart = 0.0;

        private double latestArrival = Double.MAX_VALUE;

        private boolean returnToDepot = true;

        private VehicleType type = VehicleTypeImpl.Builder.newInstance("default").build();

        private Skills.Builder skillBuilder = Skills.Builder.newInstance();

        private Skills skills;

        private Location startLocation;

        private Location endLocation;

        private Break aBreak;

        private Object userData;

        private Builder(String id) {
            super();
            this.id = id;
        }

        /**
         * Sets the {@link VehicleType}.<br>
         *
         * @param type the type to be set
         * @return this builder
         * @throws IllegalArgumentException if type is null
         */
        public Builder setType(VehicleType type) {
            if (type == null) throw new IllegalArgumentException("Vehicle type must not be null.");
            this.type = type;
            return this;
        }

        /**
         * Sets user specific domain data associated with the object.
         * <p>
         * <p>
         * The user data is a black box for the framework, it only stores it,
         * but never interacts with it in any way.
         * </p>
         *
         * @param userData any object holding the domain specific user data
         *                 associated with the object.
         * @return builder
         */
        public Builder setUserData(Object userData) {
            this.userData = userData;
            return this;
        }

        /**
         * Sets the flag whether the vehicle must return to depot or not.
         * <p>
         * <p>
         * If returnToDepot is true, the vehicle must return to specified
         * end-location. If you omit specifying the end-location, vehicle
         * returns to start-location (that must to be set). If you specify it,
         * it returns to specified end-location.
         * <p>
         * <p>
         * If returnToDepot is false, the end-location of the vehicle is
         * endogenous.
         *
         * @param returnToDepot
         *            true if vehicle need to return to depot, otherwise false
         * @return this builder
         */
        public Builder setReturnToDepot(boolean returnToDepot) {
            this.returnToDepot = returnToDepot;
            return this;
        }

        /**
         * Sets start location.
         *
         * @param startLocation start location
         * @return start location
         */
        public Builder setStartLocation(Location startLocation) {
            this.startLocation = startLocation;
            return this;
        }

        public Builder setEndLocation(Location endLocation) {
            this.endLocation = endLocation;
            return this;
        }

        /**
         * Sets earliest-start of vehicle which should be the lower bound of the vehicle's departure times.
         *
         * @param earliest_startTime the earliest start time / departure time of the vehicle at its start location
         * @return this builder
         */
        public Builder setEarliestStart(double earliest_startTime) {
            if (earliest_startTime < 0)
                throw new IllegalArgumentException("The earliest start time of vehicle " + id + " must not be negative.");
            this.earliestStart = earliest_startTime;
            return this;
        }

        /**
         * Sets the latest arrival at vehicle's end-location which is the upper bound of the vehicle's arrival times.
         *
         * @param latest_arrTime the latest arrival time of the vehicle at its end location
         * @return this builder
         */
        public Builder setLatestArrival(double latest_arrTime) {
            if (latest_arrTime < 0)
                throw new IllegalArgumentException("The latest arrival time of vehicle " + id + " must not be negative.");
            this.latestArrival = latest_arrTime;
            return this;
        }

        public Builder addSkill(String skill) {
            skillBuilder.addSkill(skill);
            return this;
        }

        /**
         * Builds and returns the vehicle.
         * <p>
         * <p>if {@link VehicleType} is not set, default vehicle-type is set with id="default" and
         * capacity=0
         * <p>
         * <p>if startLocationId || locationId is null (=> startLocationCoordinate || locationCoordinate must be set) then startLocationId=startLocationCoordinate.toString()
         * and locationId=locationCoordinate.toString() [coord.toString() --> [x=x_val][y=y_val])
         * <p>if endLocationId is null and endLocationCoordinate is set then endLocationId=endLocationCoordinate.toString()
         * <p>if endLocationId==null AND endLocationCoordinate==null then endLocationId=startLocationId AND endLocationCoord=startLocationCoord
         * Thus endLocationId can never be null even returnToDepot is false.
         *
         * @return vehicle
         * @throws IllegalArgumentException if both locationId and locationCoord is not set or (endLocationCoord!=null AND returnToDepot=false)
         *                               or (endLocationId!=null AND returnToDepot=false)
         */
        public VehicleImpl build() {
            if (latestArrival < earliestStart)
                throw new IllegalArgumentException("The latest arrival time of vehicle " + id + " must not be smaller than its start time.");
            if (startLocation != null && endLocation != null) {
                if (!startLocation.getId().equals(endLocation.getId()) && !returnToDepot)
                    throw new IllegalArgumentException("You specified both the end location and that the vehicle " + id + " does not need to return to its end location. This must not be. " +
                        "Either specify end location and return to depot or leave end location unspecified.");
            }
            if (startLocation != null && endLocation == null) {
                endLocation = startLocation;
            }
            if (startLocation == null && endLocation == null)
                throw new IllegalArgumentException("Every vehicle requires a start location, but vehicle " + id + " does not have one.");
            skills = skillBuilder.build();
            return new VehicleImpl(this);
        }

        /**
         * Returns new instance of vehicle builder.
         *
         * @param vehicleId the id of the vehicle which must be a unique identifier among all vehicles
         * @return vehicle builder
         */
        public static Builder newInstance(String vehicleId) {
            return new Builder(vehicleId);
        }

        public Builder addSkills(Skills skills) {
            this.skillBuilder.addAllSkills(skills.values());
            return this;
        }

        public Builder setBreak(Break aBreak) {
            this.aBreak = aBreak;
            return this;
        }
    }

    /**
     * Returns empty/noVehicle which is a vehicle having no capacity, no type and no reasonable id.
     * <p>
     * <p>NoVehicle has id="noVehicle" and extends {@link VehicleImpl}
     *
     * @return emptyVehicle
     */
    public static NoVehicle createNoVehicle() {
        return new NoVehicle();
    }

    private final String id;

    private final VehicleType type;

    private final double earliestDeparture;

    private final double latestArrival;

    private final boolean returnToDepot;

    private final Skills skills;

    private final Location endLocation;

    private final Location startLocation;

    private final Break aBreak;

    private VehicleImpl(Builder builder) {
        setUserData(builder.userData);
        id = builder.id;
        type = builder.type;
        earliestDeparture = builder.earliestStart;
        latestArrival = builder.latestArrival;
        returnToDepot = builder.returnToDepot;
        skills = builder.skills;
        endLocation = builder.endLocation;
        startLocation = builder.startLocation;
        aBreak = builder.aBreak;
        //        setVehicleIdentifier(new VehicleTypeKey(type.getTypeId(),startLocation.getId(),endLocation.getId(),earliestDeparture,latestArrival,skills));
        setVehicleIdentifier(new VehicleTypeKey(type.getTypeId(), startLocation.getId(), endLocation.getId(), earliestDeparture, latestArrival, skills, returnToDepot));
    }

    /**
     * Returns String with attributes of this vehicle
     * <p>
     * <p>String has the following format [attr1=val1][attr2=val2]...[attrn=valn]
     */
    @Override
    public String toString() {
        return "[id=" + id + "]" +
            "[type=" + type + "]" +
            "[startLocation=" + startLocation + "]" +
            "[endLocation=" + endLocation + "]" +
            "[isReturnToDepot=" + isReturnToDepot() + "]" +
            "[skills=" + skills + "]";
    }


    @Override
    public double getEarliestDeparture() {
        return earliestDeparture;
    }

    @Override
    public double getLatestArrival() {
        return latestArrival;
    }

    @Override
    public VehicleType getType() {
        return type;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isReturnToDepot() {
        return returnToDepot;
    }

    @Override
    public Location getStartLocation() {
        return startLocation;
    }

    @Override
    public Location getEndLocation() {
        return endLocation;
    }

    @Override
    public Skills getSkills() {
        return skills;
    }

    @Override
    public Break getBreak() {
        return aBreak;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    /**
     * Two vehicles are equal if they have the same id and if their types are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        VehicleImpl other = (VehicleImpl) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

}

