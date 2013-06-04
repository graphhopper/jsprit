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
package readers;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import basics.Service;
import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblem.FleetSize;
import basics.route.Vehicle;

public class ChristophidesReaderTest {
	
	@Test
	public void whenReadingInstance_nuOfCustomersIsCorrect(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new ChristophidesReader(builder).read(this.getClass().getClassLoader().getResource("vrpnc1.txt").getPath());
		VehicleRoutingProblem vrp = builder.build();
		assertEquals(50,vrp.getJobs().values().size());
	}
	
	@Test
	public void whenReadingInstance_fleetSizeIsInfinite(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new ChristophidesReader(builder).read(this.getClass().getClassLoader().getResource("vrpnc1.txt").getPath());
		VehicleRoutingProblem vrp = builder.build();
		assertEquals(FleetSize.INFINITE,vrp.getFleetSize());
	}
	
	@Test
	public void whenReadingInstance_vehicleCapacitiesAreCorrect(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new ChristophidesReader(builder).read(this.getClass().getClassLoader().getResource("vrpnc1.txt").getPath());
		VehicleRoutingProblem vrp = builder.build();
		for(Vehicle v : vrp.getVehicles()){
			assertEquals(160,v.getCapacity());
		}
	}
	
	@Test
	public void whenReadingInstance_vehicleLocationsAreCorrect_and_correspondToDepotLocation(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new ChristophidesReader(builder).read(this.getClass().getClassLoader().getResource("vrpnc1.txt").getPath());
		VehicleRoutingProblem vrp = builder.build();
		for(Vehicle v : vrp.getVehicles()){
			assertEquals(30.0,v.getCoord().getX(),0.01);
			assertEquals(40.0,v.getCoord().getY(),0.01);
		}
	}
	
	@Test
	public void whenReadingInstance_vehicleDurationsAreCorrect(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new ChristophidesReader(builder).read(this.getClass().getClassLoader().getResource("vrpnc13.txt").getPath());
		VehicleRoutingProblem vrp = builder.build();
		for(Vehicle v : vrp.getVehicles()){
			assertEquals(0.0,v.getEarliestDeparture(),0.01);
			assertEquals(720.0,v.getLatestArrival()-v.getEarliestDeparture(),0.01);
		}
	}
	
	@Test
	public void whenReadingInstance_demandOfCustomerOneIsCorrect(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new ChristophidesReader(builder).read(this.getClass().getClassLoader().getResource("vrpnc1.txt").getPath());
		VehicleRoutingProblem vrp = builder.build();
		assertEquals(7,vrp.getJobs().get("1").getCapacityDemand());
	}
	
	@Test
	public void whenReadingInstance_serviceDurationOfCustomerTwoIsCorrect(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new ChristophidesReader(builder).read(this.getClass().getClassLoader().getResource("vrpnc13.txt").getPath());
		VehicleRoutingProblem vrp = builder.build();
		assertEquals(50.0,((Service)vrp.getJobs().get("2")).getServiceDuration(),0.1);
	}
	
	
	

}
