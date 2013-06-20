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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import basics.route.Vehicle;

class InfiniteVehicles implements VehicleFleetManager{

//	static class TypeKeyComparator implements Comparator<TypeKey>{
//
//		@Override
//		public int compare(TypeKey k1, TypeKey k2) {
//			double k1_fix = k1.type.getVehicleCostParams().fix;
//			double k2_fix = k2.type.getVehicleCostParams().fix;
//			return (int)(k1_fix - k2_fix);
//		}
//		
//	}
	
	private static Logger logger = Logger.getLogger(InfiniteVehicles.class);
	
	private Map<TypeKey,Vehicle> types = new HashMap<TypeKey, Vehicle>();
	
	private List<TypeKey> sortedTypes = new ArrayList<VehicleFleetManager.TypeKey>();
		
	public InfiniteVehicles(Collection<Vehicle> vehicles){
		extractTypes(vehicles);
		logger.info("initialise " + this);
	}
	
	@Override
	public String toString() {
		return "[name=infiniteVehicle]";
	}

	private void extractTypes(Collection<Vehicle> vehicles) {
		for(Vehicle v : vehicles){
			TypeKey typeKey = new TypeKey(v.getType().getTypeId(),v.getLocationId());
			types.put(typeKey,v);
			sortedTypes.add(typeKey);

		}
//		Collections.sort(sortedTypes, new TypeKeyComparator());
	}

	
	@Override
	public Vehicle getEmptyVehicle(TypeKey typeId) {
		return types.get(typeId);
	}

	@Override
	public Collection<TypeKey> getAvailableVehicleTypes() {
		return sortedTypes;
	}

	@Override
	public void lock(Vehicle vehicle) {
		
	}

	@Override
	public void unlock(Vehicle vehicle) {
		
	}

	@Override
	public Collection<TypeKey> getAvailableVehicleTypes(TypeKey withoutThisType) {
		Set<TypeKey> typeSet = new HashSet<TypeKey>(types.keySet());
		typeSet.remove(withoutThisType);
		return typeSet;
	}

	@Override
	public boolean isLocked(Vehicle vehicle) {
		return false;
	}

	@Override
	public void unlockAll() {
		
	}

	@Override
	public Collection<? extends Vehicle> getAvailableVehicles() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends Vehicle> getAvailableVehicle(
			String withoutThisType, String locationId) {
		// TODO Auto-generated method stub
		return null;
	}

}
