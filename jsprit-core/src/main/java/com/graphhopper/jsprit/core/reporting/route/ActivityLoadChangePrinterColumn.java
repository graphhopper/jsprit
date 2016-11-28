package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.problem.SizeDimension;
import com.graphhopper.jsprit.core.problem.solution.route.activity.Start;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnDefinition;

/**
 * The load change value (signed size) of the activity.
 * 
 * <p>
 * If the activity is a route start, the returned value is the initial load,
 * otherwise the loadChange value of the activity.
 * </p>
 *
 * @author balage
 *
 */
public class ActivityLoadChangePrinterColumn extends AbstractSizeDimensionPrinterColumn {

    /**
     * Constructor.
     */
    public ActivityLoadChangePrinterColumn() {
        super();
    }

    /**
     * Constructor with a post creation decorator provided.
     */
    public ActivityLoadChangePrinterColumn(Consumer<ColumnDefinition.Builder> decorator) {
        super(decorator);
    }

    @Override
    public ColumnDefinition.Builder getColumnBuilder() {
        return super.getColumnBuilder().withMinWidth(10);
    }

    @Override
    protected String getDefaultTitle() {
        return "load change";
    }

    /**
     * {@inheritDoc}
     * <p>
     * If the activity is a route start, the returned value is the initial load,
     * otherwise the loadChange value of the activity.
     * </p>
     */
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
