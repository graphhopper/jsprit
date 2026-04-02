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
import com.graphhopper.jsprit.core.problem.job.*;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleFleetManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


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

    private ConstraintManager constraintManager;

    private ActivityInsertionCostsCalculator activityInsertionCostCalculator = null;

    private boolean allowVehicleSwitch = true;

    private boolean addDefaultCostCalc = true;

    private JobInsertionCostsCalculatorFactory shipmentCalculatorFactory = new ShipmentInsertionCalculatorFactory();

    private JobInsertionCostsCalculatorFactory serviceCalculatorFactory = new ServiceInsertionCalculatorFactory();

    private JobInsertionCostsCalculatorFactory breakCalculatorFactory = new BreakInsertionCalculatorFactory();

    private InsertionPositionFilter positionFilter;

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

    public JobInsertionCostsCalculatorBuilder setShipmentCalculatorFactory(JobInsertionCostsCalculatorFactory shipmentCalculatorFactory) {
        if (shipmentCalculatorFactory == null) return this;
        this.shipmentCalculatorFactory = shipmentCalculatorFactory;
        return this;
    }

    public JobInsertionCostsCalculatorBuilder setServiceCalculatorFactory(JobInsertionCostsCalculatorFactory serviceCalculatorFactory) {
        if (serviceCalculatorFactory == null) return this;
        this.serviceCalculatorFactory = serviceCalculatorFactory;
        return this;
    }

    public JobInsertionCostsCalculatorBuilder setBreakCalculatorFactory(JobInsertionCostsCalculatorFactory breakCalculatorFactory) {
        if (breakCalculatorFactory == null) return this;
        this.breakCalculatorFactory = breakCalculatorFactory;
        return this;
    }

    /**
     * Sets the position filter for reducing position evaluations in shipment insertion.
     * This is beneficial for shipments which have O(p^2) complexity.
     *
     * @param positionFilter the position filter, or null to disable filtering
     * @return this builder
     */
    public JobInsertionCostsCalculatorBuilder setPositionFilter(InsertionPositionFilter positionFilter) {
        this.positionFilter = positionFilter;
        return this;
    }

    /**
     * Sets activityStates. MUST be set.
     *
     * @param stateManager
     * @return
     */
    public JobInsertionCostsCalculatorBuilder setStateManager(RouteAndActivityStateGetter stateManager) {
        this.states = stateManager;
        return this;
    }

    /**
     * Sets routingProblem. MUST be set.
     *
     * @param vehicleRoutingProblem
     * @return
     */
    public JobInsertionCostsCalculatorBuilder setVehicleRoutingProblem(VehicleRoutingProblem vehicleRoutingProblem) {
        this.vrp = vehicleRoutingProblem;
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
        this.activityInsertionCostCalculator = activityInsertionCostsCalculator;
        return this;
    }

    /**
     * Sets a flag to build a calculator that evaluates job insertion on route-level.
     *
     * @param forwardLooking
     * @param memory
     * @param addDefaultMarginalCostCalc
     */
    public JobInsertionCostsCalculatorBuilder setRouteLevel(int forwardLooking, int memory, boolean addDefaultMarginalCostCalc) {
        local = false;
        this.forwardLooking = forwardLooking;
        this.memory = memory;
        return this;
    }

    /**
     * @deprecated This method no longer has any effect. Fixed costs are now handled via
     * {@link com.graphhopper.jsprit.core.algorithm.recreate.IncreasingAbsoluteFixedCosts} and
     * {@link com.graphhopper.jsprit.core.algorithm.recreate.DecreasingRelativeFixedCosts} which should be added
     * as soft route constraints to the {@link com.graphhopper.jsprit.core.problem.constraint.ConstraintManager}.
     * See {@link com.graphhopper.jsprit.core.algorithm.box.Jsprit.Builder} for an example using FIXED_COST_PARAM.
     *
     * @param weightOfFixedCosts the weight (ignored)
     * @return this builder
     */
    @Deprecated
    public JobInsertionCostsCalculatorBuilder considerFixedCosts(double weightOfFixedCosts) {
        considerFixedCost = true;
        this.weightOfFixedCost = weightOfFixedCosts;
        return this;
    }

    /**
     * Builds the jobInsertionCalculator.
     *
     * @return jobInsertionCalculator.
     * @throws IllegalStateException if vrp == null or activityStates == null or fleetManager == null.
     */
    public JobInsertionCostsCalculator build() {
        if (vrp == null)
            throw new IllegalStateException("vehicle-routing-problem is null, but it must be set (this.setVehicleRoutingProblem(vrp))");
        if (states == null)
            throw new IllegalStateException("states is null, but is must be set (this.setStateManager(states))");
        if (fleetManager == null)
            throw new IllegalStateException("fleetManager is null, but it must be set (this.setVehicleFleetManager(fleetManager))");

        CalculatorPlusListeners result;
        if (local) {
            result = createStandardLocal(vrp, states);
        } else {
            checkServicesOnly();
            result = createStandardRoute(vrp, states, forwardLooking, memory);
        }

        addAlgorithmListeners(result.getAlgorithmListener());
        addInsertionListeners(result.getInsertionListener());

        return result.getCalculator();
    }

    private void checkServicesOnly() {
        for (Job j : vrp.getJobs().values()) {
            if (j.getJobType().isShipment()) {
                throw new UnsupportedOperationException("currently the 'insert-on-route-level' option is only available for services (i.e. service, pickup, delivery), \n" +
                    "if you want to deal with shipments switch to option 'local-level' by either setting bestInsertionBuilder.setLocalLevel() or \n"
                    + "by omitting the xml-tag '<level forwardLooking=2 memory=1>route</level>' when defining your insertionStrategy in algo-config.xml file");
            }
        }

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
        if (constraintManager == null) throw new IllegalStateException("constraint-manager is null");

        ActivityInsertionCostsCalculator actInsertionCalc;
        ConfigureLocalActivityInsertionCalculator configLocal = null;
        if (activityInsertionCostCalculator == null && addDefaultCostCalc) {
            actInsertionCalc = new LocalActivityInsertionCostsCalculator(vrp.getTransportCosts(), vrp.getActivityCosts(), statesManager);
            configLocal = new ConfigureLocalActivityInsertionCalculator(vrp, (LocalActivityInsertionCostsCalculator) actInsertionCalc);
        } else if (activityInsertionCostCalculator == null && !addDefaultCostCalc) {
            actInsertionCalc = new ActivityInsertionCostsCalculator() {

                @Override
                public double getCosts(JobInsertionContext iContext, TourActivity prevAct, TourActivity nextAct, TourActivity newAct,
                                       double depTimeAtPrevAct) {
                    return 0.;
                }

            };
        } else {
            actInsertionCalc = activityInsertionCostCalculator;
        }

        JobActivityFactory activityFactory = job -> vrp.copyAndGetActivities(job);

        // Configure position filter on shipment factory if available
        if (positionFilter != null && shipmentCalculatorFactory instanceof ShipmentInsertionCalculatorFactory) {
            ((ShipmentInsertionCalculatorFactory) shipmentCalculatorFactory).setPositionFilter(positionFilter);
        }

        JobInsertionCostsCalculator shipmentInsertion = shipmentCalculatorFactory.create(vrp, actInsertionCalc, activityFactory, constraintManager);
        JobInsertionCostsCalculator serviceInsertion = serviceCalculatorFactory.create(vrp, actInsertionCalc, activityFactory, constraintManager);
        JobInsertionCostsCalculator breakInsertion = breakCalculatorFactory.create(vrp, actInsertionCalc, activityFactory, constraintManager);

        // Use LinkedHashMap to preserve insertion order for deterministic iteration
        Map<Class<? extends Job>, JobInsertionCostsCalculator> calculators = new LinkedHashMap<>();
        calculators.put(Shipment.class, shipmentInsertion);
        calculators.put(Service.class, serviceInsertion);
        calculators.put(Pickup.class, serviceInsertion);
        calculators.put(Delivery.class, serviceInsertion);
        calculators.put(EnRoutePickup.class, serviceInsertion);
        calculators.put(EnRouteDelivery.class, serviceInsertion);
        calculators.put(Break.class, breakInsertion);

        VehicleTypeDependentJobInsertionCalculator mainCalculator =
                new VehicleTypeDependentJobInsertionCalculator(vrp, fleetManager, calculators);
        mainCalculator.setVehicleSwitchAllowed(allowVehicleSwitch);

        CalculatorPlusListeners calculatorPlusListeners = new CalculatorPlusListeners(mainCalculator);
        if (configLocal != null) {
            calculatorPlusListeners.insertionListener.add(configLocal);
        }
        return calculatorPlusListeners;
    }

    private CalculatorPlusListeners createStandardRoute(final VehicleRoutingProblem vrp, RouteAndActivityStateGetter activityStates2, int forwardLooking, int solutionMemory) {
        ActivityInsertionCostsCalculator routeLevelCostEstimator;
        if (activityInsertionCostCalculator == null && addDefaultCostCalc) {
            RouteLevelActivityInsertionCostsEstimator routeLevelActivityInsertionCostsEstimator = new RouteLevelActivityInsertionCostsEstimator(vrp.getTransportCosts(), vrp.getActivityCosts(), activityStates2);
            routeLevelActivityInsertionCostsEstimator.setForwardLooking(forwardLooking);
            routeLevelCostEstimator = routeLevelActivityInsertionCostsEstimator;
        } else if (activityInsertionCostCalculator == null && !addDefaultCostCalc) {
            routeLevelCostEstimator = new ActivityInsertionCostsCalculator() {

                @Override
                public double getCosts(JobInsertionContext iContext, TourActivity prevAct, TourActivity nextAct, TourActivity newAct,
                                       double depTimeAtPrevAct) {
                    return 0.;
                }

            };
        } else {
            routeLevelCostEstimator = activityInsertionCostCalculator;
        }
        ServiceInsertionOnRouteLevelCalculator serviceCalculator = new ServiceInsertionOnRouteLevelCalculator(vrp.getTransportCosts(), vrp.getActivityCosts(), routeLevelCostEstimator, constraintManager, constraintManager);
        serviceCalculator.setNuOfActsForwardLooking(forwardLooking);
        serviceCalculator.setMemorySize(solutionMemory);
        serviceCalculator.setStates(activityStates2);
        serviceCalculator.setJobActivityFactory(job -> vrp.copyAndGetActivities(job));

        // Route-level only supports services (no shipments) - see checkServicesOnly()
        Map<Class<? extends Job>, JobInsertionCostsCalculator> calculators = new LinkedHashMap<>();
        calculators.put(Service.class, serviceCalculator);
        calculators.put(Pickup.class, serviceCalculator);
        calculators.put(Delivery.class, serviceCalculator);
        calculators.put(EnRoutePickup.class, serviceCalculator);
        calculators.put(EnRouteDelivery.class, serviceCalculator);

        VehicleTypeDependentJobInsertionCalculator mainCalculator =
                new VehicleTypeDependentJobInsertionCalculator(vrp, fleetManager, calculators);
        mainCalculator.setVehicleSwitchAllowed(allowVehicleSwitch);

        return new CalculatorPlusListeners(mainCalculator);
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












