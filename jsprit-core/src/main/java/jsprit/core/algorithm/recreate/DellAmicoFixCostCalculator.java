package jsprit.core.algorithm.recreate;

import java.util.Collection;

import jsprit.core.algorithm.recreate.listener.InsertionStartsListener;
import jsprit.core.algorithm.recreate.listener.JobInsertedListener;
import jsprit.core.problem.constraint.SoftRouteConstraint;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;

public class DellAmicoFixCostCalculator implements SoftRouteConstraint, InsertionStartsListener, JobInsertedListener{

	private int nuOfJobsToRecreate;
	
	private final JobInsertionConsideringFixCostsCalculator calculator;
	
	private final int nuOfJobs;
	
	public DellAmicoFixCostCalculator(final int nuOfJobs, final RouteAndActivityStateGetter stateGetter) {
		super();
		this.nuOfJobs=nuOfJobs;
		calculator = new JobInsertionConsideringFixCostsCalculator(null, stateGetter);
	}

	@Override
	public double getCosts(JobInsertionContext insertionContext) {// TODO Auto-generated method stub
		return calculator.getCosts(insertionContext);
	}
	
	@Override
	public void informInsertionStarts(Collection<VehicleRoute> routes, Collection<Job> unassignedJobs) {
		this.nuOfJobsToRecreate = unassignedJobs.size();
		double completenessRatio = (1-((double)nuOfJobsToRecreate/(double)nuOfJobs));
		calculator.setSolutionCompletenessRatio(completenessRatio);
	}

	@Override
	public void informJobInserted(Job job2insert, VehicleRoute inRoute, double additionalCosts, double additionalTime) {
		nuOfJobsToRecreate--;
		double completenessRatio = (1-((double)nuOfJobsToRecreate/(double)nuOfJobs));
		calculator.setSolutionCompletenessRatio(completenessRatio);
		System.out.println(completenessRatio);
	}

}
