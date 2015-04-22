package jsprit.core.algorithm.box;

import jsprit.core.algorithm.SearchStrategy;
import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.listener.StrategySelectedListener;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.vehicle.VehicleImpl;
import junit.framework.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by schroeder on 06/03/15.
 */
public class JspritTest {

    @Test
    public void whenRunningJspritWithSingleCustomer_itShouldWork(){
        Service s = Service.Builder.newInstance("s1").setLocation(Location.newInstance(1,1)).build();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0,0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(v).addJob(s).build();
        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        vra.setMaxIterations(10000);
        final Map<String,Integer> counts = new HashMap<String,Integer>();
        vra.addListener(new StrategySelectedListener() {

            @Override
            public void informSelectedStrategy(SearchStrategy.DiscoveredSolution discoveredSolution, VehicleRoutingProblem vehicleRoutingProblem, Collection<VehicleRoutingProblemSolution> vehicleRoutingProblemSolutions) {
                count(discoveredSolution.getStrategyId());
            }

            private void count(String strategyId) {
                if(!counts.containsKey(strategyId)) counts.put(strategyId,1);
                counts.put(strategyId,counts.get(strategyId)+1);
            }

        });
        try {
            vra.searchSolutions();
            Assert.assertTrue(true);
        }
        catch (Exception e){
            Assert.assertTrue(false);
        }

    }

//    @Test
//    public void defaultStrategyProbabilitiesShouldWork_(){
//        Service s = Service.Builder.newInstance("s1").setLocation(Location.newInstance(1,1)).build();
//        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(1,2)).build();
//        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0,0)).build();
//        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(v).addJob(s2).addJob(s).build();
//        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
//        vra.setMaxIterations(5000);
//        final Map<String,Integer> counts = new HashMap<String,Integer>();
//        vra.addListener(new StrategySelectedListener() {
//
//            @Override
//            public void informSelectedStrategy(SearchStrategy.DiscoveredSolution discoveredSolution, VehicleRoutingProblem vehicleRoutingProblem, Collection<VehicleRoutingProblemSolution> vehicleRoutingProblemSolutions) {
//                count(discoveredSolution.getStrategyId());
//            }
//
//            private void count(String strategyId) {
//                if(!counts.containsKey(strategyId)) counts.put(strategyId,1);
//                Integer integer = counts.get(strategyId);
//                counts.put(strategyId, integer +1);
//            }
//
//        });
//        vra.searchSolutions();
//        Assert.assertTrue(!counts.containsKey(Jsprit.Strategy.RADIAL_BEST.toString()));
//        Assert.assertTrue(!counts.containsKey(Jsprit.Strategy.WORST_BEST.toString()));
//        Assert.assertTrue(!counts.containsKey(Jsprit.Strategy.CLUSTER_BEST.toString()));
//        Integer randomBestCounts = counts.get(Jsprit.Strategy.RANDOM_BEST.toString());
//        Assert.assertEquals(5000.*0.5/3.5,(double) randomBestCounts,100);
//        Assert.assertEquals(5000.*0.5/3.5,(double) counts.get(Jsprit.Strategy.RANDOM_REGRET.toString()),100);
//        Assert.assertEquals(5000.*0.5/3.5,(double) counts.get(Jsprit.Strategy.RADIAL_REGRET.toString()),100);
//        Assert.assertEquals(5000.*1./3.5,(double) counts.get(Jsprit.Strategy.WORST_REGRET.toString()),100);
//        Assert.assertEquals(5000.*1./3.5,(double) counts.get(Jsprit.Strategy.CLUSTER_REGRET.toString()),100);
//
//    }
//
//    @Test
//    public void whenChangingStratProb_itShouldBeReflected(){
//        Service s = Service.Builder.newInstance("s1").setLocation(Location.newInstance(1,1)).build();
//        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(1,2)).build();
//        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0,0)).build();
//        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(v).addJob(s2).addJob(s).build();
//        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
//                .setProperty(Jsprit.Strategy.RANDOM_BEST,"100.").buildAlgorithm();
//        vra.setMaxIterations(5000);
//        final Map<String,Integer> counts = new HashMap<String,Integer>();
//        vra.addListener(new StrategySelectedListener() {
//
//            @Override
//            public void informSelectedStrategy(SearchStrategy.DiscoveredSolution discoveredSolution, VehicleRoutingProblem vehicleRoutingProblem, Collection<VehicleRoutingProblemSolution> vehicleRoutingProblemSolutions) {
//                count(discoveredSolution.getStrategyId());
//            }
//
//            private void count(String strategyId) {
//                if(!counts.containsKey(strategyId)) counts.put(strategyId,1);
//                Integer integer = counts.get(strategyId);
//                counts.put(strategyId, integer +1);
//            }
//
//        });
//        vra.searchSolutions();
//        Assert.assertTrue(!counts.containsKey(Jsprit.Strategy.RADIAL_BEST.toString()));
//        Assert.assertTrue(!counts.containsKey(Jsprit.Strategy.WORST_BEST.toString()));
//        Assert.assertTrue(!counts.containsKey(Jsprit.Strategy.CLUSTER_BEST.toString()));
//        Integer randomBestCounts = counts.get(Jsprit.Strategy.RANDOM_BEST.toString());
//        Assert.assertEquals(5000.*100./103.,(double) randomBestCounts,100);
//        Assert.assertEquals(5000.*0.5/103.,(double) counts.get(Jsprit.Strategy.RANDOM_REGRET.toString()),100);
//        Assert.assertEquals(5000.*0.5/103.,(double) counts.get(Jsprit.Strategy.RADIAL_REGRET.toString()),100);
//        Assert.assertEquals(5000.*1./103.,(double) counts.get(Jsprit.Strategy.WORST_REGRET.toString()),100);
//        Assert.assertEquals(5000.*1./103.,(double) counts.get(Jsprit.Strategy.CLUSTER_REGRET.toString()),100);
//
//    }

    @Test
    public void whenActivatingStrat_itShouldBeReflected(){
        Service s = Service.Builder.newInstance("s1").setLocation(Location.newInstance(1,1)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(1,2)).build();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0,0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(v).addJob(s2).addJob(s).build();
        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
                .setProperty(Jsprit.Strategy.RADIAL_BEST,"100.").buildAlgorithm();
        vra.setMaxIterations(5000);
        final Map<String,Integer> counts = new HashMap<String,Integer>();
        vra.addListener(new StrategySelectedListener() {

            @Override
            public void informSelectedStrategy(SearchStrategy.DiscoveredSolution discoveredSolution, VehicleRoutingProblem vehicleRoutingProblem, Collection<VehicleRoutingProblemSolution> vehicleRoutingProblemSolutions) {
                count(discoveredSolution.getStrategyId());
            }

            private void count(String strategyId) {
                if(!counts.containsKey(strategyId)) counts.put(strategyId,1);
                Integer integer = counts.get(strategyId);
                counts.put(strategyId, integer +1);
            }

        });
        vra.searchSolutions();
        Assert.assertTrue(counts.containsKey(Jsprit.Strategy.RADIAL_BEST.toString()));
    }

    @Test
    public void test_v3(){
        Service s = Service.Builder.newInstance("s1").setLocation(Location.newInstance(1,1)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(1,2)).build();
        Service s3 = Service.Builder.newInstance("s3").setLocation(Location.newInstance(1,2)).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0,0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s3).addVehicle(v).addJob(s2).addJob(s).build();
        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        vra.setMaxIterations(5000);
        final Map<String,Integer> counts = new HashMap<String,Integer>();
        vra.addListener(new StrategySelectedListener() {

            @Override
            public void informSelectedStrategy(SearchStrategy.DiscoveredSolution discoveredSolution, VehicleRoutingProblem vehicleRoutingProblem, Collection<VehicleRoutingProblemSolution> vehicleRoutingProblemSolutions) {
                count(discoveredSolution.getStrategyId());
            }

            private void count(String strategyId) {
                if(!counts.containsKey(strategyId)) counts.put(strategyId,1);
                counts.put(strategyId,counts.get(strategyId)+1);
            }

        });
        vra.searchSolutions();
        Assert.assertTrue(!counts.containsKey(Jsprit.Strategy.RADIAL_BEST));
    }

    @Test
    public void test_v4(){
        Service s = Service.Builder.newInstance("s1").setLocation(Location.newInstance(1,1)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(1,2)).build();
        Service s3 = Service.Builder.newInstance("s3").setLocation(Location.newInstance(1,2)).build();
        Service s4 = Service.Builder.newInstance("s4").setLocation(Location.newInstance(1,2)).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0,0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s4).addJob(s3).addVehicle(v).addJob(s2).addJob(s).build();
        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        vra.setMaxIterations(5000);
        final Map<String,Integer> counts = new HashMap<String,Integer>();
        vra.addListener(new StrategySelectedListener() {

            @Override
            public void informSelectedStrategy(SearchStrategy.DiscoveredSolution discoveredSolution, VehicleRoutingProblem vehicleRoutingProblem, Collection<VehicleRoutingProblemSolution> vehicleRoutingProblemSolutions) {
                count(discoveredSolution.getStrategyId());
            }

            private void count(String strategyId) {
                if(!counts.containsKey(strategyId)) counts.put(strategyId,1);
                counts.put(strategyId,counts.get(strategyId)+1);
            }

        });
        vra.searchSolutions();
        Assert.assertTrue(!counts.containsKey(Jsprit.Strategy.RADIAL_BEST));
    }


}
