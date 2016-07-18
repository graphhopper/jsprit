/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com.graphhopper.jsprit.core.problem.vehicle;


import com.graphhopper.jsprit.core.problem.Capacity;

/**
 * Implementation of {@link VehicleType}.
 * <p>
 * <p>Two vehicle-types are equal if they have the same typeId.
 *
 * @author schroeder
 */
public class VehicleTypeImpl implements VehicleType {

    /**
     * CostParameter consisting of fixed cost parameter, time-based cost parameter and distance-based cost parameter.
     *
     * @author schroeder
     */
    public static class VehicleCostParams {


        public static VehicleTypeImpl.VehicleCostParams newInstance(double fix, double perTimeUnit, double perDistanceUnit) {
            return new VehicleCostParams(fix, perTimeUnit, perDistanceUnit);
        }

        public final double fix;
        @Deprecated
        public final double perTimeUnit;
        public final double perTransportTimeUnit;
        public final double perDistanceUnit;
        public final double perWaitingTimeUnit;
        public final double perServiceTimeUnit;
        public final double perLowerLatenessTimeUnit;
        public final double perUpperLatenessTimeUnit;

        private VehicleCostParams(double fix, double perTimeUnit, double perDistanceUnit) {
            super();
            this.fix = fix;
            this.perTimeUnit = perTimeUnit;
            this.perTransportTimeUnit = perTimeUnit;
            this.perDistanceUnit = perDistanceUnit;
            this.perWaitingTimeUnit = 0.;
            this.perServiceTimeUnit = 0.;
            this.perLowerLatenessTimeUnit = 0.;
            this.perUpperLatenessTimeUnit = 0.;
        }

        public VehicleCostParams(double fix, double perTimeUnit, double perDistanceUnit, double perWaitingTimeUnit) {
            this.fix = fix;
            this.perTimeUnit = perTimeUnit;
            this.perTransportTimeUnit = perTimeUnit;
            this.perDistanceUnit = perDistanceUnit;
            this.perWaitingTimeUnit = perWaitingTimeUnit;
            this.perServiceTimeUnit = 0.;
            this.perLowerLatenessTimeUnit = 0.;
            this.perUpperLatenessTimeUnit = 0.;
        }

        public VehicleCostParams(double fix, double perTimeUnit, double perDistanceUnit, double perWaitingTimeUnit, double perServiceTimeUnit) {
            this.fix = fix;
            this.perTimeUnit = perTimeUnit;
            this.perTransportTimeUnit = perTimeUnit;
            this.perDistanceUnit = perDistanceUnit;
            this.perWaitingTimeUnit = perWaitingTimeUnit;
            this.perServiceTimeUnit = perServiceTimeUnit;
            this.perLowerLatenessTimeUnit = 0.;
            this.perUpperLatenessTimeUnit = 0.;
        }
        
        public VehicleCostParams(double fix, double perTimeUnit, double perDistanceUnit, double perWaitingTimeUnit, double perLowerLatenessTimeUnit, double perUpperLatenessTimeUnit) {
            this.fix = fix;
            this.perTimeUnit = perTimeUnit;
            this.perTransportTimeUnit = perTimeUnit;
            this.perDistanceUnit = perDistanceUnit;
            this.perWaitingTimeUnit = perWaitingTimeUnit;
            this.perServiceTimeUnit = 0.0;
            this.perLowerLatenessTimeUnit = perLowerLatenessTimeUnit;
            this.perUpperLatenessTimeUnit = perUpperLatenessTimeUnit;
        }
        
        public VehicleCostParams(double fix, double perTimeUnit, double perDistanceUnit, double perWaitingTimeUnit, double perServiceTimeUnit, double perLowerLatenessTimeUnit, double perUpperLatenessTimeUnit) {
            this.fix = fix;
            this.perTimeUnit = perTimeUnit;
            this.perTransportTimeUnit = perTimeUnit;
            this.perDistanceUnit = perDistanceUnit;
            this.perWaitingTimeUnit = perWaitingTimeUnit;
            this.perServiceTimeUnit = perServiceTimeUnit;
            this.perLowerLatenessTimeUnit = perLowerLatenessTimeUnit;
            this.perUpperLatenessTimeUnit = perUpperLatenessTimeUnit;
        }

        @Override
        public String toString() {
            return "[fixed=" + fix + "][perTime=" + perTransportTimeUnit + "][perDistance=" + perDistanceUnit + "][perWaitingTimeUnit=" + perWaitingTimeUnit + "]";
        }
    }

    /**
     * Builder that builds the vehicle-type.
     *
     * @author schroeder
     */
    public static class Builder {


        public static VehicleTypeImpl.Builder newInstance(String id) {
            if (id == null) throw new IllegalArgumentException();
            return new Builder(id);
        }

        private String id;
        private int capacity = 0;
        private double maxVelo = Double.MAX_VALUE;
        /**
         * default cost values for default vehicle type
         */
        private double fixedCost = 0.0;
        private double perDistance = 1.0;
        private double perTime = 0.0;
        private double perWaitingTime = 0.0;
        private double perServiceTime = 0.0;
        private double perUpperLatenessTime = 0.0;
        private double perLowerLatenessTime = 0.0;

        private String profile = "car";

        private Capacity.Builder capacityBuilder = Capacity.Builder.newInstance();

        private Capacity capacityDimensions = null;

        private boolean dimensionAdded = false;

        private Builder(String id) {
            this.id = id;
        }

        /**
         * Sets the maximum velocity this vehicle-type can go [in meter per seconds].
         *
         * @param inMeterPerSeconds
         * @return this builder
         * @throws IllegalArgumentException if velocity is smaller than zero
         */
        public VehicleTypeImpl.Builder setMaxVelocity(double inMeterPerSeconds) {
            if (inMeterPerSeconds < 0.0) throw new IllegalArgumentException("velocity cannot be smaller than zero");
            this.maxVelo = inMeterPerSeconds;
            return this;
        }

        /**
         * Sets the fixed costs of the vehicle-type.
         * <p>
         * <p>by default it is 0.
         *
         * @param fixedCost
         * @return this builder
         * @throws IllegalArgumentException if fixedCost is smaller than zero
         */
        public VehicleTypeImpl.Builder setFixedCost(double fixedCost) {
            if (fixedCost < 0.0) throw new IllegalArgumentException("fixed costs cannot be smaller than zero");
            this.fixedCost = fixedCost;
            return this;
        }

        /**
         * Sets the cost per distance unit, for instance € per meter.
         * <p>
         * <p>by default it is 1.0
         *
         * @param perDistance
         * @return this builder
         * @throws IllegalArgumentException if perDistance is smaller than zero
         */
        public VehicleTypeImpl.Builder setCostPerDistance(double perDistance) {
            if (perDistance < 0.0) throw new IllegalArgumentException("cost per distance must not be smaller than zero");
            this.perDistance = perDistance;
            return this;
        }

        /**
         * Sets cost per time unit, for instance € per second.
         * <p>
         * <p>by default it is 0.0
         *
         * @param perTime
         * @return this builder
         * @throws IllegalArgumentException if costPerTime is smaller than zero
         * @deprecated use .setCostPerTransportTime(..) instead
         */
        @Deprecated
        public VehicleTypeImpl.Builder setCostPerTime(double perTime) {
            if (perTime < 0.0) throw new IllegalArgumentException();
            this.perTime = perTime;
            return this;
        }

        /**
         * Sets cost per time unit, for instance € per second.
         * <p>
         * <p>by default it is 0.0
         *
         * @param perTime
         * @return this builder
         * @throws IllegalArgumentException if costPerTime is smaller than zero
         */
        public VehicleTypeImpl.Builder setCostPerTransportTime(double perTime) {
            if (perTime < 0.0) throw new IllegalArgumentException();
            this.perTime = perTime;
            return this;
        }

        /**
         * Sets cost per waiting time unit, for instance € per second.
         * <p>
         * <p>by default it is 0.0
         *
         * @param perWaitingTime
         * @return this builder
         * @throws IllegalArgumentException if costPerTime is smaller than zero
         */
        public VehicleTypeImpl.Builder setCostPerWaitingTime(double perWaitingTime) {
            if (perWaitingTime < 0.0) throw new IllegalArgumentException();
            this.perWaitingTime = perWaitingTime;
            return this;
        }

        public VehicleTypeImpl.Builder setCostPerServiceTime(double perServiceTime) {
            this.perServiceTime = perServiceTime;
            return this;
        }
        
        public VehicleTypeImpl.Builder setCostPerLowerLatenessTime(double perLowerLatenessTime) {
            if (perLowerLatenessTime < 0.0) throw new IllegalStateException();
            this.perLowerLatenessTime = perLowerLatenessTime;
            return this;
        }
        
        public VehicleTypeImpl.Builder setCostPerUpperLatenessTime(double perUpperLatenessTime) {
            if (perUpperLatenessTime < 0.0) throw new IllegalStateException();
            this.perUpperLatenessTime = perUpperLatenessTime;
            return this;
        }

        /**
         * Builds the vehicle-type.
         *
         * @return VehicleTypeImpl
         */
        public VehicleTypeImpl build() {
            if (capacityDimensions == null) {
                capacityDimensions = capacityBuilder.build();
            }
            return new VehicleTypeImpl(this);
        }

        /**
         * Adds a capacity dimension.
         *
         * @param dimIndex
         * @param dimVal
         * @return the builder
         * @throws IllegalArgumentException if dimVal < 0
         * @throws IllegalArgumentException    if capacity dimension is already set
         */
        public Builder addCapacityDimension(int dimIndex, int dimVal) {
            if (dimVal < 0) throw new IllegalArgumentException("capacity value cannot be negative");
            if (capacityDimensions != null)
                throw new IllegalArgumentException("either build your dimension with build your dimensions with " +
                    "addCapacityDimension(int dimIndex, int dimVal) or set the already built dimensions with .setCapacityDimensions(Capacity capacity)." +
                    "You used both methods.");
            dimensionAdded = true;
            capacityBuilder.addDimension(dimIndex, dimVal);
            return this;
        }

        /**
         * Sets capacity dimensions.
         * <p>
         * <p>Note if you use this you cannot use <code>addCapacityDimension(int dimIndex, int dimVal)</code> anymore. Thus either build
         * your dimensions with <code>addCapacityDimension(int dimIndex, int dimVal)</code> or set the already built dimensions with
         * this method.
         *
         * @param capacity
         * @return this builder
         * @throws IllegalArgumentException if capacityDimension has already been added
         */
        public Builder setCapacityDimensions(Capacity capacity) {
            if (dimensionAdded)
                throw new IllegalArgumentException("either build your dimension with build your dimensions with " +
                    "addCapacityDimension(int dimIndex, int dimVal) or set the already built dimensions with .setCapacityDimensions(Capacity capacity)." +
                    "You used both methods.");
            this.capacityDimensions = capacity;
            return this;
        }

        public Builder setProfile(String profile) {
            this.profile = profile;
            return this;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
            + ((typeId == null) ? 0 : typeId.hashCode());
        return result;
    }

    /**
     * Two vehicle-types are equal if they have the same vehicleId.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        VehicleTypeImpl other = (VehicleTypeImpl) obj;
        if (typeId == null) {
            if (other.typeId != null)
                return false;
        } else if (!typeId.equals(other.typeId))
            return false;
        return true;
    }

    private final String typeId;

    private final int capacity;

    private final String profile;

    private final VehicleTypeImpl.VehicleCostParams vehicleCostParams;

    private final Capacity capacityDimensions;

    private final double maxVelocity;

    /**
     * priv constructor constructing vehicle-type
     *
     * @param builder
     */
    private VehicleTypeImpl(VehicleTypeImpl.Builder builder) {
        typeId = builder.id;
        capacity = builder.capacity;
        maxVelocity = builder.maxVelo;
        vehicleCostParams = new VehicleCostParams(builder.fixedCost, builder.perTime, builder.perDistance, builder.perWaitingTime, builder.perServiceTime, builder.perLowerLatenessTime, builder.perUpperLatenessTime);
        capacityDimensions = builder.capacityDimensions;
        profile = builder.profile;
    }

    /* (non-Javadoc)
     * @see basics.route.VehicleType#getTypeId()
     */
    @Override
    public String getTypeId() {
        return typeId;
    }

    /* (non-Javadoc)
     * @see basics.route.VehicleType#getVehicleCostParams()
     */
    @Override
    public VehicleTypeImpl.VehicleCostParams getVehicleCostParams() {
        return vehicleCostParams;
    }

    @Override
    public String toString() {
        return "[typeId=" + typeId + "]" +
            "[capacity=" + capacityDimensions + "]" +
            "[costs=" + vehicleCostParams + "]";
    }

    @Override
    public double getMaxVelocity() {
        return maxVelocity;
    }

    @Override
    public Capacity getCapacityDimensions() {
        return capacityDimensions;
    }

    @Override
    public String getProfile() {
        return profile;
    }

}
