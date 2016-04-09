package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.algorithm.recreate.listener.JobInsertedListener;
import com.graphhopper.jsprit.core.algorithm.ruin.listener.RuinListener;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.job.Break;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;

import java.util.*;

/**
 * Created by schroeder on 07/04/16.
 */
public class BreakScheduling implements JobInsertedListener, RuinListener {

    private final StateManager stateManager;

    private final BreakInsertionCalculator breakInsertionCalculator;

    private final EventListeners eventListeners;

    private Set<VehicleRoute> modifiedRoutes = new HashSet<VehicleRoute>();

    public BreakScheduling(VehicleRoutingProblem vrp, StateManager stateManager, ConstraintManager constraintManager) {
        this.stateManager = stateManager;
        this.breakInsertionCalculator = new BreakInsertionCalculator(vrp.getTransportCosts(),vrp.getActivityCosts(),new LocalActivityInsertionCostsCalculator(vrp.getTransportCosts(),vrp.getActivityCosts(),stateManager),constraintManager);
        this.breakInsertionCalculator.setJobActivityFactory(vrp.getJobActivityFactory());
        eventListeners = new EventListeners();
    }

    @Override
    public void informJobInserted(Job job2insert, VehicleRoute inRoute, double additionalCosts, double additionalTime) {
        Break aBreak = inRoute.getVehicle().getBreak();
//        if(job2insert == aBreak) return;
        if(aBreak != null){
            boolean removed = inRoute.getTourActivities().removeJob(aBreak);
            if(removed){
                stateManager.removed(aBreak,inRoute);
                stateManager.reCalculateStates(inRoute);
                //updateRoute --> alles wichtiges states
            }
            if(inRoute.getEnd().getArrTime() > aBreak.getTimeWindow().getEnd()){
                InsertionData iData = breakInsertionCalculator.getInsertionData(inRoute, aBreak, inRoute.getVehicle(), inRoute.getDepartureTime(), inRoute.getDriver(), Double.MAX_VALUE);
                if(!(iData instanceof InsertionData.NoInsertionFound)){
                    for(Event e : iData.getEvents()){
                        eventListeners.inform(e);
                    }
                    //inform job inserted
                    stateManager.informJobInserted(aBreak,inRoute,0,0);
                }
            }


        }
    }

    @Override
    public void ruinStarts(Collection<VehicleRoute> routes) {

    }

    @Override
    public void ruinEnds(Collection<VehicleRoute> routes, Collection<Job> unassignedJobs) {
        for(VehicleRoute route : modifiedRoutes){
            Break aBreak = route.getVehicle().getBreak();
            route.getTourActivities().removeJob(aBreak);
        }
        List<Break> breaks = new ArrayList<Break>();
        if(!modifiedRoutes.isEmpty()) {
            for (Job j : unassignedJobs) {
                if (j instanceof Break) {
                    breaks.add((Break) j);
                }
            }
        }
        for(Break b : breaks){ unassignedJobs.remove(b); }
        modifiedRoutes.clear();
    }

    @Override
    public void removed(Job job, VehicleRoute fromRoute) {
        if(fromRoute.getVehicle().getBreak() != null) modifiedRoutes.add(fromRoute);
    }
}
