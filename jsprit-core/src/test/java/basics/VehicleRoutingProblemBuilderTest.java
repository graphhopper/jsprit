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
package basics;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import basics.VehicleRoutingProblem.FleetSize;
import basics.route.Vehicle;
import basics.route.VehicleImpl;
import basics.route.VehicleTypeImpl;

public class VehicleRoutingProblemBuilderTest {
	
	@Test
	public void buildsProblemWithInfiniteVehiclesCorrectly(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		builder.setFleetSize(FleetSize.INFINITE);
//		Depot depot = new Depot("depot1", Coordinate.newInstance(0, 0));
//		builder.assignVehicleType(depot, VehicleType.Builder.newInstance("t1", 20).build());
//		builder.assignVehicleType(depot, VehicleType.Builder.newInstance("t2", 200).build());
		VehicleRoutingProblem vrp = builder.build();
		assertEquals(FleetSize.INFINITE,vrp.getFleetSize());
		
	}

	@Test
	public void buildsProblemWithFiniteVehiclesCorrectly_checkVehiclesAndTypesSizes(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		builder.setFleetSize(FleetSize.FINITE);
		
		VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("t1", 20).build();
		VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("t2", 200).build();
		
		Vehicle v1 = VehicleImpl.VehicleBuilder.newInstance("v1").setLocationId("yo").setType(type1).build();
		Vehicle v2 = VehicleImpl.VehicleBuilder.newInstance("v2").setLocationId("yo").setType(type1).build();
		Vehicle v3 = VehicleImpl.VehicleBuilder.newInstance("v3").setLocationId("yo").setType(type2).build();
		Vehicle v4 = VehicleImpl.VehicleBuilder.newInstance("v4").setLocationId("yo").setType(type2).build();
		
		builder.addVehicle(v1);
		builder.addVehicle(v2);
		builder.addVehicle(v3);
		builder.addVehicle(v4);
		
		VehicleRoutingProblem vrp = builder.build();
		assertEquals(FleetSize.FINITE,vrp.getFleetSize());
		assertEquals(4,vrp.getVehicles().size());
		assertEquals(2,vrp.getTypes().size());

	}
	

}
