package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.algorithm.recreate.listener.JobInsertedListener;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.job.Break;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;

/**
 * Created by schroeder on 07/04/16.
 */
public class BreakScheduling implements JobInsertedListener {

    private final StateManager stateManager;

    private final BreakInsertionCalculator breakInsertionCalculator;

    private final EventListeners eventListeners;

    public BreakScheduling(VehicleRoutingProblem vrp, StateManager stateManager, ConstraintManager constraintManager) {
        this.stateManager = stateManager;
        this.breakInsertionCalculator = new BreakInsertionCalculator(vrp.getTransportCosts(),vrp.getActivityCosts(),new LocalActivityInsertionCostsCalculator(vrp.getTransportCosts(),vrp.getActivityCosts(),stateManager),constraintManager);
        this.breakInsertionCalculator.setJobActivityFactory(vrp.getJobActivityFactory());
        eventListeners = new EventListeners();
    }

    @Override
    public void informJobInserted(Job job2insert, VehicleRoute inRoute, double additionalCosts, double additionalTime) {
        Break aBreak = inRoute.getVehicle().getBreak();
        if(aBreak != null){
            boolean removed = inRoute.getTourActivities().removeJob(aBreak);
            if(removed){
                stateManager.removed(aBreak,inRoute);
                stateManager.reCalculateStates(inRoute);
            }
            if(inRoute.getEnd().getArrTime() > aBreak.getTimeWindow().getEnd()){
                InsertionData iData = breakInsertionCalculator.getInsertionData(inRoute, aBreak, inRoute.getVehicle(), inRoute.getDepartureTime(), inRoute.getDriver(), Double.MAX_VALUE);
                if(!(iData instanceof InsertionData.NoInsertionFound)){
                    for(Event e : iData.getEvents()){
                        eventListeners.inform(e);
                    }
                    stateManager.reCalculateStates(inRoute);
                }
            }


        }
    }

}
