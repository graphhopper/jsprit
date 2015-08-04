package jsprit.core.algorithm.ruin;

import jsprit.core.algorithm.ruin.listener.RuinListener;
import jsprit.core.problem.job.Break;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.route.VehicleRoute;

import java.util.Collection;

/**
 * Created by schroeder on 04/08/15.
 */
public class RuinBreaks implements RuinListener {

    @Override
    public void ruinStarts(Collection<VehicleRoute> routes) {}

    @Override
    public void ruinEnds(Collection<VehicleRoute> routes, Collection<Job> unassignedJobs) {
        for(VehicleRoute r : routes){
            Break aBreak = r.getVehicle().getBreak();
            if(aBreak != null){
                r.getTourActivities().removeJob(aBreak);
                unassignedJobs.add(aBreak);
            }
        }
    }

    @Override
    public void removed(Job job, VehicleRoute fromRoute) {}
}
