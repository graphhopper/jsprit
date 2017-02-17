package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.problem.solution.route.activity.Start;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnDefinition;

/**
 * Arrival time of the activity.
 * <p>
 * For route start the value is undefined (null), for other activities, it is
 * the earliest time the location of the activity is reached. (Note, that it is
 * not the time the activity is started, there may be an idle time before.)
 * </p>
 *
 * @author balage
 *
 * @see {@linkplain StartTimePrinterColumn}
 * @see {@linkplain EndTimePrinterColumn}
 * @see {@linkplain TravelDurationPrinterColumn}
 * @see {@linkplain WaitingDurationPrinterColumn}
 * @see {@linkplain OperationDurationPrinterColumn}
 * @see {@linkplain ActivityDurationPrinterColumn}
 */
public class ArrivalTimePrinterColumn extends AbstractTimePrinterColumn<ArrivalTimePrinterColumn> {

    /**
     * Constructor.
     */
    public ArrivalTimePrinterColumn() {
        super();
    }

    /**
     * Constructor with a post creation decorator provided.
     */
    public ArrivalTimePrinterColumn(Consumer<ColumnDefinition.Builder> decorator) {
        super(decorator);
    }


    @Override
    protected String getDefaultTitle() {
        return "arrTime";
    }

    @Override
    public Long getValue(RoutePrinterContext context) {
        TourActivity act = context.getActivity();
        if (act instanceof Start) {
            return null;
        } else {
            return (long) act.getArrTime();
        }
    }


}
