package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.graphhopper.jsprit.core.problem.SizeDimension;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.reporting.AbstractPrinterColumn;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.ColumnDefinition;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.ColumnDefinition.Builder;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.StringColumnType;

public abstract class AbstractSizeDimensionPrinterColumn extends AbstractPrinterColumn<RoutePrinterContext, String> {

    public AbstractSizeDimensionPrinterColumn() {
        super();
    }

    public AbstractSizeDimensionPrinterColumn(Consumer<Builder> decorator) {
        super(decorator);
    }

    @Override
    public ColumnDefinition.Builder getColumnBuilder() {
        return new ColumnDefinition.Builder(new StringColumnType(), getTitle());
    }

    @Override
    public String getData(RoutePrinterContext context) {
        SizeDimension sd = getSizeDimension(context);
        if (sd != null) {
            return IntStream.range(0, sd.getNuOfDimensions()).mapToObj(i -> "" + sd.get(i))
                            .collect(Collectors.joining(", ", "[", "]"));
        } else {
            return null;
        }
    }

    protected abstract String getTitle();

    protected abstract SizeDimension getSizeDimension(RoutePrinterContext context);

    protected SizeDimension calculateInitialLoad(RoutePrinterContext context) {
        SizeDimension sd = SizeDimension.EMPTY;
        for (TourActivity a : context.getRoute().getActivities()) {
            sd = sd.add(a.getLoadChange());
        }
        sd = sd.getNegativeDimensions().abs();
        return sd;
    }

}
