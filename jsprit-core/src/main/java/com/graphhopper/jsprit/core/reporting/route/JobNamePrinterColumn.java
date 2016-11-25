package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.activity.JobActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.reporting.AbstractPrinterColumn;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.ColumnDefinition;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.ColumnDefinition.Builder;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.StringColumnType;

public class JobNamePrinterColumn extends AbstractPrinterColumn<RoutePrinterContext, String, JobNamePrinterColumn> {

    public JobNamePrinterColumn() {
        super();
    }

    public JobNamePrinterColumn(Consumer<Builder> decorator) {
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
    public String getData(RoutePrinterContext context) {
        TourActivity act = context.getActivity();
        if (act instanceof JobActivity) {
            Job job = ((JobActivity) context.getActivity()).getJob();
            return job.getId();
        } else {
            return null;
        }
    }

}
