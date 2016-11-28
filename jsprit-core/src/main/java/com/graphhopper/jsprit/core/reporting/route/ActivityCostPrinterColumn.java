package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnDefinition;

/**
 * Cost of the activity.
 *
 * @author balage
 *
 */
public class ActivityCostPrinterColumn extends AbstractCostPrinterColumn {

    /**
     * Constructor.
     */
    public ActivityCostPrinterColumn() {
        super();
    }

    /**
     * Constructor with a post creation decorator provided.
     */
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
