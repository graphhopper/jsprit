package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.graphhopper.jsprit.core.problem.SizeDimension;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.reporting.AbstractPrinterColumn;
import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnDefinition;
import com.graphhopper.jsprit.core.reporting.columndefinition.HumanReadableTimeFormatter;
import com.graphhopper.jsprit.core.reporting.columndefinition.StringColumnType;

/**
 * Abstract base class for size columns.
 *
 * <p>
 * The representation of a size is the dimension values listed comma separated
 * and wrapped by brackets. (For example: [2, 0, -1])
 * </p>
 *
 * @author balage
 *
 * @See {@linkplain HumanReadableTimeFormatter}
 */
public abstract class AbstractSizeDimensionPrinterColumn
extends AbstractPrinterColumn<RoutePrinterContext, String, AbstractSizeDimensionPrinterColumn> {

    /**
     * Constructor.
     */
    public AbstractSizeDimensionPrinterColumn() {
        super();
    }

    /**
     * Constructor with a post creation decorator provided.
     */
    public AbstractSizeDimensionPrinterColumn(Consumer<ColumnDefinition.Builder> decorator) {
        super(decorator);
    }

    @Override
    public ColumnDefinition.Builder getColumnBuilder() {
        return new ColumnDefinition.Builder(new StringColumnType());
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The result is a string representation of the size (the dimension values
     * listed comma separated and wrapped by brackets) or null.
     * </p>
     */
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

    /**
     * Extracts the size dimension.
     *
     * @param context
     *            The context.
     * @return The size dimension or null.
     */
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
