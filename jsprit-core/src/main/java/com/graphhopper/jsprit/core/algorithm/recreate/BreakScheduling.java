package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.algorithm.recreate.listener.InsertionStartsListener;
import com.graphhopper.jsprit.core.algorithm.recreate.listener.JobInsertedListener;
import com.graphhopper.jsprit.core.algorithm.ruin.listener.RuinListener;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.job.Break;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Created by schroeder on 07/04/16.
 */
public class BreakScheduling implements InsertionStartsListener,JobInsertedListener, RuinListener {

    private final static Logger logger = LogManager.getLogger();

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
        if(aBreak != null){
            boolean removed = inRoute.getTourActivities().removeJob(aBreak);
            if(removed){
                logger.trace("ruin: {}", aBreak.getId());
                stateManager.removed(aBreak,inRoute);
                stateManager.reCalculateStates(inRoute);
            }
            if(inRoute.getEnd().getArrTime() > aBreak.getTimeWindow().getEnd()){
                InsertionData iData = breakInsertionCalculator.getInsertionData(inRoute, aBreak, inRoute.getVehicle(), inRoute.getDepartureTime(), inRoute.getDriver(), Double.MAX_VALUE);
                if(!(iData instanceof InsertionData.NoInsertionFound)){
                    logger.trace("insert: [jobId={}]{}", aBreak.getId(), iData);
                    for(Event e : iData.getEvents()){
                        eventListeners.inform(e);
                    }
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
        for(VehicleRoute route : routes){
            Break aBreak = route.getVehicle().getBreak();
            boolean removed = route.getTourActivities().removeJob(aBreak);
            if(removed) logger.trace("ruin: {}", aBreak.getId());
        }
        List<Break> breaks = new ArrayList<Break>();
        for (Job j : unassignedJobs) {
            if (j instanceof Break) {
                breaks.add((Break) j);
            }
        }
        for(Break b : breaks){ unassignedJobs.remove(b); }
    }

    @Override
    public void removed(Job job, VehicleRoute fromRoute) {
        if(fromRoute.getVehicle().getBreak() != null) modifiedRoutes.add(fromRoute);
    }

    @Override
    public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
        for(VehicleRoute route : vehicleRoutes){
            Break aBreak = route.getVehicle().getBreak();
            if(aBreak != null && !route.getTourActivities().servesJob(aBreak)){
                if(route.getEnd().getArrTime() > aBreak.getTimeWindow().getEnd()){
                    InsertionData iData = breakInsertionCalculator.getInsertionData(route, aBreak, route.getVehicle(), route.getDepartureTime(), route.getDriver(), Double.MAX_VALUE);
                    if(!(iData instanceof InsertionData.NoInsertionFound)){
                        logger.trace("insert: [jobId={}]{}", aBreak.getId(), iData);
                        for(Event e : iData.getEvents()){
                            eventListeners.inform(e);
                        }
                        stateManager.informJobInserted(aBreak,route,0,0);
                    }
                }
            }
        }

    }
}
