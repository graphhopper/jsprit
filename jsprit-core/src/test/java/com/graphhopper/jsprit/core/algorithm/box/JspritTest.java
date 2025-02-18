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
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.listener.StrategySelectedListener;
import com.graphhopper.jsprit.core.algorithm.recreate.InsertionData;
import com.graphhopper.jsprit.core.algorithm.recreate.listener.BeforeJobInsertionListener;
import com.graphhopper.jsprit.core.algorithm.recreate.listener.JobInsertedListener;
import com.graphhopper.jsprit.core.algorithm.ruin.listener.RuinListener;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.util.RandomNumberGeneration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by schroeder on 06/03/15.
 */
@DisplayName("Jsprit Test")
class JspritTest {

    @Test
    @DisplayName("When Running Jsprit With Single Customer _ it Should Work")
    void whenRunningJspritWithSingleCustomer_itShouldWork() {
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
                if (!counts.containsKey(strategyId))
                    counts.put(strategyId, 1);
                counts.put(strategyId, counts.get(strategyId) + 1);
            }
        });
        try {
            vra.searchSolutions();
            assertTrue(true);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @DisplayName("When Activating Strat _ it Should Be Reflected")
    void whenActivatingStrat_itShouldBeReflected() {
        Service s = Service.Builder.newInstance("s1").setLocation(Location.newInstance(1, 1)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(1, 2)).build();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(v).addJob(s2).addJob(s).build();
        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setProperty(Jsprit.Strategy.RADIAL_BEST, "100.").buildAlgorithm();
        vra.setMaxIterations(100);
        final Map<String, Integer> counts = new HashMap<String, Integer>();
        vra.addListener(new StrategySelectedListener() {

            @Override
            public void informSelectedStrategy(SearchStrategy.DiscoveredSolution discoveredSolution, VehicleRoutingProblem vehicleRoutingProblem, Collection<VehicleRoutingProblemSolution> vehicleRoutingProblemSolutions) {
                count(discoveredSolution.getStrategyId());
            }

            private void count(String strategyId) {
                if (!counts.containsKey(strategyId))
                    counts.put(strategyId, 1);
                Integer integer = counts.get(strategyId);
                counts.put(strategyId, integer + 1);
            }
        });
        vra.searchSolutions();
        assertTrue(counts.containsKey(Jsprit.Strategy.RADIAL_BEST.toString()));
    }

    @Test
    @DisplayName("When Activating Strat _ it Should Be Reflected V 2")
    void whenActivatingStrat_itShouldBeReflectedV2() {
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
                if (!counts.containsKey(strategyId))
                    counts.put(strategyId, 1);
                counts.put(strategyId, counts.get(strategyId) + 1);
            }
        });
        vra.searchSolutions();
        assertTrue(!counts.containsKey(Jsprit.Strategy.RADIAL_BEST));
    }

    @Test
    @DisplayName("Test _ v 4")
    void test_v4() {
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
                if (!counts.containsKey(strategyId))
                    counts.put(strategyId, 1);
                counts.put(strategyId, counts.get(strategyId) + 1);
            }
        });
        vra.searchSolutions();
        assertTrue(!counts.containsKey(Jsprit.Strategy.RADIAL_BEST));
    }

    @Test
    @DisplayName("Strategy Draw Should Be Reproducible")
    void strategyDrawShouldBeReproducible() {
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
                assertFalse(true);
            }
        }
        assertTrue(true);
    }

    @Test
    @DisplayName("Strategy Draw Should Be Reproducible V 2")
    void strategyDrawShouldBeReproducibleV2() {
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
                assertFalse(true);
            }
        }
        assertTrue(true);
    }

    @Test
    @DisplayName("Ruined Jobs Should Be Reproducible")
    void ruinedJobsShouldBeReproducible() {
        Service s = Service.Builder.newInstance("s1").setLocation(Location.newInstance(1, 1)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(1, 2)).build();
        Service s3 = Service.Builder.newInstance("s3").setLocation(Location.newInstance(1, 2)).build();
        Service s4 = Service.Builder.newInstance("s4").setLocation(Location.newInstance(1, 2)).build();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s4).addJob(s3).addVehicle(v).addJob(s2).addJob(s).build();
        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setProperty(Jsprit.Strategy.WORST_REGRET, "0.").setProperty(Jsprit.Strategy.WORST_BEST, "0.").setProperty(Jsprit.Parameter.THREADS, "2").buildAlgorithm();
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
        VehicleRoutingAlgorithm second = Jsprit.Builder.newInstance(vrp).setProperty(Jsprit.Parameter.THREADS, "4").setProperty(Jsprit.Strategy.WORST_REGRET, "0.").setProperty(Jsprit.Strategy.WORST_BEST, "0.").buildAlgorithm();
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
        assertEquals(secondRecord.size(), firstRecord.size());
        for (int i = 0; i < firstRecord.size(); i++) {
            if (!firstRecord.get(i).equals(secondRecord.get(i))) {
                assertFalse(true);
            }
        }
        assertTrue(true);
    }

    @Test
    @DisplayName("Ruined Jobs Should Be Reproducible V 2")
    void ruinedJobsShouldBeReproducibleV2() {
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
        assertEquals(secondRecord.size(), firstRecord.size());
        for (int i = 0; i < firstRecord.size(); i++) {
            if (!firstRecord.get(i).equals(secondRecord.get(i))) {
                assertFalse(true);
            }
        }
        assertTrue(true);
    }

    @Test
    @DisplayName("Insertion Should Be Reproducible")
    void insertionShouldBeReproducible() {
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
            public void informJobInserted(Job job2insert, VehicleRoute inRoute, InsertionData insertionData) {
                firstRecord.add(job2insert.getId());
            }
        });
        vra.searchSolutions();
        VehicleRoutingAlgorithm second = Jsprit.createAlgorithm(vrp);
        second.setMaxIterations(100);
        final List<String> secondRecord = new ArrayList<String>();
        second.addListener(new JobInsertedListener() {

            @Override
            public void informJobInserted(Job job2insert, VehicleRoute inRoute, InsertionData insertionData) {
                secondRecord.add(job2insert.getId());
            }
        });
        second.searchSolutions();
        assertEquals(secondRecord.size(), firstRecord.size());
        for (int i = 0; i < firstRecord.size(); i++) {
            if (!firstRecord.get(i).equals(secondRecord.get(i))) {
                assertFalse(true);
            }
        }
        assertTrue(true);
    }

    @Test
    @DisplayName("Insertion Should Be Reproducible V 2")
    void insertionShouldBeReproducibleV2() {
        Service s = Service.Builder.newInstance("s1").setLocation(Location.newInstance(1, 1)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(1, 1)).build();
        Service s3 = Service.Builder.newInstance("s3").setLocation(Location.newInstance(1, 3)).build();
        Service s4 = Service.Builder.newInstance("s4").setLocation(Location.newInstance(1, 4)).build();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().setFleetSize(VehicleRoutingProblem.FleetSize.FINITE).addJob(s4).addJob(s3).addVehicle(v).addJob(s2).addJob(s).build();
        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setProperty(Jsprit.Strategy.WORST_REGRET, "0.").setProperty(Jsprit.Strategy.WORST_BEST, "0.").setProperty(Jsprit.Parameter.THREADS, "4").buildAlgorithm();
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
        VehicleRoutingAlgorithm second = Jsprit.Builder.newInstance(vrp).setProperty(Jsprit.Strategy.WORST_REGRET, "0.").setProperty(Jsprit.Strategy.WORST_BEST, "0.").setProperty(Jsprit.Parameter.THREADS, "5").buildAlgorithm();
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
        // for(int i=0;i<firstRecord.size();i++){
        // System.out.print(firstRecord.get(i) + " (" + ((int)(firstRecordCosts.get(i)*100.))/100. + "), ");
        // }
        // System.out.println();
        // for(int i=0;i<secondRecord.size();i++){
        // System.out.print(secondRecord.get(i) + " (" + ((int)(firstRecordCosts.get(i)*100.))/100. + "), ");
        // }
        assertEquals(secondRecord.size(), firstRecord.size());
        for (int i = 0; i < firstRecord.size(); i++) {
            if (!firstRecord.get(i).equals(secondRecord.get(i))) {
                assertFalse(true);
            }
        }
        assertTrue(true);
    }

    @Test
    @DisplayName("Compare")
    void compare() {
        String s1 = "s2234";
        String s2 = "s1";
        int c = s1.compareTo(s2);
        assertEquals(1, c);
    }
}
