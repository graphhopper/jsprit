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

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.recreate.InsertionData;
import com.graphhopper.jsprit.core.algorithm.state.StateId;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.constraint.MaxDistanceConstraint;
import com.graphhopper.jsprit.core.problem.cost.TransportDistance;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import org.apache.commons.math3.stat.Frequency;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by schroeder on 06/02/17.
 */
public class UnassignedJobReasonTrackerTest {

    Vehicle vehicle;

    @Before
    public void doBefore() {
        VehicleTypeImpl.Builder vehicleTypeBuilder = VehicleTypeImpl.Builder.newInstance("vehicleType").addCapacityDimension(0, 1);
        VehicleType vehicleType = vehicleTypeBuilder.build();
        VehicleImpl.Builder vehicleBuilder = VehicleImpl.Builder.newInstance("vehicle");
        vehicleBuilder.setStartLocation(Location.newInstance(10, 10));
        vehicleBuilder.setType(vehicleType);
        vehicleBuilder.setEarliestStart(0).setLatestArrival(100);
        vehicle = vehicleBuilder.build();
    }

    @Test
    public void shouldReturnCorrectCapacityReasonCode() {
        Service service = Service.Builder.newInstance("1").addSizeDimension(0, 5).setLocation(Location.newInstance(5, 7)).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().setFleetSize(VehicleRoutingProblem.FleetSize.FINITE).addVehicle(vehicle).addJob(service)
            .build();

        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        UnassignedJobReasonTracker reasonTracker = new UnassignedJobReasonTracker();
        vra.addListener(reasonTracker);

        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        Assert.assertEquals(1, solution.getUnassignedJobs().size());
        Assert.assertEquals(3, reasonTracker.getMostLikelyReasonCode(solution.getUnassignedJobs().iterator().next().getId()));
    }

    @Test
    public void shouldReturnCorrectSkillReasonCode() {
        Service service = Service.Builder.newInstance("1").addSizeDimension(0, 1).addRequiredSkill("ice").setLocation(Location.newInstance(5, 7)).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().setFleetSize(VehicleRoutingProblem.FleetSize.FINITE).addVehicle(vehicle).addJob(service)
            .build();

        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        UnassignedJobReasonTracker reasonTracker = new UnassignedJobReasonTracker();
        vra.addListener(reasonTracker);

        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        Assert.assertEquals(1, solution.getUnassignedJobs().size());
        Assert.assertEquals(1, reasonTracker.getMostLikelyReasonCode(solution.getUnassignedJobs().iterator().next().getId()));
    }

    @Test
    public void shouldReturnCorrectTWReasonCode() {
        Service service = Service.Builder.newInstance("1").addSizeDimension(0, 1).setTimeWindow(TimeWindow.newInstance(110, 200)).setLocation(Location.newInstance(5, 7)).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().setFleetSize(VehicleRoutingProblem.FleetSize.FINITE).addVehicle(vehicle).addJob(service)
            .build();

        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        UnassignedJobReasonTracker reasonTracker = new UnassignedJobReasonTracker();
        vra.addListener(reasonTracker);

        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        Assert.assertEquals(1, solution.getUnassignedJobs().size());
        Assert.assertEquals(2, reasonTracker.getMostLikelyReasonCode(solution.getUnassignedJobs().iterator().next().getId()));
    }

    @Test
    public void shouldReturnCorrectMaxDistanceReasonCode() {
        Service service = Service.Builder.newInstance("1").setLocation(Location.newInstance(51, 0)).build();

        Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();

        final VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().setFleetSize(VehicleRoutingProblem.FleetSize.FINITE).addVehicle(vehicle).addJob(service).build();

        StateManager stateManager = new StateManager(vrp);
        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);
        StateId maxDistance = stateManager.createStateId("max-distance");
        Map<Vehicle, Double> distMap = new HashMap<>();
        distMap.put(vehicle, 100d);
        MaxDistanceConstraint distanceConstraint = new MaxDistanceConstraint(stateManager, maxDistance, new TransportDistance() {
            @Override
            public double getDistance(Location from, Location to, double departureTime, Vehicle vehicle) {
                return vrp.getTransportCosts().getTransportCost(from, to, departureTime, null, vehicle);
            }
        }, distMap);
        constraintManager.addConstraint(distanceConstraint, ConstraintManager.Priority.CRITICAL);

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setStateAndConstraintManager(stateManager, constraintManager)
            .buildAlgorithm();
        UnassignedJobReasonTracker reasonTracker = new UnassignedJobReasonTracker();
        vra.addListener(reasonTracker);

        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        Assert.assertEquals(1, solution.getUnassignedJobs().size());
        Assert.assertEquals(4, reasonTracker.getMostLikelyReasonCode(solution.getUnassignedJobs().iterator().next().getId()));
    }

    @Test
    public void shouldAggregateFailedConstraintNamesFrequencyMapping() {
        // given
        UnassignedJobReasonTracker reasonTracker = new UnassignedJobReasonTracker();
        Job job = Service.Builder.newInstance("job").setLocation(Location.newInstance(0, 0)).build();

        // iteration 0
        InsertionData insertion1 = new InsertionData(1, 1, 1, null,null);
        insertion1.addFailedConstrainName("constraint1");
        insertion1.addFailedConstrainName("constraint2");
        reasonTracker.informIterationStarts(0, null, null);
        reasonTracker.informJobUnassigned(job, insertion1);

        // iteration 1
        InsertionData insertion2 = new InsertionData(2, 2, 2, null,null);
        insertion1.addFailedConstrainName("constraint2");
        insertion1.addFailedConstrainName("constraint3");
        reasonTracker.informIterationStarts(1, null, null);
        reasonTracker.informJobUnassigned(job, insertion2);

        //when
        Map<String, Frequency> frequencyMap = reasonTracker.aggregateFailedConstraintNamesFrequencyMapping();

        //then
        Assert.assertEquals(1, frequencyMap.get("job").getCount("constraint1"));
        Assert.assertEquals(2, frequencyMap.get("job").getCount("constraint2"));
        Assert.assertEquals(1, frequencyMap.get("job").getCount("constraint3"));
    }

    @Test
    public void shouldGetFailedInsertionsForJobWhenWhenFailedInsertionsAreAddedForJob() {
        // given
        UnassignedJobReasonTracker reasonTracker = new UnassignedJobReasonTracker();
        Job job = Service.Builder.newInstance("job").setLocation(Location.newInstance(0, 0)).build();

        // iteration1
        InsertionData insertion1 = new InsertionData(1, 1, 1, null,null);
        reasonTracker.informIterationStarts(0, null, null);
        reasonTracker.informJobUnassigned(job, insertion1);

        // iteration2
        InsertionData insertion2 = new InsertionData(2, 2, 2, null,null);
        reasonTracker.informIterationStarts(2, null, null);
        reasonTracker.informJobUnassigned(job, insertion2);

        //when
        Collection<InsertionData> failedInsertionsJob = reasonTracker.getFailedInsertionsForJob("job");
        Collection<InsertionData> failedInsertionsNonExistentJob = reasonTracker.getFailedInsertionsForJob("non_existent");

        //then
        Assert.assertEquals(2, failedInsertionsJob.size());
        Assert.assertEquals(1, failedInsertionsJob.toArray(new InsertionData[]{})[0].getPickupInsertionIndex());
        Assert.assertEquals(2, failedInsertionsJob.toArray(new InsertionData[]{})[1].getPickupInsertionIndex());
        Assert.assertEquals(0, failedInsertionsNonExistentJob.size());
    }

    @Test
    public void shouldGetFailedInsertionsForJobWhenDifferentInsertionsForDifferentJobsAreAdded() {
        // given
        UnassignedJobReasonTracker reasonTracker = new UnassignedJobReasonTracker();
        Job job1 = Service.Builder.newInstance("job1").setLocation(Location.newInstance(0, 0)).build();
        Job job2 = Service.Builder.newInstance("job2").setLocation(Location.newInstance(0, 0)).build();

        // iteration1
        InsertionData insertion1 = new InsertionData(1, 1, 1, null,null);
        reasonTracker.informIterationStarts(0, null, null);
        reasonTracker.informJobUnassigned(job1, insertion1);

        // iteration2
        InsertionData insertion2 = new InsertionData(2, 2, 2, null,null);
        reasonTracker.informIterationStarts(2, null, null);
        reasonTracker.informJobUnassigned(job2, insertion2);

        //when
        Collection<InsertionData> failedInsertionsJob1 = reasonTracker.getFailedInsertionsForJob("job1");
        Collection<InsertionData> failedInsertionsJob2 = reasonTracker.getFailedInsertionsForJob("job2");

        //then
        Assert.assertEquals(1, failedInsertionsJob1.size());
        Assert.assertEquals(1, failedInsertionsJob2.size());
        Assert.assertEquals(1, failedInsertionsJob1.toArray(new InsertionData[]{})[0].getPickupInsertionIndex());
        Assert.assertEquals(2, failedInsertionsJob2.toArray(new InsertionData[]{})[0].getPickupInsertionIndex());
    }

    @Test
    public void shouldGetFailedInsertionsForJobWhenNoFailedInsertions() {
        // given
        UnassignedJobReasonTracker reasonTracker = new UnassignedJobReasonTracker();

        //when
        Collection<InsertionData> failedInsertions = reasonTracker.getFailedInsertionsForJob("job1");

        //then
        Assert.assertEquals(0, failedInsertions.size());
    }

    @Test
    public void shouldGetFailedInsertionsInIterationForJobWhenNoFailedInsertions() {
        // given
        UnassignedJobReasonTracker reasonTracker = new UnassignedJobReasonTracker();

        //when
        Collection<InsertionData> failedInsertions = reasonTracker.getFailedInsertionsInIterationForJob(1, "job1");

        //then
        Assert.assertEquals(0, failedInsertions.size());
    }

    @Test
    public void shouldGetFailedInsertionsInIterationForJobWhenFailedInsertionsAddedInDifferentIterations() {
        // given
        UnassignedJobReasonTracker reasonTracker = new UnassignedJobReasonTracker();
        Job job = Service.Builder.newInstance("job").setLocation(Location.newInstance(0, 0)).build();

        // adding the failed insertion in iteration 1
        InsertionData insertion1 = new InsertionData(1, 1, 1, null,null);
        reasonTracker.informIterationStarts(1, null, null);
        reasonTracker.informJobUnassigned(job, insertion1);

        //when
        Collection<InsertionData> failedInsertionsIteration2 = reasonTracker.getFailedInsertionsInIterationForJob(2, "job");

        //then
        Assert.assertEquals(0, failedInsertionsIteration2.size());
    }

    @Test
    public void shouldGetFailedInsertionsInIterationForJobWhenDifferentJobInsertionsAddedInDifferentIterations() {
        // given
        UnassignedJobReasonTracker reasonTracker = new UnassignedJobReasonTracker();
        Job job1 = Service.Builder.newInstance("job1").setLocation(Location.newInstance(0, 0)).build();
        Job job2 = Service.Builder.newInstance("job2").setLocation(Location.newInstance(0, 0)).build();

        // adding the failed insertion in iteration 1 for job1
        InsertionData insertion1 = new InsertionData(1, 1, 1, null,null);
        reasonTracker.informIterationStarts(1, null, null);
        reasonTracker.informJobUnassigned(job1, insertion1);

        // adding the failed insertion in iteration 2 for job2
        InsertionData insertion2 = new InsertionData(2, 2, 2, null,null);
        reasonTracker.informIterationStarts(2, null, null);
        reasonTracker.informJobUnassigned(job2, insertion2);

        //when
        Collection<InsertionData> failedInsertionsJob1Iteration1 =
            reasonTracker.getFailedInsertionsInIterationForJob(1, "job1");
        Collection<InsertionData> failedInsertionsJob1Iteration2 =
            reasonTracker.getFailedInsertionsInIterationForJob(2, "job1");
        Collection<InsertionData> failedInsertionsJob2Iteration1 =
            reasonTracker.getFailedInsertionsInIterationForJob(1, "job2");
        Collection<InsertionData> failedInsertionsJob2Iteration2 =
            reasonTracker.getFailedInsertionsInIterationForJob(2, "job2");
        Collection<InsertionData> failedInsertionsNonExistentJobIteration1 =
            reasonTracker.getFailedInsertionsInIterationForJob(1, "non_existent");

        //then
        // job 1
        Assert.assertEquals(1, failedInsertionsJob1Iteration1.size());
        Assert.assertEquals(0, failedInsertionsJob1Iteration2.size());
        Assert.assertEquals(1, failedInsertionsJob1Iteration1.toArray(new InsertionData[]{})[0].getPickupInsertionIndex());
        // job 2
        Assert.assertEquals(0, failedInsertionsJob2Iteration1.size());
        Assert.assertEquals(1, failedInsertionsJob2Iteration2.size());
        Assert.assertEquals(2, failedInsertionsJob2Iteration2.toArray(new InsertionData[]{})[0].getPickupInsertionIndex());
        // non existent job
        Assert.assertEquals(0, failedInsertionsNonExistentJobIteration1.size());
    }

    @Test
    public void shouldFrequencyWork() {
        Frequency frequency = new Frequency();
        frequency.addValue("VehicleDependentTimeWindowHardActivityConstraint");
        frequency.addValue("b");
        frequency.addValue("VehicleDependentTimeWindowHardActivityConstraint");

        Iterator<Map.Entry<Comparable<?>, Long>> entryIterator = frequency.entrySetIterator();
        while (entryIterator.hasNext()) {
            Map.Entry<Comparable<?>, Long> e = entryIterator.next();
            System.out.println(e.getKey().toString() + " " + e.getValue());
        }

        Assert.assertEquals(2, frequency.getCount("VehicleDependentTimeWindowHardActivityConstraint"));
        Assert.assertEquals(1, frequency.getCount("b"));
    }
}
