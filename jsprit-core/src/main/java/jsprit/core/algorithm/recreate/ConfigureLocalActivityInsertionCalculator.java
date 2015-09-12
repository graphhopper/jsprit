package jsprit.core.algorithm.recreate;

import jsprit.core.algorithm.recreate.listener.InsertionStartsListener;
import jsprit.core.algorithm.recreate.listener.JobInsertedListener;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.route.VehicleRoute;

import java.util.Collection;

/**
 * Created by schroeder on 22/07/15.
 */
public class ConfigureLocalActivityInsertionCalculator implements InsertionStartsListener, JobInsertedListener {

    private VehicleRoutingProblem vrp;

    private LocalActivityInsertionCostsCalculator localActivityInsertionCostsCalculator;

    private int nuOfJobsToRecreate;

    public ConfigureLocalActivityInsertionCalculator(VehicleRoutingProblem vrp, LocalActivityInsertionCostsCalculator localActivityInsertionCostsCalculator) {
        this.vrp = vrp;
        this.localActivityInsertionCostsCalculator = localActivityInsertionCostsCalculator;
    }

    @Override
    public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
        this.nuOfJobsToRecreate = unassignedJobs.size();
        double completenessRatio = (1 - ((double) nuOfJobsToRecreate / (double) vrp.getJobs().values().size()));
        localActivityInsertionCostsCalculator.setSolutionCompletenessRatio(Math.max(0.5, completenessRatio));
    }

    @Override
    public void informJobInserted(Job job2insert, VehicleRoute inRoute, double additionalCosts, double additionalTime) {
        nuOfJobsToRecreate--;
        double completenessRatio = (1 - ((double) nuOfJobsToRecreate / (double) vrp.getJobs().values().size()));
        localActivityInsertionCostsCalculator.setSolutionCompletenessRatio(Math.max(0.5, completenessRatio));
    }
}
