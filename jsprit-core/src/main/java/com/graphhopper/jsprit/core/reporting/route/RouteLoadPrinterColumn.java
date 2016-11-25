package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.problem.SizeDimension;
import com.graphhopper.jsprit.core.problem.solution.route.activity.Start;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.ColumnDefinition.Builder;

public class RouteLoadPrinterColumn extends AbstractSizeDimensionPrinterColumn {

    private SizeDimension aggregated;

    public RouteLoadPrinterColumn() {
        super();
    }

    public RouteLoadPrinterColumn(Consumer<Builder> decorator) {
        super(decorator);
    }

    @Override
    protected String getDefaultTitle() {
        return "load";
    }

    @Override
    protected SizeDimension getSizeDimension(RoutePrinterContext context) {
        TourActivity act = context.getActivity();
        if (act instanceof Start) {
            aggregated = calculateInitialLoad(context);
        } else {
            aggregated = aggregated.add(act.getLoadChange());
        }
        return aggregated;
    }


}
