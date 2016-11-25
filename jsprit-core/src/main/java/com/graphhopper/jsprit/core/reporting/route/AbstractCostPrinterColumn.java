package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.reporting.AbstractPrinterColumn;
import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnAlignment;
import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnDefinition;
import com.graphhopper.jsprit.core.reporting.columndefinition.IntColumnType;

public abstract class AbstractCostPrinterColumn
extends AbstractPrinterColumn<RoutePrinterContext, Integer, AbstractCostPrinterColumn>
implements CostAndTimeExtractor {

    public AbstractCostPrinterColumn() {
        super();
    }

    public AbstractCostPrinterColumn(Consumer<ColumnDefinition.Builder> decorator) {
        super(decorator);
    }

    @Override
    public ColumnDefinition.Builder getColumnBuilder() {
        return new ColumnDefinition.Builder(new IntColumnType()).withAlignment(ColumnAlignment.RIGHT);
    }

}
