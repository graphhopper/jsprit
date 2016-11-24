package com.graphhopper.jsprit.core.reporting.route;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.problem.solution.route.activity.Start;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.ColumnDefinition.Builder;

public class TransportCostPrinterColumn extends AbstractCostPrinterColumn {

    private TourActivity prevAct;

    public TransportCostPrinterColumn() {
        super();
    }

    public TransportCostPrinterColumn(Consumer<Builder> decorator) {
        super(decorator);
    }

    @Override
    protected String getTitle() {
        return "transCost";
    }


    @Override
    public Integer getData(RoutePrinterContext context) {
        TourActivity act = context.getActivity();
        if (act instanceof Start) {
            prevAct = null;
        }
        double res = getTransportCost(context, prevAct);
        prevAct = act;
        return (int) res;
    }

}
