package com.graphhopper.jsprit.core.problem;

import com.graphhopper.jsprit.core.problem.job.Job;

import java.util.List;

/**
 * JobActivityFactory that creates the activities to the specified job.
 */
public interface JobActivityFactory {

    public List<AbstractActivity> createActivities(Job job);

}
