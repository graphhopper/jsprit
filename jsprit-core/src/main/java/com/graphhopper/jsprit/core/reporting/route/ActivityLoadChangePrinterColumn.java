package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.problem.SizeDimension;
import com.graphhopper.jsprit.core.problem.solution.route.activity.Start;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.ColumnDefinition.Builder;

public class ActivityLoadChangePrinterColumn extends AbstractSizeDimensionPrinterColumn {

    public ActivityLoadChangePrinterColumn() {
        super();
    }

    public ActivityLoadChangePrinterColumn(Consumer<Builder> decorator) {
        super(decorator);
    }

    @Override
    public Builder getColumnBuilder() {
        return super.getColumnBuilder().withMinWidth(10);
    }

    @Override
    protected String getTitle() {
        return "load change";
    }

    @Override
    protected SizeDimension getSizeDimension(RoutePrinterContext context) {
        TourActivity act = context.getActivity();
        if (act instanceof Start) {
            return calculateInitialLoad(context);
        } else {
            return act.getLoadChange();
        }
    }


}
