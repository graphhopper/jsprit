package com.graphhopper.jsprit.core.reporting.route;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;

import com.graphhopper.jsprit.core.problem.solution.route.activity.JobActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnDefinition;

/**
 * The time window used in activity.
 *
 * <p>
 * This is the time window which was choosen by the algorithm. The start time of
 * the activity is within this time window and the end time is within or matches
 * the end value of this time window.
 * </p>
 *
 * @author balage
 *
 * @see {@linkplain TimeWindowsPrinterColumn}
 * @see {@linkplain StartTimePrinterColumn}
 */
public class SelectedTimeWindowPrinterColumn extends AbstractTimeWindowPrinterColumn<SelectedTimeWindowPrinterColumn> {

    /**
     * Constructor.
     */
    public SelectedTimeWindowPrinterColumn() {
        super();
    }

    /**
     * Constructor with a post creation decorator provided.
     */
    public SelectedTimeWindowPrinterColumn(Consumer<ColumnDefinition.Builder> decorator) {
        super(decorator);
    }

    @Override
    protected String getDefaultTitle() {
        return "selTimeWindow";
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * This implementation returns at most one time window: the one the activity
     * start time is within.
     * </p>
     */
    @Override
    protected Collection<TimeWindow> getValue(RoutePrinterContext context) {
        TourActivity act = context.getActivity();
        if (act instanceof JobActivity) {
            Optional<TimeWindow> optTw = ((JobActivity) act).getTimeWindows().stream()
                            .filter(tw -> tw.contains(act.getEndTime() - act.getOperationTime()))
                            .findAny();
            if (optTw.isPresent()) {
                return Collections.singleton(optTw.get());
            } else {
                return null;
            }
        } else {
            return null;
        }
    }


}
