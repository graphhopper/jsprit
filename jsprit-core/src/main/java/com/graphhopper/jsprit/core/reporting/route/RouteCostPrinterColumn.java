package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.problem.solution.route.activity.Start;
import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnDefinition;

public class RouteCostPrinterColumn extends TransportCostPrinterColumn {

    private int aggregatedCost = 0;

    public RouteCostPrinterColumn() {
        super();
    }

    public RouteCostPrinterColumn(Consumer<ColumnDefinition.Builder> decorator) {
        super(decorator);
    }


    @Override
    protected String getDefaultTitle() {
        return "routeCost";
    }

    @Override
    public Integer getData(RoutePrinterContext context) {
        if (context.getActivity() instanceof Start) {
            aggregatedCost = 0;
        }

        Integer res = super.getData(context);
        if (res != null) {
            aggregatedCost += res;
        }
        aggregatedCost += getActivityCost(context);
        return aggregatedCost;
    }


}
