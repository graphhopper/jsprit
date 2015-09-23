package jsprit.core.algorithm.box;

import jsprit.core.algorithm.SearchStrategy;
import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.listener.StrategySelectedListener;
import jsprit.core.algorithm.recreate.listener.JobInsertedListener;
import jsprit.core.algorithm.ruin.listener.RuinListener;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.util.RandomNumberGeneration;
import junit.framework.Assert;
import org.junit.Test;

import java.util.*;

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
    public void ruinedJobsShouldBeReproducible() {
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


}
