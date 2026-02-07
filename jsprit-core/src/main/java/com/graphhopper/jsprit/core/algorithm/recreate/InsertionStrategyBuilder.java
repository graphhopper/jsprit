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

import com.graphhopper.jsprit.core.algorithm.listener.VehicleRoutingAlgorithmListeners;
import com.graphhopper.jsprit.core.algorithm.recreate.listener.InsertionListener;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleFleetManager;
import com.graphhopper.jsprit.core.util.RandomNumberGeneration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;


public class InsertionStrategyBuilder {

    public enum Strategy {
        REGRET, BEST, CHEAPEST
    }

    private final VehicleRoutingProblem vrp;

    private final StateManager stateManager;

    private boolean local = true;

    private final ConstraintManager constraintManager;

    private final VehicleFleetManager fleetManager;

    private double weightOfFixedCosts;

    private boolean considerFixedCosts = false;

    private ActivityInsertionCostsCalculator actInsertionCostsCalculator = null;

    private int forwaredLooking;

    private int memory;

    private ExecutorService executor;

    private int nuOfThreads;

    private boolean allowVehicleSwitch = true;

    private boolean addDefaultCostCalc = true;

    private Strategy strategy = Strategy.BEST;

    private boolean isFastRegret = false;

    private JobInsertionCostsCalculatorFactory shipmentInsertionCalculatorFactory;

    private JobInsertionCostsCalculatorFactory serviceInsertionCalculatorFactory;

    private JobInsertionCostsCalculatorFactory breakInsertionCalculatorFactory;

    private Random random = RandomNumberGeneration.getRandom();

    public InsertionStrategyBuilder(VehicleRoutingProblem vrp, VehicleFleetManager vehicleFleetManager, StateManager stateManager, ConstraintManager constraintManager) {
        super();
        this.vrp = vrp;
        this.stateManager = stateManager;
        this.constraintManager = constraintManager;
        this.fleetManager = vehicleFleetManager;
    }

    public InsertionStrategyBuilder setShipmentInsertionCalculatorFactory(JobInsertionCostsCalculatorFactory shipmentInsertionCalculatorFactory) {
        this.shipmentInsertionCalculatorFactory = shipmentInsertionCalculatorFactory;
        return this;
    }

    public InsertionStrategyBuilder setServiceInsertionCalculator(JobInsertionCostsCalculatorFactory serviceInsertionCalculator) {
        this.serviceInsertionCalculatorFactory = serviceInsertionCalculator;
        return this;
    }

    public InsertionStrategyBuilder setBreakInsertionCalculator(JobInsertionCostsCalculatorFactory breakInsertionCalculator) {
        this.breakInsertionCalculatorFactory = breakInsertionCalculator;
        return this;
    }

    public InsertionStrategyBuilder setInsertionStrategy(Strategy strategy) {
        this.strategy = strategy;
        return this;
    }

    public InsertionStrategyBuilder setRandom(Random random) {
        this.random = random;
        return this;
    }

    public InsertionStrategyBuilder setRouteLevel(int forwardLooking, int memory) {
        local = false;
        this.forwaredLooking = forwardLooking;
        this.memory = memory;
        return this;
    }

    public InsertionStrategyBuilder setRouteLevel(int forwardLooking, int memory, boolean addDefaultMarginalCostCalculation) {
        local = false;
        this.forwaredLooking = forwardLooking;
        this.memory = memory;
        this.addDefaultCostCalc = addDefaultMarginalCostCalculation;
        return this;
    }

    public InsertionStrategyBuilder setFastRegret(boolean fastRegret) {
        this.isFastRegret = fastRegret;
        return this;
    }


    public InsertionStrategyBuilder setLocalLevel() {
        local = true;
        return this;
    }

    public InsertionStrategyBuilder setLocalLevel(boolean addDefaultMarginalCostCalculation) {
        local = true;
        addDefaultCostCalc = addDefaultMarginalCostCalculation;
        return this;
    }

    public InsertionStrategyBuilder considerFixedCosts(double weightOfFixedCosts) {
        this.weightOfFixedCosts = weightOfFixedCosts;
        this.considerFixedCosts = true;
        return this;
    }

    public InsertionStrategyBuilder setActivityInsertionCostCalculator(ActivityInsertionCostsCalculator activityInsertionCostsCalculator) {
        this.actInsertionCostsCalculator = activityInsertionCostsCalculator;
        return this;
    }

    public InsertionStrategyBuilder setConcurrentMode(ExecutorService executor, int nuOfThreads) {
        this.executor = executor;
        this.nuOfThreads = nuOfThreads;
        return this;
    }


    public InsertionStrategy build() {
        List<InsertionListener> iListeners = new ArrayList<>();
        List<VehicleRoutingAlgorithmListeners.PrioritizedVRAListener> algorithmListeners = new ArrayList<>();
        JobInsertionCostsCalculatorBuilder calcBuilder = new JobInsertionCostsCalculatorBuilder(iListeners, algorithmListeners);
        if (local) {
            calcBuilder.setLocalLevel(addDefaultCostCalc);
        } else {
            calcBuilder.setRouteLevel(forwaredLooking, memory, addDefaultCostCalc);
        }
        if (shipmentInsertionCalculatorFactory != null)
            calcBuilder.setShipmentCalculatorFactory(shipmentInsertionCalculatorFactory);
        if (serviceInsertionCalculatorFactory != null)
            calcBuilder.setServiceCalculatorFactory(serviceInsertionCalculatorFactory);
        if (breakInsertionCalculatorFactory != null)
            calcBuilder.setBreakCalculatorFactory(breakInsertionCalculatorFactory);
        calcBuilder.setConstraintManager(constraintManager);
        calcBuilder.setStateManager(stateManager);
        calcBuilder.setVehicleRoutingProblem(vrp);
        calcBuilder.setVehicleFleetManager(fleetManager);
        calcBuilder.setActivityInsertionCostsCalculator(actInsertionCostsCalculator);
        if (considerFixedCosts) {
            calcBuilder.considerFixedCosts(weightOfFixedCosts);
        }
        calcBuilder.setAllowVehicleSwitch(allowVehicleSwitch);
        JobInsertionCostsCalculator costCalculator = calcBuilder.build();

        InsertionStrategy insertion;
        if (strategy.equals(Strategy.BEST)) {
            if (executor == null) {
                BestInsertion bestInsertion = new BestInsertion(costCalculator, vrp);
                bestInsertion.setRandom(random);
                insertion = bestInsertion;
            } else {
                BestInsertionConcurrent bestInsertion = new BestInsertionConcurrent(costCalculator, executor, nuOfThreads, vrp);
                bestInsertion.setRandom(random);
                insertion = bestInsertion;
            }
        } else if (strategy.equals(Strategy.CHEAPEST)) {
            // True Best Insertion as defined in VRP literature
            CheapestInsertion cheapestInsertion = new CheapestInsertion(costCalculator, vrp);
            cheapestInsertion.setRandom(random);
            insertion = cheapestInsertion;
        } else if (strategy.equals(Strategy.REGRET)) {
            if (executor == null) {
                if (isFastRegret) {
                    RegretInsertionFast regret = new RegretInsertionFast(costCalculator, vrp, fleetManager);
                    regret.setSwitchAllowed(allowVehicleSwitch);
                    regret.setRandom(random);
                    insertion = regret;
                } else {
                    RegretInsertion regret = new RegretInsertion(costCalculator, vrp);
                    regret.setRandom(random);
                    insertion = regret;
                }

            } else {
                if (isFastRegret) {
                    RegretInsertionConcurrentFast regret = new RegretInsertionConcurrentFast(costCalculator, vrp, executor, fleetManager);
                    regret.setSwitchAllowed(allowVehicleSwitch);
                    regret.setRandom(random);
                    insertion = regret;
                } else {
                    RegretInsertionConcurrent regret = new RegretInsertionConcurrent(costCalculator, vrp, executor);
                    regret.setRandom(random);
                    insertion = regret;
                }

            }
        } else throw new IllegalStateException("you should never get here");
        for (InsertionListener l : iListeners) insertion.addListener(l);
        return insertion;
    }

    public InsertionStrategyBuilder setAllowVehicleSwitch(boolean allowVehicleSwitch) {
        this.allowVehicleSwitch = allowVehicleSwitch;
        return this;
    }


}
