package com.graphhopper.jsprit.core.reporting.job;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.problem.job.AbstractJob;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.reporting.AbstractPrinterColumn;
import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnDefinition;
import com.graphhopper.jsprit.core.reporting.columndefinition.StringColumnType;

/**
 * Name (id) of the job.
 *
 * <p>
 * This column provides the {@linkplain Job#getId()} of the associated job of
 * the activity for job activities and null for other route activities.
 * </p>
 *
 * @author balage
 */
public class JobNamePrinterColumn<T extends JobPrinterContext> extends AbstractPrinterColumn<T, String, JobNamePrinterColumn<T>> {

    /**
     * Constructor.
     */
    public JobNamePrinterColumn() {
        super();
    }

    /**
     * Constructor with a post creation decorator provided.
     */
    public JobNamePrinterColumn(Consumer<ColumnDefinition.Builder> decorator) {
        super(decorator);
    }

    @Override
    public ColumnDefinition.Builder getColumnBuilder() {
        return new ColumnDefinition.Builder(new StringColumnType("-"));
    }

    @Override
    protected String getDefaultTitle() {
        return "job name";
    }

    @Override
    public String getData(T context) {
        AbstractJob job = context.getJob();
        return job == null ? null : job.getId();
    }

}
