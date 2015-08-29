/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
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
package jsprit.core.util;

import jsprit.core.problem.Location;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FastVehicleRoutingTransportCostsMatrixTest {

    @Test
    public void whenAddingDistanceToSymmetricMatrix_itShouldReturnCorrectValues() {
        FastVehicleRoutingTransportCostsMatrix.Builder matrixBuilder = FastVehicleRoutingTransportCostsMatrix.Builder.newInstance(3, true);
        matrixBuilder.addTransportDistance(1, 2, 2.);
        FastVehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
        assertEquals(2., matrix.getTransportCost(loc(1), loc(2), 0.0, null, null), 0.1);
        assertEquals(2., matrix.getDistance(1, 2), 0.1);
        assertEquals(2., matrix.getTransportCost(loc(2), loc(1), 0.0, null, null), 0.1);
        assertEquals(2., matrix.getDistance(2, 1), 0.1);
    }

    @Test
    public void whenAddingDistanceToAsymmetricMatrix_itShouldReturnCorrectValues() {
        FastVehicleRoutingTransportCostsMatrix.Builder matrixBuilder = FastVehicleRoutingTransportCostsMatrix.Builder.newInstance(3, false);
        matrixBuilder.addTransportDistance(1, 2, 2.);
        FastVehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
        assertEquals(2., matrix.getTransportCost(loc(1), loc(2), 0.0, null, null), 0.1);
    }

    private Location loc(int index) {
        return Location.Builder.newInstance().setIndex(index).build();
    }


    @Test
    public void whenAddingTimeToSymmetricMatrix_itShouldReturnCorrectValues() {
        FastVehicleRoutingTransportCostsMatrix.Builder matrixBuilder = FastVehicleRoutingTransportCostsMatrix.Builder.newInstance(3, true);
        matrixBuilder.addTransportTime(1, 2, 2.);
        FastVehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
        assertEquals(2., matrix.getTransportTime(loc(1), loc(2), 0.0, null, null), 0.1);
        assertEquals(2., matrix.getTransportTime(loc(2), loc(1), 0.0, null, null), 0.1);
    }

    @Test
    public void whenAddingTimeToAsymmetricMatrix_itShouldReturnCorrectValues() {
        FastVehicleRoutingTransportCostsMatrix.Builder matrixBuilder = FastVehicleRoutingTransportCostsMatrix.Builder.newInstance(3, false);
        matrixBuilder.addTransportTime(1, 2, 2.);
        FastVehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
        assertEquals(2., matrix.getTransportTime(loc(1), loc(2), 0.0, null, null), 0.1);
    }

    @Test
    public void whenAddingTimeAndDistanceToSymmetricMatrix_itShouldReturnCorrectValues() {
        FastVehicleRoutingTransportCostsMatrix.Builder matrixBuilder = FastVehicleRoutingTransportCostsMatrix.Builder.newInstance(3, true);
        matrixBuilder.addTransportDistance(1, 2, 20.);
        matrixBuilder.addTransportTime(1, 2, 2.);
        FastVehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
        Vehicle vehicle = mock(Vehicle.class);
        VehicleType type = VehicleTypeImpl.Builder.newInstance("t").setCostPerDistance(1.).setCostPerTime(2.).build();
        when(vehicle.getType()).thenReturn(type);
        assertEquals(24., matrix.getTransportCost(loc(1), loc(2), 0.0, null, vehicle), 0.1);
        assertEquals(24., matrix.getTransportCost(loc(2), loc(1), 0.0, null, vehicle), 0.1);
    }

    @Test
    public void whenAddingTimeAndDistanceToAsymmetricMatrix_itShouldReturnCorrectValues() {
        FastVehicleRoutingTransportCostsMatrix.Builder matrixBuilder = FastVehicleRoutingTransportCostsMatrix.Builder.newInstance(3, false);
        matrixBuilder.addTransportTime(1, 2, 2.);
        matrixBuilder.addTransportTime(2, 1, 8.);
        FastVehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
        Vehicle vehicle = mock(Vehicle.class);
        VehicleType type = VehicleTypeImpl.Builder.newInstance("t").setCostPerDistance(1.).setCostPerTime(2.).build();
        when(vehicle.getType()).thenReturn(type);
        assertEquals(4., matrix.getTransportCost(loc(1), loc(2), 0.0, null, vehicle), 0.1);
        assertEquals(16., matrix.getTransportCost(loc(2), loc(1), 0.0, null, vehicle), 0.1);
    }


}
