package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.problem.solution.route.activity.Start;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnDefinition;

/**
 * Travel duration toward the location of the activity.
 * <p>
 * The time it takes to travel to the location of the activity. The value is
 * undefined for route start activity (null).
 * </p>
 * <p>
 * This column is stateful and stores the previous activity.
 * </p>
 *
 * @author balage
 *
 * @see {@linkplain ArrivalTimePrinterColumn}
 * @see {@linkplain StartTimePrinterColumn}
 * @see {@linkplain EndTimePrinterColumn}
 * @see {@linkplain WaitingDurationPrinterColumn}
 * @see {@linkplain OperationDurationPrinterColumn}
 * @see {@linkplain ActivityDurationPrinterColumn}
 */
public class TravelDurationPrinterColumn extends AbstractDurationPrinterColumn<TravelDurationPrinterColumn>
implements CostAndTimeExtractor {

    // The previous activity
    private TourActivity prevAct;

    /**
     * Constructor.
     */
    public TravelDurationPrinterColumn() {
        super();
    }

    /**
     * Constructor with a post creation decorator provided.
     */
    public TravelDurationPrinterColumn(Consumer<ColumnDefinition.Builder> decorator) {
        super(decorator);
    }


    @Override
    protected String getDefaultTitle() {
        return "travel";
    }

    @Override
    public Long getValue(RoutePrinterContext context) {
        TourActivity act = context.getActivity();
        if (act instanceof Start) {
            prevAct = null;
        }
        long val = (long) (getTransportTime(context, prevAct));
        prevAct = act;
        return val;
    }


}
