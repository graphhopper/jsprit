package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.problem.job.AbstractJob;
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
public class JobTypePrinterColumn extends AbstractPrinterColumn<RoutePrinterContext, String, JobTypePrinterColumn> {

    /**
     * Constructor.
     */
    public JobTypePrinterColumn() {
        super();
    }

    /**
     * Constructor with a post creation decorator provided.
     */
    public JobTypePrinterColumn(Consumer<ColumnDefinition.Builder> decorator) {
        super(decorator);
    }

    @Override
    public ColumnDefinition.Builder getColumnBuilder() {
        return new ColumnDefinition.Builder(new StringColumnType("-"));
    }

    @Override
    protected String getDefaultTitle() {
        return "job type";
    }

    @Override
    public String getData(RoutePrinterContext context) {
        AbstractJob job = context.getJob();
        return job == null ? null : job.getClass().getSimpleName();
    }

}
