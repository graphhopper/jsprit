package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.problem.SizeDimension;
import com.graphhopper.jsprit.core.problem.solution.route.activity.Start;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnDefinition;

/**
 * The load of the vehicle after the current activity is finished.
 *
 * <p>
 * This column represents the current load of the vehicle on the route after the
 * cargo load/unload performed on the activity. For the start activity (at the
 * start of the route) the value is the initialLoad.
 * </p>
 * <p>
 * This column is stateful and stores the vehicle load from the prior activity
 * on the route.
 * </p>
 *
 * @author balage
 */
public class RouteLoadPrinterColumn extends AbstractSizeDimensionPrinterColumn {

    // The current vehicle load
    private SizeDimension aggregated;

    /**
     * Constructor.
     */
    public RouteLoadPrinterColumn() {
        super();
    }

    /**
     * Constructor with a post creation decorator provided.
     */
    public RouteLoadPrinterColumn(Consumer<ColumnDefinition.Builder> decorator) {
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
