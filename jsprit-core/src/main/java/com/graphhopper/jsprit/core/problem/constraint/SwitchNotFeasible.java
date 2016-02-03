package com.graphhopper.jsprit.core.problem.constraint;

import com.graphhopper.jsprit.core.algorithm.state.InternalStates;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;

/**
 * Created by schroeder on 19/09/15.
 */
public class SwitchNotFeasible implements HardRouteConstraint {

    private StateManager stateManager;

    public SwitchNotFeasible(StateManager stateManager) {
        this.stateManager = stateManager;
    }

    @Override
    public boolean fulfilled(JobInsertionContext insertionContext) {
        Boolean notFeasible = stateManager.getRouteState(insertionContext.getRoute(), insertionContext.getNewVehicle(), InternalStates.SWITCH_NOT_FEASIBLE, Boolean.class);
        if (notFeasible == null || insertionContext.getRoute().getVehicle().getVehicleTypeIdentifier().equals(insertionContext.getNewVehicle().getVehicleTypeIdentifier()))
            return true;
        else return !notFeasible;
    }

}
