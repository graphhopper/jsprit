/*******************************************************************************
 * Copyright (c) 2014 Stefan Schroeder.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 3.0 of the License, or (at your option) any later version.
 *  
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package jsprit.core.problem.vehicle;

import jsprit.core.problem.AbstractVehicle;

/**
 * Key to identify similar vehicles
 * 
 * <p>Two vehicles are equal if they share the same type, the same start and end-location and the same earliestStart and latestStart.
 * 
 * @author stefan
 *
 */
public class VehicleTypeKey extends AbstractVehicle.AbstractTypeKey{
	
	public final String type;
	public final String startLocationId;
	public final String endLocationId;
	public final double earliestStart;
	public final double latestEnd;
	
	public VehicleTypeKey(String typeId, String startLocationId, String endLocationId, double earliestStart, double latestEnd) {
		super();
		this.type = typeId;
		this.startLocationId = startLocationId;
		this.endLocationId = endLocationId;
		this.earliestStart = earliestStart;
		this.latestEnd = latestEnd;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(earliestStart);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result
				+ ((endLocationId == null) ? 0 : endLocationId.hashCode());
		temp = Double.doubleToLongBits(latestEnd);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result
				+ ((startLocationId == null) ? 0 : startLocationId.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VehicleTypeKey other = (VehicleTypeKey) obj;
		if (Double.doubleToLongBits(earliestStart) != Double
				.doubleToLongBits(other.earliestStart))
			return false;
		if (endLocationId == null) {
			if (other.endLocationId != null)
				return false;
		} else if (!endLocationId.equals(other.endLocationId))
			return false;
		if (Double.doubleToLongBits(latestEnd) != Double
				.doubleToLongBits(other.latestEnd))
			return false;
		if (startLocationId == null) {
			if (other.startLocationId != null)
				return false;
		} else if (!startLocationId.equals(other.startLocationId))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(type).append("_").append(startLocationId).append("_").append(endLocationId)
			.append("_").append(Double.toString(earliestStart)).append("_").append(Double.toString(latestEnd));
		return stringBuilder.toString();
	}

	
	
}
