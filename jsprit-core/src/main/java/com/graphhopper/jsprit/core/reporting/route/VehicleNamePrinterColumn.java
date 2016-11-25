package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.reporting.AbstractPrinterColumn;
import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnDefinition;
import com.graphhopper.jsprit.core.reporting.columndefinition.StringColumnType;

public class VehicleNamePrinterColumn extends AbstractPrinterColumn<RoutePrinterContext, String, VehicleNamePrinterColumn> {

    public VehicleNamePrinterColumn() {
        super();
    }

    public VehicleNamePrinterColumn(Consumer<ColumnDefinition.Builder> decorator) {
        super(decorator);
    }

    @Override
    public ColumnDefinition.Builder getColumnBuilder() {
        return new ColumnDefinition.Builder(new StringColumnType());
    }

    @Override
    protected String getDefaultTitle() {
        return "vehicle";
    }

    @Override
    public String getData(RoutePrinterContext context) {
        return context.getRoute().getVehicle().getId();
    }

}
