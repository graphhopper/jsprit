/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package jsprit.core.algorithm.recreate;

import jsprit.core.algorithm.recreate.RegretInsertion.DefaultScorer;
import jsprit.core.algorithm.recreate.RegretInsertion.ScoredJob;
import jsprit.core.algorithm.recreate.RegretInsertion.ScoringFunction;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.route.VehicleRoute;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
* Insertion based on regret approach.
*
* <p>Basically calculates the insertion cost of the firstBest and the secondBest alternative. The score is then calculated as difference
* between secondBest and firstBest, plus additional scoring variables that can defined in this.ScoringFunction.
* The idea is that if the cost of the secondBest alternative is way higher than the first best, it seems to be important to insert this
* customer immediatedly. If difference is not that high, it might not impact solution if this customer is inserted later.
*
* @author stefan schroeder
*
*/
public class RegretInsertionConcurrent extends AbstractInsertionStrategy {


    private static Logger logger = LogManager.getLogger(RegretInsertionConcurrent.class);

	private ScoringFunction scoringFunction;

    private final JobInsertionCostsCalculator insertionCostsCalculator;

    private final ExecutorCompletionService<ScoredJob> completionService;

    /**
	 * Sets the scoring function.
	 *
	 * <p>By default, the this.TimeWindowScorer is used.
	 *
	 * @param scoringFunction to score
	 */
	public void setScoringFunction(ScoringFunction scoringFunction) {
		this.scoringFunction = scoringFunction;
	}

	public RegretInsertionConcurrent(JobInsertionCostsCalculator jobInsertionCalculator, VehicleRoutingProblem vehicleRoutingProblem, ExecutorService executorService) {
		super(vehicleRoutingProblem);
        this.scoringFunction = new DefaultScorer(vehicleRoutingProblem);
		this.insertionCostsCalculator = jobInsertionCalculator;
        this.vrp = vehicleRoutingProblem;
        completionService = new ExecutorCompletionService<ScoredJob>(executorService);
		logger.debug("initialise " + this);
	}

	@Override
	public String toString() {
		return "[name=regretInsertion][additionalScorer="+scoringFunction+"]";
	}


	/**
	 * Runs insertion.
	 *
	 * <p>Before inserting a job, all unassigned jobs are scored according to its best- and secondBest-insertion plus additional scoring variables.
     *
     * @throws java.lang.RuntimeException if smth went wrong with thread execution
	 *
	 */
	@Override
	public Collection<Job> insertUnassignedJobs(Collection<VehicleRoute> routes, Collection<Job> unassignedJobs) {
        List<Job> badJobs = new ArrayList<Job>(unassignedJobs.size());
        List<Job> jobs = new ArrayList<Job>(unassignedJobs);

        while (!jobs.isEmpty()) {
            List<Job> unassignedJobList = new ArrayList<Job>(jobs);
            List<Job> badJobList = new ArrayList<Job>();
            ScoredJob bestScoredJob = nextJob(routes, unassignedJobList, badJobList);
            if(bestScoredJob != null){
                if(bestScoredJob.isNewRoute()){
                    routes.add(bestScoredJob.getRoute());
                }
                insertJob(bestScoredJob.getJob(),bestScoredJob.getInsertionData(),bestScoredJob.getRoute());
                jobs.remove(bestScoredJob.getJob());
            }
            for(Job j : badJobList) {
                jobs.remove(j);
                badJobs.add(j);
            }
        }
        return badJobs;
    }

    private ScoredJob nextJob(final Collection<VehicleRoute> routes, List<Job> unassignedJobList, List<Job> badJobList) {
        ScoredJob bestScoredJob = null;

        for (final Job unassignedJob : unassignedJobList) {
            completionService.submit(new Callable<ScoredJob>() {

                @Override
                public ScoredJob call() throws Exception {
                    return RegretInsertion.getScoredJob(routes, unassignedJob, insertionCostsCalculator, scoringFunction);
                }

            });
        }

        try{
            for(int i=0; i < unassignedJobList.size(); i++){
                Future<ScoredJob> fsj = completionService.take();
                ScoredJob sJob = fsj.get();
                if(sJob instanceof RegretInsertion.BadJob){
                    badJobList.add(sJob.getJob());
                    continue;
                }
                if(bestScoredJob == null){
                    bestScoredJob = sJob;
                }
                else if(sJob.getScore() > bestScoredJob.getScore()){
                    bestScoredJob = sJob;
                }
            }
        }
        catch(InterruptedException e){
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        return bestScoredJob;
    }







}
