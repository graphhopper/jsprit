package jsprit.core.algorithm.recreate;

import jsprit.core.problem.solution.route.VehicleRoute;

/**
 * Created by schroeder on 15/10/15.
 */
class VersionedInsertionData {

    private InsertionData iData;

    private VehicleRoute route;

    private int version;

    public VersionedInsertionData(InsertionData iData, int version, VehicleRoute route) {
        this.iData = iData;
        this.version = version;
        this.route = route;
    }

    public InsertionData getiData() {
        return iData;
    }

    public int getVersion() {
        return version;
    }

    public VehicleRoute getRoute() {
        return route;
    }
}
