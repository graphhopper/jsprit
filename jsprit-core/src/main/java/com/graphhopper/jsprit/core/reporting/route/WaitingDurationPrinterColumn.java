package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.problem.solution.route.activity.End;
import com.graphhopper.jsprit.core.problem.solution.route.activity.Start;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnDefinition;

/**
 * Idle duration before starting the activity.
 * <p>
 * This is the time duration between the vehicle arrives to the location (
 * {@linkplain ArrivalTimePrinterColumn}) and the activity could be started (
 * {@linkplain StartTimePrinterColumn}). For route start and end this value is
 * not defined (null).
 * </p>
 *
 * @author balage
 *
 * @see {@linkplain ArrivalTimePrinterColumn}
 * @see {@linkplain StartTimePrinterColumn}
 * @see {@linkplain EndTimePrinterColumn}
 * @see {@linkplain TravelDurationPrinterColumn}
 * @see {@linkplain OperationDurationPrinterColumn}
 * @see {@linkplain ActivityDurationPrinterColumn}
 */
public class WaitingDurationPrinterColumn extends AbstractDurationPrinterColumn<WaitingDurationPrinterColumn> {

    /**
     * Constructor.
     */
    public WaitingDurationPrinterColumn() {
        super();
    }

    /**
     * Constructor with a post creation decorator provided.
     */
    public WaitingDurationPrinterColumn(Consumer<ColumnDefinition.Builder> decorator) {
        super(decorator);
    }


    @Override
    protected String getDefaultTitle() {
        return "waiting";
    }

    @Override
    public Long getValue(RoutePrinterContext context) {
        TourActivity act = context.getActivity();
        if (act instanceof Start || act instanceof End) {
            return null;
        } else {
            return (long) (act.getEndTime() - act.getOperationTime() - act.getArrTime());
        }
    }


}
