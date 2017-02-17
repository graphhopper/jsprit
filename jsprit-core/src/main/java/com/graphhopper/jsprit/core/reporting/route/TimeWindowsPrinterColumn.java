package com.graphhopper.jsprit.core.reporting.route;

import java.util.Collection;
import java.util.function.Consumer;

import com.graphhopper.jsprit.core.problem.solution.route.activity.JobActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnDefinition;

/**
 * The time windows of the activity.
 *
 * <p>
 * Returns all time windows assigned to the activity.
 * </p>
 *
 * @author balage
 *
 * @see {@linkplain SelectedTimeWindowPrinterColumn}
 */
public class TimeWindowsPrinterColumn extends AbstractTimeWindowPrinterColumn<TimeWindowsPrinterColumn> {

    /**
     * Constructor.
     */
    public TimeWindowsPrinterColumn() {
        super();
    }

    /**
     * Constructor with a post creation decorator provided.
     */
    public TimeWindowsPrinterColumn(Consumer<ColumnDefinition.Builder> decorator) {
        super(decorator);
    }

    @Override
    protected String getDefaultTitle() {
        return "timeWindows";
    }

    @Override
    protected Collection<TimeWindow> getValue(RoutePrinterContext context) {
        TourActivity act = context.getActivity();
        if (act instanceof JobActivity) {
            return ((JobActivity) act).getTimeWindows();
        } else {
            return null;
        }
    }


}
