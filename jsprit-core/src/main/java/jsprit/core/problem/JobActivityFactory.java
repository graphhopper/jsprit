package jsprit.core.problem;

import jsprit.core.problem.job.Job;

import java.util.List;

/**
 * Created by schroeder on 14.07.14.
 */
public interface JobActivityFactory {

    public List<AbstractActivity> createActivity(Job job);

}
