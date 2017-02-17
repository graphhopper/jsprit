package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.problem.solution.route.activity.AbstractActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnDefinition;

/**
 * Duration of the activity.
 * <p>
 * The time it takes to complete the on-site task of the activity. This is the
 * value from {@linkplain AbstractActivity#getOperationTime()}.
 * </p>
 *
 * @author balage
 *
 * @see {@linkplain ArrivalTimePrinterColumn}
 * @see {@linkplain StartTimePrinterColumn}
 * @see {@linkplain EndTimePrinterColumn}
 * @see {@linkplain TravelDurationPrinterColumn}
 * @see {@linkplain WaitingDurationPrinterColumn}
 * @see {@linkplain ActivityDurationPrinterColumn}
 */
public class OperationDurationPrinterColumn extends AbstractDurationPrinterColumn<OperationDurationPrinterColumn> {

    /**
     * Constructor.
     */
    public OperationDurationPrinterColumn() {
        super();
    }

    /**
     * Constructor with a post creation decorator provided.
     */
    public OperationDurationPrinterColumn(Consumer<ColumnDefinition.Builder> decorator) {
        super(decorator);
    }


    @Override
    protected String getDefaultTitle() {
        return "opTime";
    }

    @Override
    public Long getValue(RoutePrinterContext context) {
        TourActivity act = context.getActivity();
        return (long) act.getOperationTime();
    }

}
