package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.problem.solution.route.activity.End;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnDefinition;

/**
 * Start time of the activity.
 * <p>
 * For route end the value is undefined (null), for other activities, it is the
 * time when the task on location is effectively started.
 * </p>
 *
 * @author balage
 *
 * @see {@linkplain ArrivalTimePrinterColumn}
 * @see {@linkplain EndTimePrinterColumn}
 * @see {@linkplain TravelDurationPrinterColumn}
 * @see {@linkplain WaitingDurationPrinterColumn}
 * @see {@linkplain OperationDurationPrinterColumn}
 * @see {@linkplain ActivityDurationPrinterColumn}
 */
public class StartTimePrinterColumn extends AbstractTimePrinterColumn<StartTimePrinterColumn> {

    /**
     * Constructor.
     */
    public StartTimePrinterColumn() {
        super();
    }

    /**
     * Constructor with a post creation decorator provided.
     */
    public StartTimePrinterColumn(Consumer<ColumnDefinition.Builder> decorator) {
        super(decorator);
    }

    @Override
    protected String getDefaultTitle() {
        return "startTime";
    }

    @Override
    public Long getValue(RoutePrinterContext context) {
        TourActivity act = context.getActivity();
        if (act instanceof End) {
            return null;
        } else {
            return (long) (act.getEndTime() - act.getOperationTime());
        }
    }


}
