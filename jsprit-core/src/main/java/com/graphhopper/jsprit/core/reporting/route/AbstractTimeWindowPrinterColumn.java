package com.graphhopper.jsprit.core.reporting.route;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.reporting.AbstractPrinterColumn;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.ColumnDefinition;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.ColumnDefinition.Builder;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.StringColumnType;

public abstract class AbstractTimeWindowPrinterColumn<T extends AbstractTimeWindowPrinterColumn<T>>
extends AbstractPrinterColumn<RoutePrinterContext, String> {

    private HumanReadableTimeFormatter formatter;
    private boolean humanReadable = false;

    public AbstractTimeWindowPrinterColumn() {
        this(null);
    }

    public AbstractTimeWindowPrinterColumn(Consumer<Builder> decorator) {
        super(decorator);
        formatter = new HumanReadableTimeFormatter();
    }

    @SuppressWarnings("unchecked")
    public T withFormatter(HumanReadableTimeFormatter formatter) {
        this.formatter = formatter;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T asHumanReadable() {
        this.humanReadable = true;
        return (T) this;
    }


    @Override
    public ColumnDefinition.Builder getColumnBuilder() {
        return new ColumnDefinition.Builder(new StringColumnType("-"), getTitle() + (humanReadable ? " (H)" : ""));
    }

    @Override
    public String getData(RoutePrinterContext context) {
        Collection<TimeWindow> timeWindows = getValue(context);
        if (timeWindows == null || timeWindows.isEmpty()) {
            return null;
        }
        return timeWindows.stream().map(tw -> formatTimeWindow(tw)).collect(Collectors.joining());
    }

    protected String formatTimeWindow(TimeWindow tw) {
        String res = "";
        if (humanReadable) {
            res = "[" + formatter.format((long) tw.getStart()) + "-";
            if (tw.getEnd() == Double.MAX_VALUE) {
                res += "";
            } else {
                res += formatter.format((long) tw.getEnd());
            }
            res += "]";

        } else {
            res = "[" + (long) tw.getStart() + "-";
            if (tw.getEnd() == Double.MAX_VALUE) {
                res += "";
            } else {
                res += (long) tw.getEnd();
            }
            res += "]";
        }
        return res;
    }

    protected abstract Collection<TimeWindow> getValue(RoutePrinterContext context);

    protected abstract String getTitle();

}
