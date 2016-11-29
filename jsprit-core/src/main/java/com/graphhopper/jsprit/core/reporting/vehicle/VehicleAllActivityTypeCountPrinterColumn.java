package com.graphhopper.jsprit.core.reporting.vehicle;

import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.graphhopper.jsprit.core.reporting.AbstractPrinterColumn;
import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnDefinition;
import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnDefinition.Builder;
import com.graphhopper.jsprit.core.reporting.columndefinition.StringColumnType;

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
public class VehicleAllActivityTypeCountPrinterColumn
extends AbstractPrinterColumn<VehicleSummaryContext, String, VehicleAllActivityTypeCountPrinterColumn> {

    /**
     * Constructor.
     */
    public VehicleAllActivityTypeCountPrinterColumn() {
        super();
    }

    /**
     * Constructor with a post creation decorator provided.
     */
    public VehicleAllActivityTypeCountPrinterColumn(Consumer<ColumnDefinition.Builder> decorator) {
        super(decorator);
    }

    @Override
    protected Builder getColumnBuilder() {
        return new ColumnDefinition.Builder(new StringColumnType());
    }

    @Override
    public String getData(VehicleSummaryContext context) {
        return context.getActivityCountByType().entrySet().stream()
                        .map(en -> "[" + en.getKey() + "=" + en.getValue() + "]")
                        .collect(Collectors.joining());
    }

    @Override
    protected String getDefaultTitle() {
        return "act counts";
    }



}
