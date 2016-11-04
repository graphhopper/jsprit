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

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getJob() == null) ? 0 : getJob().hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        JobActivity other = (JobActivity) obj;
        if (job == null) {
            if (other.getJob() != null) {
                return false;
            }
        } else if (!job.equals(other.getJob())) {
            return false;
        }
        return true;
    }


}