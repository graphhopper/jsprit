package com.graphhopper.jsprit.core.problem.solution.route.activity;



import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.graphhopper.jsprit.core.problem.SizeDimension;
import com.graphhopper.jsprit.core.problem.Location;

public abstract class AbstractActivityNEW implements TourActivity {

    private int index;
    protected SizeDimension capacity;
    protected double arrTime;
    protected double endTime;
    protected double theoreticalEarliest = 0;
    protected double theoreticalLatest = Double.MAX_VALUE;
    protected String type;
    protected Location location;


    public AbstractActivityNEW(String type, Location location, SizeDimension capacity) {
        super();
        this.capacity = capacity;
        this.type = type;
        this.location = location;
    }


    public AbstractActivityNEW(AbstractActivityNEW sourceActivity) {
        arrTime = sourceActivity.getArrTime();
        endTime = sourceActivity.getEndTime();
        capacity = sourceActivity.getSize();
        setIndex(sourceActivity.getIndex());
        theoreticalEarliest = sourceActivity.getTheoreticalEarliestOperationStartTime();
        theoreticalLatest = sourceActivity.getTheoreticalLatestOperationStartTime();
        type = sourceActivity.type;
        location = sourceActivity.location;
    }

    @Override
    public void setTheoreticalEarliestOperationStartTime(double earliest) {
        theoreticalEarliest = earliest;
    }

    @Override
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
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
    public SizeDimension getSize() {
        return capacity;
    }

    @Override
    public String getName() {
        return getType();
    }

    public String getType() {
        return type;
    }


    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return "[name=" + getName() + "][locationId=" + getLocation().getId()
                        + "][size=" + getSize().toString()
                        + "][twStart=" + Activities.round(getTheoreticalEarliestOperationStartTime())
                        + "][twEnd=" + Activities.round(getTheoreticalLatestOperationStartTime()) + "]";
    }


    @Override
    public TourActivity duplicate() {
        // TODO - Balage1551 - It uses safe reflection. But this is reflection which is expensive, so
        // in case it is a bottleneck, this should be refactored
        try {
            Constructor<? extends AbstractActivityNEW> constructor = getClass().getConstructor(getClass());
            return constructor.newInstance(this);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException e) {
            System.out.println(this.getClass().getCanonicalName() + " : " + this);
            throw new IllegalStateException(e);
        }
    }

}