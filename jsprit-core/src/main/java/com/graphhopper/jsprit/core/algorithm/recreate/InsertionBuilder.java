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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;


public class InsertionBuilder {

    private boolean fastRegret;


    public enum Strategy {
        REGRET, BEST
    }

    private VehicleRoutingProblem vrp;

    private StateManager stateManager;

    private boolean local = true;

    private ConstraintManager constraintManager;

    private VehicleFleetManager fleetManager;

    private double weightOfFixedCosts;

    private boolean considerFixedCosts = false;

    private ActivityInsertionCostsCalculator actInsertionCostsCalculator = null;

    private int forwaredLooking;

    private int memory;

    private ExecutorService executor;

    private int nuOfThreads;

    private double timeSlice;

    private int nNeighbors;

    private boolean timeScheduling = false;

    private boolean allowVehicleSwitch = true;

    private boolean addDefaultCostCalc = true;

    private Strategy strategy = Strategy.BEST;

    private boolean isFastRegret = false;

    public InsertionBuilder(VehicleRoutingProblem vrp, VehicleFleetManager vehicleFleetManager, StateManager stateManager, ConstraintManager constraintManager) {
        super();
        this.vrp = vrp;
        this.stateManager = stateManager;
        this.constraintManager = constraintManager;
        this.fleetManager = vehicleFleetManager;
    }

    public InsertionBuilder setInsertionStrategy(Strategy strategy) {
        this.strategy = strategy;
        return this;
    }

    public InsertionBuilder setRouteLevel(int forwardLooking, int memory) {
        local = false;
        this.forwaredLooking = forwardLooking;
        this.memory = memory;
        return this;
    }

    public InsertionBuilder setRouteLevel(int forwardLooking, int memory, boolean addDefaultMarginalCostCalculation) {
        local = false;
        this.forwaredLooking = forwardLooking;
        this.memory = memory;
        this.addDefaultCostCalc = addDefaultMarginalCostCalculation;
        return this;
    }

    public InsertionBuilder setFastRegret(boolean fastRegret) {
        this.isFastRegret = fastRegret;
        return this;
    }


    public InsertionBuilder setLocalLevel() {
        local = true;
        return this;
    }

    /**
     * If addDefaulMarginalCostCalculation is false, no calculator is set which implicitly assumes that marginal cost calculation
     * is controlled by your custom soft constraints.
     *
     * @param addDefaultMarginalCostCalculation
     * @return
     */
    public InsertionBuilder setLocalLevel(boolean addDefaultMarginalCostCalculation) {
        local = true;
        addDefaultCostCalc = addDefaultMarginalCostCalculation;
        return this;
    }

    public InsertionBuilder considerFixedCosts(double weightOfFixedCosts) {
        this.weightOfFixedCosts = weightOfFixedCosts;
        this.considerFixedCosts = true;
        return this;
    }

    public InsertionBuilder setActivityInsertionCostCalculator(ActivityInsertionCostsCalculator activityInsertionCostsCalculator) {
        this.actInsertionCostsCalculator = activityInsertionCostsCalculator;
        return this;
    }

    public InsertionBuilder setConcurrentMode(ExecutorService executor, int nuOfThreads) {
        this.executor = executor;
        this.nuOfThreads = nuOfThreads;
        return this;
    }


    public InsertionStrategy build() {
        List<InsertionListener> iListeners = new ArrayList<InsertionListener>();
        List<VehicleRoutingAlgorithmListeners.PrioritizedVRAListener> algorithmListeners = new ArrayList<VehicleRoutingAlgorithmListeners.PrioritizedVRAListener>();
        JobInsertionCostsCalculatorBuilder calcBuilder = new JobInsertionCostsCalculatorBuilder(iListeners, algorithmListeners);
        if (local) {
            calcBuilder.setLocalLevel(addDefaultCostCalc);
        } else {
            calcBuilder.setRouteLevel(forwaredLooking, memory, addDefaultCostCalc);
        }
        calcBuilder.setConstraintManager(constraintManager);
        calcBuilder.setStateManager(stateManager);
        calcBuilder.setVehicleRoutingProblem(vrp);
        calcBuilder.setVehicleFleetManager(fleetManager);
        calcBuilder.setActivityInsertionCostsCalculator(actInsertionCostsCalculator);
        if (considerFixedCosts) {
            calcBuilder.considerFixedCosts(weightOfFixedCosts);
        }
        if (timeScheduling) {
            calcBuilder.experimentalTimeScheduler(timeSlice, nNeighbors);
        }
        calcBuilder.setAllowVehicleSwitch(allowVehicleSwitch);
        JobInsertionCostsCalculator costCalculator = calcBuilder.build();

        InsertionStrategy insertion;
        if (strategy.equals(Strategy.BEST)) {
            if (executor == null) {
                insertion = new BestInsertion(costCalculator, vrp);
            } else {
                insertion = new BestInsertionConcurrent(costCalculator, executor, nuOfThreads, vrp);
            }
        } else if (strategy.equals(Strategy.REGRET)) {
            if (executor == null) {
                if(isFastRegret){
                    RegretInsertionFast regret = new RegretInsertionFast(costCalculator, vrp, fleetManager);
                    regret.setSwitchAllowed(allowVehicleSwitch);
                    insertion = regret;
                }
                else {
                    RegretInsertion regret = new RegretInsertion(costCalculator, vrp);
                    insertion = regret;
                }

            } else {
                if(isFastRegret){
                    RegretInsertionConcurrentFast regret = new RegretInsertionConcurrentFast(costCalculator, vrp, executor, fleetManager);
                    regret.setSwitchAllowed(allowVehicleSwitch);
                    insertion = regret;
                }
                else{
                    RegretInsertionConcurrent regret = new RegretInsertionConcurrent(costCalculator, vrp, executor);
                    insertion = regret;
                }

            }
        } else throw new IllegalStateException("you should never get here");
        for (InsertionListener l : iListeners) insertion.addListener(l);
        return insertion;
    }

    public InsertionBuilder setAllowVehicleSwitch(boolean allowVehicleSwitch) {
        this.allowVehicleSwitch = allowVehicleSwitch;
        return this;
    }


}
