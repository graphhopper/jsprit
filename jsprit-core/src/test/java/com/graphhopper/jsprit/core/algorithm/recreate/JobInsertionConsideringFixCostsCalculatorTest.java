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

import com.graphhopper.jsprit.core.algorithm.state.InternalStates;
import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JobInsertionConsideringFixCostsCalculatorTest {

    private IncreasingAbsoluteFixedCosts absFixedCosts;

    private DecreasingRelativeFixedCosts relFixedCosts;

    private Vehicle small;

    private Vehicle medium;

    private Vehicle large;

    private Job job;

    private VehicleRoute route;

    private RouteAndActivityStateGetter stateGetter;

    @Before
    public void doBefore() {
        JobInsertionCostsCalculator jobInsertionCosts = mock(JobInsertionCostsCalculator.class);
        job = mock(Job.class);
        when(job.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 50).build());

        small = mock(Vehicle.class);
        VehicleType smallType = VehicleTypeImpl.Builder.newInstance("smallType").addCapacityDimension(0, 50).setFixedCost(50.0).build();
        when(small.getType()).thenReturn(smallType);

        medium = mock(Vehicle.class);
        VehicleType mediumType = VehicleTypeImpl.Builder.newInstance("mediumType").addCapacityDimension(0, 100).setFixedCost(100.0).build();
        when(medium.getType()).thenReturn(mediumType);

        large = mock(Vehicle.class);
        VehicleType largeType = VehicleTypeImpl.Builder.newInstance("largeType").addCapacityDimension(0, 400).setFixedCost(200.0).build();
        when(large.getType()).thenReturn(largeType);

        InsertionData iData = new InsertionData(0.0, 1, 1, medium, null);
        route = mock(VehicleRoute.class);

        when(jobInsertionCosts.getInsertionData(route, job, medium, 0.0, null, Double.MAX_VALUE)).thenReturn(iData);
        when(jobInsertionCosts.getInsertionData(route, job, large, 0.0, null, Double.MAX_VALUE)).thenReturn(new InsertionData(0.0, 1, 1, large, null));

        stateGetter = mock(RouteAndActivityStateGetter.class);
        when(stateGetter.getRouteState(route, InternalStates.MAXLOAD, Capacity.class)).thenReturn(Capacity.Builder.newInstance().build());

        absFixedCosts = new IncreasingAbsoluteFixedCosts(10);
        relFixedCosts = new DecreasingRelativeFixedCosts(stateGetter, 10);
    }

    @Test
    public void whenOldVehicleIsNullAndSolutionComplete_itShouldReturnFixedCostsOfNewVehicle() {
        absFixedCosts.setSolutionCompletenessRatio(1.0);
        absFixedCosts.setWeightOfFixCost(1.0);

        relFixedCosts.setSolutionCompletenessRatio(1.0);
        relFixedCosts.setWeightOfFixCost(1.0);
        //(1.*absFix + 0.*relFix) * completeness * weight  = (1.*100. + 0.*50.) * 1. * 1. = 100.
        JobInsertionContext context = new JobInsertionContext(route, job, medium, null, 0d);
        assertEquals(100., absFixedCosts.getCosts(context) + relFixedCosts.getCosts(context), 0.01);
    }

    @Test
    public void whenOldVehicleIsNullAndSolutionIs0PercentComplete_itShouldReturnNoFixedCosts() {
        absFixedCosts.setSolutionCompletenessRatio(0.0);
        absFixedCosts.setWeightOfFixCost(1.0);

        relFixedCosts.setSolutionCompletenessRatio(0.0);
        relFixedCosts.setWeightOfFixCost(1.0);
        //(0.*absFix + 1.*relFix) * completeness * weight = 0.
        JobInsertionContext context = new JobInsertionContext(route, job, medium, null, 0d);

        assertEquals(0., absFixedCosts.getCosts(context) + relFixedCosts.getCosts(context), 0.1);
    }

    @Test
    public void whenOldVehicleIsNullAndSolutionIs50PercentComplete_itShouldReturnAvgOfRelFixedAndAbsFixedCostOfNewVehicle() {
        absFixedCosts.setSolutionCompletenessRatio(0.5);
        absFixedCosts.setWeightOfFixCost(1.0);

        relFixedCosts.setSolutionCompletenessRatio(0.5);
        relFixedCosts.setWeightOfFixCost(1.0);

        JobInsertionContext context = new JobInsertionContext(route, job, medium, null, 0d);
        //(0.5*absFix + 0.5*relFix) * 0.5 * 1. = (0.5*100+0.5*50)*0.5*1. = 37.5
        assertEquals(37.5, absFixedCosts.getCosts(context) + relFixedCosts.getCosts(context), 0.01);
    }

    @Test
    public void whenOldVehicleIsNullAndSolutionIs75PercentComplete_itShouldReturnAvgOfRelFixedAndAbsFixedCostOfNewVehicle() {
        absFixedCosts.setSolutionCompletenessRatio(0.75);
        absFixedCosts.setWeightOfFixCost(1.0);

        relFixedCosts.setSolutionCompletenessRatio(0.75);
        relFixedCosts.setWeightOfFixCost(1.0);

        JobInsertionContext context = new JobInsertionContext(route, job, medium, null, 0d);

        //(0.75*absFix + 0.25*relFix) * 0.75 * 1.= (0.75*100.+0.25*50.)*0.75*1. = 65.625
        assertEquals(65.625, absFixedCosts.getCosts(context) + relFixedCosts.getCosts(context), 0.01);
    }

    @Test
    public void whenOldVehicleIsNullAndSolutionCompleteAndWeightIs05_itShouldReturnHalfOfFixedCostsOfNewVehicle() {
        absFixedCosts.setSolutionCompletenessRatio(1.0);
        absFixedCosts.setWeightOfFixCost(.5);

        relFixedCosts.setSolutionCompletenessRatio(1.0);
        relFixedCosts.setWeightOfFixCost(.5);

        JobInsertionContext context = new JobInsertionContext(route, job, medium, null, 0d);
        //(1.*absFix + 0.*relFix) * 1. * 0.5 = (1.*100. + 0.*50.) * 1. * 0.5 = 5.
        assertEquals(50., absFixedCosts.getCosts(context) + relFixedCosts.getCosts(context), 0.01);
    }

    @Test
    public void whenOldVehicleIsNullAndSolutionIs0PercentCompleteAndWeightIs05_itShouldReturnHalfOfNoFixedCosts() {
        absFixedCosts.setSolutionCompletenessRatio(0.0);
        absFixedCosts.setWeightOfFixCost(.5);

        relFixedCosts.setSolutionCompletenessRatio(0.0);
        relFixedCosts.setWeightOfFixCost(.5);

        JobInsertionContext context = new JobInsertionContext(route, job, medium, null, 0d);
        //(0.*absFix + 1.*relFix) * 0. * .5 = 0.
        assertEquals(0., absFixedCosts.getCosts(context) + relFixedCosts.getCosts(context), 0.01);
    }

    @Test
    public void whenOldVehicleIsNullAndSolutionIs50PercentCompleteAndWeightIs05_itShouldReturnHalfOfAvgOfRelFixedAndAbsFixedCostOfNewVehicle() {
        absFixedCosts.setSolutionCompletenessRatio(0.5);
        absFixedCosts.setWeightOfFixCost(.5);

        relFixedCosts.setSolutionCompletenessRatio(0.5);
        relFixedCosts.setWeightOfFixCost(.5);

        JobInsertionContext context = new JobInsertionContext(route, job, medium, null, 0d);

        //(0.5*absFix + 0.5*relFix) * 0.5 * 0.= (0.5*100+0.5*50)*0.5*0.5 = 18.75
        assertEquals(18.75, absFixedCosts.getCosts(context) + relFixedCosts.getCosts(context), 0.01);
    }

    @Test
    public void whenOldVehicleIsNullAndSolutionIs75PercentCompleteAndWeightIs05_itShouldReturnHalfOfAvgOfRelFixedAndAbsFixedCostOfNewVehicle() {
        absFixedCosts.setSolutionCompletenessRatio(0.75);
        absFixedCosts.setWeightOfFixCost(0.5);

        relFixedCosts.setSolutionCompletenessRatio(0.75);
        relFixedCosts.setWeightOfFixCost(0.5);

        JobInsertionContext context = new JobInsertionContext(route, job, medium, null, 0d);

        //(0.75*absFix + 0.25*relFix) * 0.75 * 0.5 = (0.75*100.+0.25*50.)*0.75*0.5 = 32.8125
        assertEquals(32.8125, absFixedCosts.getCosts(context) + relFixedCosts.getCosts(context), 0.01);
    }

    @Test
    public void whenOldVehicleIsNotNullAndSolutionComplete_itShouldReturnHalfOfFixedCostsOfNewVehicle() {
        absFixedCosts.setSolutionCompletenessRatio(1.0);
        absFixedCosts.setWeightOfFixCost(1.0);

        relFixedCosts.setSolutionCompletenessRatio(1.0);
        relFixedCosts.setWeightOfFixCost(1.0);

        when(route.getVehicle()).thenReturn(small);

        JobInsertionContext context = new JobInsertionContext(route, job, medium, null, 0d);

        //(1.*absFix + 0.*relFix) * completeness * weight  = (1.*(100.-50.) + 0.*(50.-0.)) * 1. * 1. = 50.
        assertEquals(50., absFixedCosts.getCosts(context) + relFixedCosts.getCosts(context), 0.01);
    }

    @Test
    public void whenOldVehicleIsNotNullAndSolutionIs0PercentComplete_itShouldReturnNoFixedCosts() {
        absFixedCosts.setSolutionCompletenessRatio(0.0);
        absFixedCosts.setWeightOfFixCost(1.0);

        relFixedCosts.setSolutionCompletenessRatio(0.0);
        relFixedCosts.setWeightOfFixCost(1.0);

        when(route.getVehicle()).thenReturn(small);

        JobInsertionContext context = new JobInsertionContext(route, job, medium, null, 0d);
        //(0.*absFix + 1.*relFix) * completeness * weight = 0.
        assertEquals(0., absFixedCosts.getCosts(context) + relFixedCosts.getCosts(context), 0.01);
    }

    @Test
    public void whenOldVehicleIsNotNullAndSolutionIs50PercentComplete_itShouldCorrectVal() {
        absFixedCosts.setSolutionCompletenessRatio(0.5);
        absFixedCosts.setWeightOfFixCost(1.0);

        relFixedCosts.setSolutionCompletenessRatio(0.5);
        relFixedCosts.setWeightOfFixCost(1.0);

        when(route.getVehicle()).thenReturn(small);

        JobInsertionContext context = new JobInsertionContext(route, job, medium, null, 0d);
        //(0.5*absFix + 0.5*relFix) * 0.5 * 1. = (0.5*(100-50)+0.5*(50-0))*0.5*1. = 25.
        assertEquals(25., absFixedCosts.getCosts(context) + relFixedCosts.getCosts(context), 0.01);
    }

    @Test
    public void whenOldVehicleIsNotNullAndSolutionIs75PercentComplete_itShouldReturnCorrectVal() {
        absFixedCosts.setSolutionCompletenessRatio(0.75);
        absFixedCosts.setWeightOfFixCost(1.0);

        relFixedCosts.setSolutionCompletenessRatio(0.75);
        relFixedCosts.setWeightOfFixCost(1.0);

        when(route.getVehicle()).thenReturn(small);
        JobInsertionContext context = new JobInsertionContext(route, job, medium, null, 0d);
        //(0.75*absFix + 0.25*relFix) * 0.75 * 1.= (0.75*(100.-50.)+0.25*(50.-0.))*0.75*1. = 37.5
        assertEquals(37.5, absFixedCosts.getCosts(context) + relFixedCosts.getCosts(context), 0.01);
    }

    @Test
    public void whenOldVehicleIsNotNullAndSolutionCompleteAndWeightIs05_itShouldReturnCorrectVal() {
        absFixedCosts.setSolutionCompletenessRatio(1.0);
        absFixedCosts.setWeightOfFixCost(.5);

        relFixedCosts.setSolutionCompletenessRatio(1.0);
        relFixedCosts.setWeightOfFixCost(.5);

        when(route.getVehicle()).thenReturn(small);
        JobInsertionContext context = new JobInsertionContext(route, job, medium, null, 0d);
        //(1.*absFix + 0.*relFix) * 1. * 0.5 = (1.*(100.-50.) + 0.*(50.-0.)) * 1. * 0.5 = 25.
        assertEquals(25., absFixedCosts.getCosts(context) + relFixedCosts.getCosts(context), 0.01);
    }

    @Test
    public void whenOldVehicleIsNotNullAndSolutionIs0PercentCompleteAndWeightIs05_itShouldReturnCorrectVal() {
        absFixedCosts.setSolutionCompletenessRatio(0.0);
        absFixedCosts.setWeightOfFixCost(.5);

        relFixedCosts.setSolutionCompletenessRatio(0.0);
        relFixedCosts.setWeightOfFixCost(.5);

        when(route.getVehicle()).thenReturn(small);
        JobInsertionContext context = new JobInsertionContext(route, job, medium, null, 0d);
        //(0.*absFix + 1.*relFix) * 0. * .5 = 0.
        assertEquals(0., absFixedCosts.getCosts(context) + relFixedCosts.getCosts(context), 0.01);
    }

    @Test
    public void whenOldVehicleIsNotNullAndSolutionIs50PercentCompleteAndWeightIs05_itShouldReturnCorrectVal() {
        absFixedCosts.setSolutionCompletenessRatio(0.5);
        absFixedCosts.setWeightOfFixCost(.5);

        relFixedCosts.setSolutionCompletenessRatio(0.5);
        relFixedCosts.setWeightOfFixCost(.5);

        when(route.getVehicle()).thenReturn(small);
        JobInsertionContext context = new JobInsertionContext(route, job, medium, null, 0d);
        //(0.5*absFix + 0.5*relFix) * 0.5 * 0.= (0.5*(100-50)+0.5*(50-0))*0.5*0.5 = 12.5
        assertEquals(12.5, absFixedCosts.getCosts(context) + relFixedCosts.getCosts(context), 0.01);
    }

    @Test
    public void whenOldVehicleIsNotNullAndSolutionIs75PercentCompleteAndWeightIs05_itShouldReturnCorrectVal() {
        absFixedCosts.setSolutionCompletenessRatio(0.75);
        absFixedCosts.setWeightOfFixCost(0.5);

        relFixedCosts.setSolutionCompletenessRatio(0.75);
        relFixedCosts.setWeightOfFixCost(0.5);

        when(route.getVehicle()).thenReturn(small);
        JobInsertionContext context = new JobInsertionContext(route, job, medium, null, 0d);
        //(0.75*absFix + 0.25*relFix) * 0.75 * 0.5 = (0.75*(100.-50.)+0.25*(50.-0.))*0.75*0.5 = 18.75
        assertEquals(18.75, absFixedCosts.getCosts(context) + relFixedCosts.getCosts(context), 0.01);
    }

    @Test
    public void whenOldVehicleIsNotNullAndCurrentLoadIs25AndSolutionIs50PercentCompleteAndWeightIs05_itShouldReturnCorrectVal() {
        absFixedCosts.setSolutionCompletenessRatio(0.5);
        absFixedCosts.setWeightOfFixCost(.5);

        relFixedCosts.setSolutionCompletenessRatio(0.5);
        relFixedCosts.setWeightOfFixCost(.5);

        when(route.getVehicle()).thenReturn(small);
        when(stateGetter.getRouteState(route, InternalStates.MAXLOAD, Capacity.class)).thenReturn(Capacity.Builder.newInstance().addDimension(0, 25).build());

        JobInsertionContext context = new JobInsertionContext(route, job, medium, null, 0d);
        //(0.5*absFix + 0.5*relFix) * 0.5 * 0.= (0.5*(100-50)+0.5*(75-25))*0.5*0.5 = 12.5
        assertEquals(12.5, absFixedCosts.getCosts(context) + relFixedCosts.getCosts(context), 0.01);
    }

    @Test
    public void whenOldVehicleIsNotNullAndCurrentLoadIs25AndSolutionIs75PercentCompleteAndWeightIs05_itShouldReturnCorrectVal() {
        absFixedCosts.setSolutionCompletenessRatio(0.75);
        absFixedCosts.setWeightOfFixCost(0.5);

        relFixedCosts.setSolutionCompletenessRatio(0.75);
        relFixedCosts.setWeightOfFixCost(0.5);

        when(route.getVehicle()).thenReturn(small);

        JobInsertionContext context = new JobInsertionContext(route, job, medium, null, 0d);
        //(0.75*absFix + 0.25*relFix) * 0.75 * 0.5 = (0.75*(100.-50.)+0.25*(75.-25.))*0.75*0.5 = 18.75
        assertEquals(18.75, absFixedCosts.getCosts(context) + relFixedCosts.getCosts(context), 0.01);
    }

    @Test
    public void whenOldVehicleIsNotNullAndCurrentLoadIs25AndSolutionIs50PercentCompleteAndWeightIs05WithMultipleCapDims_itShouldReturnCorrectVal() {
        absFixedCosts.setSolutionCompletenessRatio(.5);
        absFixedCosts.setWeightOfFixCost(.5);

        relFixedCosts.setSolutionCompletenessRatio(.5);
        relFixedCosts.setWeightOfFixCost(.5);

        when(job.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 50).addDimension(1, 0).build());

        VehicleType oType = VehicleTypeImpl.Builder.newInstance("otype").addCapacityDimension(0, 50).addCapacityDimension(1, 100).setFixedCost(50.0).build();
        when(small.getType()).thenReturn(oType);

        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0, 100).addCapacityDimension(1, 400).setFixedCost(100.0).build();
        when(medium.getType()).thenReturn(type);

        when(route.getVehicle()).thenReturn(small);
        when(stateGetter.getRouteState(route, InternalStates.MAXLOAD, Capacity.class)).thenReturn(Capacity.Builder.newInstance().addDimension(0, 25).addDimension(1, 100).build());

        JobInsertionContext context = new JobInsertionContext(route, job, medium, null, 0d);
        //(0.5*absFix + 0.5*relFix) * 0.5 * 0.= (0.5*(100-50)+0.5*(75-25))*0.5*0.5 = 12.5
        /*
         * (0.5*(100-50)+0.5*(
		 * relFixNew - relFixOld = (75/100+100/400)/2.*100 - ((25/50+100/100)/2.*50.) =
		 * )*0.5*0.5
		 * = (0.5*(100-50)+0.5*((75/100+100/400)/2.*100 - ((25/50+100/100)/2.*50.)))*0.5*0.5
		 * = (0.5*(100-50)+0.5*12.5)*0.5*0.5 = 7.8125
		 */
        assertEquals(7.8125, absFixedCosts.getCosts(context) + relFixedCosts.getCosts(context), 0.01);
    }

    @Test
    public void whenOldVehicleIsMoreExpensive() {
        absFixedCosts.setSolutionCompletenessRatio(1);
        absFixedCosts.setWeightOfFixCost(1);

        relFixedCosts.setSolutionCompletenessRatio(1);
        relFixedCosts.setWeightOfFixCost(1);

        when(job.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 50).addDimension(1, 0).build());

        VehicleType oType = VehicleTypeImpl.Builder.newInstance("otype").addCapacityDimension(0, 50).addCapacityDimension(1, 100).setFixedCost(50.0).build();
        when(medium.getType()).thenReturn(oType);

        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0, 100).addCapacityDimension(1, 400).setFixedCost(100.0).build();
        when(small.getType()).thenReturn(type);


        when(route.getVehicle()).thenReturn(small);
        when(stateGetter.getRouteState(route, InternalStates.MAXLOAD, Capacity.class)).thenReturn(Capacity.Builder.newInstance().addDimension(0, 25).addDimension(1, 100).build());
        //(0.5*absFix + 0.5*relFix) * 0.5 * 0.= (0.5*(100-50)+0.5*(75-25))*0.5*0.5 = 12.5
        /*
         * (0.5*(100-50)+0.5*(
		 * relFixNew - relFixOld = (75/100+100/400)/2.*100 - ((25/50+100/100)/2.*50.) =
		 * )*0.5*0.5
		 * = (0.5*(100-50)+0.5*((75/100+100/400)/2.*100 - ((25/50+100/100)/2.*50.)))*0.5*0.5
		 * = (0.5*(100-50)+0.5*12.5)*0.5*0.5 = 7.8125
		 */

        JobInsertionContext context = new JobInsertionContext(route, job, medium, null, 0d);

        double insertionCost = absFixedCosts.getCosts(context) + relFixedCosts.getCosts(context);
        assertEquals(-50d, insertionCost, 0.01);
    }

    @Test
    public void smallVSMediumAbsCosts() {
        absFixedCosts.setSolutionCompletenessRatio(1);
        absFixedCosts.setWeightOfFixCost(1);

        relFixedCosts.setSolutionCompletenessRatio(1);
        relFixedCosts.setWeightOfFixCost(1);

        when(route.getVehicle()).thenReturn(small);
        JobInsertionContext context = new JobInsertionContext(route, job, medium, null, 0d);
        double insertionCost = absFixedCosts.getCosts(context) + relFixedCosts.getCosts(context);
        assertEquals(50d, insertionCost, 0.01);
    }

    @Test
    public void smallVSLargeAbsCosts() {
        absFixedCosts.setSolutionCompletenessRatio(1);
        absFixedCosts.setWeightOfFixCost(1);

        relFixedCosts.setSolutionCompletenessRatio(1);
        relFixedCosts.setWeightOfFixCost(1);

        when(route.getVehicle()).thenReturn(small);
        JobInsertionContext context = new JobInsertionContext(route, job, large, null, 0d);
        double insertionCost = absFixedCosts.getCosts(context) + relFixedCosts.getCosts(context);
        assertEquals(150d, insertionCost, 0.01);
    }

    @Test
    public void largeVSMediumAbsCosts() {
        absFixedCosts.setSolutionCompletenessRatio(1);
        absFixedCosts.setWeightOfFixCost(1);

        relFixedCosts.setSolutionCompletenessRatio(1);
        relFixedCosts.setWeightOfFixCost(1);

        when(route.getVehicle()).thenReturn(large);
        JobInsertionContext context = new JobInsertionContext(route, job, medium, null, 0d);
        double insertionCost = absFixedCosts.getCosts(context) + relFixedCosts.getCosts(context);
        assertEquals(-100d, insertionCost, 0.01);
    }

    @Test
    public void mediumVSLargeAbsCosts() {
        absFixedCosts.setSolutionCompletenessRatio(1);
        absFixedCosts.setWeightOfFixCost(1);

        relFixedCosts.setSolutionCompletenessRatio(1);
        relFixedCosts.setWeightOfFixCost(1);

        when(route.getVehicle()).thenReturn(medium);
        JobInsertionContext context = new JobInsertionContext(route, job, large, null, 0d);

        double insertionCost = absFixedCosts.getCosts(context) + relFixedCosts.getCosts(context);
        assertEquals(100d, insertionCost, 0.01);
    }

    @Test
    public void whenOldVehicleIsMoreExpensive2() {
        absFixedCosts.setSolutionCompletenessRatio(0.1);
        absFixedCosts.setWeightOfFixCost(1);

        relFixedCosts.setSolutionCompletenessRatio(0.1);
        relFixedCosts.setWeightOfFixCost(1);

        when(job.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 50).addDimension(1, 0).build());

        VehicleType oType = VehicleTypeImpl.Builder.newInstance("otype").addCapacityDimension(0, 50).addCapacityDimension(1, 100).setFixedCost(50.0).build();
        when(medium.getType()).thenReturn(oType);

        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0, 100).addCapacityDimension(1, 400).setFixedCost(100.0).build();
        when(small.getType()).thenReturn(type);


        when(route.getVehicle()).thenReturn(small);
        when(stateGetter.getRouteState(route, InternalStates.MAXLOAD, Capacity.class)).thenReturn(Capacity.Builder.newInstance().addDimension(0, 25).addDimension(1, 100).build());
        /*
        job = 50
        abs = (50 - 100) * 0.1 * 0.1 * 1.0 = -0.5
        rel = ( (75/50+100/100)/2 * 50 - (25/100 + 100/400)/2 * 100) * 0.9 * 0.1 = 3.375
        c = -0.5 + 3.375 = 2.875

         */
        JobInsertionContext context = new JobInsertionContext(route, job, medium, null, 0d);

        double insertionCost = absFixedCosts.getCosts(context) + relFixedCosts.getCosts(context);
        assertEquals(2.875, insertionCost, 0.01);
    }

    @Test
    public void whenOldVehicleIsNotNullAndCurrentLoadIs25AndSolutionIs75PercentCompleteAndWeightIs05WithMultipleCapDims_itShouldReturnCorrectVal() {
        absFixedCosts.setSolutionCompletenessRatio(0.75);
        absFixedCosts.setWeightOfFixCost(0.5);

        relFixedCosts.setSolutionCompletenessRatio(0.75);
        relFixedCosts.setWeightOfFixCost(0.5);

        when(job.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 50).addDimension(1, 0).build());

        VehicleType oType = VehicleTypeImpl.Builder.newInstance("otype").addCapacityDimension(0, 50).addCapacityDimension(1, 100).setFixedCost(50.0).build();
        when(small.getType()).thenReturn(oType);

        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0, 100).addCapacityDimension(1, 400).setFixedCost(100.0).build();
        when(medium.getType()).thenReturn(type);

        when(route.getVehicle()).thenReturn(small);
        when(stateGetter.getRouteState(route, InternalStates.MAXLOAD, Capacity.class)).thenReturn(Capacity.Builder.newInstance().addDimension(0, 25).addDimension(1, 100).build());

        JobInsertionContext context = new JobInsertionContext(route, job, medium, null, 0d);
        //(0.75*absFix + 0.25*relFix) * 0.75 * 0.5 = (0.75*(100.-50.)+0.25*12.5)*0.75*0.5 = 15.234375

        assertEquals(15.234375, absFixedCosts.getCosts(context) + relFixedCosts.getCosts(context), 0.01);
    }


}
