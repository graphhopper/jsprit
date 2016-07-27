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
package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.solution.route.activity.End;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestAuxilliaryCostCalculator {

    private VehicleRoutingTransportCosts routingCosts;

    private VehicleRoutingActivityCosts actCosts;

    private Vehicle vehicle;

    @Before
    public void doBefore() {
        vehicle = mock(Vehicle.class);

        routingCosts = mock(VehicleRoutingTransportCosts.class);
        actCosts = mock(VehicleRoutingActivityCosts.class);

        when(routingCosts.getTransportCost(loc("i"), loc("j"), 0.0, null, vehicle)).thenReturn(2.0);
        when(routingCosts.getTransportTime(loc("i"), loc("j"), 0.0, null, vehicle)).thenReturn(0.0);
        when(routingCosts.getTransportCost(loc("i"), loc("k"), 0.0, null, vehicle)).thenReturn(3.0);
        when(routingCosts.getTransportTime(loc("i"), loc("k"), 0.0, null, vehicle)).thenReturn(0.0);
        when(routingCosts.getTransportCost(loc("k"), loc("j"), 0.0, null, vehicle)).thenReturn(3.0);
        when(routingCosts.getTransportTime(loc("k"), loc("j"), 0.0, null, vehicle)).thenReturn(0.0);
    }

    private Location loc(String i) {
        return Location.Builder.newInstance().setId(i).build();
    }

    @Test
    public void whenRouteIsClosed_itCalculatesCostUpToEnd_v1() {
        TourActivity prevAct = mock(TourActivity.class);
        when(prevAct.getLocation()).thenReturn(loc("i"));
        TourActivity nextAct = mock(TourActivity.class);
        when(nextAct.getLocation()).thenReturn(loc("j"));
        TourActivity newAct = mock(TourActivity.class);
        when(newAct.getLocation()).thenReturn(loc("k"));

        when(vehicle.isReturnToDepot()).thenReturn(true);

        AuxilliaryCostCalculator aCalc = new AuxilliaryCostCalculator(routingCosts, actCosts);
        double costs = aCalc.costOfPath(Arrays.asList(prevAct, newAct, nextAct), 0.0, null, vehicle);
        assertEquals(6.0, costs, 0.01);
    }

    @Test
    public void whenRouteIsClosed_itCalculatesCostUpToEnd_v2() {
        TourActivity prevAct = mock(TourActivity.class);
        when(prevAct.getLocation()).thenReturn(loc("i"));
        End nextAct = new End("j", 0.0, 0.0);
        TourActivity newAct = mock(TourActivity.class);
        when(newAct.getLocation()).thenReturn(loc("k"));

        when(vehicle.isReturnToDepot()).thenReturn(true);

        AuxilliaryCostCalculator aCalc = new AuxilliaryCostCalculator(routingCosts, actCosts);
        double costs = aCalc.costOfPath(Arrays.asList(prevAct, newAct, nextAct), 0.0, null, vehicle);
        assertEquals(6.0, costs, 0.01);
    }

    @Test
    public void whenRouteIsOpen_itCalculatesCostUpToEnd_v1() {
        TourActivity prevAct = mock(TourActivity.class);
        when(prevAct.getLocation()).thenReturn(loc("i"));
        TourActivity nextAct = mock(TourActivity.class);
        when(nextAct.getLocation()).thenReturn(loc("j"));
        TourActivity newAct = mock(TourActivity.class);
        when(newAct.getLocation()).thenReturn(loc("k"));

        when(vehicle.isReturnToDepot()).thenReturn(false);

        AuxilliaryCostCalculator aCalc = new AuxilliaryCostCalculator(routingCosts, actCosts);
        double costs = aCalc.costOfPath(Arrays.asList(prevAct, newAct, nextAct), 0.0, null, vehicle);
        assertEquals(6.0, costs, 0.01);
    }

    @Test
    public void whenRouteIsOpen_itCalculatesCostUpToEnd_v2() {
        TourActivity prevAct = mock(TourActivity.class);
        when(prevAct.getLocation()).thenReturn(loc("i"));
        End nextAct = End.newInstance("j", 0.0, 0.0);
        TourActivity newAct = mock(TourActivity.class);
        when(newAct.getLocation()).thenReturn(loc("k"));

        when(vehicle.isReturnToDepot()).thenReturn(false);

        AuxilliaryCostCalculator aCalc = new AuxilliaryCostCalculator(routingCosts, actCosts);
        double costs = aCalc.costOfPath(Arrays.asList(prevAct, newAct, nextAct), 0.0, null, vehicle);
        assertEquals(3.0, costs, 0.01);
    }

}
