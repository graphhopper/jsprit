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
public class VehicleActivityCountPrinterColumn
                extends AbstractPrinterColumn<VehicleStatisticsContext, Integer, VehicleActivityCountPrinterColumn> {

    /**
     * Constructor.
     */
    public VehicleActivityCountPrinterColumn() {
        super();
    }

    /**
     * Constructor with a post creation decorator provided.
     */
    public VehicleActivityCountPrinterColumn(Consumer<ColumnDefinition.Builder> decorator) {
        super(decorator);
    }

    @Override
    protected Builder getColumnBuilder() {
        return new ColumnDefinition.Builder(new IntColumnType()).withAlignment(ColumnAlignment.RIGHT);
    }

    @Override
    public Integer getData(VehicleStatisticsContext context) {
        return context.getActivityCount();
    }

    @Override
    protected String getDefaultTitle() {
        return "act count";
    }



}
