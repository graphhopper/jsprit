package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.problem.solution.route.activity.Start;
import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnDefinition;

/**
 * The aggregated cost of the route from start till the current activity.
 *
 * <p>
 * This column sumarizes the cost of all activities from start till the current
 * activity.
 * </p>
 * <p>
 * This column is stateful and stores the sum from the prior activities on the
 * route.
 * </p>
 *
 * @author balage
 */
public class RouteCostPrinterColumn extends TransportCostPrinterColumn {

    // The aggregated cost of the route so far.
    private int aggregatedCost = 0;

    /**
     * Constructor.
     */
    public RouteCostPrinterColumn() {
        super();
    }

    /**
     * Constructor with a post creation decorator provided.
     */
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
