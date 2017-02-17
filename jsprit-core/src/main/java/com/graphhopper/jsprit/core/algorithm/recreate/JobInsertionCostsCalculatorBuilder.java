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
import com.graphhopper.jsprit.core.problem.JobActivityFactory;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.job.Break;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleFleetManager;

import java.util.ArrayList;
import java.util.List;


public class JobInsertionCostsCalculatorBuilder {


    private static class CalculatorPlusListeners {

        private JobInsertionCostsCalculator calculator;

        public JobInsertionCostsCalculator getCalculator() {
            return calculator;
        }

        private List<PrioritizedVRAListener> algorithmListener = new ArrayList<PrioritizedVRAListener>();
        private List<InsertionListener> insertionListener = new ArrayList<InsertionListener>();

        public CalculatorPlusListeners(JobInsertionCostsCalculator calculator) {
            super();
            this.calculator = calculator;
        }

        public List<PrioritizedVRAListener> getAlgorithmListener() {
            return algorithmListener;
        }

        public List<InsertionListener> getInsertionListener() {
            return insertionListener;
        }
    }

    private List<InsertionListener> insertionListeners;

    private List<PrioritizedVRAListener> algorithmListeners;

    private VehicleRoutingProblem vrp;

    private RouteAndActivityStateGetter states;

    private boolean local = true;

    private int forwardLooking = 0;

    private int memory = 1;

    private boolean considerFixedCost = false;

    private double weightOfFixedCost = 0;

    private VehicleFleetManager fleetManager;

    private boolean timeScheduling = false;

    private double timeSlice;

    private int neighbors;

    private ConstraintManager constraintManager;

    private ActivityInsertionCostsCalculator activityInsertionCostCalculator = null;

    private boolean allowVehicleSwitch = true;

    private boolean addDefaultCostCalc = true;

    /**
     * Constructs the builder.
     * <p>
     * <p>Some calculators require information from the overall algorithm or the higher-level insertion procedure. Thus listeners inform them.
     * These listeners are cached in the according list and can thus be added when its time to add them.
     *
     * @param insertionListeners
     * @param algorithmListeners
     */
    public JobInsertionCostsCalculatorBuilder(List<InsertionListener> insertionListeners, List<PrioritizedVRAListener> algorithmListeners) {
        super();
        this.insertionListeners = insertionListeners;
        this.algorithmListeners = algorithmListeners;
    }

    /**
     * Sets activityStates. MUST be set.
     *
     * @param stateManager
     * @return
     */
    public JobInsertionCostsCalculatorBuilder setStateManager(RouteAndActivityStateGetter stateManager) {
        states = stateManager;
        return this;
    }

    /**
     * Sets routingProblem. MUST be set.
     *
     * @param vehicleRoutingProblem
     * @return
     */
    public JobInsertionCostsCalculatorBuilder setVehicleRoutingProblem(VehicleRoutingProblem vehicleRoutingProblem) {
        vrp = vehicleRoutingProblem;
        return this;
    }

    /**
     * Sets fleetManager. MUST be set.
     *
     * @param fleetManager
     * @return
     */
    public JobInsertionCostsCalculatorBuilder setVehicleFleetManager(VehicleFleetManager fleetManager) {
        this.fleetManager = fleetManager;
        return this;
    }

    /**
     * Sets a flag to build a calculator based on local calculations.
     * <p>
     * <p>Insertion of a job and job-activity is evaluated based on the previous and next activity.
     *
     * @param addDefaultCostCalc
     */
    public JobInsertionCostsCalculatorBuilder setLocalLevel(boolean addDefaultCostCalc) {
        local = true;
        this.addDefaultCostCalc = addDefaultCostCalc;
        return this;
    }

    public JobInsertionCostsCalculatorBuilder setActivityInsertionCostsCalculator(ActivityInsertionCostsCalculator activityInsertionCostsCalculator) {
        activityInsertionCostCalculator = activityInsertionCostsCalculator;
        return this;
    }

    /**
     * Sets a flag to build a calculator that evaluates job insertion on route-level.
     *
     * @param forwardLooking
     * @param memory
     * @param addDefaultMarginalCostCalc
     */
    @Deprecated
    public JobInsertionCostsCalculatorBuilder setRouteLevel(int forwardLooking, int memory, boolean addDefaultMarginalCostCalc) {
        local = false;
        this.forwardLooking = forwardLooking;
        this.memory = memory;
        return this;
    }

    /**
     * Sets a flag to consider also fixed-cost when evaluating the insertion of a job. The weight of the fixed-cost can be determined by setting
     * weightofFixedCosts.
     *
     * @param weightOfFixedCosts
     */
    public JobInsertionCostsCalculatorBuilder considerFixedCosts(double weightOfFixedCosts) {
        considerFixedCost = true;
        weightOfFixedCost = weightOfFixedCosts;
        return this;
    }

    @Deprecated
    public JobInsertionCostsCalculatorBuilder experimentalTimeScheduler(double timeSlice, int neighbors) {
        timeScheduling = true;
        this.timeSlice = timeSlice;
        this.neighbors = neighbors;
        return this;
    }

    /**
     * Builds the jobInsertionCalculator.
     *
     * @return jobInsertionCalculator.
     * @throws IllegalStateException if vrp == null or activityStates == null or fleetManager == null.
     */
    public JobInsertionCostsCalculator build() {
        if (vrp == null) {
            throw new IllegalStateException("vehicle-routing-problem is null, but it must be set (this.setVehicleRoutingProblem(vrp))");
        }
        if (states == null) {
            throw new IllegalStateException("states is null, but is must be set (this.setStateManager(states))");
        }
        if (fleetManager == null) {
            throw new IllegalStateException("fleetManager is null, but it must be set (this.setVehicleFleetManager(fleetManager))");
        }
        JobInsertionCostsCalculator baseCalculator = null;
        CalculatorPlusListeners standardLocal = null;
        if (local) {
            standardLocal = createStandardLocal(vrp, states);
        } else {
            throw new UnsupportedOperationException("route level cal is not supported anymore");
        }
        baseCalculator = standardLocal.getCalculator();
        addAlgorithmListeners(standardLocal.getAlgorithmListener());
        addInsertionListeners(standardLocal.getInsertionListener());
        if (considerFixedCost) {
//            CalculatorPlusListeners withFixed = createCalculatorConsideringFixedCosts(vrp, baseCalculator, states, weightOfFixedCost);
//            baseCalculator = withFixed.getCalculator();
//            addAlgorithmListeners(withFixed.getAlgorithmListener());
//            addInsertionListeners(withFixed.getInsertionListener());
        }
        if (timeScheduling) {
//			baseCalculator = new CalculatesServiceInsertionWithTimeSchedulingInSlices(baseCalculator,timeSlice,neighbors);
            CalculatesServiceInsertionWithTimeScheduling wts = new CalculatesServiceInsertionWithTimeScheduling(baseCalculator, timeSlice, neighbors);
            CalculatorPlusListeners calcPlusListeners = new CalculatorPlusListeners(wts);
            calcPlusListeners.getInsertionListener().add(new CalculatesServiceInsertionWithTimeScheduling.KnowledgeInjection(wts));
            addInsertionListeners(calcPlusListeners.getInsertionListener());
            baseCalculator = calcPlusListeners.getCalculator();
        }
        return createFinalInsertion(fleetManager, baseCalculator, states);
    }

    private void addInsertionListeners(List<InsertionListener> list) {
        for (InsertionListener iL : list) {
            insertionListeners.add(iL);
        }
    }

    private void addAlgorithmListeners(List<PrioritizedVRAListener> list) {
        for (PrioritizedVRAListener aL : list) {
            algorithmListeners.add(aL);
        }
    }

    private CalculatorPlusListeners createStandardLocal(final VehicleRoutingProblem vrp, RouteAndActivityStateGetter statesManager) {
        if (constraintManager == null) {
            throw new IllegalStateException("constraint-manager is null");
        }

        ActivityInsertionCostsCalculator actInsertionCalc;
        ConfigureLocalActivityInsertionCalculator configLocal = null;
        if (activityInsertionCostCalculator == null && addDefaultCostCalc) {
            actInsertionCalc = new LocalActivityInsertionCostsCalculator(vrp.getTransportCosts(), vrp.getActivityCosts(), statesManager);
            configLocal = new ConfigureLocalActivityInsertionCalculator(vrp, (LocalActivityInsertionCostsCalculator) actInsertionCalc);
        } else if (activityInsertionCostCalculator == null && !addDefaultCostCalc) {
            actInsertionCalc = (iContext, prevAct, nextAct, newAct, depTimeAtPrevAct) -> 0.;
        } else {
            actInsertionCalc = activityInsertionCostCalculator;
        }

        JobActivityFactory activityFactory = job -> vrp.copyAndGetActivities(job);

//        ShipmentInsertionCalculator shipmentInsertion = new ShipmentInsertionCalculator(vrp.getTransportCosts(), vrp.getActivityCosts(),actInsertionCalc, constraintManager);
//        shipmentInsertion.setJobActivityFactory(activityFactory);
//        ServiceInsertionCalculator serviceInsertion = new ServiceInsertionCalculator(vrp.getTransportCosts(), vrp.getActivityCosts(), actInsertionCalc, constraintManager);
//        serviceInsertion.setJobActivityFactory(activityFactory);

        GeneralJobInsertionCalculator generalJobInsertionCalculator = new GeneralJobInsertionCalculator(vrp.getTransportCosts(), vrp.getActivityCosts(), actInsertionCalc, constraintManager);

        BreakInsertionCalculator breakInsertionCalculator = new BreakInsertionCalculator(vrp.getTransportCosts(), vrp.getActivityCosts(), actInsertionCalc, constraintManager);
        breakInsertionCalculator.setJobActivityFactory(activityFactory);

        JobCalculatorSwitcher switcher = new JobCalculatorSwitcher();
        switcher.put(Job.class, generalJobInsertionCalculator);
//        switcher.put(Service.class, serviceInsertion);
//        switcher.put(Pickup.class, serviceInsertion);
//        switcher.put(Delivery.class, serviceInsertion);
        switcher.put(Break.class, breakInsertionCalculator);

        CalculatorPlusListeners calculatorPlusListeners = new CalculatorPlusListeners(switcher);
        if (configLocal != null) {
            calculatorPlusListeners.insertionListener.add(configLocal);
        }
        return calculatorPlusListeners;
    }

    private CalculatorPlusListeners createStandardRoute(final VehicleRoutingProblem vrp, RouteAndActivityStateGetter activityStates2, int forwardLooking, int solutionMemory) {
        throw new UnsupportedOperationException("route level insertion calculation is not supported anymore");
    }

    private JobInsertionCostsCalculator createFinalInsertion(VehicleFleetManager fleetManager, JobInsertionCostsCalculator baseCalc, RouteAndActivityStateGetter activityStates2) {
        VehicleTypeDependentJobInsertionCalculator vehicleTypeDependentJobInsertionCalculator = new VehicleTypeDependentJobInsertionCalculator(vrp, fleetManager, baseCalc);
        vehicleTypeDependentJobInsertionCalculator.setVehicleSwitchAllowed(allowVehicleSwitch);
        return vehicleTypeDependentJobInsertionCalculator;
    }

    public JobInsertionCostsCalculatorBuilder setConstraintManager(ConstraintManager constraintManager) {
        this.constraintManager = constraintManager;
        return this;
    }

    public JobInsertionCostsCalculatorBuilder setAllowVehicleSwitch(boolean allowVehicleSwitch) {
        this.allowVehicleSwitch = allowVehicleSwitch;
        return this;
    }

}












