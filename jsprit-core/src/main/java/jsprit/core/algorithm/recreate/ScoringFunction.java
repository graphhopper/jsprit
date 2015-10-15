package jsprit.core.algorithm.recreate;

import jsprit.core.problem.job.Job;

/**
 * Created by schroeder on 15/10/15.
 */
public interface ScoringFunction {

    public double score(InsertionData best, Job job);

}
