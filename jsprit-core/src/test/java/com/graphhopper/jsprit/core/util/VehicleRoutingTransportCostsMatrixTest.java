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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Vehicle Routing Transport Costs Matrix Test")
class VehicleRoutingTransportCostsMatrixTest {

    @Test
    @DisplayName("When Adding Distance To Symmetric Matrix _ it Should Return Correct Values")
    void whenAddingDistanceToSymmetricMatrix_itShouldReturnCorrectValues() {
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
        matrixBuilder.addTransportDistance("1", "2", 2.);
        VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
        Assertions.assertEquals(2., matrix.getTransportCost(loc("1"), loc("2"), 0.0, null, null), 0.1);
        Assertions.assertEquals(2., matrix.getDistance("1", "2"), 0.1);
        Assertions.assertEquals(2., matrix.getTransportCost(loc("2"), loc("1"), 0.0, null, null), 0.1);
        Assertions.assertEquals(2., matrix.getDistance("2", "1"), 0.1);
    }

    @Test
    @DisplayName("When Adding Distance To Symmetric Matrix Using String As Key _ it Should Return Correct Values")
    void whenAddingDistanceToSymmetricMatrixUsingStringAsKey_itShouldReturnCorrectValues() {
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
        matrixBuilder.addTransportDistance("from", "to", 2.);
        VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
        Assertions.assertEquals(2., matrix.getTransportCost(loc("from"), loc("to"), 0.0, null, null), 0.1);
        Assertions.assertEquals(2., matrix.getDistance("from", "to"), 0.1);
        Assertions.assertEquals(2., matrix.getTransportCost(loc("to"), loc("from"), 0.0, null, null), 0.1);
        Assertions.assertEquals(2., matrix.getDistance("from", "to"), 0.1);
    }

    @Test
    @DisplayName("When Adding Distance To Symmetric Matrix Where Key Already Exists _ it Should Override Values")
    void whenAddingDistanceToSymmetricMatrixWhereKeyAlreadyExists_itShouldOverrideValues() {
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
        matrixBuilder.addTransportDistance("from", "to", 2.);
        // overide
        matrixBuilder.addTransportDistance("from", "to", 4.);
        VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
        Assertions.assertEquals(4., matrix.getTransportCost(loc("from"), loc("to"), 0.0, null, null), 0.1);
        Assertions.assertEquals(4., matrix.getDistance("from", "to"), 0.1);
        Assertions.assertEquals(4., matrix.getTransportCost(loc("to"), loc("from"), 0.0, null, null), 0.1);
        Assertions.assertEquals(4., matrix.getDistance("from", "to"), 0.1);
    }

    @Test
    @DisplayName("When Adding Distance To Symmetric Matrix Where Reverse Key Already Exists _ it Should Override Values")
    void whenAddingDistanceToSymmetricMatrixWhereReverseKeyAlreadyExists_itShouldOverrideValues() {
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
        matrixBuilder.addTransportDistance("from", "to", 2.);
        // overide
        matrixBuilder.addTransportDistance("to", "from", 4.);
        VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
        Assertions.assertEquals(4., matrix.getTransportCost(loc("from"), loc("to"), 0.0, null, null), 0.1);
        Assertions.assertEquals(4., matrix.getDistance("from", "to"), 0.1);
        Assertions.assertEquals(4., matrix.getTransportCost(loc("to"), loc("from"), 0.0, null, null), 0.1);
        Assertions.assertEquals(4., matrix.getDistance("from", "to"), 0.1);
    }

    @Test
    @DisplayName("When Adding Distance To Asymmetric Matrix _ it Should Return Correct Values")
    void whenAddingDistanceToAsymmetricMatrix_itShouldReturnCorrectValues() {
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);
        matrixBuilder.addTransportDistance("1", "2", 2.);
        VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
        Assertions.assertEquals(2., matrix.getTransportCost(loc("1"), loc("2"), 0.0, null, null), 0.1);
    }

    private Location loc(String s) {
        return Location.Builder.newInstance().setId(s).build();
    }

    @Test
    @DisplayName("When Requesting Relation That Does Not Exist _ it Should Throw Exception")
    void whenRequestingRelationThatDoesNotExist_itShouldThrowException() {
        assertThrows(IllegalStateException.class, () -> {
            VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);
            matrixBuilder.addTransportDistance("1", "2", 2.);
            VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
            matrix.getTransportCost(loc("2"), loc("1"), 0.0, null, null);
        });
    }

    @Test
    @DisplayName("When Adding Distance To Asymmetric Matrix Using String As Key _ it Should Return Correct Values")
    void whenAddingDistanceToAsymmetricMatrixUsingStringAsKey_itShouldReturnCorrectValues() {
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);
        matrixBuilder.addTransportDistance("from", "to", 2.);
        matrixBuilder.addTransportDistance("to", "from", 4.);
        VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
        Assertions.assertEquals(2., matrix.getTransportCost(loc("from"), loc("to"), 0.0, null, null), 0.1);
        Assertions.assertEquals(4., matrix.getTransportCost(loc("to"), loc("from"), 0.0, null, null), 0.1);
    }

    @Test
    @DisplayName("When Adding Time To Symmetric Matrix _ it Should Return Correct Values")
    void whenAddingTimeToSymmetricMatrix_itShouldReturnCorrectValues() {
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
        matrixBuilder.addTransportTime("1", "2", 2.);
        VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
        Assertions.assertEquals(2., matrix.getTransportTime(loc("1"), loc("2"), 0.0, null, null), 0.1);
        Assertions.assertEquals(2., matrix.getTransportTime(loc("2"), loc("1"), 0.0, null, null), 0.1);
    }

    @Test
    @DisplayName("When Adding Time To Symmetric Matrix Using String As Key _ it Should Return Correct Values")
    void whenAddingTimeToSymmetricMatrixUsingStringAsKey_itShouldReturnCorrectValues() {
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
        matrixBuilder.addTransportTime("from", "to", 2.);
        VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
        Assertions.assertEquals(2., matrix.getTransportTime(loc("from"), loc("to"), 0.0, null, null), 0.1);
        Assertions.assertEquals(2., matrix.getTransportTime(loc("to"), loc("from"), 0.0, null, null), 0.1);
    }

    @Test
    @DisplayName("When Adding Time To Asymmetric Matrix _ it Should Return Correct Values")
    void whenAddingTimeToAsymmetricMatrix_itShouldReturnCorrectValues() {
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);
        matrixBuilder.addTransportTime("1", "2", 2.);
        VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
        Assertions.assertEquals(2., matrix.getTransportTime(loc("1"), loc("2"), 0.0, null, null), 0.1);
    }

    @Test
    @DisplayName("When Requesting Time Of Relation That Does Not Exist _ it Should Throw Exception")
    void whenRequestingTimeOfRelationThatDoesNotExist_itShouldThrowException() {
        assertThrows(IllegalStateException.class, () -> {
            VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);
            matrixBuilder.addTransportTime("1", "2", 2.);
            VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
            matrix.getTransportTime(loc("2"), loc("1"), 0.0, null, null);
        });
    }

    @Test
    @DisplayName("When Adding Time To Asymmetric Matrix Using String As Key _ it Should Return Correct Values")
    void whenAddingTimeToAsymmetricMatrixUsingStringAsKey_itShouldReturnCorrectValues() {
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);
        matrixBuilder.addTransportTime("from", "to", 2.);
        matrixBuilder.addTransportTime("to", "from", 4.);
        VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
        Assertions.assertEquals(2., matrix.getTransportTime(loc("from"), loc("to"), 0.0, null, null), 0.1);
        Assertions.assertEquals(4., matrix.getTransportTime(loc("to"), loc("from"), 0.0, null, null), 0.1);
    }

    @Test
    @DisplayName("When Adding Time To Asymmetric Matrix Using String As Key _ it Should Return Correct Cost Values")
    void whenAddingTimeToAsymmetricMatrixUsingStringAsKey_itShouldReturnCorrectCostValues() {
        VehicleType type = VehicleTypeImpl.Builder.newInstance("t").setCostPerDistance(0.).setCostPerTime(1.).build();
        Vehicle vehicle = mock(Vehicle.class);
        when(vehicle.getType()).thenReturn(type);
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);
        matrixBuilder.addTransportTime("from", "to", 2.);
        matrixBuilder.addTransportTime("to", "from", 4.);
        VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
        // assertEquals(2.,matrix.getTransportTime("from", "to", 0.0, null, null),0.1);
        // assertEquals(4.,matrix.getTransportTime("to", "from", 0.0, null, null),0.1);
        Assertions.assertEquals(2., matrix.getTransportCost(loc("from"), loc("to"), 0.0, null, vehicle), 0.1);
        Assertions.assertEquals(4., matrix.getTransportCost(loc("to"), loc("from"), 0.0, null, vehicle), 0.1);
    }

    @Test
    @DisplayName("When Adding Time And Distance To Symmetric Matrix _ it Should Return Correct Values")
    void whenAddingTimeAndDistanceToSymmetricMatrix_itShouldReturnCorrectValues() {
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
        matrixBuilder.addTransportDistance("1", "2", 20.);
        matrixBuilder.addTransportTime("1", "2", 2.);
        VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
        Vehicle vehicle = mock(Vehicle.class);
        VehicleType type = VehicleTypeImpl.Builder.newInstance("t").setCostPerDistance(1.).setCostPerTime(2.).build();
        when(vehicle.getType()).thenReturn(type);
        Assertions.assertEquals(24., matrix.getTransportCost(loc("1"), loc("2"), 0.0, null, vehicle), 0.1);
        Assertions.assertEquals(24., matrix.getTransportCost(loc("2"), loc("1"), 0.0, null, vehicle), 0.1);
    }

    @Test
    @DisplayName("When Adding Time And Distance To Symmetric Matrix Using String As Key _ it Should Return Correct Values")
    void whenAddingTimeAndDistanceToSymmetricMatrixUsingStringAsKey_itShouldReturnCorrectValues() {
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
        matrixBuilder.addTransportTime("from", "to", 2.);
        Vehicle vehicle = mock(Vehicle.class);
        VehicleType type = VehicleTypeImpl.Builder.newInstance("t").setCostPerDistance(1.).setCostPerTime(2.).build();
        when(vehicle.getType()).thenReturn(type);
        VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
        Assertions.assertEquals(4., matrix.getTransportCost(loc("from"), loc("to"), 0.0, null, vehicle), 0.1);
        Assertions.assertEquals(4., matrix.getTransportCost(loc("to"), loc("from"), 0.0, null, vehicle), 0.1);
    }

    @Test
    @DisplayName("When Adding Time And Distance To Asymmetric Matrix _ it Should Return Correct Values")
    void whenAddingTimeAndDistanceToAsymmetricMatrix_itShouldReturnCorrectValues() {
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);
        matrixBuilder.addTransportTime("1", "2", 2.);
        matrixBuilder.addTransportTime("2", "1", 8.);
        VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
        Vehicle vehicle = mock(Vehicle.class);
        VehicleType type = VehicleTypeImpl.Builder.newInstance("t").setCostPerDistance(1.).setCostPerTime(2.).build();
        when(vehicle.getType()).thenReturn(type);
        Assertions.assertEquals(4., matrix.getTransportCost(loc("1"), loc("2"), 0.0, null, vehicle), 0.1);
        Assertions.assertEquals(16., matrix.getTransportCost(loc("2"), loc("1"), 0.0, null, vehicle), 0.1);
    }

    @Test
    @DisplayName("When Adding Time And Distance To Asymmetric Matrix Using String As Key _ it Should Return Correct Values")
    void whenAddingTimeAndDistanceToAsymmetricMatrixUsingStringAsKey_itShouldReturnCorrectValues() {
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);
        matrixBuilder.addTransportTime("from", "to", 2.);
        matrixBuilder.addTransportDistance("from", "to", 1.);
        matrixBuilder.addTransportTime("to", "from", 4.);
        matrixBuilder.addTransportDistance("to", "from", 3.);
        VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
        Vehicle vehicle = mock(Vehicle.class);
        VehicleType type = VehicleTypeImpl.Builder.newInstance("t").setCostPerDistance(1.).setCostPerTime(2.).build();
        when(vehicle.getType()).thenReturn(type);
        Assertions.assertEquals(5., matrix.getTransportCost(loc("from"), loc("to"), 0.0, null, vehicle), 0.1);
        Assertions.assertEquals(11., matrix.getTransportCost(loc("to"), loc("from"), 0.0, null, vehicle), 0.1);
    }

    @Test
    @DisplayName("When Adding Time And Distance To Asymmetric Matrix Using String As Key _ it Should Return Correct Cost Values")
    void whenAddingTimeAndDistanceToAsymmetricMatrixUsingStringAsKey_itShouldReturnCorrectCostValues() {
        VehicleType type = VehicleTypeImpl.Builder.newInstance("t").setCostPerDistance(2.).setCostPerTime(1.).build();
        Vehicle vehicle = mock(Vehicle.class);
        when(vehicle.getType()).thenReturn(type);
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);
        matrixBuilder.addTransportTime("from", "to", 2.);
        matrixBuilder.addTransportDistance("from", "to", 3.);
        matrixBuilder.addTransportTime("to", "from", 4.);
        matrixBuilder.addTransportDistance("to", "from", 5.);
        VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
        Assertions.assertEquals(8., matrix.getTransportCost(loc("from"), loc("to"), 0.0, null, vehicle), 0.1);
        Assertions.assertEquals(14., matrix.getTransportCost(loc("to"), loc("from"), 0.0, null, vehicle), 0.1);
    }

    @Test
    @DisplayName("When Adding Time And Distance To Symmetric Matrix Using String As Key _ and _ overrides Entry _ it Should Return Correct Values")
    void whenAddingTimeAndDistanceToSymmetricMatrixUsingStringAsKey_and_overridesEntry_itShouldReturnCorrectValues() {
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
        matrixBuilder.addTransportDistance("from", "to", 1.);
        matrixBuilder.addTransportTime("from", "to", 2.);
        matrixBuilder.addTransportDistance("to", "from", 1.);
        matrixBuilder.addTransportTime("to", "from", 2.);
        VehicleRoutingTransportCostsMatrix matrix = matrixBuilder.build();
        Vehicle vehicle = mock(Vehicle.class);
        VehicleType type = VehicleTypeImpl.Builder.newInstance("t").setCostPerDistance(1.).setCostPerTime(0.).build();
        when(vehicle.getType()).thenReturn(type);
        Assertions.assertEquals(1., matrix.getDistance("from", "to"), 0.1);
        Assertions.assertEquals(1., matrix.getDistance("to", "from"), 0.1);
        Assertions.assertEquals(1., matrix.getTransportCost(loc("from"), loc("to"), 0.0, null, vehicle), 0.1);
        Assertions.assertEquals(1., matrix.getTransportCost(loc("to"), loc("from"), 0.0, null, vehicle), 0.1);
    }
}
