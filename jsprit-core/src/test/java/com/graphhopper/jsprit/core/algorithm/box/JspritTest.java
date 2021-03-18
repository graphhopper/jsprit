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

package com.graphhopper.jsprit.core.algorithm.box;

import com.graphhopper.jsprit.core.algorithm.SearchStrategy;
import com.graphhopper.jsprit.core.algorithm.SearchStrategyModule;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.acceptor.AcceptNewRemoveFirst;
import com.graphhopper.jsprit.core.algorithm.listener.StrategySelectedListener;
import com.graphhopper.jsprit.core.algorithm.module.RuinAndRecreateModule;
import com.graphhopper.jsprit.core.algorithm.recreate.InsertionData;
import com.graphhopper.jsprit.core.algorithm.recreate.listener.BeforeJobInsertionListener;
import com.graphhopper.jsprit.core.algorithm.recreate.listener.JobInsertedListener;
import com.graphhopper.jsprit.core.algorithm.ruin.listener.RuinListener;
import com.graphhopper.jsprit.core.algorithm.selector.SelectBest;
import com.graphhopper.jsprit.core.algorithm.selector.SelectRandomly;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.util.RandomNumberGeneration;
import junit.framework.Assert;
import org.junit.Test;

import java.util.*;

import static com.graphhopper.jsprit.core.algorithm.box.Jsprit.Parameter.BREAK_SCHEDULING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by schroeder on 06/03/15.
 */
public class JspritTest {


    @Test
    public void whenRunningJspritWithSingleCustomer_itShouldWork() {
        Service s = Service.Builder.newInstance("s1").setLocation(Location.newInstance(1, 1)).build();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(v).addJob(s).build();
        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        vra.setMaxIterations(10);
        final Map<String, Integer> counts = new HashMap<String, Integer>();
        vra.addListener(new StrategySelectedListener() {

            @Override
            public void informSelectedStrategy(SearchStrategy.DiscoveredSolution discoveredSolution, VehicleRoutingProblem vehicleRoutingProblem, Collection<VehicleRoutingProblemSolution> vehicleRoutingProblemSolutions) {
                count(discoveredSolution.getStrategyId());
            }

            private void count(String strategyId) {
                if (!counts.containsKey(strategyId)) counts.put(strategyId, 1);
                counts.put(strategyId, counts.get(strategyId) + 1);
            }

        });
        try {
            vra.searchSolutions();
            Assert.assertTrue(true);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }

    }

    @Test
    public void whenActivatingStrat_itShouldBeReflected() {
        Service s = Service.Builder.newInstance("s1").setLocation(Location.newInstance(1, 1)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(1, 2)).build();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(v).addJob(s2).addJob(s).build();
        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
            .setProperty(Jsprit.Strategy.RADIAL_BEST, "100.").buildAlgorithm();
        vra.setMaxIterations(100);
        final Map<String, Integer> counts = new HashMap<String, Integer>();
        vra.addListener(new StrategySelectedListener() {

            @Override
            public void informSelectedStrategy(SearchStrategy.DiscoveredSolution discoveredSolution, VehicleRoutingProblem vehicleRoutingProblem, Collection<VehicleRoutingProblemSolution> vehicleRoutingProblemSolutions) {
                count(discoveredSolution.getStrategyId());
            }

            private void count(String strategyId) {
                if (!counts.containsKey(strategyId)) counts.put(strategyId, 1);
                Integer integer = counts.get(strategyId);
                counts.put(strategyId, integer + 1);
            }

        });
        vra.searchSolutions();
        Assert.assertTrue(counts.containsKey(Jsprit.Strategy.RADIAL_BEST.toString()));
    }

    @Test
    public void whenActivatingStrat_itShouldBeReflectedV2() {
        Service s = Service.Builder.newInstance("s1").setLocation(Location.newInstance(1, 1)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(1, 2)).build();
        Service s3 = Service.Builder.newInstance("s3").setLocation(Location.newInstance(1, 2)).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s3).addVehicle(v).addJob(s2).addJob(s).build();
        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        vra.setMaxIterations(100);
        final Map<String, Integer> counts = new HashMap<String, Integer>();
        vra.addListener(new StrategySelectedListener() {

            @Override
            public void informSelectedStrategy(SearchStrategy.DiscoveredSolution discoveredSolution, VehicleRoutingProblem vehicleRoutingProblem, Collection<VehicleRoutingProblemSolution> vehicleRoutingProblemSolutions) {
                count(discoveredSolution.getStrategyId());
            }

            private void count(String strategyId) {
                if (!counts.containsKey(strategyId)) counts.put(strategyId, 1);
                counts.put(strategyId, counts.get(strategyId) + 1);
            }

        });
        vra.searchSolutions();
        Assert.assertTrue(!counts.containsKey(Jsprit.Strategy.RADIAL_BEST));
    }

    @Test
    public void test_v4() {
        Service s = Service.Builder.newInstance("s1").setLocation(Location.newInstance(1, 1)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(1, 2)).build();
        Service s3 = Service.Builder.newInstance("s3").setLocation(Location.newInstance(1, 2)).build();
        Service s4 = Service.Builder.newInstance("s4").setLocation(Location.newInstance(1, 2)).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s4).addJob(s3).addVehicle(v).addJob(s2).addJob(s).build();
        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        vra.setMaxIterations(100);
        final Map<String, Integer> counts = new HashMap<String, Integer>();
        vra.addListener(new StrategySelectedListener() {

            @Override
            public void informSelectedStrategy(SearchStrategy.DiscoveredSolution discoveredSolution, VehicleRoutingProblem vehicleRoutingProblem, Collection<VehicleRoutingProblemSolution> vehicleRoutingProblemSolutions) {
                count(discoveredSolution.getStrategyId());
            }

            private void count(String strategyId) {
                if (!counts.containsKey(strategyId)) counts.put(strategyId, 1);
                counts.put(strategyId, counts.get(strategyId) + 1);
            }

        });
        vra.searchSolutions();
        Assert.assertTrue(!counts.containsKey(Jsprit.Strategy.RADIAL_BEST));
    }


    @Test
    public void strategyDrawShouldBeReproducible() {
        Service s = Service.Builder.newInstance("s1").setLocation(Location.newInstance(1, 1)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(1, 2)).build();
        Service s3 = Service.Builder.newInstance("s3").setLocation(Location.newInstance(1, 2)).build();
        Service s4 = Service.Builder.newInstance("s4").setLocation(Location.newInstance(1, 2)).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s4).addJob(s3).addVehicle(v).addJob(s2).addJob(s).build();
        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        vra.setMaxIterations(100);
        final List<String> firstRecord = new ArrayList<String>();
        vra.addListener(new StrategySelectedListener() {

            @Override
            public void informSelectedStrategy(SearchStrategy.DiscoveredSolution discoveredSolution, VehicleRoutingProblem vehicleRoutingProblem, Collection<VehicleRoutingProblemSolution> vehicleRoutingProblemSolutions) {
                firstRecord.add(discoveredSolution.getStrategyId());
            }

        });
        vra.searchSolutions();

        RandomNumberGeneration.reset();
        VehicleRoutingAlgorithm second = Jsprit.createAlgorithm(vrp);
        second.setMaxIterations(100);
        final List<String> secondRecord = new ArrayList<String>();
        second.addListener(new StrategySelectedListener() {

            @Override
            public void informSelectedStrategy(SearchStrategy.DiscoveredSolution discoveredSolution, VehicleRoutingProblem vehicleRoutingProblem, Collection<VehicleRoutingProblemSolution> vehicleRoutingProblemSolutions) {
                secondRecord.add(discoveredSolution.getStrategyId());
            }

        });
        second.searchSolutions();

        for (int i = 0; i < 100; i++) {
            if (!firstRecord.get(i).equals(secondRecord.get(i))) {
                org.junit.Assert.assertFalse(true);
            }
        }
        org.junit.Assert.assertTrue(true);

    }

    @Test
    public void strategyDrawShouldBeReproducibleV2() {
        Service s = Service.Builder.newInstance("s1").setLocation(Location.newInstance(1, 1)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(1, 2)).build();
        Service s3 = Service.Builder.newInstance("s3").setLocation(Location.newInstance(1, 2)).build();
        Service s4 = Service.Builder.newInstance("s4").setLocation(Location.newInstance(1, 2)).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s4).addJob(s3).addVehicle(v).addJob(s2).addJob(s).build();
        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setProperty(Jsprit.Parameter.THREADS, "4").buildAlgorithm();
        vra.setMaxIterations(100);
        final List<String> firstRecord = new ArrayList<String>();
        vra.addListener(new StrategySelectedListener() {

            @Override
            public void informSelectedStrategy(SearchStrategy.DiscoveredSolution discoveredSolution, VehicleRoutingProblem vehicleRoutingProblem, Collection<VehicleRoutingProblemSolution> vehicleRoutingProblemSolutions) {
                firstRecord.add(discoveredSolution.getStrategyId());
            }

        });
        vra.searchSolutions();

        RandomNumberGeneration.reset();
        VehicleRoutingAlgorithm second = Jsprit.Builder.newInstance(vrp).setProperty(Jsprit.Parameter.THREADS, "2").buildAlgorithm();
        second.setMaxIterations(100);
        final List<String> secondRecord = new ArrayList<String>();
        second.addListener(new StrategySelectedListener() {

            @Override
            public void informSelectedStrategy(SearchStrategy.DiscoveredSolution discoveredSolution, VehicleRoutingProblem vehicleRoutingProblem, Collection<VehicleRoutingProblemSolution> vehicleRoutingProblemSolutions) {
                secondRecord.add(discoveredSolution.getStrategyId());
            }

        });
        second.searchSolutions();

        for (int i = 0; i < 100; i++) {
            if (!firstRecord.get(i).equals(secondRecord.get(i))) {
                org.junit.Assert.assertFalse(true);
            }
        }
        org.junit.Assert.assertTrue(true);

    }

    @Test
    public void ruinedJobsShouldBeReproducible() {
        Service s = Service.Builder.newInstance("s1").setLocation(Location.newInstance(1, 1)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(1, 2)).build();
        Service s3 = Service.Builder.newInstance("s3").setLocation(Location.newInstance(1, 2)).build();
        Service s4 = Service.Builder.newInstance("s4").setLocation(Location.newInstance(1, 2)).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s4).addJob(s3).addVehicle(v).addJob(s2).addJob(s).build();
        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
            .setProperty(Jsprit.Strategy.WORST_REGRET, "0.")
            .setProperty(Jsprit.Strategy.WORST_BEST, "0.")
            .setProperty(Jsprit.Parameter.THREADS, "2").buildAlgorithm();
        vra.setMaxIterations(100);
        final List<String> firstRecord = new ArrayList<String>();
        vra.addListener(new RuinListener() {
            @Override
            public void ruinStarts(Collection<VehicleRoute> routes) {

            }

            @Override
            public void ruinEnds(Collection<VehicleRoute> routes, Collection<Job> unassignedJobs) {

            }

            @Override
            public void removed(Job job, VehicleRoute fromRoute) {
                firstRecord.add(job.getId());
            }
        });
        vra.searchSolutions();

        VehicleRoutingAlgorithm second = Jsprit.Builder.newInstance(vrp).setProperty(Jsprit.Parameter.THREADS, "4")
            .setProperty(Jsprit.Strategy.WORST_REGRET, "0.")
            .setProperty(Jsprit.Strategy.WORST_BEST, "0.")
            .buildAlgorithm();
        second.setMaxIterations(100);
        final List<String> secondRecord = new ArrayList<String>();
        second.addListener(new RuinListener() {
            @Override
            public void ruinStarts(Collection<VehicleRoute> routes) {

            }

            @Override
            public void ruinEnds(Collection<VehicleRoute> routes, Collection<Job> unassignedJobs) {

            }

            @Override
            public void removed(Job job, VehicleRoute fromRoute) {
                secondRecord.add(job.getId());
            }
        });
        second.searchSolutions();

        Assert.assertEquals(secondRecord.size(), firstRecord.size());
        for (int i = 0; i < firstRecord.size(); i++) {
            if (!firstRecord.get(i).equals(secondRecord.get(i))) {
                Assert.assertFalse(true);
            }
        }
        Assert.assertTrue(true);
    }

    @Test
    public void ruinedJobsShouldBeReproducibleV2() {
        Service s = Service.Builder.newInstance("s1").setLocation(Location.newInstance(1, 1)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(1, 2)).build();
        Service s3 = Service.Builder.newInstance("s3").setLocation(Location.newInstance(1, 2)).build();
        Service s4 = Service.Builder.newInstance("s4").setLocation(Location.newInstance(1, 2)).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s4).addJob(s3).addVehicle(v).addJob(s2).addJob(s).build();
        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        vra.setMaxIterations(100);
        final List<String> firstRecord = new ArrayList<String>();
        vra.addListener(new RuinListener() {
            @Override
            public void ruinStarts(Collection<VehicleRoute> routes) {

            }

            @Override
            public void ruinEnds(Collection<VehicleRoute> routes, Collection<Job> unassignedJobs) {

            }

            @Override
            public void removed(Job job, VehicleRoute fromRoute) {
                firstRecord.add(job.getId());
            }
        });
        vra.searchSolutions();

        VehicleRoutingAlgorithm second = Jsprit.createAlgorithm(vrp);
        second.setMaxIterations(100);
        final List<String> secondRecord = new ArrayList<String>();
        second.addListener(new RuinListener() {
            @Override
            public void ruinStarts(Collection<VehicleRoute> routes) {

            }

            @Override
            public void ruinEnds(Collection<VehicleRoute> routes, Collection<Job> unassignedJobs) {

            }

            @Override
            public void removed(Job job, VehicleRoute fromRoute) {
                secondRecord.add(job.getId());
            }
        });
        second.searchSolutions();

        Assert.assertEquals(secondRecord.size(), firstRecord.size());
        for (int i = 0; i < firstRecord.size(); i++) {
            if (!firstRecord.get(i).equals(secondRecord.get(i))) {
                Assert.assertFalse(true);
            }
        }
        Assert.assertTrue(true);
    }

    @Test
    public void insertionShouldBeReproducible() {
        Service s = Service.Builder.newInstance("s1").setLocation(Location.newInstance(1, 1)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(1, 2)).build();
        Service s3 = Service.Builder.newInstance("s3").setLocation(Location.newInstance(1, 2)).build();
        Service s4 = Service.Builder.newInstance("s4").setLocation(Location.newInstance(1, 2)).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s4).addJob(s3).addVehicle(v).addJob(s2).addJob(s).build();

        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        vra.setMaxIterations(100);
        final List<String> firstRecord = new ArrayList<String>();
        vra.addListener(new JobInsertedListener() {
            @Override
            public void informJobInserted(Job job2insert, VehicleRoute inRoute, double additionalCosts, double additionalTime) {
                firstRecord.add(job2insert.getId());
            }
        });
        vra.searchSolutions();

        VehicleRoutingAlgorithm second = Jsprit.createAlgorithm(vrp);
        second.setMaxIterations(100);
        final List<String> secondRecord = new ArrayList<String>();
        second.addListener(new JobInsertedListener() {
            @Override
            public void informJobInserted(Job job2insert, VehicleRoute inRoute, double additionalCosts, double additionalTime) {
                secondRecord.add(job2insert.getId());
            }
        });
        second.searchSolutions();

        Assert.assertEquals(secondRecord.size(), firstRecord.size());
        for (int i = 0; i < firstRecord.size(); i++) {
            if (!firstRecord.get(i).equals(secondRecord.get(i))) {
                Assert.assertFalse(true);
            }
        }
        Assert.assertTrue(true);
    }

    @Test
    public void insertionShouldBeReproducibleV2() {
        Service s = Service.Builder.newInstance("s1").setLocation(Location.newInstance(1, 1)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(1, 1)).build();
        Service s3 = Service.Builder.newInstance("s3").setLocation(Location.newInstance(1, 3)).build();
        Service s4 = Service.Builder.newInstance("s4").setLocation(Location.newInstance(1, 4)).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().setFleetSize(VehicleRoutingProblem.FleetSize.FINITE).addJob(s4).addJob(s3).addVehicle(v).addJob(s2).addJob(s).build();

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
            .setProperty(Jsprit.Strategy.WORST_REGRET, "0.")
            .setProperty(Jsprit.Strategy.WORST_BEST, "0.")
            .setProperty(Jsprit.Parameter.THREADS, "4").buildAlgorithm();
        vra.setMaxIterations(100);
        final List<String> firstRecord = new ArrayList<String>();
        final List<Double> firstRecordCosts = new ArrayList<Double>();
        vra.addListener(new BeforeJobInsertionListener() {
            @Override
            public void informBeforeJobInsertion(Job job, InsertionData data, VehicleRoute route) {
                String id = job.getId();
                firstRecordCosts.add(data.getInsertionCost());
                firstRecord.add(id);
            }
        });
        vra.searchSolutions();

        VehicleRoutingAlgorithm second = Jsprit.Builder.newInstance(vrp)
            .setProperty(Jsprit.Strategy.WORST_REGRET, "0.")
            .setProperty(Jsprit.Strategy.WORST_BEST, "0.")
            .setProperty(Jsprit.Parameter.THREADS, "5").buildAlgorithm();
        second.setMaxIterations(100);
        final List<String> secondRecord = new ArrayList<String>();
        final List<Double> secondRecordCosts = new ArrayList<Double>();
        second.addListener(new BeforeJobInsertionListener() {
            @Override
            public void informBeforeJobInsertion(Job job, InsertionData data, VehicleRoute route) {
                secondRecord.add(job.getId());
                secondRecordCosts.add(data.getInsertionCost());
            }
        });
        second.searchSolutions();

//        for(int i=0;i<firstRecord.size();i++){
//            System.out.print(firstRecord.get(i) + " (" + ((int)(firstRecordCosts.get(i)*100.))/100. + "), ");
//        }
//        System.out.println();
//        for(int i=0;i<secondRecord.size();i++){
//            System.out.print(secondRecord.get(i) + " (" + ((int)(firstRecordCosts.get(i)*100.))/100. + "), ");
//        }

        Assert.assertEquals(secondRecord.size(), firstRecord.size());
        for (int i = 0; i < firstRecord.size(); i++) {
            if (!firstRecord.get(i).equals(secondRecord.get(i))) {
                Assert.assertFalse(true);
            }
        }
        Assert.assertTrue(true);
    }

    @Test
    public void compare() {
        String s1 = "s2234";
        String s2 = "s1";
        int c = s1.compareTo(s2);
        Assert.assertEquals(1, c);
    }

    @Test
    public void algBuildTest() {
        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(1, 1)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(1, 1)).build();
        Service s3 = Service.Builder.newInstance("s3").setLocation(Location.newInstance(1, 3)).build();
        Service s4 = Service.Builder.newInstance("s4").setLocation(Location.newInstance(1, 4)).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().setFleetSize(VehicleRoutingProblem.FleetSize.FINITE).addJob(s4).addJob(s3).addVehicle(v).addJob(s2).addJob(s1).build();

        final StateManager stateManager = new StateManager(vrp);
        final ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);
        final VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setStateAndConstraintManager(stateManager, constraintManager)
            .addCoreStateAndConstraintStuff(true)
            .setCustomAcceptor(new AcceptNewRemoveFirst(20))
            .setProperty(Jsprit.Parameter.RANDOM_REGRET_MIN_SHARE, String.valueOf((int) Math.ceil(Math.max(1, .5 * vrp.getJobs().size()))))
            .setProperty(Jsprit.Parameter.RANDOM_REGRET_MAX_SHARE, String.valueOf((int) Math.ceil(Math.max(1, .5 * vrp.getJobs().size()))))
            .setProperty(Jsprit.Parameter.CLUSTER_MIN_SHARE, String.valueOf((int) Math.ceil(Math.max(1, .2 * vrp.getJobs().size()))))
            .setProperty(Jsprit.Parameter.CLUSTER_MAX_SHARE, String.valueOf((int) Math.ceil(Math.max(1, .2 * vrp.getJobs().size()))))
            .setProperty(Jsprit.Parameter.RANDOM_RANDOM_MIN_SHARE, String.valueOf((int) Math.ceil(Math.max(1, .8 * vrp.getJobs().size()))))
            .setProperty(Jsprit.Parameter.RANDOM_RANDOM_MAX_SHARE, String.valueOf((int) Math.ceil(Math.max(1, .8 * vrp.getJobs().size()))))
            .setProperty(Jsprit.Parameter.RADIAL_MIN_SHARE, String.valueOf((int) Math.ceil(Math.max(1, .3 * vrp.getJobs().size()))))
            .setProperty(Jsprit.Parameter.RADIAL_MAX_SHARE, String.valueOf((int) Math.ceil(Math.max(1, .3 * vrp.getJobs().size()))))

            .setProperty(BREAK_SCHEDULING, Boolean.FALSE.toString())

            .setProperty(Jsprit.Strategy.RADIAL_REGRET, "0.4")
            .setProperty(Jsprit.Strategy.CLUSTER_REGRET, "0.2")
            .setProperty(Jsprit.Strategy.RANDOM_REGRET, "0.4")
            .setProperty(Jsprit.Strategy.RANDOM, "0.01")
            .setProperty(Jsprit.Strategy.RANDOM_BEST, "0")
            .setProperty(Jsprit.Strategy.WORST_REGRET, "0")
            .setProperty(Jsprit.Strategy.GREEDY_BY_DISTANCE_REGRET, "0.5")
            .setProperty(Jsprit.Strategy.GREEDY_BY_NEIGHBORS_REGRET, "0.5")
            .buildAlgorithm();

        final List<SearchStrategy> strategies = vra.getSearchStrategyManager().getStrategies();
        assertEquals(6, strategies.size());

        for (SearchStrategy searchStrategy : strategies) {
            if (!searchStrategy.getId().equals(Jsprit.Strategy.RANDOM.strategyName) &&
                !searchStrategy.getId().equals(Jsprit.Strategy.GREEDY_BY_NEIGHBORS_REGRET.strategyName) &&
                !searchStrategy.getId().equals(Jsprit.Strategy.GREEDY_BY_DISTANCE_REGRET.strategyName)) {
                assertTrue(searchStrategy.getSolutionAcceptor() instanceof AcceptNewRemoveFirst);
                assertTrue(searchStrategy.getSolutionSelector() instanceof SelectBest);
                final SearchStrategyModule searchStrategyModule = searchStrategy.getSearchStrategyModules().iterator().next();
                assertTrue(searchStrategyModule instanceof RuinAndRecreateModule);
                assertTrue(searchStrategyModule.getName().equals(searchStrategy.getId()));
            } else {
                assertTrue(searchStrategy.getSolutionAcceptor() instanceof AcceptNewRemoveFirst);
                assertTrue(searchStrategy.getSolutionSelector() instanceof SelectRandomly);
                final SearchStrategyModule searchStrategyModule = searchStrategy.getSearchStrategyModules().iterator().next();
                assertTrue(searchStrategyModule instanceof RuinAndRecreateModule);
                System.out.println(searchStrategyModule.getName() + " _ " + searchStrategy.getId());
                assertTrue(searchStrategyModule.getName().equals(searchStrategy.getId()));
            }
        }
    }

}
