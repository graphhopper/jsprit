package jsprit.core.algorithm.ruin;

import jsprit.core.algorithm.ruin.distance.JobDistance;
import jsprit.core.problem.VehicleRoutingProblem;

/**
 * Created by schroeder on 05/03/15.
 */
public class JobNeighborhoodsFactory {

    public JobNeighborhoods createNeighborhoods(VehicleRoutingProblem vrp, JobDistance jobDistance){
        return new JobNeighborhoodsImpl(vrp,jobDistance);
    }

    public JobNeighborhoods createNeighborhoods(VehicleRoutingProblem vrp, JobDistance jobDistance, int capacity){
        return new JobNeighborhoodsImplWithCapRestriction(vrp,jobDistance,capacity);
    }

}
