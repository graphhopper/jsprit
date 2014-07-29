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
import jsprit.core.problem.Skills;

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
    public final Skills skills;
	
	public VehicleTypeKey(String typeId, String startLocationId, String endLocationId, double earliestStart, double latestEnd, Skills skills) {
		super();
		this.type = typeId;
		this.startLocationId = startLocationId;
		this.endLocationId = endLocationId;
		this.earliestStart = earliestStart;
		this.latestEnd = latestEnd;
        this.skills = skills;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VehicleTypeKey)) return false;

        VehicleTypeKey that = (VehicleTypeKey) o;

        if (Double.compare(that.earliestStart, earliestStart) != 0) return false;
        if (Double.compare(that.latestEnd, latestEnd) != 0) return false;
        if (!endLocationId.equals(that.endLocationId)) return false;
        if (!skills.equals(that.skills)) return false;
        if (!startLocationId.equals(that.startLocationId)) return false;
        if (!type.equals(that.type)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = type.hashCode();
        result = 31 * result + startLocationId.hashCode();
        result = 31 * result + endLocationId.hashCode();
        temp = Double.doubleToLongBits(earliestStart);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(latestEnd);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + skills.hashCode();
        return result;
    }

    @Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(type).append("_").append(startLocationId).append("_").append(endLocationId)
			.append("_").append(Double.toString(earliestStart)).append("_").append(Double.toString(latestEnd));
		return stringBuilder.toString();
	}

	
	
}
