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
package jsprit.core.algorithm.io;

import jsprit.core.algorithm.SearchStrategy;
import jsprit.core.algorithm.SearchStrategyModule;
import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.acceptor.GreedyAcceptance;
import jsprit.core.algorithm.acceptor.SolutionAcceptor;
import jsprit.core.algorithm.io.VehicleRoutingAlgorithms.ModKey;
import jsprit.core.algorithm.io.VehicleRoutingAlgorithms.TypedMap.AcceptorKey;
import jsprit.core.algorithm.io.VehicleRoutingAlgorithms.TypedMap.RuinStrategyKey;
import jsprit.core.algorithm.io.VehicleRoutingAlgorithms.TypedMap.SelectorKey;
import jsprit.core.algorithm.io.VehicleRoutingAlgorithms.TypedMap.StrategyModuleKey;
import jsprit.core.algorithm.listener.IterationEndsListener;
import jsprit.core.algorithm.listener.SearchStrategyModuleListener;
import jsprit.core.algorithm.ruin.RuinStrategy;
import jsprit.core.algorithm.ruin.listener.RuinListener;
import jsprit.core.algorithm.selector.SelectBest;
import jsprit.core.algorithm.selector.SolutionSelector;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.io.VrpXMLReader;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import junit.framework.Assert;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class TestAlgorithmReader {

    AlgorithmConfig config;

    VehicleRoutingProblem vrp;

    Collection<VehicleRoutingProblemSolution> solutions;

    @Before
    public void doBefore() throws ConfigurationException {
        config = new AlgorithmConfig();
        new AlgorithmConfigXmlReader(config).setSchemaValidation(false).read("src/test/resources/testConfig.xml");
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        solutions = new ArrayList<VehicleRoutingProblemSolution>();
        new VrpXMLReader(vrpBuilder, solutions).read("src/test/resources/finiteVrp.xml");
        vrp = vrpBuilder.build();
    }

    @Test
    public void itShouldReadMaxIterations() {
        VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "src/test/resources/algorithmConfigForReaderTest.xml");
        Assert.assertEquals(2000, vra.getMaxIterations());
    }

    static class IterationCounter implements IterationEndsListener {

        int iterations = 0;

        @Override
        public void informIterationEnds(int i, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
            iterations = i;
        }

    }

    @Test
    public void whenSettingPrematureBreak_itShouldReadTermination() {
        VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "src/test/resources/algorithmConfigForReaderTest2.xml");
        IterationCounter iCounter = new IterationCounter();
        vra.addListener(iCounter);
        vra.searchSolutions();
        Assert.assertEquals(100, iCounter.iterations);
    }

    @Test
    public void itShouldReadTermination() {
        VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "src/test/resources/algorithmConfigForReaderTest.xml");
        IterationCounter iCounter = new IterationCounter();
        vra.addListener(iCounter);
        vra.searchSolutions();
        Assert.assertEquals(25, iCounter.iterations);
    }


    @Test
    public void testTypedMap() {
        jsprit.core.algorithm.io.VehicleRoutingAlgorithms.TypedMap typedMap = new jsprit.core.algorithm.io.VehicleRoutingAlgorithms.TypedMap();

        String acceptorName = "acceptor";
        String acceptorId = "acceptorId";

        ModKey key = new ModKey(acceptorName, acceptorId);
        AcceptorKey accKey = new AcceptorKey(key);

        SolutionAcceptor acceptor = new GreedyAcceptance(1);

        typedMap.put(accKey, acceptor);

        assertEquals(acceptor, typedMap.get(accKey));

    }

    @Test
    public void testTypedMap2() {
        jsprit.core.algorithm.io.VehicleRoutingAlgorithms.TypedMap typedMap = new jsprit.core.algorithm.io.VehicleRoutingAlgorithms.TypedMap();

        String acceptorName = "acceptor";
        String acceptorId = "acceptorId";

        String selectorName = "selector";
        String selectorId = "selectorId";

        ModKey key = new ModKey(acceptorName, acceptorId);
        AcceptorKey accKey = new AcceptorKey(key);
        SolutionAcceptor acceptor = new GreedyAcceptance(1);

        SelectorKey selKey = new SelectorKey(new ModKey(selectorName, selectorId));
        SolutionSelector selector = new SelectBest();

        typedMap.put(accKey, acceptor);
        typedMap.put(selKey, selector);

        assertEquals(acceptor, typedMap.get(accKey));
        assertEquals(selector, typedMap.get(selKey));
    }

    @Test
    public void testTypedMap3() {
        jsprit.core.algorithm.io.VehicleRoutingAlgorithms.TypedMap typedMap = new jsprit.core.algorithm.io.VehicleRoutingAlgorithms.TypedMap();

        String acceptorName = "acceptor";
        String acceptorId = "acceptorId";

        String acceptorName2 = "acceptor2";
        String acceptorId2 = "acceptorId2";

        String selectorName = "selector";
        String selectorId = "selectorId";

        ModKey key = new ModKey(acceptorName, acceptorId);
        AcceptorKey accKey = new AcceptorKey(key);
        SolutionAcceptor acceptor = new GreedyAcceptance(1);

        SelectorKey selKey = new SelectorKey(new ModKey(selectorName, selectorId));
        SolutionSelector selector = new SelectBest();

        AcceptorKey accKey2 = new AcceptorKey(new ModKey(acceptorName2, acceptorId2));
        SolutionAcceptor acceptor2 = new GreedyAcceptance(1);

        typedMap.put(accKey, acceptor);
        typedMap.put(selKey, selector);
        typedMap.put(accKey2, acceptor2);

        assertEquals(acceptor, typedMap.get(accKey));
        assertEquals(selector, typedMap.get(selKey));
        assertEquals(acceptor2, typedMap.get(accKey2));
    }

    @Test
    public void testTypedMap4() {
        jsprit.core.algorithm.io.VehicleRoutingAlgorithms.TypedMap typedMap = new jsprit.core.algorithm.io.VehicleRoutingAlgorithms.TypedMap();

        String acceptorName = "acceptor";
        String acceptorId = "acceptorId";

        ModKey key = new ModKey(acceptorName, acceptorId);
        RuinStrategyKey accKey = new RuinStrategyKey(key);
        RuinStrategy acceptor = new RuinStrategy() {

            @Override
            public Collection<Job> ruin(Collection<VehicleRoute> vehicleRoutes) {
                return null;
            }

            @Override
            public Collection<Job> ruin(Collection<VehicleRoute> vehicleRoutes,
                                        Job targetJob, int nOfJobs2BeRemoved) {
                return null;
            }

            @Override
            public void addListener(RuinListener ruinListener) {

            }

            @Override
            public void removeListener(RuinListener ruinListener) {

            }

            @Override
            public Collection<RuinListener> getListeners() {
                return null;
            }

        };

        StrategyModuleKey moduleKey = new StrategyModuleKey(key);
        SearchStrategyModule stratModule = new SearchStrategyModule() {

            @Override
            public VehicleRoutingProblemSolution runAndGetSolution(VehicleRoutingProblemSolution vrpSolution) {
                return null;
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            public void addModuleListener(
                SearchStrategyModuleListener moduleListener) {

            }
        };

        typedMap.put(accKey, acceptor);
        typedMap.put(moduleKey, stratModule);
        typedMap.put(moduleKey, stratModule);

        assertEquals(acceptor, typedMap.get(accKey));
        assertEquals(stratModule, typedMap.get(moduleKey));

    }

    @Test
    public void initialiseConstructionAlgoCorrectly() {
        VehicleRoutingAlgorithms.createAlgorithm(vrp, config);
        assertTrue(true);
    }

    @Test
    public void whenCreatingAlgorithm_nOfStrategiesIsCorrect() {
        VehicleRoutingAlgorithm algo = VehicleRoutingAlgorithms.createAlgorithm(vrp, config);
        assertEquals(3, algo.getSearchStrategyManager().getStrategies().size());
    }

    @Test
    public void whenCreatingAlgorithm_nOfIterationsIsReadCorrectly() {
        VehicleRoutingAlgorithm algo = VehicleRoutingAlgorithms.createAlgorithm(vrp, config);
        assertEquals(10, algo.getMaxIterations());
    }

    @Test
    public void whenCreatingAlgorithm_nOfStrategyModulesIsCorrect() {
        VehicleRoutingAlgorithm algo = VehicleRoutingAlgorithms.createAlgorithm(vrp, config);
        int nOfModules = 0;
        for (SearchStrategy strat : algo.getSearchStrategyManager().getStrategies()) {
            nOfModules += strat.getSearchStrategyModules().size();
        }
        assertEquals(3, nOfModules);
    }

    @Test
    public void readerTest_whenReadingAlgoWithSchemaValidation_itReadsCorrectly() {
        AlgorithmConfig algoConfig = new AlgorithmConfig();
        new AlgorithmConfigXmlReader(algoConfig).read("src/test/resources/algorithmConfig.xml");

    }

    @Test
    public void readerTest_whenReadingAlgoWithSchemaValidationWithoutIterations_itReadsCorrectly() {
        AlgorithmConfig algoConfig = new AlgorithmConfig();
        new AlgorithmConfigXmlReader(algoConfig).read("src/test/resources/algorithmConfig_withoutIterations.xml");

    }

}
