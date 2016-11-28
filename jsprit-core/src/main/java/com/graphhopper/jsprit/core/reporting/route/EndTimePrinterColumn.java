package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.problem.solution.route.activity.End;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnDefinition;

/**
 * End time of the activity.
 * <p>
 * For route end the value is undefined (null), for other activities, it is the
 * time when the activity is finished and the vehicle could progress toward the
 * next activity.
 * </p>
 *
 * @author balage
 *
 * @see {@linkplain ArrivalTimePrinterColumn}
 * @see {@linkplain StartTimePrinterColumn}
 * @see {@linkplain TravelDurationPrinterColumn}
 * @see {@linkplain WaitingDurationPrinterColumn}
 * @see {@linkplain OperationDurationPrinterColumn}
 * @see {@linkplain ActivityDurationPrinterColumn}
 */
public class EndTimePrinterColumn extends AbstractTimePrinterColumn<EndTimePrinterColumn> {

    /**
     * Constructor.
     */
    public EndTimePrinterColumn() {
        super();
    }

    /**
     * Constructor with a post creation decorator provided.
     */
    public EndTimePrinterColumn(Consumer<ColumnDefinition.Builder> decorator) {
        super(decorator);
    }

    @Override
    protected String getDefaultTitle() {
        return "endTime";
    }

    @Override
    public Long getValue(RoutePrinterContext context) {
        TourActivity act = context.getActivity();
        if (act instanceof End) {
            return null;
        } else {
            return (long) act.getEndTime();
        }
    }


}
