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

import com.graphhopper.jsprit.core.problem.Capacity;

/**
 * Basic interface for vehicle-type-data.
 *
 * @author schroeder
 */
public interface VehicleType {

    /**
     * Returns typeId.
     *
     * @return typeId
     */
    String getTypeId();

    /**
     * Returns capacity dimensions.
     *
     * @return {@link com.graphhopper.jsprit.core.problem.Capacity}
     */
    Capacity getCapacityDimensions();

    /**
     * Returns maximum velocity of this vehicle-type.
     *
     * @return max velocity
     */
    double getMaxVelocity();

    /**
     * Return the cost-parameter of this vehicle-type.
     *
     * @return parameter
     */
    VehicleTypeImpl.VehicleCostParams getVehicleCostParams();

    String getProfile();

    /**
     * @return User-specific domain data associated with the vehicle type
     */
    Object getUserData();

}
