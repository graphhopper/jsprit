package com.graphhopper.jsprit.core.problem;

import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.activity.JobActivity;

import java.util.List;

public class SimpleJobActivityFactory implements JobActivityFactory {

    @Override
    public List<JobActivity> createActivities(Job job) {
        return job.getActivityList().getAll();
    }
}
