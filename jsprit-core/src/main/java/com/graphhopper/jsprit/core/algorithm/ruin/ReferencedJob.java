package com.graphhopper.jsprit.core.algorithm.ruin;

import com.graphhopper.jsprit.core.problem.job.Job;

/**
 * Created by schroeder on 07/01/15.
 */
class ReferencedJob {
    private Job job;
    private double distance;

    public ReferencedJob(Job job, double distance) {
        super();
        this.job = job;
        this.distance = distance;
    }

    public Job getJob() {
        return job;
    }

    public double getDistance() {
        return distance;
    }
}
