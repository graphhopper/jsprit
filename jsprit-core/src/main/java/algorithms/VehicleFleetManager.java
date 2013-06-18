/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package algorithms;

import java.util.Collection;

import basics.route.Vehicle;
import basics.route.VehicleImpl.VehicleType;

interface VehicleFleetManager {
	
	public static class TypeKey {
		
		public final VehicleType type;
		public final String locationId;
		
		public TypeKey(VehicleType type, String locationId) {
			super();
			this.type = type;
			this.locationId = locationId;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((locationId == null) ? 0 : locationId.hashCode());
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
			TypeKey other = (TypeKey) obj;
			if (locationId == null) {
				if (other.locationId != null)
					return false;
			} else if (!locationId.equals(other.locationId))
				return false;
			if (type == null) {
				if (other.type != null)
					return false;
			} else if (!type.equals(other.type))
				return false;
			return true;
		}
		
		
	}

	public abstract Vehicle getEmptyVehicle(TypeKey typeId);

	public abstract Collection<TypeKey> getAvailableVehicleTypes();

	public abstract void lock(Vehicle vehicle);

	public abstract void unlock(Vehicle vehicle);

	public abstract Collection<TypeKey> getAvailableVehicleTypes(TypeKey withoutThisType);

	public abstract boolean isLocked(Vehicle vehicle);

	public abstract void unlockAll();

}
