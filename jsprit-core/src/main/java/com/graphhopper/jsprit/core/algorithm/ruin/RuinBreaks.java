package com.graphhopper.jsprit.core.algorithm.ruin;

import com.graphhopper.jsprit.core.algorithm.ruin.listener.RuinListener;
import com.graphhopper.jsprit.core.problem.job.Break;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Created by schroeder on 04/08/15.
 */
public class RuinBreaks implements RuinListener {

    private final static Logger logger = LoggerFactory.getLogger(RuinBreaks.class);

    @Override
    public void ruinStarts(Collection<VehicleRoute> routes) {
    }

    @Override
    public void ruinEnds(Collection<VehicleRoute> routes, Collection<Job> unassignedJobs) {
        for (VehicleRoute r : routes) {
            Break aBreak = r.getVehicle().getBreak();
            if (aBreak != null) {
                r.getTourActivities().removeJob(aBreak);
                logger.trace("ruin: {}", aBreak.getId());
                unassignedJobs.add(aBreak);
            }
        }
    }

    @Override
    public void removed(Job job, VehicleRoute fromRoute) {
    }
}
