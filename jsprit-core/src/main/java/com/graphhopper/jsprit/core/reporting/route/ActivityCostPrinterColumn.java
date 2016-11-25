package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.ColumnDefinition.Builder;

public class ActivityCostPrinterColumn extends AbstractCostPrinterColumn {

    public ActivityCostPrinterColumn() {
        super();
    }

    public ActivityCostPrinterColumn(Consumer<Builder> decorator) {
        super(decorator);
    }


    @Override
    protected String getTitle() {
        return "actCost";
    }

    @Override
    public Integer getData(RoutePrinterContext context) {
        return (int) getActivityCost(context);
    }

}
