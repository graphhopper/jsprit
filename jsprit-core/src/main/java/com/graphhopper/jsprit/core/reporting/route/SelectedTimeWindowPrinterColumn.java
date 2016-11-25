package com.graphhopper.jsprit.core.reporting.route;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;

import com.graphhopper.jsprit.core.problem.solution.route.activity.JobActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.ColumnDefinition.Builder;

public class SelectedTimeWindowPrinterColumn extends AbstractTimeWindowPrinterColumn<SelectedTimeWindowPrinterColumn> {

    public SelectedTimeWindowPrinterColumn() {
        super();
    }

    public SelectedTimeWindowPrinterColumn(Consumer<Builder> decorator) {
        super(decorator);
    }

    @Override
    protected String getTitle() {
        return "selTimeWindow";
    }

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
