package jsprit.core.algorithm.ruin;

import jsprit.core.algorithm.ruin.distance.AvgServiceAndShipmentDistance;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.vehicle.VehicleImpl;
import junit.framework.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by schroeder on 06/03/15.
 */
public class RuinClustersTest {

    @Test
    public void itShouldRuinTwoObviousClusters() {
        Service s0 = Service.Builder.newInstance("s0").setLocation(Location.newInstance(9, 0)).build();
        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(9, 1)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(9, 10)).build();
        Service s3 = Service.Builder.newInstance("s3").setLocation(Location.newInstance(9, 9)).build();
        Service s4 = Service.Builder.newInstance("s4").setLocation(Location.newInstance(9, 16)).build();
        Service s5 = Service.Builder.newInstance("s5").setLocation(Location.newInstance(9, 17)).build();
        Service s6 = Service.Builder.newInstance("s6").setLocation(Location.newInstance(9, 15.5)).build();
        Service s7 = Service.Builder.newInstance("s7").setLocation(Location.newInstance(9, 30)).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s1).addJob(s2)
            .addJob(s6).addJob(s7).addJob(s0).addJob(s3).addJob(s4).addJob(s5).addVehicle(v).build();

        VehicleRoute vr1 = VehicleRoute.Builder.newInstance(v).addService(s0).addService(s1).addService(s2).addService(s3).setJobActivityFactory(vrp.getJobActivityFactory()).build();
        VehicleRoute vr2 = VehicleRoute.Builder.newInstance(v)
            .addService(s6).addService(s7).addService(s4).addService(s5).setJobActivityFactory(vrp.getJobActivityFactory()).build();

        JobNeighborhoods n = new JobNeighborhoodsFactory().createNeighborhoods(vrp, new AvgServiceAndShipmentDistance(vrp.getTransportCosts()));
        n.initialise();
        RuinClusters rc = new RuinClusters(vrp, 5, n);
        Collection<Job> ruined = rc.ruinRoutes(Arrays.asList(vr1, vr2));
        Assert.assertEquals(5, ruined.size());

    }
}
