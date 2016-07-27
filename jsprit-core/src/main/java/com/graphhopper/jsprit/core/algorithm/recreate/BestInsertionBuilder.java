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

import com.graphhopper.jsprit.core.algorithm.listener.VehicleRoutingAlgorithmListeners.PrioritizedVRAListener;
import com.graphhopper.jsprit.core.algorithm.recreate.listener.InsertionListener;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleFleetManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;


public class BestInsertionBuilder {

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

    public BestInsertionBuilder(VehicleRoutingProblem vrp, VehicleFleetManager vehicleFleetManager, StateManager stateManager, ConstraintManager constraintManager) {
        super();
        this.vrp = vrp;
        this.stateManager = stateManager;
        this.constraintManager = constraintManager;
        this.fleetManager = vehicleFleetManager;
    }

    public BestInsertionBuilder setRouteLevel(int forwardLooking, int memory) {
        local = false;
        this.forwaredLooking = forwardLooking;
        this.memory = memory;
        return this;
    }

    ;

    public BestInsertionBuilder setRouteLevel(int forwardLooking, int memory, boolean addDefaultMarginalCostCalculation) {
        local = false;
        this.forwaredLooking = forwardLooking;
        this.memory = memory;
        this.addDefaultCostCalc = addDefaultMarginalCostCalculation;
        return this;
    }

    ;

    public BestInsertionBuilder setLocalLevel() {
        local = true;
        return this;
    }

    ;

    /**
     * If addDefaulMarginalCostCalculation is false, no calculator is set which implicitly assumes that marginal cost calculation
     * is controlled by your custom soft constraints.
     *
     * @param addDefaultMarginalCostCalculation
     * @return
     */
    public BestInsertionBuilder setLocalLevel(boolean addDefaultMarginalCostCalculation) {
        local = true;
        addDefaultCostCalc = addDefaultMarginalCostCalculation;
        return this;
    }

    public BestInsertionBuilder considerFixedCosts(double weightOfFixedCosts) {
        this.weightOfFixedCosts = weightOfFixedCosts;
        this.considerFixedCosts = true;
        return this;
    }

    public BestInsertionBuilder setActivityInsertionCostCalculator(ActivityInsertionCostsCalculator activityInsertionCostsCalculator) {
        this.actInsertionCostsCalculator = activityInsertionCostsCalculator;
        return this;
    }

    ;

    public BestInsertionBuilder setConcurrentMode(ExecutorService executor, int nuOfThreads) {
        this.executor = executor;
        this.nuOfThreads = nuOfThreads;
        return this;
    }


    public InsertionStrategy build() {
        List<InsertionListener> iListeners = new ArrayList<InsertionListener>();
        List<PrioritizedVRAListener> algorithmListeners = new ArrayList<PrioritizedVRAListener>();
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
        JobInsertionCostsCalculator jobInsertions = calcBuilder.build();
        InsertionStrategy bestInsertion;
        if (executor == null) {
            bestInsertion = new BestInsertion(jobInsertions, vrp);
        } else {
            bestInsertion = new BestInsertionConcurrent(jobInsertions, executor, nuOfThreads, vrp);
        }
        for (InsertionListener l : iListeners) bestInsertion.addListener(l);
        return bestInsertion;
    }


    public void setAllowVehicleSwitch(boolean allowVehicleSwitch) {
        this.allowVehicleSwitch = allowVehicleSwitch;
    }


}
