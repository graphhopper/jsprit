package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.problem.solution.route.activity.Start;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnDefinition;

/**
 * Activity duration column.
 * <p>
 * The activity duration is the sum of the activity operation (service) time and
 * the transport time to the location.
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
 * @see {@linkplain TravelDurationPrinterColumn}
 * @see {@linkplain WaitingDurationPrinterColumn}
 * @see {@linkplain OperationDurationPrinterColumn}
 */
public class ActivityDurationPrinterColumn extends AbstractDurationPrinterColumn<ActivityDurationPrinterColumn>
implements CostAndTimeExtractor {

    // The previous activity
    private TourActivity prevAct;

    /**
     * Constructor.
     */
    public ActivityDurationPrinterColumn() {
        super();
    }

    /**
     * Constructor with a post creation decorator provided.
     */
    public ActivityDurationPrinterColumn(Consumer<ColumnDefinition.Builder> decorator) {
        super(decorator);
    }


    @Override
    protected String getDefaultTitle() {
        return "duration";
    }

    @Override
    public Long getValue(RoutePrinterContext context) {
        TourActivity act = context.getActivity();
        if (act instanceof Start) {
            prevAct = null;
        }
        long val = (long) (getTransportTime(context, prevAct) + act.getOperationTime());
        prevAct = act;
        return val;
    }


}
