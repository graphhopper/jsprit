package com.graphhopper.jsprit.core.problem.solution.route.activity;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.graphhopper.jsprit.core.problem.AbstractJob;
import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.IndexedActivity;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.Job;

/**
 * Basic interface of job-activies.
 * <p>
 * <p>
 * A job activity is related to a {@link Job}.
 *
 * @author schroeder
 */
public abstract class JobActivity extends IndexedActivity {

    private AbstractJob job;

    private Capacity capacity;

    private double arrTime;

    private double endTime;

    private double theoreticalEarliest = 0;

    private double theoreticalLatest = Double.MAX_VALUE;

    private String name;
    private double operationTime;
    protected Location location;

    public JobActivity(AbstractJob job, String name, Location location, double operationTime, Capacity capacity) {
        super();
        this.job = job;
        this.name = name;
        this.location = location;
        this.operationTime = operationTime;
        this.capacity = capacity;
    }

    protected JobActivity(JobActivity sourceActivity) {
        job = sourceActivity.getJob();
        arrTime = sourceActivity.getArrTime();
        endTime = sourceActivity.getEndTime();
        capacity = sourceActivity.getSize();
        setIndex(sourceActivity.getIndex());
        theoreticalEarliest = sourceActivity.getTheoreticalEarliestOperationStartTime();
        theoreticalLatest = sourceActivity.getTheoreticalLatestOperationStartTime();
        name = sourceActivity.name;
        location = sourceActivity.location;
        operationTime = sourceActivity.getOperationTime();
    }

    public AbstractJob getJob() {
        return job;
    }

    @Override
    public void setTheoreticalEarliestOperationStartTime(double earliest) {
        theoreticalEarliest = earliest;
    }

    @Override
    public void setTheoreticalLatestOperationStartTime(double latest) {
        theoreticalLatest = latest;
    }


    @Override
    public double getTheoreticalEarliestOperationStartTime() {
        return theoreticalEarliest;
    }

    @Override
    public double getTheoreticalLatestOperationStartTime() {
        return theoreticalLatest;
    }

    @Override
    public double getArrTime() {
        return arrTime;
    }

    @Override
    public double getEndTime() {
        return endTime;
    }

    @Override
    public void setArrTime(double arrTime) {
        this.arrTime = arrTime;
    }

    @Override
    public void setEndTime(double endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "[type=" + getName() + "][locationId=" + getLocation().getId()
                + "][size=" + getSize().toString()
                + "][twStart=" + Activities.round(getTheoreticalEarliestOperationStartTime())
                + "][twEnd=" + Activities.round(getTheoreticalLatestOperationStartTime()) + "]";
    }

    @Override
    public Capacity getSize() {
        return capacity;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public double getOperationTime() {
        return operationTime;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public TourActivity duplicate() {
        // TODO - Balage1551 - It uses safe reflection. But this is reflection which is expensive, so
        // in case it is a bottlenect, this should be refactored
        try {
            Constructor<? extends JobActivity> constructor = getClass().getConstructor(getClass());
            return constructor.newInstance(this);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }


}