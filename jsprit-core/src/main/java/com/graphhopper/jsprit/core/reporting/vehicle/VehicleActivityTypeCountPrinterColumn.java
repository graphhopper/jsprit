package com.graphhopper.jsprit.core.reporting.vehicle;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.reporting.AbstractPrinterColumn;
import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnAlignment;
import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnDefinition;
import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnDefinition.Builder;
import com.graphhopper.jsprit.core.reporting.columndefinition.IntColumnType;

/**
 * Travel duration toward the location of the activity.
 * <p>
 * The time it takes to travel to the location of the activity. The value is
 * undefined for route start activity (null).
 * </p>
 *
 * @author balage
 *
 */
public class VehicleActivityTypeCountPrinterColumn
                extends AbstractPrinterColumn<VehicleStatisticsContext, Integer, VehicleActivityTypeCountPrinterColumn> {

    private String activityType = "";

    /**
     * Constructor.
     */
    public VehicleActivityTypeCountPrinterColumn() {
        super();
    }

    /**
     * Constructor with a post creation decorator provided.
     */
    public VehicleActivityTypeCountPrinterColumn(Consumer<ColumnDefinition.Builder> decorator) {
        super(decorator);
    }

    public VehicleActivityTypeCountPrinterColumn forActivity(String type) {
        activityType = type;
        return this;
    }

    @Override
    protected Builder getColumnBuilder() {
        return new ColumnDefinition.Builder(new IntColumnType("0")).withAlignment(ColumnAlignment.RIGHT);
    }

    @Override
    public Integer getData(VehicleStatisticsContext context) {
        return context.getActivityCountByType().getOrDefault(activityType, 0);
    }

    @Override
    protected String getDefaultTitle() {
        return activityType;
    }



}
