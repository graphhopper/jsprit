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

package com.graphhopper.jsprit.analysis.toolbox;

import com.graphhopper.jsprit.core.algorithm.SearchStrategy;
import com.graphhopper.jsprit.core.algorithm.listener.AlgorithmEndsListener;
import com.graphhopper.jsprit.core.algorithm.listener.IterationStartsListener;
import com.graphhopper.jsprit.core.algorithm.listener.StrategySelectedListener;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.*;

/**
 * Created by schroeder on 27/04/17.
 */
public class StrategyAnalyser implements AlgorithmEndsListener, StrategySelectedListener, IterationStartsListener {


    public static class Strategy {

        private final String id;

        private int selected = 0;

        private int improved = 0;

        private int countNewSolution = 0;

        private List<Double> improvements = new ArrayList<>();

        public Strategy(String id) {
            this.id = id;
        }

        public void selected() {
            selected++;
        }

        public void improvedSolution(double improvement) {
            improved++;
            improvements.add(improvement);
        }

        public void newSolution() {
            countNewSolution++;
        }

        public String getId() {
            return id;
        }

        public int getCountSelected() {
            return selected;
        }

        public int getCountImproved() {
            return improved;
        }

        public int getCountNewSolution() {
            return countNewSolution;
        }

        public List<Double> getImprovements() {
            return improvements;
        }
    }

    private Map<String, Strategy> strategyMap = new HashMap<>();

    private Collection<VehicleRoutingProblemSolution> last;

    private Writer out;

    @Override
    public void informIterationStarts(int i, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
        last = new ArrayList<>(solutions);
    }

    @Override
    public void informSelectedStrategy(SearchStrategy.DiscoveredSolution discoveredSolution, VehicleRoutingProblem vehicleRoutingProblem, Collection<VehicleRoutingProblemSolution> vehicleRoutingProblemSolutions) {
        String strategyId = discoveredSolution.getStrategyId();
        if (!strategyMap.containsKey(strategyId)) {
            strategyMap.put(strategyId, new Strategy(strategyId));
        }
        Strategy strategy = strategyMap.get(strategyId);
        strategy.selected();
        if (discoveredSolution.isAccepted()) strategy.newSolution();
        if (isBetter(vehicleRoutingProblemSolutions, last)) {
            strategy.improvedSolution(getImprovement(vehicleRoutingProblemSolutions, last));

        }
    }

    public void setOutWriter(Writer out) {
        this.out = out;
    }

    @Override
    public void informAlgorithmEnds(VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
        if (out == null) out = new PrintWriter(System.out);
        try {
            for (String stratId : strategyMap.keySet()) {
                StrategyAnalyser.Strategy strategy = strategyMap.get(stratId);
                out.write("id: " + stratId + ", #selected: " + strategy.getCountSelected() + ", #newSolutions: " + strategy.getCountNewSolution()
                    + ", #improvedSolutions: " + strategy.getCountImproved() + ", improvements: " + strategy.getImprovements().toString() + "\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                out.flush();
                out.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private double getImprovement(Collection<VehicleRoutingProblemSolution> vehicleRoutingProblemSolutions, Collection<VehicleRoutingProblemSolution> last) {
        for (VehicleRoutingProblemSolution solution : vehicleRoutingProblemSolutions) {
            for (VehicleRoutingProblemSolution lastSolution : last) {
                if (solution.getCost() < lastSolution.getCost())
                    return Math.round(lastSolution.getCost() - solution.getCost());
            }
        }
        return 0;
    }

    private boolean isBetter(Collection<VehicleRoutingProblemSolution> vehicleRoutingProblemSolutions, Collection<VehicleRoutingProblemSolution> last) {
        for (VehicleRoutingProblemSolution solution : vehicleRoutingProblemSolutions) {
            for (VehicleRoutingProblemSolution lastSolution : last) {
                if (solution.getCost() < lastSolution.getCost()) return true;
            }
        }
        return false;
    }

    public Map<String, Strategy> getStrategies() {
        return strategyMap;
    }
}
