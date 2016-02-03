/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
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
    public String getTypeId();

    /**
     * Returns capacity dimensions.
     *
     * @return {@link com.graphhopper.jsprit.core.problem.Capacity}
     */
    public Capacity getCapacityDimensions();

    /**
     * Returns maximum velocity of this vehicle-type.
     *
     * @return max velocity
     */
    public double getMaxVelocity();

    /**
     * Return the cost-parameter of this vehicle-type.
     *
     * @return parameter
     */
    public VehicleTypeImpl.VehicleCostParams getVehicleCostParams();

    public String getProfile();

}
