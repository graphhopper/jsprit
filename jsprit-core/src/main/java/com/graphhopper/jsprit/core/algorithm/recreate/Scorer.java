package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.problem.job.Job;

/**
 * Created by schroeder on 24/05/16.
 */
class Scorer {

    static double score(Job unassignedJob, InsertionData best, InsertionData secondBest, ScoringFunction scoringFunction){
        if (best == null) {
            throw new IllegalStateException("cannot insert job " + unassignedJob.getId());
        }
        double score;
        if (secondBest == null) { //either there is only one vehicle or there are more vehicles, but they cannot load unassignedJob
            //if only one vehicle, I want the job to be inserted with min iCosts
            //if there are more vehicles, I want this job to be prioritized since there are no alternatives
            score = (4 - unassignedJob.getPriority()) * (Integer.MAX_VALUE - best.getInsertionCost()) + scoringFunction.score(best, unassignedJob);
        } else {
            score = (4 - unassignedJob.getPriority()) * (secondBest.getInsertionCost() - best.getInsertionCost()) + scoringFunction.score(best, unassignedJob);
        }
        return score;
    }
}
