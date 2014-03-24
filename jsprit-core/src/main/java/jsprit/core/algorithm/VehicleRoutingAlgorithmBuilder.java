package jsprit.core.algorithm;

import jsprit.core.algorithm.io.VehicleRoutingAlgorithms;
import jsprit.core.algorithm.state.StateManager;
import jsprit.core.algorithm.state.UpdateActivityTimes;
import jsprit.core.algorithm.state.UpdateEndLocationIfRouteIsOpen;
import jsprit.core.algorithm.state.UpdateVariableCosts;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.constraint.ConstraintManager;
import jsprit.core.problem.solution.SolutionCostCalculator;

public class VehicleRoutingAlgorithmBuilder {

	private final String algorithmConfig;
	
	private final VehicleRoutingProblem vrp;

	private SolutionCostCalculator solutionCostCalculator;

	private StateManager stateManager;
	
	private boolean addCoreConstraints = false;
	
	private boolean addDefaultCostCalculators = false;

	private ConstraintManager constraintManager;

	private int nuOfThreads=0;
	
	public VehicleRoutingAlgorithmBuilder(VehicleRoutingProblem problem, String algorithmConfig) {
		this.vrp=problem;
		this.algorithmConfig=algorithmConfig;
	}

	public void setObjectiveFunction(SolutionCostCalculator objectiveFunction) {
		this.solutionCostCalculator = objectiveFunction;
	}

	public void setStateManager(StateManager stateManager) {
		this.stateManager=stateManager;
	}

	public void addCoreConstraints() {
		addCoreConstraints=true;
	}

	public void addDefaultCostCalculators() {
		addDefaultCostCalculators=true;
	}

	public void setConstraintManager(ConstraintManager constraintManager) {
		this.constraintManager=constraintManager;
	}
	
	public void setNuOfThreads(int nuOfThreads){
		this.nuOfThreads=nuOfThreads;
	}

	public VehicleRoutingAlgorithm build() {
		if(stateManager == null) stateManager = new StateManager(vrp.getTransportCosts());
		if(constraintManager == null) constraintManager = new ConstraintManager(vrp,stateManager,vrp.getConstraints());
		//add core updater
		stateManager.addStateUpdater(new UpdateEndLocationIfRouteIsOpen());
//		stateManager.addStateUpdater(new OpenRouteStateVerifier());
		stateManager.addStateUpdater(new UpdateActivityTimes(vrp.getTransportCosts()));
		stateManager.addStateUpdater(new UpdateVariableCosts(vrp.getActivityCosts(), vrp.getTransportCosts(), stateManager));
		if(addCoreConstraints){
			constraintManager.addLoadConstraint();
			constraintManager.addTimeWindowConstraint();
			stateManager.updateLoadStates();
			stateManager.updateTimeWindowStates();
		}
		return VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, algorithmConfig, nuOfThreads, solutionCostCalculator, stateManager, constraintManager, addDefaultCostCalculators);
	}

	
}
