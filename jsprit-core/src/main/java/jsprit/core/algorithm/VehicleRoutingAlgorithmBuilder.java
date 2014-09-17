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

import jsprit.core.algorithm.io.AlgorithmConfig;
import jsprit.core.algorithm.io.AlgorithmConfigXmlReader;
import jsprit.core.algorithm.io.VehicleRoutingAlgorithms;
import jsprit.core.algorithm.state.StateManager;
import jsprit.core.algorithm.state.UpdateEndLocationIfRouteIsOpen;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.constraint.ConstraintManager;
import jsprit.core.problem.solution.SolutionCostCalculator;

/**
 * Builder that builds a {@link VehicleRoutingAlgorithm}.
 * 
 * @author schroeder
 *
 */
public class VehicleRoutingAlgorithmBuilder {

	private String algorithmConfigFile;
	
	private AlgorithmConfig algorithmConfig;
	
	private final VehicleRoutingProblem vrp;

	private SolutionCostCalculator solutionCostCalculator;

	private StateManager stateManager;
	
	private boolean addCoreConstraints = false;
	
	private boolean addDefaultCostCalculators = false;

	private ConstraintManager constraintManager;

	private int nuOfThreads=0;
	
	/**
	 * Constructs the builder with the problem and an algorithmConfigFile. Latter is to configure and specify the ruin-and-recreate meta-heuristic.
	 * 
	 * @param problem to solve
	 * @param algorithmConfig config file of VehicleRoutingAlgorithm
	 */
	public VehicleRoutingAlgorithmBuilder(VehicleRoutingProblem problem, String algorithmConfig) {
		this.vrp=problem;
		this.algorithmConfigFile=algorithmConfig;
		this.algorithmConfig=null;
	}
	
	/**
	 * Constructs the builder with the problem and an algorithmConfig. Latter is to configure and specify the ruin-and-recreate meta-heuristic.
	 *
     * @param problem to solve
     * @param algorithmConfig config file of VehicleRoutingAlgorithm
	 */
	public VehicleRoutingAlgorithmBuilder(VehicleRoutingProblem problem, AlgorithmConfig algorithmConfig) {
		this.vrp=problem;
		this.algorithmConfigFile=null;
		this.algorithmConfig=algorithmConfig;
	}

	/**
	 * Sets custom objective function. 
	 * 
	 * <p>If objective function is not set, a default function is applied (which basically minimizes 
	 * fixed and variable transportation costs ({@link VariablePlusFixedSolutionCostCalculatorFactory}).
	 * 
	 * @param objectiveFunction to be minimized
	 * @see VariablePlusFixedSolutionCostCalculatorFactory
	 */
	public void setObjectiveFunction(SolutionCostCalculator objectiveFunction) {
		this.solutionCostCalculator = objectiveFunction;
	}

	/**
	 * Sets stateManager to memorize states.
	 * 
	 * @param stateManager that memorizes your states
	 * @see StateManager
	 */
	public void setStateManager(StateManager stateManager) {
		this.stateManager=stateManager;
	}

	/**
	 * Adds core constraints.
	 * 
	 * <p>Thus, it adds vehicle-capacity, time-window and skills constraints and their
	 * required stateUpdater.
	 * 
	 */
	public void addCoreConstraints() {
		addCoreConstraints=true;
	}

	/**
	 * Adds default cost calculators used by the insertion heuristic,
	 * to calculate activity insertion costs.
	 * By default, marginal transportation costs are calculated. Thus when inserting
	 * act_k between act_i and act_j, marginal (additional) transportation costs
	 * are basically c(act_i,act_k)+c(act_k,act_j)-c(act_i,act_j).
	 * 
	 * <p>Do not use this method, if you plan to control the insertion heuristic
	 * entirely via hard- and soft-constraints.
	 */
	public void addDefaultCostCalculators() {
		addDefaultCostCalculators=true;
	}

	/**
	 * Sets state- and constraintManager. 
	 * 
	 * @param stateManager that memorizes your states
	 * @param constraintManager that manages your constraints
	 * @see StateManager
	 * @see ConstraintManager
	 */
	public void setStateAndConstraintManager(StateManager stateManager, ConstraintManager constraintManager) {
		this.stateManager=stateManager;
		this.constraintManager=constraintManager;
	}
	
	/**
	 * Sets nuOfThreads.
	 * 
	 * @param nuOfThreads to be operated
	 */
	public void setNuOfThreads(int nuOfThreads){
		this.nuOfThreads=nuOfThreads;
	}

	/**
	 * Builds and returns the algorithm.
	 * 
	 * <p>If algorithmConfigFile is set, it reads the configuration.
	 * 
	 * @return the algorithm
	 */
	public VehicleRoutingAlgorithm build() {
		if(stateManager == null) stateManager = new StateManager(vrp);
		if(constraintManager == null) constraintManager = new ConstraintManager(vrp,stateManager);
		//add core updater
		stateManager.addStateUpdater(new UpdateEndLocationIfRouteIsOpen());
//		stateManager.addStateUpdater(new OpenRouteStateVerifier());

		if(addCoreConstraints){
			constraintManager.addLoadConstraint();
			constraintManager.addTimeWindowConstraint();
			constraintManager.addSkillsConstraint();
            stateManager.updateLoadStates();
			stateManager.updateTimeWindowStates();
            stateManager.updateSkillStates();
		}
		if(algorithmConfig==null){
			algorithmConfig = new AlgorithmConfig();
			AlgorithmConfigXmlReader xmlReader = new AlgorithmConfigXmlReader(algorithmConfig);
			xmlReader.read(algorithmConfigFile);
		}
		return VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, algorithmConfig, nuOfThreads, solutionCostCalculator, stateManager, constraintManager, addDefaultCostCalculators);
	}

	
}
