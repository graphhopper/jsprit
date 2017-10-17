package com.graphhopper.jsprit.core.problem.solution.route.activity;

import java.util.Collection;
import java.util.HashSet;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.SizeDimension;
import com.graphhopper.jsprit.core.problem.job.AbstractJob;
import com.graphhopper.jsprit.core.problem.job.AbstractListBackedJobActivityList.FriendlyHandshake;
import com.graphhopper.jsprit.core.problem.job.Job;

/**
 * Basic interface of job-related activies.
 * <p>
 * A job activity may have time windows, operation time and is related to a
 * {@link Job}.
 * </p>
 *
 * @author schroeder
 * @author Balage
 */
public abstract class JobActivity extends AbstractActivity {

    private AbstractJob job;

    private double operationTime;

    private Collection<TimeWindow> timeWindows;

    private int orderNumber;

    /**
     * Constructor.
     *
     * @param job
     *            The job the activity is part of.
     * @param type
     *            The type of the activity.
     * @param location
     *            The location of the activity.
     * @param operationTime
     *            The duration of the activity.
     * @param capacity
     *            The cargo change of the activity.
     * @param timeWindows
     *            The time windows of the activity.
     */
    public JobActivity(AbstractJob job, String type, Location location, double operationTime,
            SizeDimension capacity, Collection<TimeWindow> timeWindows) {
        super(type, location, capacity);
        this.job = job;
        this.operationTime = operationTime;
        this.timeWindows = timeWindows;
    }

    /**
     * Copy constructor.
     * <p>
     * This makes a <b>shallow</b> copy of the <code>sourceActivity</code>.
     * </p>
     *
     * @param sourceActivity
     *            The activity to copy.
     */
    protected JobActivity(JobActivity sourceActivity) {
        super(sourceActivity);
        job = sourceActivity.getJob();
        operationTime = sourceActivity.getOperationTime();
        orderNumber = sourceActivity.getOrderNumber();
        // REMARK - Balage1551 - Do we need to deep copy time window set? I
        // guess we don't.
        if (sourceActivity.timeWindows != null) {
            timeWindows = new HashSet<>(sourceActivity.timeWindows);
        }
    }

    /**
     * @return The job the activity is associated with.
     */
    public AbstractJob getJob() {
        return job;
    }

    @Override
    public double getOperationTime() {
        return operationTime;
    }

    @Override
    public String getName() {
        return job.getId() + "." + getType();
    }

    /**
     * @return The time windows.
     */
    public Collection<TimeWindow> getTimeWindows() {
        return timeWindows;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((job == null) ? 0 : job.hashCode());
        result = prime * result + orderNumber;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        JobActivity other = (JobActivity) obj;
        if (job == null) {
            if (other.job != null)
                return false;
        } else if (!job.equals(other.job))
            return false;
        if (orderNumber != other.orderNumber)
            return false;
        return true;
    }

    /**
     * @return The order of the task within its job
     */
    public int getOrderNumber() {
        return orderNumber;
    }

    /**
     * Sets the order number of the activity within the job.
     * <p>
     * <b>Warning! This function is not part of the API. Calling it would throw
     * {@linkplain IllegalStateException}. </b>
     * </p>
     *
     * @param friendLock
     *            Internal friend handshake object.
     * @param orderNumber
     *            The order number.
     */
    public void impl_setOrderNumber(FriendlyHandshake hadshake, int orderNumber) {
        if (hadshake == null)
            throw new IllegalStateException();
        this.orderNumber = orderNumber;
    }

}
