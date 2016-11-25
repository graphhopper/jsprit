package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.problem.solution.route.activity.Start;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnDefinition;

public class TravelDurationPrinterColumn extends AbstractDurationPrinterColumn<TravelDurationPrinterColumn>
implements CostAndTimeExtractor {

    private TourActivity prevAct;

    public TravelDurationPrinterColumn() {
        super();
    }

    public TravelDurationPrinterColumn(Consumer<ColumnDefinition.Builder> decorator) {
        super(decorator);
    }


    @Override
    protected String getDefaultTitle() {
        return "travel";
    }

    @Override
    public Long getValue(RoutePrinterContext context) {
        TourActivity act = context.getActivity();
        if (act instanceof Start) {
            prevAct = null;
        }
        long val = (long) (getTransportTime(context, prevAct));
        prevAct = act;
        return val;
    }


}
