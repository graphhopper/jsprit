package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.reporting.AbstractPrinterColumn;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.Alignment;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.ColumnDefinition;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.ColumnDefinition.Builder;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.IntColumnType;

public abstract class AbstractCostPrinterColumn
extends AbstractPrinterColumn<RoutePrinterContext, Integer, AbstractCostPrinterColumn>
implements CostAndTimeExtractor {

    public AbstractCostPrinterColumn() {
        super();
    }

    public AbstractCostPrinterColumn(Consumer<Builder> decorator) {
        super(decorator);
    }

    @Override
    public ColumnDefinition.Builder getColumnBuilder() {
        return new ColumnDefinition.Builder(new IntColumnType()).withAlignment(Alignment.RIGHT);
    }

}
