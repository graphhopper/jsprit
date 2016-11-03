package com.graphhopper.jsprit.core.problem.solution.route.activity;



import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.IndexedActivity;
import com.graphhopper.jsprit.core.problem.Location;

public abstract class AbstractActivityNEW extends IndexedActivity {

    protected Capacity capacity;
    protected double arrTime;
    protected double endTime;
    protected double theoreticalEarliest = 0;
    protected double theoreticalLatest = Double.MAX_VALUE;
    protected String name;
    protected Location location;


    public AbstractActivityNEW(String name, Location location, Capacity capacity) {
        super();
        this.capacity = capacity;
        this.name = name;
        this.location = location;
    }

    public AbstractActivityNEW(AbstractActivityNEW sourceActivity) {
        arrTime = sourceActivity.getArrTime();
        endTime = sourceActivity.getEndTime();
        capacity = sourceActivity.getSize();
        setIndex(sourceActivity.getIndex());
        theoreticalEarliest = sourceActivity.getTheoreticalEarliestOperationStartTime();
        theoreticalLatest = sourceActivity.getTheoreticalLatestOperationStartTime();
        name = sourceActivity.name;
        location = sourceActivity.location;
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
    public Capacity getSize() {
        return capacity;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return "[type=" + getName() + "][locationId=" + getLocation().getId()
                + "][size=" + getSize().toString()
                + "][twStart=" + Activities.round(getTheoreticalEarliestOperationStartTime())
                + "][twEnd=" + Activities.round(getTheoreticalLatestOperationStartTime()) + "]";
    }


    @Override
    public TourActivity duplicate() {
        // TODO - Balage1551 - It uses safe reflection. But this is reflection which is expensive, so
        // in case it is a bottlenect, this should be refactored
        try {
            Constructor<? extends AbstractActivityNEW> constructor = getClass().getConstructor(getClass());
            return constructor.newInstance(this);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

}