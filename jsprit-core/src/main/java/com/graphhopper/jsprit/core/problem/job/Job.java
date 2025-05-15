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


import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.HasId;
import com.graphhopper.jsprit.core.problem.HasIndex;
import com.graphhopper.jsprit.core.problem.Skills;

import java.util.List;

/**
 * Basic interface for all jobs.
 *
 * @author schroeder
 */
public interface Job extends HasId, HasIndex {

    enum Type {
        SHIPMENT,
        SERVICE,
        PICKUP_SERVICE,
        DELIVERY_SERVICE,
        BREAK_SERVICE,
        EN_ROUTE_DELIVERY,
        EN_ROUTE_PICKUP;

        public boolean isShipment() {
            return this == SHIPMENT;
        }

        public boolean isService() {
            return !isShipment();
        }

        public boolean isPickup() {
            return this == PICKUP_SERVICE;
        }

        public boolean isDelivery() {
            return this == DELIVERY_SERVICE;
        }

        public boolean isBreak() {
            return this == BREAK_SERVICE;
        }
    }

    // Add default method for type to maintain backward compatibility
    default Type getJobType() {
        // Default implementation based on class type
        if (this instanceof Shipment) return Type.SHIPMENT;
        if (this instanceof Pickup) return Type.PICKUP_SERVICE;
        if (this instanceof Delivery) return Type.DELIVERY_SERVICE;
        if (this instanceof Break) return Type.BREAK_SERVICE;
        if (this instanceof Service) return Type.SERVICE;
        throw new IllegalStateException("Unknown job type: " + this.getClass());
    }

    /**
     * Indicates whether this job is picked up at the vehicle's start location.
     * By default, only Delivery jobs are picked up at the vehicle's start location.
     *
     * @return true if job is picked up at vehicle start, false otherwise
     */
    default boolean isPickedUpAtVehicleStart() {
        return this instanceof Delivery;
    }

    /**
     * Indicates whether this job is delivered to the vehicle's end location.
     * By default, only Pickup and Service jobs are delivered to the vehicle's end location.
     *
     * @return true if job is delivered to vehicle end, false otherwise
     */
    default boolean isDeliveredToVehicleEnd() {
        return this instanceof Pickup || (this instanceof Service && !(this instanceof Delivery));
    }


    /**
     * Returns the unique identifier (id) of a job.
     *
     * @return id
     */
    String getId();

    /**
     * Returns size, i.e. capacity-demand, of this job which can consist of an arbitrary number of capacity dimensions.
     *
     * @return Capacity
     */
    Capacity getSize();

    Skills getRequiredSkills();

    /**
     * Returns name.
     *
     * @return name
     */
    String getName();

    /**
     * Get priority of job. Only 1 (very high) to 10 (very low) are allowed.
     * <p>
     * Default is 2.
     *
     * @return priority
     */
    int getPriority();

    double getMaxTimeInVehicle();

    List<Activity> getActivities();

}
