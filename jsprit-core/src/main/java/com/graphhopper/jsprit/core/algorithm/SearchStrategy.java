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
package com.graphhopper.jsprit.core.algorithm;

import com.graphhopper.jsprit.core.algorithm.acceptor.SolutionAcceptor;
import com.graphhopper.jsprit.core.algorithm.listener.SearchStrategyModuleListener;
import com.graphhopper.jsprit.core.algorithm.selector.SolutionSelector;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.SolutionCostCalculator;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;


public class SearchStrategy {

    public static class DiscoveredSolution {

        private VehicleRoutingProblemSolution solution;

        private boolean accepted;

        private String strategyId;

        public DiscoveredSolution(VehicleRoutingProblemSolution solution, boolean accepted, String strategyId) {
            super();
            this.solution = solution;
            this.accepted = accepted;
            this.strategyId = strategyId;
        }

        public VehicleRoutingProblemSolution getSolution() {
            return solution;
        }

        public boolean isAccepted() {
            return accepted;
        }

        public String getStrategyId() {
            return strategyId;
        }

        @Override
        public String toString() {
            return "[strategyId=" + strategyId + "][solution=" + solution + "][accepted=" + accepted + "]";
        }
    }

    private static Logger logger = LoggerFactory.getLogger(SearchStrategy.class);

    private final Collection<SearchStrategyModule> searchStrategyModules = new ArrayList<SearchStrategyModule>();

    private final SolutionSelector solutionSelector;

    private final SolutionCostCalculator solutionCostCalculator;

    private final SolutionAcceptor solutionAcceptor;

    private final String id;

    private String name;

    public SearchStrategy(String id, SolutionSelector solutionSelector, SolutionAcceptor solutionAcceptor, SolutionCostCalculator solutionCostCalculator) {
        if (id == null) throw new IllegalStateException("strategy id cannot be null");
        this.solutionSelector = solutionSelector;
        this.solutionAcceptor = solutionAcceptor;
        this.solutionCostCalculator = solutionCostCalculator;
        this.id = id;
        logger.debug("initialise {}", this);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<SearchStrategyModule> getSearchStrategyModules() {
        return Collections.unmodifiableCollection(searchStrategyModules);
    }

    @SuppressWarnings("UnusedDeclaration")
    public SolutionSelector getSolutionSelector() {
        return solutionSelector;
    }

    @SuppressWarnings("UnusedDeclaration")
    public SolutionAcceptor getSolutionAcceptor() {
        return solutionAcceptor;
    }

    @Override
    public String toString() {
        return "searchStrategy [#modules=" + searchStrategyModules.size() + "][selector=" + solutionSelector + "][acceptor=" + solutionAcceptor + "]";
    }

    /**
     * Runs the search-strategy and its according modules, and returns DiscoveredSolution.
     * <p>
     * <p>This involves three basic steps: 1) Selecting a solution from solutions (input parameter) according to {@link com.graphhopper.jsprit.core.algorithm.selector.SolutionSelector}, 2) running the modules
     * ({@link SearchStrategyModule}) on the selectedSolution and 3) accepting the new solution according to {@link com.graphhopper.jsprit.core.algorithm.acceptor.SolutionAcceptor}.
     * <p> Note that after 1) the selected solution is copied, thus the original solution is not modified.
     * <p> Note also that 3) modifies the input parameter solutions by adding, removing, replacing the existing solutions or whatever is defined in the solutionAcceptor.
     *
     * @param vrp       the underlying vehicle routing problem
     * @param solutions which will be modified
     * @return discoveredSolution
     * @throws java.lang.IllegalStateException if selector cannot select any solution
     */
    @SuppressWarnings("UnusedParameters")
    public DiscoveredSolution run(VehicleRoutingProblem vrp, Collection<VehicleRoutingProblemSolution> solutions) {
        VehicleRoutingProblemSolution solution = solutionSelector.selectSolution(solutions);
        if (solution == null) throw new IllegalStateException(getErrMsg());
        VehicleRoutingProblemSolution lastSolution = VehicleRoutingProblemSolution.copyOf(solution);
        for (SearchStrategyModule module : searchStrategyModules) {
            lastSolution = module.runAndGetSolution(lastSolution);
        }
        double costs = solutionCostCalculator.getCosts(lastSolution);
        lastSolution.setCost(costs);
        boolean solutionAccepted = solutionAcceptor.acceptSolution(solutions, lastSolution);
        return new DiscoveredSolution(lastSolution, solutionAccepted, getId());
    }

    private String getErrMsg() {
        return "solution is null. check solutionSelector to return an appropriate solution. " +
            "\nfigure out whether you start with an initial solution. either you set it manually by algorithm.addInitialSolution(...)"
            + " or let the algorithm create an initial solution for you. then add the <construction>...</construction> xml-snippet to your algorithm's config file.";
    }


    public void addModule(SearchStrategyModule module) {
        if (module == null) throw new IllegalStateException("module to be added is null.");
        searchStrategyModules.add(module);
        logger.debug("module added [module={}][#modules={}]", module, searchStrategyModules.size());
    }

    public void addModuleListener(SearchStrategyModuleListener moduleListener) {
        for (SearchStrategyModule module : searchStrategyModules) {
            module.addModuleListener(moduleListener);
        }

    }

}
