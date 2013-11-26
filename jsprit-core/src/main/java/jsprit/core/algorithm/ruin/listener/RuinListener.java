package jsprit.core.algorithm.ruin.listener;

import java.util.Collection;

import jsprit.core.algorithm.listener.SearchStrategyModuleListener;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.route.VehicleRoute;


/**
 * Listener that listens to the ruin-process. It informs whoever is interested about start, end and about a removal of a job.
 * 
 * @author schroeder
 *
 */
public interface RuinListener extends SearchStrategyModuleListener{
	
	/**
	 * informs about ruin-start.
	 * 
	 * @param routes
	 */
	public void ruinStarts(Collection<VehicleRoute> routes);
	
	/**
	 * informs about ruin-end.
	 * 
	 * @param routes
	 * @param unassignedJobs
	 */
	public void ruinEnds(Collection<VehicleRoute> routes, Collection<Job> unassignedJobs);
	
	/**
	 * informs if a {@link Job} has been removed from a {@link VehicleRoute}.
	 * 
	 * @param job
	 * @param fromRoute
	 */
	public void removed(Job job, VehicleRoute fromRoute);
	
}