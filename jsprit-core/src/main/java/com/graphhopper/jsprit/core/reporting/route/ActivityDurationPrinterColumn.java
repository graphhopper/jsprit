package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.problem.solution.route.activity.Start;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.ColumnDefinition.Builder;

public class ActivityDurationPrinterColumn extends AbstractDurationPrinterColumn<ActivityDurationPrinterColumn>
implements CostAndTimeExtractor {

    private TourActivity prevAct;

    public ActivityDurationPrinterColumn() {
        super();
    }

    public ActivityDurationPrinterColumn(Consumer<Builder> decorator) {
        super(decorator);
    }


    @Override
    protected String getTitle() {
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
