package com.graphhopper.jsprit.core.problem.solution.route.activity;

import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.AbstractJob;
import com.graphhopper.jsprit.core.problem.job.Job;

/**
 * Basic interface of job-activies.
 * <p>
 * <p>
 * A job activity is related to a {@link Job}.
 *
 * @author schroeder
 */
public abstract class JobActivity extends AbstractActivityNEW {

    private AbstractJob job;

    private double operationTime;
    public JobActivity(AbstractJob job, String name, Location location, double operationTime, Capacity capacity) {
        super(name, location, capacity);
        this.job = job;
        this.name = name;
        this.location = location;
        this.operationTime = operationTime;
        this.capacity = capacity;
    }

    protected JobActivity(JobActivity sourceActivity) {
        super(sourceActivity);
        job = sourceActivity.getJob();
        operationTime = sourceActivity.getOperationTime();
    }

    public AbstractJob getJob() {
        return job;
    }

    @Override
    public double getOperationTime() {
        return operationTime;
    }


}