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
package com.graphhopper.jsprit.io.algorithm;

import com.graphhopper.jsprit.core.algorithm.SearchStrategy;
import com.graphhopper.jsprit.core.algorithm.SearchStrategyModule;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.acceptor.GreedyAcceptance;
import com.graphhopper.jsprit.core.algorithm.acceptor.SolutionAcceptor;
import com.graphhopper.jsprit.core.algorithm.listener.IterationEndsListener;
import com.graphhopper.jsprit.core.algorithm.listener.SearchStrategyModuleListener;
import com.graphhopper.jsprit.core.algorithm.ruin.RuinStrategy;
import com.graphhopper.jsprit.core.algorithm.ruin.listener.RuinListener;
import com.graphhopper.jsprit.core.algorithm.selector.SelectBest;
import com.graphhopper.jsprit.core.algorithm.selector.SolutionSelector;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.io.algorithm.VehicleRoutingAlgorithms.ModKey;
import com.graphhopper.jsprit.io.algorithm.VehicleRoutingAlgorithms.TypedMap.AcceptorKey;
import com.graphhopper.jsprit.io.algorithm.VehicleRoutingAlgorithms.TypedMap.RuinStrategyKey;
import com.graphhopper.jsprit.io.algorithm.VehicleRoutingAlgorithms.TypedMap.SelectorKey;
import com.graphhopper.jsprit.io.algorithm.VehicleRoutingAlgorithms.TypedMap.StrategyModuleKey;
import com.graphhopper.jsprit.io.problem.VrpXMLReader;
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
        new AlgorithmConfigXmlReader(config).setSchemaValidation(false).read(getClass().getResource("testConfig.xml"));
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        solutions = new ArrayList<VehicleRoutingProblemSolution>();
        new VrpXMLReader(vrpBuilder, solutions).read(getClass().getResourceAsStream("finiteVrp.xml"));
        vrp = vrpBuilder.build();
    }

    @Test
    public void itShouldReadMaxIterations() {
        VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, getClass().getResource("algorithmConfigForReaderTest.xml"));
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
        VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, getClass().getResource("algorithmConfigForReaderTest2.xml"));
        IterationCounter iCounter = new IterationCounter();
        vra.addListener(iCounter);
        vra.searchSolutions();
        Assert.assertEquals(100, iCounter.iterations);
    }

    @Test
    public void itShouldReadTermination() {
        VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, getClass().getResource("algorithmConfigForReaderTest.xml"));
        IterationCounter iCounter = new IterationCounter();
        vra.addListener(iCounter);
        vra.searchSolutions();
        Assert.assertEquals(25, iCounter.iterations);
    }


    @Test
    public void testTypedMap() {
        VehicleRoutingAlgorithms.TypedMap typedMap = new VehicleRoutingAlgorithms.TypedMap();

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
        VehicleRoutingAlgorithms.TypedMap typedMap = new VehicleRoutingAlgorithms.TypedMap();

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
        VehicleRoutingAlgorithms.TypedMap typedMap = new VehicleRoutingAlgorithms.TypedMap();

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
        VehicleRoutingAlgorithms.TypedMap typedMap = new VehicleRoutingAlgorithms.TypedMap();

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
        new AlgorithmConfigXmlReader(algoConfig).read(getClass().getResource("algorithmConfig.xml"));

    }

    @Test
    public void readerTest_whenReadingAlgoWithSchemaValidationWithoutIterations_itReadsCorrectly() {
        AlgorithmConfig algoConfig = new AlgorithmConfig();
        new AlgorithmConfigXmlReader(algoConfig).read(getClass().getResource("algorithmConfig_withoutIterations.xml"));

    }

}
