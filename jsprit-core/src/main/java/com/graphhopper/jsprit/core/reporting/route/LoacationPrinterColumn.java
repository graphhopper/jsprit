package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.reporting.AbstractPrinterColumn;
import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnDefinition;
import com.graphhopper.jsprit.core.reporting.columndefinition.StringColumnType;

/**
 * Priority of the job.
 *
 * <p>
 * This column provides the simple class name of the associated job of the
 * activity for job activities and null for other route activities.
 * </p>
 *
 * @author balage
 */
public class LoacationPrinterColumn extends AbstractPrinterColumn<RoutePrinterContext, String, LoacationPrinterColumn> {

    /**
     * Constructor.
     */
    public LoacationPrinterColumn() {
        super();
    }

    /**
     * Constructor with a post creation decorator provided.
     */
    public LoacationPrinterColumn(Consumer<ColumnDefinition.Builder> decorator) {
        super(decorator);
    }

    @Override
    public ColumnDefinition.Builder getColumnBuilder() {
        return new ColumnDefinition.Builder(new StringColumnType("-"));
    }

    @Override
    protected String getDefaultTitle() {
        return "location";
    }

    @Override
    public String getData(RoutePrinterContext context) {
        TourActivity act = context.getActivity();
        Location loc = act.getLocation();
        return loc == null ? null : loc.getId();
    }

}
