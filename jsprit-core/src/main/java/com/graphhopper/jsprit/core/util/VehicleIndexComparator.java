package com.graphhopper.jsprit.core.util;

import java.util.Comparator;

import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;

public class VehicleIndexComparator implements Comparator<VehicleRoute> {
        public int compare(VehicleRoute a, VehicleRoute b) {
            return  a.getVehicle().getIndex() - b.getVehicle().getIndex();
        }
}
