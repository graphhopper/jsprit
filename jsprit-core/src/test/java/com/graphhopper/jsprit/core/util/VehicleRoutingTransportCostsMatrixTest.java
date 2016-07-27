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

public class VehicleRoutingTransportCostsMatrixTest {

    @Test
    public void whenAddingDistanceToSymmetricMatrix_itShouldReturnCorrectValues() {
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
        matrixBuilder.addTransportDistance("1", "2", 2.);
        VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
        assertEquals(2., matrix.getTransportCost(loc("1"), loc("2"), 0.0, null, null), 0.1);
        assertEquals(2., matrix.getDistance("1", "2"), 0.1);
        assertEquals(2., matrix.getTransportCost(loc("2"), loc("1"), 0.0, null, null), 0.1);
        assertEquals(2., matrix.getDistance("2", "1"), 0.1);
    }

    @Test
    public void whenAddingDistanceToSymmetricMatrixUsingStringAsKey_itShouldReturnCorrectValues() {
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
        matrixBuilder.addTransportDistance("from", "to", 2.);
        VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
        assertEquals(2., matrix.getTransportCost(loc("from"), loc("to"), 0.0, null, null), 0.1);
        assertEquals(2., matrix.getDistance("from", "to"), 0.1);
        assertEquals(2., matrix.getTransportCost(loc("to"), loc("from"), 0.0, null, null), 0.1);
        assertEquals(2., matrix.getDistance("from", "to"), 0.1);
    }

    @Test
    public void whenAddingDistanceToSymmetricMatrixWhereKeyAlreadyExists_itShouldOverrideValues() {
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
        matrixBuilder.addTransportDistance("from", "to", 2.);
        //overide
        matrixBuilder.addTransportDistance("from", "to", 4.);
        VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();

        assertEquals(4., matrix.getTransportCost(loc("from"), loc("to"), 0.0, null, null), 0.1);
        assertEquals(4., matrix.getDistance("from", "to"), 0.1);
        assertEquals(4., matrix.getTransportCost(loc("to"), loc("from"), 0.0, null, null), 0.1);
        assertEquals(4., matrix.getDistance("from", "to"), 0.1);
    }

    @Test
    public void whenAddingDistanceToSymmetricMatrixWhereReverseKeyAlreadyExists_itShouldOverrideValues() {
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
        matrixBuilder.addTransportDistance("from", "to", 2.);
        //overide
        matrixBuilder.addTransportDistance("to", "from", 4.);
        VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();

        assertEquals(4., matrix.getTransportCost(loc("from"), loc("to"), 0.0, null, null), 0.1);
        assertEquals(4., matrix.getDistance("from", "to"), 0.1);
        assertEquals(4., matrix.getTransportCost(loc("to"), loc("from"), 0.0, null, null), 0.1);
        assertEquals(4., matrix.getDistance("from", "to"), 0.1);
    }

    @Test
    public void whenAddingDistanceToAsymmetricMatrix_itShouldReturnCorrectValues() {
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);
        matrixBuilder.addTransportDistance("1", "2", 2.);
        VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
        assertEquals(2., matrix.getTransportCost(loc("1"), loc("2"), 0.0, null, null), 0.1);
    }

    private Location loc(String s) {
        return Location.Builder.newInstance().setId(s).build();
    }

    @Test(expected = IllegalStateException.class)
    public void whenRequestingRelationThatDoesNotExist_itShouldThrowException() {
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);
        matrixBuilder.addTransportDistance("1", "2", 2.);
        VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
        matrix.getTransportCost(loc("2"), loc("1"), 0.0, null, null);
    }

    @Test
    public void whenAddingDistanceToAsymmetricMatrixUsingStringAsKey_itShouldReturnCorrectValues() {
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);
        matrixBuilder.addTransportDistance("from", "to", 2.);
        matrixBuilder.addTransportDistance("to", "from", 4.);
        VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
        assertEquals(2., matrix.getTransportCost(loc("from"), loc("to"), 0.0, null, null), 0.1);
        assertEquals(4., matrix.getTransportCost(loc("to"), loc("from"), 0.0, null, null), 0.1);
    }

    @Test
    public void whenAddingTimeToSymmetricMatrix_itShouldReturnCorrectValues() {
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
        matrixBuilder.addTransportTime("1", "2", 2.);
        VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
        assertEquals(2., matrix.getTransportTime(loc("1"), loc("2"), 0.0, null, null), 0.1);
        assertEquals(2., matrix.getTransportTime(loc("2"), loc("1"), 0.0, null, null), 0.1);
    }

    @Test
    public void whenAddingTimeToSymmetricMatrixUsingStringAsKey_itShouldReturnCorrectValues() {
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
        matrixBuilder.addTransportTime("from", "to", 2.);
        VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
        assertEquals(2., matrix.getTransportTime(loc("from"), loc("to"), 0.0, null, null), 0.1);
        assertEquals(2., matrix.getTransportTime(loc("to"), loc("from"), 0.0, null, null), 0.1);
    }

    @Test
    public void whenAddingTimeToAsymmetricMatrix_itShouldReturnCorrectValues() {
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);
        matrixBuilder.addTransportTime("1", "2", 2.);
        VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
        assertEquals(2., matrix.getTransportTime(loc("1"), loc("2"), 0.0, null, null), 0.1);
    }

    @Test(expected = IllegalStateException.class)
    public void whenRequestingTimeOfRelationThatDoesNotExist_itShouldThrowException() {
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);
        matrixBuilder.addTransportTime("1", "2", 2.);
        VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
        matrix.getTransportTime(loc("2"), loc("1"), 0.0, null, null);
    }

    @Test
    public void whenAddingTimeToAsymmetricMatrixUsingStringAsKey_itShouldReturnCorrectValues() {
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);
        matrixBuilder.addTransportTime("from", "to", 2.);
        matrixBuilder.addTransportTime("to", "from", 4.);
        VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
        assertEquals(2., matrix.getTransportTime(loc("from"), loc("to"), 0.0, null, null), 0.1);
        assertEquals(4., matrix.getTransportTime(loc("to"), loc("from"), 0.0, null, null), 0.1);
    }

    @Test
    public void whenAddingTimeToAsymmetricMatrixUsingStringAsKey_itShouldReturnCorrectCostValues() {
        VehicleType type = VehicleTypeImpl.Builder.newInstance("t").setCostPerDistance(0.).setCostPerTime(1.).build();
        Vehicle vehicle = mock(Vehicle.class);
        when(vehicle.getType()).thenReturn(type);
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);
        matrixBuilder.addTransportTime("from", "to", 2.);
        matrixBuilder.addTransportTime("to", "from", 4.);
        VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
//		assertEquals(2.,matrix.getTransportTime("from", "to", 0.0, null, null),0.1);
//		assertEquals(4.,matrix.getTransportTime("to", "from", 0.0, null, null),0.1);
        assertEquals(2., matrix.getTransportCost(loc("from"), loc("to"), 0.0, null, vehicle), 0.1);
        assertEquals(4., matrix.getTransportCost(loc("to"), loc("from"), 0.0, null, vehicle), 0.1);
    }

    @Test
    public void whenAddingTimeAndDistanceToSymmetricMatrix_itShouldReturnCorrectValues() {
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
        matrixBuilder.addTransportDistance("1", "2", 20.);
        matrixBuilder.addTransportTime("1", "2", 2.);
        VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
        Vehicle vehicle = mock(Vehicle.class);
        VehicleType type = VehicleTypeImpl.Builder.newInstance("t").setCostPerDistance(1.).setCostPerTime(2.).build();
        when(vehicle.getType()).thenReturn(type);
        assertEquals(24., matrix.getTransportCost(loc("1"), loc("2"), 0.0, null, vehicle), 0.1);
        assertEquals(24., matrix.getTransportCost(loc("2"), loc("1"), 0.0, null, vehicle), 0.1);
    }

    @Test
    public void whenAddingTimeAndDistanceToSymmetricMatrixUsingStringAsKey_itShouldReturnCorrectValues() {
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
        matrixBuilder.addTransportTime("from", "to", 2.);
        Vehicle vehicle = mock(Vehicle.class);
        VehicleType type = VehicleTypeImpl.Builder.newInstance("t").setCostPerDistance(1.).setCostPerTime(2.).build();
        when(vehicle.getType()).thenReturn(type);
        VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
        assertEquals(4., matrix.getTransportCost(loc("from"), loc("to"), 0.0, null, vehicle), 0.1);
        assertEquals(4., matrix.getTransportCost(loc("to"), loc("from"), 0.0, null, vehicle), 0.1);
    }

    @Test
    public void whenAddingTimeAndDistanceToAsymmetricMatrix_itShouldReturnCorrectValues() {
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);
        matrixBuilder.addTransportTime("1", "2", 2.);
        matrixBuilder.addTransportTime("2", "1", 8.);
        VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
        Vehicle vehicle = mock(Vehicle.class);
        VehicleType type = VehicleTypeImpl.Builder.newInstance("t").setCostPerDistance(1.).setCostPerTime(2.).build();
        when(vehicle.getType()).thenReturn(type);
        assertEquals(4., matrix.getTransportCost(loc("1"), loc("2"), 0.0, null, vehicle), 0.1);
        assertEquals(16., matrix.getTransportCost(loc("2"), loc("1"), 0.0, null, vehicle), 0.1);
    }

    @Test
    public void whenAddingTimeAndDistanceToAsymmetricMatrixUsingStringAsKey_itShouldReturnCorrectValues() {
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);
        matrixBuilder.addTransportTime("from", "to", 2.);
        matrixBuilder.addTransportDistance("from", "to", 1.);
        matrixBuilder.addTransportTime("to", "from", 4.);
        matrixBuilder.addTransportDistance("to", "from", 3.);
        VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
        Vehicle vehicle = mock(Vehicle.class);
        VehicleType type = VehicleTypeImpl.Builder.newInstance("t").setCostPerDistance(1.).setCostPerTime(2.).build();
        when(vehicle.getType()).thenReturn(type);
        assertEquals(5., matrix.getTransportCost(loc("from"), loc("to"), 0.0, null, vehicle), 0.1);
        assertEquals(11., matrix.getTransportCost(loc("to"), loc("from"), 0.0, null, vehicle), 0.1);
    }


    @Test
    public void whenAddingTimeAndDistanceToAsymmetricMatrixUsingStringAsKey_itShouldReturnCorrectCostValues() {
        VehicleType type = VehicleTypeImpl.Builder.newInstance("t").setCostPerDistance(2.).setCostPerTime(1.).build();
        Vehicle vehicle = mock(Vehicle.class);
        when(vehicle.getType()).thenReturn(type);
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);
        matrixBuilder.addTransportTime("from", "to", 2.);
        matrixBuilder.addTransportDistance("from", "to", 3.);
        matrixBuilder.addTransportTime("to", "from", 4.);
        matrixBuilder.addTransportDistance("to", "from", 5.);
        VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();

        assertEquals(8., matrix.getTransportCost(loc("from"), loc("to"), 0.0, null, vehicle), 0.1);
        assertEquals(14., matrix.getTransportCost(loc("to"), loc("from"), 0.0, null, vehicle), 0.1);
    }

    @Test
    public void whenAddingTimeAndDistanceToSymmetricMatrixUsingStringAsKey_and_overridesEntry_itShouldReturnCorrectValues() {
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);

        matrixBuilder.addTransportDistance("from", "to", 1.);
        matrixBuilder.addTransportTime("from", "to", 2.);

        matrixBuilder.addTransportDistance("to", "from", 1.);
        matrixBuilder.addTransportTime("to", "from", 2.);

        VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
        Vehicle vehicle = mock(Vehicle.class);
        VehicleType type = VehicleTypeImpl.Builder.newInstance("t").setCostPerDistance(1.).setCostPerTime(0.).build();
        when(vehicle.getType()).thenReturn(type);
        assertEquals(1., matrix.getDistance("from", "to"), 0.1);
        assertEquals(1., matrix.getDistance("to", "from"), 0.1);
        assertEquals(1., matrix.getTransportCost(loc("from"), loc("to"), 0.0, null, vehicle), 0.1);
        assertEquals(1., matrix.getTransportCost(loc("to"), loc("from"), 0.0, null, vehicle), 0.1);
    }

}
