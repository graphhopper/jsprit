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
package jsprit.core.problem.vehicle;

import jsprit.core.problem.Capacity;
import jsprit.core.problem.vehicle.VehicleTypeImpl.VehicleCostParams;

public class PenaltyVehicleType implements VehicleType{

	private VehicleType type;
	
	private double penaltyFactor = 2;
	
	public PenaltyVehicleType(VehicleType type) {
		super();
		this.type = type;
	}
	
	public PenaltyVehicleType(VehicleType type, double penaltyFactor) {
		super();
		this.type = type;
		this.penaltyFactor = penaltyFactor;
	}

	public double getPenaltyFactor(){
		return this.penaltyFactor;
	}

	@Override
	public String getTypeId() {
		return type.getTypeId();
	}

	@Override
	public VehicleCostParams getVehicleCostParams() {
		return type.getVehicleCostParams();
	}

	@Override
	public double getMaxVelocity() {
		return type.getMaxVelocity();
	}

	@Override
	public Capacity getCapacityDimensions() {
		return type.getCapacityDimensions();
	}

	

}
