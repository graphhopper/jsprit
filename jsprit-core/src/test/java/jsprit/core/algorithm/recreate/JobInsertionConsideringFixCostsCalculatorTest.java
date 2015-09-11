/*******************************************************************************
 * Copyright (c) 2014 Stefan Schroeder.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package jsprit.core.algorithm.recreate;

import jsprit.core.algorithm.state.InternalStates;
import jsprit.core.problem.Capacity;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JobInsertionConsideringFixCostsCalculatorTest {

    private JobInsertionConsideringFixCostsCalculator calc;

    private Vehicle oVehicle;

    private Vehicle nVehicle;

    private Job job;

    private VehicleRoute route;

    private RouteAndActivityStateGetter stateGetter;

    @Before
    public void doBefore() {
        JobInsertionCostsCalculator jobInsertionCosts = mock(JobInsertionCostsCalculator.class);
        job = mock(Job.class);
        when(job.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 50).build());

        oVehicle = mock(Vehicle.class);
        VehicleType oType = VehicleTypeImpl.Builder.newInstance("otype").addCapacityDimension(0, 50).setFixedCost(50.0).build();
        when(oVehicle.getType()).thenReturn(oType);

        nVehicle = mock(Vehicle.class);
        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0, 100).setFixedCost(100.0).build();
        when(nVehicle.getType()).thenReturn(type);

        InsertionData iData = new InsertionData(0.0, 1, 1, nVehicle, null);
        route = mock(VehicleRoute.class);

        when(jobInsertionCosts.getInsertionData(route, job, nVehicle, 0.0, null, Double.MAX_VALUE)).thenReturn(iData);

        stateGetter = mock(RouteAndActivityStateGetter.class);
        when(stateGetter.getRouteState(route, InternalStates.MAXLOAD, Capacity.class)).thenReturn(Capacity.Builder.newInstance().build());

        calc = new JobInsertionConsideringFixCostsCalculator(jobInsertionCosts, stateGetter);
    }

    @Test
    public void whenOldVehicleIsNullAndSolutionComplete_itShouldReturnFixedCostsOfNewVehicle() {
        calc.setSolutionCompletenessRatio(1.0);
        calc.setWeightOfFixCost(1.0);
        //(1.*absFix + 0.*relFix) * completeness * weight  = (1.*100. + 0.*50.) * 1. * 1. = 100.
        assertEquals(100., calc.getInsertionData(route, job, nVehicle, 0.0, null, Double.MAX_VALUE).getInsertionCost(), 0.01);
    }

    @Test
    public void whenOldVehicleIsNullAndSolutionIs0PercentComplete_itShouldReturnNoFixedCosts() {
        calc.setSolutionCompletenessRatio(0.0);
        calc.setWeightOfFixCost(1.0);
        //(0.*absFix + 1.*relFix) * completeness * weight = 0.
        assertEquals(0., calc.getInsertionData(route, job, nVehicle, 0.0, null, Double.MAX_VALUE).getInsertionCost(), 0.01);
    }

    @Test
    public void whenOldVehicleIsNullAndSolutionIs50PercentComplete_itShouldReturnAvgOfRelFixedAndAbsFixedCostOfNewVehicle() {
        calc.setSolutionCompletenessRatio(0.5);
        calc.setWeightOfFixCost(1.0);
        //(0.5*absFix + 0.5*relFix) * 0.5 * 1. = (0.5*100+0.5*50)*0.5*1. = 37.5
        assertEquals(37.5, calc.getInsertionData(route, job, nVehicle, 0.0, null, Double.MAX_VALUE).getInsertionCost(), 0.01);
    }

    @Test
    public void whenOldVehicleIsNullAndSolutionIs75PercentComplete_itShouldReturnAvgOfRelFixedAndAbsFixedCostOfNewVehicle() {
        calc.setSolutionCompletenessRatio(0.75);
        calc.setWeightOfFixCost(1.0);
        //(0.75*absFix + 0.25*relFix) * 0.75 * 1.= (0.75*100.+0.25*50.)*0.75*1. = 65.625
        assertEquals(65.625, calc.getInsertionData(route, job, nVehicle, 0.0, null, Double.MAX_VALUE).getInsertionCost(), 0.01);
    }

    @Test
    public void whenOldVehicleIsNullAndSolutionCompleteAndWeightIs05_itShouldReturnHalfOfFixedCostsOfNewVehicle() {
        calc.setSolutionCompletenessRatio(1.0);
        calc.setWeightOfFixCost(.5);
        //(1.*absFix + 0.*relFix) * 1. * 0.5 = (1.*100. + 0.*50.) * 1. * 0.5 = 5.
        assertEquals(50., calc.getInsertionData(route, job, nVehicle, 0.0, null, Double.MAX_VALUE).getInsertionCost(), 0.01);
    }

    @Test
    public void whenOldVehicleIsNullAndSolutionIs0PercentCompleteAndWeightIs05_itShouldReturnHalfOfNoFixedCosts() {
        calc.setSolutionCompletenessRatio(0.0);
        calc.setWeightOfFixCost(.5);
        //(0.*absFix + 1.*relFix) * 0. * .5 = 0.
        assertEquals(0., calc.getInsertionData(route, job, nVehicle, 0.0, null, Double.MAX_VALUE).getInsertionCost(), 0.01);
    }

    @Test
    public void whenOldVehicleIsNullAndSolutionIs50PercentCompleteAndWeightIs05_itShouldReturnHalfOfAvgOfRelFixedAndAbsFixedCostOfNewVehicle() {
        calc.setSolutionCompletenessRatio(0.5);
        calc.setWeightOfFixCost(.5);
        //(0.5*absFix + 0.5*relFix) * 0.5 * 0.= (0.5*100+0.5*50)*0.5*0.5 = 18.75
        assertEquals(18.75, calc.getInsertionData(route, job, nVehicle, 0.0, null, Double.MAX_VALUE).getInsertionCost(), 0.01);
    }

    @Test
    public void whenOldVehicleIsNullAndSolutionIs75PercentCompleteAndWeightIs05_itShouldReturnHalfOfAvgOfRelFixedAndAbsFixedCostOfNewVehicle() {
        calc.setSolutionCompletenessRatio(0.75);
        calc.setWeightOfFixCost(0.5);
        //(0.75*absFix + 0.25*relFix) * 0.75 * 0.5 = (0.75*100.+0.25*50.)*0.75*0.5 = 32.8125
        assertEquals(32.8125, calc.getInsertionData(route, job, nVehicle, 0.0, null, Double.MAX_VALUE).getInsertionCost(), 0.01);
    }

    @Test
    public void whenOldVehicleIsNotNullAndSolutionComplete_itShouldReturnHalfOfFixedCostsOfNewVehicle() {
        calc.setSolutionCompletenessRatio(1.0);
        calc.setWeightOfFixCost(1.0);
        when(route.getVehicle()).thenReturn(oVehicle);
        //(1.*absFix + 0.*relFix) * completeness * weight  = (1.*(100.-50.) + 0.*(50.-0.)) * 1. * 1. = 50.
        assertEquals(50., calc.getInsertionData(route, job, nVehicle, 0.0, null, Double.MAX_VALUE).getInsertionCost(), 0.01);
    }

    @Test
    public void whenOldVehicleIsNotNullAndSolutionIs0PercentComplete_itShouldReturnNoFixedCosts() {
        calc.setSolutionCompletenessRatio(0.0);
        calc.setWeightOfFixCost(1.0);
        when(route.getVehicle()).thenReturn(oVehicle);
        //(0.*absFix + 1.*relFix) * completeness * weight = 0.
        assertEquals(0., calc.getInsertionData(route, job, nVehicle, 0.0, null, Double.MAX_VALUE).getInsertionCost(), 0.01);
    }

    @Test
    public void whenOldVehicleIsNotNullAndSolutionIs50PercentComplete_itShouldCorrectVal() {
        calc.setSolutionCompletenessRatio(0.5);
        calc.setWeightOfFixCost(1.0);
        when(route.getVehicle()).thenReturn(oVehicle);
        //(0.5*absFix + 0.5*relFix) * 0.5 * 1. = (0.5*(100-50)+0.5*(50-0))*0.5*1. = 25.
        assertEquals(25., calc.getInsertionData(route, job, nVehicle, 0.0, null, Double.MAX_VALUE).getInsertionCost(), 0.01);
    }

    @Test
    public void whenOldVehicleIsNotNullAndSolutionIs75PercentComplete_itShouldReturnCorrectVal() {
        calc.setSolutionCompletenessRatio(0.75);
        calc.setWeightOfFixCost(1.0);
        when(route.getVehicle()).thenReturn(oVehicle);
        //(0.75*absFix + 0.25*relFix) * 0.75 * 1.= (0.75*(100.-50.)+0.25*(50.-0.))*0.75*1. = 37.5
        assertEquals(37.5, calc.getInsertionData(route, job, nVehicle, 0.0, null, Double.MAX_VALUE).getInsertionCost(), 0.01);
    }

    @Test
    public void whenOldVehicleIsNotNullAndSolutionCompleteAndWeightIs05_itShouldReturnCorrectVal() {
        calc.setSolutionCompletenessRatio(1.0);
        calc.setWeightOfFixCost(.5);
        when(route.getVehicle()).thenReturn(oVehicle);
        //(1.*absFix + 0.*relFix) * 1. * 0.5 = (1.*(100.-50.) + 0.*(50.-0.)) * 1. * 0.5 = 25.
        assertEquals(25., calc.getInsertionData(route, job, nVehicle, 0.0, null, Double.MAX_VALUE).getInsertionCost(), 0.01);
    }

    @Test
    public void whenOldVehicleIsNotNullAndSolutionIs0PercentCompleteAndWeightIs05_itShouldReturnCorrectVal() {
        calc.setSolutionCompletenessRatio(0.0);
        calc.setWeightOfFixCost(.5);
        when(route.getVehicle()).thenReturn(oVehicle);
        //(0.*absFix + 1.*relFix) * 0. * .5 = 0.
        assertEquals(0., calc.getInsertionData(route, job, nVehicle, 0.0, null, Double.MAX_VALUE).getInsertionCost(), 0.01);
    }

    @Test
    public void whenOldVehicleIsNotNullAndSolutionIs50PercentCompleteAndWeightIs05_itShouldReturnCorrectVal() {
        calc.setSolutionCompletenessRatio(0.5);
        calc.setWeightOfFixCost(.5);
        when(route.getVehicle()).thenReturn(oVehicle);
        //(0.5*absFix + 0.5*relFix) * 0.5 * 0.= (0.5*(100-50)+0.5*(50-0))*0.5*0.5 = 12.5
        assertEquals(12.5, calc.getInsertionData(route, job, nVehicle, 0.0, null, Double.MAX_VALUE).getInsertionCost(), 0.01);
    }

    @Test
    public void whenOldVehicleIsNotNullAndSolutionIs75PercentCompleteAndWeightIs05_itShouldReturnCorrectVal() {
        calc.setSolutionCompletenessRatio(0.75);
        calc.setWeightOfFixCost(0.5);
        when(route.getVehicle()).thenReturn(oVehicle);
        //(0.75*absFix + 0.25*relFix) * 0.75 * 0.5 = (0.75*(100.-50.)+0.25*(50.-0.))*0.75*0.5 = 18.75
        assertEquals(18.75, calc.getInsertionData(route, job, nVehicle, 0.0, null, Double.MAX_VALUE).getInsertionCost(), 0.01);
    }

    @Test
    public void whenOldVehicleIsNotNullAndCurrentLoadIs25AndSolutionIs50PercentCompleteAndWeightIs05_itShouldReturnCorrectVal() {
        calc.setSolutionCompletenessRatio(0.5);
        calc.setWeightOfFixCost(.5);
        when(route.getVehicle()).thenReturn(oVehicle);
        when(stateGetter.getRouteState(route, InternalStates.MAXLOAD, Capacity.class)).thenReturn(Capacity.Builder.newInstance().addDimension(0, 25).build());
        //(0.5*absFix + 0.5*relFix) * 0.5 * 0.= (0.5*(100-50)+0.5*(75-25))*0.5*0.5 = 12.5
        assertEquals(12.5, calc.getInsertionData(route, job, nVehicle, 0.0, null, Double.MAX_VALUE).getInsertionCost(), 0.01);
    }

    @Test
    public void whenOldVehicleIsNotNullAndCurrentLoadIs25AndSolutionIs75PercentCompleteAndWeightIs05_itShouldReturnCorrectVal() {
        calc.setSolutionCompletenessRatio(0.75);
        calc.setWeightOfFixCost(0.5);
        when(route.getVehicle()).thenReturn(oVehicle);
        //(0.75*absFix + 0.25*relFix) * 0.75 * 0.5 = (0.75*(100.-50.)+0.25*(75.-25.))*0.75*0.5 = 18.75
        assertEquals(18.75, calc.getInsertionData(route, job, nVehicle, 0.0, null, Double.MAX_VALUE).getInsertionCost(), 0.01);
    }

    @Test
    public void whenOldVehicleIsNotNullAndCurrentLoadIs25AndSolutionIs50PercentCompleteAndWeightIs05WithMultipleCapDims_itShouldReturnCorrectVal() {
        calc.setSolutionCompletenessRatio(.5);
        calc.setWeightOfFixCost(.5);

        when(job.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 50).addDimension(1, 0).build());

        VehicleType oType = VehicleTypeImpl.Builder.newInstance("otype").addCapacityDimension(0, 50).addCapacityDimension(1, 100).setFixedCost(50.0).build();
        when(oVehicle.getType()).thenReturn(oType);

        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0, 100).addCapacityDimension(1, 400).setFixedCost(100.0).build();
        when(nVehicle.getType()).thenReturn(type);

        when(route.getVehicle()).thenReturn(oVehicle);
        when(stateGetter.getRouteState(route, InternalStates.MAXLOAD, Capacity.class)).thenReturn(Capacity.Builder.newInstance().addDimension(0, 25).addDimension(1, 100).build());
        //(0.5*absFix + 0.5*relFix) * 0.5 * 0.= (0.5*(100-50)+0.5*(75-25))*0.5*0.5 = 12.5
        /*
         * (0.5*(100-50)+0.5*(
		 * relFixNew - relFixOld = (75/100+100/400)/2.*100 - ((25/50+100/100)/2.*50.) =
		 * )*0.5*0.5
		 * = (0.5*(100-50)+0.5*((75/100+100/400)/2.*100 - ((25/50+100/100)/2.*50.)))*0.5*0.5
		 * = (0.5*(100-50)+0.5*12.5)*0.5*0.5 = 7.8125
		 */
        assertEquals(7.8125, calc.getInsertionData(route, job, nVehicle, 0.0, null, Double.MAX_VALUE).getInsertionCost(), 0.01);
    }

    @Test
    public void whenOldVehicleIsNotNullAndCurrentLoadIs25AndSolutionIs75PercentCompleteAndWeightIs05WithMultipleCapDims_itShouldReturnCorrectVal() {
        calc.setSolutionCompletenessRatio(0.75);
        calc.setWeightOfFixCost(0.5);
        when(job.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 50).addDimension(1, 0).build());

        VehicleType oType = VehicleTypeImpl.Builder.newInstance("otype").addCapacityDimension(0, 50).addCapacityDimension(1, 100).setFixedCost(50.0).build();
        when(oVehicle.getType()).thenReturn(oType);

        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0, 100).addCapacityDimension(1, 400).setFixedCost(100.0).build();
        when(nVehicle.getType()).thenReturn(type);

        when(route.getVehicle()).thenReturn(oVehicle);
        when(stateGetter.getRouteState(route, InternalStates.MAXLOAD, Capacity.class)).thenReturn(Capacity.Builder.newInstance().addDimension(0, 25).addDimension(1, 100).build());
        //(0.75*absFix + 0.25*relFix) * 0.75 * 0.5 = (0.75*(100.-50.)+0.25*12.5)*0.75*0.5 = 15.234375

        assertEquals(15.234375, calc.getInsertionData(route, job, nVehicle, 0.0, null, Double.MAX_VALUE).getInsertionCost(), 0.01);
    }


}
