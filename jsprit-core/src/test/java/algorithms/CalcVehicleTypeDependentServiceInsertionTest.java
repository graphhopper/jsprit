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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import basics.Service;
import basics.route.TimeWindow;
import basics.route.Vehicle;
import basics.route.VehicleImpl;
import basics.route.VehicleRoute;
import basics.route.VehicleTypeImpl;





public class CalcVehicleTypeDependentServiceInsertionTest {
	
	Vehicle veh1;
	Vehicle veh2;
	VehicleFleetManager fleetManager;
	Service service;
	VehicleRoute vehicleRoute;
	
	@Before
	public void doBefore(){
		veh1 = mock(Vehicle.class);
		veh2 = mock(Vehicle.class);
		when(veh1.getType()).thenReturn(VehicleTypeImpl.Builder.newInstance("type1", 0).build());
		when(veh2.getType()).thenReturn(VehicleTypeImpl.Builder.newInstance("type2", 0).build());
		when(veh1.getLocationId()).thenReturn("loc1");
		when(veh2.getLocationId()).thenReturn("loc2");
		fleetManager = mock(VehicleFleetManager.class);
		service = mock(Service.class);
		vehicleRoute = mock(VehicleRoute.class);
		
		when(veh1.getCapacity()).thenReturn(10);
		when(veh2.getCapacity()).thenReturn(10);
		
		when(service.getCapacityDemand()).thenReturn(0);
		when(service.getTimeWindow()).thenReturn(TimeWindow.newInstance(0.0, Double.MAX_VALUE));
		
		when(vehicleRoute.getDriver()).thenReturn(null);
		when(vehicleRoute.getVehicle()).thenReturn(VehicleImpl.createNoVehicle());
	}
	
	@Test
	public void whenHaving2Vehicle_calcInsertionOfCheapest(){
		JobInsertionCalculator calc = mock(JobInsertionCalculator.class);
		InsertionData iDataVeh1 = new InsertionData(10.0,InsertionData.NO_INDEX, 1, veh1, null);
		InsertionData iDataVeh2 = new InsertionData(20.0,InsertionData.NO_INDEX, 1, veh2, null);
		when(calc.calculate(vehicleRoute, service, veh1, veh1.getEarliestDeparture(), null, Double.MAX_VALUE)).thenReturn(iDataVeh1);
		when(calc.calculate(vehicleRoute, service, veh2, veh2.getEarliestDeparture(), null, Double.MAX_VALUE)).thenReturn(iDataVeh2);
		when(calc.calculate(vehicleRoute, service, veh2, veh2.getEarliestDeparture(), null, 10.0)).thenReturn(iDataVeh2);
		CalculatesVehTypeDepServiceInsertion insertion = new CalculatesVehTypeDepServiceInsertion(fleetManager,calc);
		InsertionData iData = insertion.calculate(vehicleRoute, service, null, 0.0, null, Double.MAX_VALUE);
		assertThat(iData.getSelectedVehicle(), is(veh1));

	}

	@Test
	public void whenHaving2Vehicle_calcInsertionOfCheapest2(){
		JobInsertionCalculator calc = mock(JobInsertionCalculator.class);
		InsertionData iDataVeh1 = new InsertionData(20.0,InsertionData.NO_INDEX, 1, veh1, null);
		InsertionData iDataVeh2 = new InsertionData(10.0,InsertionData.NO_INDEX, 1, veh2, null);
		when(calc.calculate(vehicleRoute, service, veh1, veh1.getEarliestDeparture(), null, Double.MAX_VALUE)).thenReturn(iDataVeh1);
		when(calc.calculate(vehicleRoute, service, veh2, veh2.getEarliestDeparture(), null, Double.MAX_VALUE)).thenReturn(iDataVeh2);
		when(calc.calculate(vehicleRoute, service, veh2, veh2.getEarliestDeparture(), null, 20.0)).thenReturn(iDataVeh2);
		CalculatesVehTypeDepServiceInsertion insertion = new CalculatesVehTypeDepServiceInsertion(fleetManager,calc);
		InsertionData iData = insertion.calculate(vehicleRoute, service, null, 0.0, null, Double.MAX_VALUE);
		assertThat(iData.getSelectedVehicle(), is(veh2));

	}
}
