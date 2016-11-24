package com.graphhopper.jsprit.core.reporting.route;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;

import com.graphhopper.jsprit.core.reporting.AbstractPrinterColumn;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.ColumnDefinition;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.ColumnDefinition.Builder;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.StringColumnType;

public abstract class AbstractHumanReadableTimePrinterColumn extends AbstractPrinterColumn<RoutePrinterContext, String> {

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private LocalDateTime origin = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
    private ChronoUnit unit = ChronoUnit.SECONDS;

    public AbstractHumanReadableTimePrinterColumn() {
        super();
    }

    public AbstractHumanReadableTimePrinterColumn withDateFormat(String pattern) {
        dateFormatter = DateTimeFormatter.ofPattern(pattern);
        return this;
    }

    public AbstractHumanReadableTimePrinterColumn withOrigin(LocalDateTime origin) {
        this.origin = origin;
        return this;
    }

    public AbstractHumanReadableTimePrinterColumn withUnit(ChronoUnit unit) {
        this.unit = unit;
        return this;
    }

    public AbstractHumanReadableTimePrinterColumn(Consumer<Builder> decorator) {
        super(decorator);
    }

    @Override
    public ColumnDefinition.Builder getColumnBuilder() {
        return new ColumnDefinition.Builder(new StringColumnType("-"), getTitle());
    }

    @Override
    public String getData(RoutePrinterContext context) {
        Long v = getTimeValue(context);
        if (v == null) {
            return null;
        } else {
            LocalDateTime dt = origin.plus(v, unit);
            return dateFormatter.format(dt);
        }
    }

    protected abstract Long getTimeValue(RoutePrinterContext context);

    protected abstract String getTitle();

}
