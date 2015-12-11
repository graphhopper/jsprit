package jsprit.core.algorithm.recreate;

import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.job.Shipment;

/**
 * Created by schroeder on 15/10/15.
 */
public class DefaultScorer implements ScoringFunction  {

    private VehicleRoutingProblem vrp;

    private double tw_param = -0.5;

    private double depotDistance_param = +0.1;

    private double minTimeWindowScore = -100000;

    public DefaultScorer(VehicleRoutingProblem vrp) {
        this.vrp = vrp;
    }

    public void setTimeWindowParam(double tw_param) {
        this.tw_param = tw_param;
    }

    public void setDepotDistanceParam(double depotDistance_param) {
        this.depotDistance_param = depotDistance_param;
    }

    @Override
    public double score(InsertionData best, Job job) {
        double score;
        if (job instanceof Service) {
            score = scoreService(best, job);
        } else if (job instanceof Shipment) {
            score = scoreShipment(best, job);
        } else throw new IllegalStateException("not supported");
        return score;
    }

    private double scoreShipment(InsertionData best, Job job) {
        Shipment shipment = (Shipment) job;
        double maxDepotDistance_1 = Math.max(
            getDistance(best.getSelectedVehicle().getStartLocation(), shipment.getPickupLocation()),
            getDistance(best.getSelectedVehicle().getStartLocation(), shipment.getDeliveryLocation())
        );
        double maxDepotDistance_2 = Math.max(
            getDistance(best.getSelectedVehicle().getEndLocation(), shipment.getPickupLocation()),
            getDistance(best.getSelectedVehicle().getEndLocation(), shipment.getDeliveryLocation())
        );
        double maxDepotDistance = Math.max(maxDepotDistance_1, maxDepotDistance_2);
        double minTimeToOperate = Math.min(shipment.getPickupTimeWindow().getEnd() - shipment.getPickupTimeWindow().getStart(),
            shipment.getDeliveryTimeWindow().getEnd() - shipment.getDeliveryTimeWindow().getStart());
        return Math.max(tw_param * minTimeToOperate, minTimeWindowScore) + depotDistance_param * maxDepotDistance;
    }

    private double scoreService(InsertionData best, Job job) {
        Location location = ((Service) job).getLocation();
        double maxDepotDistance = 0;
        if (location != null) {
            maxDepotDistance = Math.max(
                getDistance(best.getSelectedVehicle().getStartLocation(), location),
                getDistance(best.getSelectedVehicle().getEndLocation(), location)
            );
        }
        return Math.max(tw_param * (((Service) job).getTimeWindow().getEnd() - ((Service) job).getTimeWindow().getStart()), minTimeWindowScore) +
            depotDistance_param * maxDepotDistance;
    }


    private double getDistance(Location loc1, Location loc2) {
        return vrp.getTransportCosts().getTransportCost(loc1, loc2, 0., null, null);
    }

    @Override
    public String toString() {
        return "[name=defaultScorer][twParam=" + tw_param + "][depotDistanceParam=" + depotDistance_param + "]";
    }
}
