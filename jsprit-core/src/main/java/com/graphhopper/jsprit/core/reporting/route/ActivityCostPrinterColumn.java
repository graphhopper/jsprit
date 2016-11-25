package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnDefinition;

public class ActivityCostPrinterColumn extends AbstractCostPrinterColumn {

    public ActivityCostPrinterColumn() {
        super();
    }

    public ActivityCostPrinterColumn(Consumer<ColumnDefinition.Builder> decorator) {
        super(decorator);
    }


    @Override
    protected String getDefaultTitle() {
        return "actCost";
    }

    @Override
    public Integer getData(RoutePrinterContext context) {
        return (int) getActivityCost(context);
    }

}
