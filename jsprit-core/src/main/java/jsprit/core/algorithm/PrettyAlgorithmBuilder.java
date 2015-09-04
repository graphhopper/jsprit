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

package jsprit.core.algorithm;

import jsprit.core.algorithm.acceptor.SchrimpfAcceptance;
import jsprit.core.algorithm.acceptor.SolutionAcceptor;
import jsprit.core.algorithm.listener.AlgorithmStartsListener;
import jsprit.core.algorithm.recreate.InsertionStrategy;
import jsprit.core.algorithm.recreate.VehicleSwitched;
import jsprit.core.algorithm.state.*;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.constraint.ConstraintManager;
import jsprit.core.problem.solution.SolutionCostCalculator;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleFleetManager;
import jsprit.core.problem.vehicle.VehicleTypeKey;
import jsprit.core.util.ActivityTimeTracker;

import java.util.*;

/**
* Created by schroeder on 10.12.14.
*/
public class PrettyAlgorithmBuilder {

    private final VehicleRoutingProblem vrp;

    private final VehicleFleetManager fleetManager;

    private final StateManager stateManager;

    private final ConstraintManager constraintManager;

    private SearchStrategyManager searchStrategyManager;

    private InsertionStrategy iniInsertionStrategy;

    private SolutionCostCalculator iniObjFunction;

    private boolean coreStuff = false;

    public static PrettyAlgorithmBuilder newInstance(VehicleRoutingProblem vrp, VehicleFleetManager fleetManager, StateManager stateManager, ConstraintManager constraintManager){
        return new PrettyAlgorithmBuilder(vrp,fleetManager,stateManager,constraintManager);
    }

    PrettyAlgorithmBuilder(VehicleRoutingProblem vrp, VehicleFleetManager fleetManager, StateManager stateManager, ConstraintManager constraintManager){
        this.vrp = vrp;
        this.fleetManager = fleetManager;
        this.stateManager = stateManager;
        this.constraintManager = constraintManager;
        this.searchStrategyManager = new SearchStrategyManager();
    }

    public PrettyAlgorithmBuilder setRandom(Random random){
        searchStrategyManager.setRandom(random);
        return this;
    }

    public PrettyAlgorithmBuilder withStrategy(SearchStrategy strategy, double weight){
        searchStrategyManager.addStrategy(strategy,weight);
        return this;
    }

    public PrettyAlgorithmBuilder constructInitialSolutionWith(InsertionStrategy insertionStrategy, SolutionCostCalculator objFunction){
        this.iniInsertionStrategy = insertionStrategy;
        this.iniObjFunction = objFunction;
        return this;
    }

    public VehicleRoutingAlgorithm build(){
        if(coreStuff){
            constraintManager.addTimeWindowConstraint();
            constraintManager.addLoadConstraint();
            constraintManager.addSkillsConstraint();
            stateManager.updateLoadStates();
            stateManager.updateTimeWindowStates();
            UpdateVehicleDependentPracticalTimeWindows tw_updater = new UpdateVehicleDependentPracticalTimeWindows(stateManager, vrp.getTransportCosts());
            tw_updater.setVehiclesToUpdate(new UpdateVehicleDependentPracticalTimeWindows.VehiclesToUpdate() {

                Map<VehicleTypeKey,Vehicle> uniqueTypes = new HashMap<VehicleTypeKey,Vehicle>();

                @Override
                public Collection<Vehicle> get(VehicleRoute vehicleRoute) {
                    if(uniqueTypes.isEmpty()){
                        for( Vehicle v : vrp.getVehicles()){
                            if(!uniqueTypes.containsKey(v.getVehicleTypeIdentifier())){
                                uniqueTypes.put(v.getVehicleTypeIdentifier(),v);
                            }
                        }
                    }
                    Collection<Vehicle> vehicles = new ArrayList<Vehicle>();
                    vehicles.addAll(uniqueTypes.values());
                    return vehicles;
                }
            });
            stateManager.addStateUpdater(tw_updater);
            stateManager.updateSkillStates();
            stateManager.addStateUpdater(new UpdateEndLocationIfRouteIsOpen());
            stateManager.addStateUpdater(new UpdateActivityTimes(vrp.getTransportCosts(), ActivityTimeTracker.ActivityPolicy.AS_SOON_AS_TIME_WINDOW_OPENS));
            stateManager.addStateUpdater(new UpdateVariableCosts(vrp.getActivityCosts(), vrp.getTransportCosts(), stateManager));
        }
        VehicleRoutingAlgorithm vra = new VehicleRoutingAlgorithm(vrp,searchStrategyManager);
        vra.addListener(stateManager);
        RemoveEmptyVehicles removeEmptyVehicles = new RemoveEmptyVehicles(fleetManager);
        ResetAndIniFleetManager resetAndIniFleetManager = new ResetAndIniFleetManager(fleetManager);
        VehicleSwitched vehicleSwitched = new VehicleSwitched(fleetManager);
        vra.addListener(removeEmptyVehicles);
        vra.addListener(resetAndIniFleetManager);
        vra.addListener(vehicleSwitched);
        if(iniInsertionStrategy != null) {
            if (!iniInsertionStrategy.getListeners().contains(removeEmptyVehicles))
                iniInsertionStrategy.addListener(removeEmptyVehicles);
            if (!iniInsertionStrategy.getListeners().contains(resetAndIniFleetManager))
                iniInsertionStrategy.addListener(resetAndIniFleetManager);
            if (!iniInsertionStrategy.getListeners().contains(vehicleSwitched))
                iniInsertionStrategy.addListener(vehicleSwitched);
            if (!iniInsertionStrategy.getListeners().contains(stateManager))
                iniInsertionStrategy.addListener(stateManager);
            vra.addListener(new AlgorithmStartsListener() {
                @Override
                public void informAlgorithmStarts(VehicleRoutingProblem problem, VehicleRoutingAlgorithm algorithm, Collection<VehicleRoutingProblemSolution> solutions) {
                    if (solutions.isEmpty()) {
                        solutions.add(new InsertionInitialSolutionFactory(iniInsertionStrategy, iniObjFunction).createSolution(vrp));
                    }
                }
            });
        }
        addArbitraryListener(vra);
        return vra;
    }

    private void addArbitraryListener(VehicleRoutingAlgorithm vra) {
        searchSchrimpfAndRegister(vra);
    }

    private void searchSchrimpfAndRegister(VehicleRoutingAlgorithm vra) {
        boolean schrimpfAdded = false;
        for(SearchStrategy strategy : vra.getSearchStrategyManager().getStrategies()){
            SolutionAcceptor acceptor = strategy.getSolutionAcceptor();
            if(acceptor instanceof SchrimpfAcceptance){
                if(!schrimpfAdded) {
                    vra.addListener((SchrimpfAcceptance) acceptor);
                    schrimpfAdded = true;
                }
            }
        }
    }

    public PrettyAlgorithmBuilder addCoreStateAndConstraintStuff() {
        this.coreStuff = true;
        return this;
    }


}
