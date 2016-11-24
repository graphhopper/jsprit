package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.reporting.AbstractPrinterColumn;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.Alignment;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.ColumnDefinition;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.ColumnDefinition.Builder;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.IntColumnType;

public abstract class AbstractCostPrinterColumn extends AbstractPrinterColumn<RoutePrinterContext, Integer> {

    public AbstractCostPrinterColumn() {
        super();
    }

    public AbstractCostPrinterColumn(Consumer<Builder> decorator) {
        super(decorator);
    }

    @Override
    public ColumnDefinition.Builder getColumnBuilder() {
        return new ColumnDefinition.Builder(new IntColumnType(), getTitle()).withAlignment(Alignment.RIGHT);
    }

    protected abstract String getTitle();

    protected double getActivityCost(RoutePrinterContext context) {
        return context.getProblem().getActivityCosts().getActivityCost(context.getActivity(),
                        context.getActivity().getArrTime(), context.getRoute().getDriver(), context.getRoute().getVehicle());
    }

    protected double getTransportCost(RoutePrinterContext context, TourActivity prevAct) {
        return prevAct == null ? 0d
                        : context.getProblem().getTransportCosts().getTransportCost(prevAct.getLocation(),
                                        context.getActivity().getLocation(),
                                        context.getActivity().getArrTime(), context.getRoute().getDriver(), context.getRoute().getVehicle());
    }

}
