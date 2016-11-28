package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.problem.solution.route.activity.Start;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnDefinition;

/**
 * The cost of travelling to the activity.
 *
 * <p>
 * This is the cost of the transport from the previous to this activity. For the
 * start of the route this value is undefined (null).
 * </p>
 * <p>
 * This column is stateful and stores the previous activity.
 * </p>
 *
 * @author balage
 */
public class TransportCostPrinterColumn extends AbstractCostPrinterColumn {

    // The previous activity
    private TourActivity prevAct;

    /**
     * Constructor.
     */
    public TransportCostPrinterColumn() {
        super();
    }

    /**
     * Constructor with a post creation decorator provided.
     */
    public TransportCostPrinterColumn(Consumer<ColumnDefinition.Builder> decorator) {
        super(decorator);
    }

    @Override
    protected String getDefaultTitle() {
        return "transCost";
    }


    @Override
    public Integer getData(RoutePrinterContext context) {
        TourActivity act = context.getActivity();
        if (act instanceof Start) {
            prevAct = null;
        }
        double res = getTransportCost(context, prevAct);
        prevAct = act;
        return (int) res;
    }

}
