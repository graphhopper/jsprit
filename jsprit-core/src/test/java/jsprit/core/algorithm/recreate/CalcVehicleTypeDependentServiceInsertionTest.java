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
package jsprit.core.algorithm.recreate;

import jsprit.core.problem.Capacity;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TimeWindow;
import jsprit.core.problem.vehicle.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class CalcVehicleTypeDependentServiceInsertionTest {

    Vehicle veh1;
    Vehicle veh2;
    VehicleFleetManager fleetManager;
    Service service;
    VehicleRoute vehicleRoute;

    @Before
    public void doBefore() {
        veh1 = mock(Vehicle.class);
        veh2 = mock(Vehicle.class);
        when(veh1.getType()).thenReturn(VehicleTypeImpl.Builder.newInstance("type1").build());
        when(veh2.getType()).thenReturn(VehicleTypeImpl.Builder.newInstance("type2").build());
        when(veh1.getStartLocation()).thenReturn(Location.newInstance("loc1"));
        when(veh2.getStartLocation()).thenReturn(Location.newInstance("loc2"));
        fleetManager = mock(VehicleFleetManager.class);
        service = mock(Service.class);
        vehicleRoute = mock(VehicleRoute.class);

        when(fleetManager.getAvailableVehicles()).thenReturn(Arrays.asList(veh1, veh2));

        VehicleType type = mock(VehicleType.class);
        when(type.getCapacityDimensions()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 10).build());
        when(veh1.getType()).thenReturn(type);

        when(veh2.getType()).thenReturn(type);

        when(service.getSize()).thenReturn(Capacity.Builder.newInstance().build());
        when(service.getTimeWindow()).thenReturn(TimeWindow.newInstance(0.0, Double.MAX_VALUE));

        when(vehicleRoute.getDriver()).thenReturn(null);
        when(vehicleRoute.getVehicle()).thenReturn(VehicleImpl.createNoVehicle());
    }

    @Test
    public void whenHaving2Vehicle_calcInsertionOfCheapest() {
        JobInsertionCostsCalculator calc = mock(JobInsertionCostsCalculator.class);
        InsertionData iDataVeh1 = new InsertionData(10.0, InsertionData.NO_INDEX, 1, veh1, null);
        InsertionData iDataVeh2 = new InsertionData(20.0, InsertionData.NO_INDEX, 1, veh2, null);
        when(calc.getInsertionData(vehicleRoute, service, veh1, veh1.getEarliestDeparture(), null, Double.MAX_VALUE)).thenReturn(iDataVeh1);
        when(calc.getInsertionData(vehicleRoute, service, veh2, veh2.getEarliestDeparture(), null, Double.MAX_VALUE)).thenReturn(iDataVeh2);
        when(calc.getInsertionData(vehicleRoute, service, veh2, veh2.getEarliestDeparture(), null, 10.0)).thenReturn(iDataVeh2);
        VehicleRoutingProblem vrp = mock(VehicleRoutingProblem.class);
        when(vrp.getInitialVehicleRoutes()).thenReturn(Collections.<VehicleRoute>emptyList());
        VehicleTypeDependentJobInsertionCalculator insertion = new VehicleTypeDependentJobInsertionCalculator(vrp, fleetManager, calc);
        InsertionData iData = insertion.getInsertionData(vehicleRoute, service, null, 0.0, null, Double.MAX_VALUE);
        assertThat(iData.getSelectedVehicle(), is(veh1));

    }

    @Test
    public void whenHaving2Vehicle_calcInsertionOfCheapest2() {
        JobInsertionCostsCalculator calc = mock(JobInsertionCostsCalculator.class);
        InsertionData iDataVeh1 = new InsertionData(20.0, InsertionData.NO_INDEX, 1, veh1, null);
        InsertionData iDataVeh2 = new InsertionData(10.0, InsertionData.NO_INDEX, 1, veh2, null);
        when(calc.getInsertionData(vehicleRoute, service, veh1, veh1.getEarliestDeparture(), null, Double.MAX_VALUE)).thenReturn(iDataVeh1);
        when(calc.getInsertionData(vehicleRoute, service, veh2, veh2.getEarliestDeparture(), null, Double.MAX_VALUE)).thenReturn(iDataVeh2);
        when(calc.getInsertionData(vehicleRoute, service, veh2, veh2.getEarliestDeparture(), null, 20.0)).thenReturn(iDataVeh2);
        VehicleRoutingProblem vrp = mock(VehicleRoutingProblem.class);
        when(vrp.getInitialVehicleRoutes()).thenReturn(Collections.<VehicleRoute>emptyList());
        VehicleTypeDependentJobInsertionCalculator insertion = new VehicleTypeDependentJobInsertionCalculator(vrp, fleetManager, calc);
        InsertionData iData = insertion.getInsertionData(vehicleRoute, service, null, 0.0, null, Double.MAX_VALUE);
        assertThat(iData.getSelectedVehicle(), is(veh2));

    }
}
