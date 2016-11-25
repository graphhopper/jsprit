package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.activity.JobActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.reporting.AbstractPrinterColumn;
import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnAlignment;
import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnDefinition;
import com.graphhopper.jsprit.core.reporting.columndefinition.StringColumnType;

public class JobPriorityPrinterColumn extends AbstractPrinterColumn<RoutePrinterContext, String, JobPriorityPrinterColumn> {

    private static final String[] PRIORITY_NAMES = new String[] { "", "HIGH", "MEDIUM", "LOW" };

    public JobPriorityPrinterColumn() {
        super();
    }

    public JobPriorityPrinterColumn(Consumer<ColumnDefinition.Builder> decorator) {
        super(decorator);
    }

    @Override
    public ColumnDefinition.Builder getColumnBuilder() {
        return new ColumnDefinition.Builder(new StringColumnType("-")).withAlignment(ColumnAlignment.CENTER);
    }

    @Override
    protected String getDefaultTitle() {
        return "priority";
    }

    @Override
    public String getData(RoutePrinterContext context) {
        TourActivity act = context.getActivity();
        if (act instanceof JobActivity) {
            Job job = ((JobActivity) context.getActivity()).getJob();
            return PRIORITY_NAMES[job.getPriority()];
        } else {
            return null;
        }
    }

}
