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
package jsprit.core.algorithm.recreate;

import jsprit.core.problem.Location;
import jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.End;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.vehicle.Vehicle;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestLocalActivityInsertionCostsCalculator {

    VehicleRoutingTransportCosts tpCosts;

    VehicleRoutingActivityCosts actCosts;

    LocalActivityInsertionCostsCalculator calc;

    Vehicle vehicle;

    VehicleRoute route;

    JobInsertionContext jic;

    @Before
    public void doBefore() {

        vehicle = mock(Vehicle.class);
        route = mock(VehicleRoute.class);
        when(route.isEmpty()).thenReturn(false);
        when(route.getVehicle()).thenReturn(vehicle);

        jic = mock(JobInsertionContext.class);
        when(jic.getRoute()).thenReturn(route);
        when(jic.getNewVehicle()).thenReturn(vehicle);

        tpCosts = mock(VehicleRoutingTransportCosts.class);
        when(tpCosts.getTransportCost(loc("i"), loc("j"), 0.0, null, vehicle)).thenReturn(2.0);
        when(tpCosts.getTransportTime(loc("i"), loc("j"), 0.0, null, vehicle)).thenReturn(0.0);
        when(tpCosts.getTransportCost(loc("i"), loc("k"), 0.0, null, vehicle)).thenReturn(3.0);
        when(tpCosts.getTransportTime(loc("i"), loc("k"), 0.0, null, vehicle)).thenReturn(0.0);
        when(tpCosts.getTransportCost(loc("k"), loc("j"), 0.0, null, vehicle)).thenReturn(3.0);
        when(tpCosts.getTransportTime(loc("k"), loc("j"), 0.0, null, vehicle)).thenReturn(0.0);

        actCosts = mock(VehicleRoutingActivityCosts.class);
        calc = new LocalActivityInsertionCostsCalculator(tpCosts, actCosts);
    }

    private Location loc(String i) {
        return Location.Builder.newInstance().setId(i).build();
    }

    @Test
    public void whenInsertingActBetweenTwoRouteActs_itCalcsMarginalTpCosts() {
        TourActivity prevAct = mock(TourActivity.class);
        when(prevAct.getLocation()).thenReturn(loc("i"));
        TourActivity nextAct = mock(TourActivity.class);
        when(nextAct.getLocation()).thenReturn(loc("j"));
        TourActivity newAct = mock(TourActivity.class);
        when(newAct.getLocation()).thenReturn(loc("k"));

        when(vehicle.isReturnToDepot()).thenReturn(true);

        double costs = calc.getCosts(jic, prevAct, nextAct, newAct, 0.0);
        assertEquals(4.0, costs, 0.01);
    }

    @Test
    public void whenInsertingActBetweenLastActAndEnd_itCalcsMarginalTpCosts() {
        TourActivity prevAct = mock(TourActivity.class);
        when(prevAct.getLocation()).thenReturn(loc("i"));
        End nextAct = End.newInstance("j", 0.0, 0.0);
        TourActivity newAct = mock(TourActivity.class);
        when(newAct.getLocation()).thenReturn(loc("k"));

        when(vehicle.isReturnToDepot()).thenReturn(true);

        double costs = calc.getCosts(jic, prevAct, nextAct, newAct, 0.0);
        assertEquals(4.0, costs, 0.01);
    }

    @Test
    public void whenInsertingActBetweenTwoRouteActsAndRouteIsOpen_itCalcsMarginalTpCosts() {
        TourActivity prevAct = mock(TourActivity.class);
        when(prevAct.getLocation()).thenReturn(loc("i"));
        TourActivity nextAct = mock(TourActivity.class);
        when(nextAct.getLocation()).thenReturn(loc("j"));
        TourActivity newAct = mock(TourActivity.class);
        when(newAct.getLocation()).thenReturn(loc("k"));

        when(vehicle.isReturnToDepot()).thenReturn(false);

        double costs = calc.getCosts(jic, prevAct, nextAct, newAct, 0.0);
        assertEquals(4.0, costs, 0.01);
    }

    @Test
    public void whenInsertingActBetweenLastActAndEndAndRouteIsOpen_itCalculatesTpCostsFromPrevToNewAct() {
        TourActivity prevAct = mock(TourActivity.class);
        when(prevAct.getLocation()).thenReturn(loc("i"));
        End nextAct = End.newInstance("j", 0.0, 0.0);
        TourActivity newAct = mock(TourActivity.class);
        when(newAct.getLocation()).thenReturn(loc("k"));

        when(vehicle.isReturnToDepot()).thenReturn(false);

        double costs = calc.getCosts(jic, prevAct, nextAct, newAct, 0.0);
        assertEquals(3.0, costs, 0.01);
    }
}
