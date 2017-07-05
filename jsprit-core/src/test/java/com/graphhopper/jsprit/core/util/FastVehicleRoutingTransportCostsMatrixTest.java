/*
 * Licensed to GraphHopper GmbH under one or more contributor
 * license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * GraphHopper GmbH licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.graphhopper.jsprit.core.util;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
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
    public void whenAddingTimeAndDistanceToSymmetricMatrix_itShouldReturnCorrectValues2() {
        FastVehicleRoutingTransportCostsMatrix.Builder matrixBuilder = FastVehicleRoutingTransportCostsMatrix.Builder.newInstance(3, true);
        matrixBuilder.addTransportTimeAndDistance(1, 2, 2., 100.);
        FastVehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
        assertEquals(2., matrix.getTransportTime(loc(1), loc(2), 0.0, null, null), 0.1);
        assertEquals(2., matrix.getTransportTime(loc(2), loc(1), 0.0, null, null), 0.1);

        assertEquals(100., matrix.getDistance(loc(1), loc(2), 0.0, null), 0.1);
        assertEquals(100., matrix.getDistance(loc(2), loc(1), 0.0, null), 0.1);
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
