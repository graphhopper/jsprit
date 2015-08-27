package jsprit.core.algorithm.ruin;

import jsprit.core.problem.Location;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.util.EuclideanCosts;
import junit.framework.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Created by schroeder on 06/03/15.
 */
public class DBSCANClustererTest {

    @Test
    public void itShouldReturnOneClusterOfSizeTwo() {
        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(1, 1)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(10, 10)).build();
        Service s3 = Service.Builder.newInstance("s3").setLocation(Location.newInstance(9, 9)).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoute r = VehicleRoute.Builder.newInstance(v).addService(s1).addService(s2).addService(s3).build();

        DBSCANClusterer c = new DBSCANClusterer(new EuclideanCosts());
        c.setEpsDistance(3);
        List<Job> cluster = c.getRandomCluster(r);
        Assert.assertEquals(2, cluster.size());

    }

    @Test
    public void itShouldReturnOneCluster() {
        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(1, 1)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(10, 10)).build();
        Service s3 = Service.Builder.newInstance("s3").setLocation(Location.newInstance(9, 9)).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoute r = VehicleRoute.Builder.newInstance(v).addService(s1).addService(s2).addService(s3).build();

        DBSCANClusterer c = new DBSCANClusterer(new EuclideanCosts());
        c.setEpsDistance(3);
        List<List<Job>> cluster = c.getClusters(r);
        Assert.assertEquals(1, cluster.size());

    }

    @Test
    public void itShouldReturnTwoClusters() {
        Service s0 = Service.Builder.newInstance("s0").setLocation(Location.newInstance(9, 0)).build();
        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(9, 1)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(9, 10)).build();
        Service s3 = Service.Builder.newInstance("s3").setLocation(Location.newInstance(9, 9)).build();
        Service s4 = Service.Builder.newInstance("s4").setLocation(Location.newInstance(9, 16)).build();
        Service s5 = Service.Builder.newInstance("s5").setLocation(Location.newInstance(9, 17)).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoute r = VehicleRoute.Builder.newInstance(v).addService(s1).addService(s2).addService(s3)
            .addService(s0).addService(s4).addService(s5).build();

        DBSCANClusterer c = new DBSCANClusterer(new EuclideanCosts());
        c.setMinPts(1);
        c.setEpsDistance(2);
        List<List<Job>> cluster = c.getClusters(r);
        for (List<Job> l : cluster) {
            System.out.println("--");
            for (Job j : l) System.out.println(j);
        }
        Assert.assertEquals(3, cluster.size());

    }
}
