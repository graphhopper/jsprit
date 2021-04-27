package com.graphhopper.jsprit.core.algorithm.ruin;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

public class RuinFarthestTest extends TestCase {
    Service s1 = Service.Builder.newInstance("s1").addSizeDimension(0,1).setLocation(Location.newInstance(0, 10)).build();
    Service s2 = Service.Builder.newInstance("s2").addSizeDimension(0,1).setLocation(Location.newInstance(0, -10)).build();
    Service s3 = Service.Builder.newInstance("s3").addSizeDimension(0,1).setLocation(Location.newInstance(0, -11)).build();
    Service s4 = Service.Builder.newInstance("s4").addSizeDimension(0,1).setLocation(Location.newInstance(0, 11)).build();

    VehicleType type = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0,2).build();
    VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setType(type).setStartLocation(Location.newInstance(0, 10))
        .setEarliestStart(0).setLatestArrival(8 * 60).build();
    VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setType(type).setStartLocation(Location.newInstance(0, -10))
        .setEarliestStart(0).setLatestArrival(8 * 60).build();
    final VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s1).addJob(s2).addJob(s3).addJob(s4)
        .addVehicle(v1).addVehicle(v2).setFleetSize(VehicleRoutingProblem.FleetSize.FINITE).build();
    @Test
    public void testIdleRouteWillBeRemoved() {
        RuinFarthest ruinFarthest = new RuinFarthest(vrp, 0.8);
        ruinFarthest.setRuinShareFactory(new RuinShareFactory() {
            @Override
            public int createNumberToBeRemoved() {
                return 1;
            }
        });
        VehicleRoute route1 = VehicleRoute.Builder.newInstance(v1)
            .addService(s1)
            .build();
        route1.getStart().setEndTime(0);
        route1.getEnd().setArrTime(50);
        VehicleRoute route2 = VehicleRoute.Builder.newInstance(v2)
            .addService(s2)
            .addService(s3)
            .addService(s4)
            .build();
        route2.getStart().setEndTime(0);
        route2.getEnd().setArrTime(6 * 60);

        ArrayList<VehicleRoute> routes = new ArrayList<>();
        routes.add(route1);
        routes.add(route2);

        Collection<Job> jobs = ruinFarthest.ruinRoutes(routes);
        assertTrue(jobs.size() == 1);
        assertTrue(jobs.contains(s1));
    }

    @Test
    public void testFarthestJobsWillBeRemoved() {
        RuinFarthest ruinFarthest = new RuinFarthest(vrp, 0.8);
        ruinFarthest.setRuinShareFactory(new RuinShareFactory() {
            @Override
            public int createNumberToBeRemoved() {
                return 2;
            }
        });
        VehicleRoute route1 = VehicleRoute.Builder.newInstance(v1)
            .addService(s1)
            .build();
        route1.getStart().setEndTime(0);
        route1.getEnd().setArrTime(5 * 60);
        VehicleRoute route2 = VehicleRoute.Builder.newInstance(v2)
            .addService(s2)
            .addService(s3)
            .addService(s4)
            .build();
        route2.getStart().setEndTime(0);
        route2.getEnd().setArrTime(6 * 60);

        ArrayList<VehicleRoute> routes = new ArrayList<>();
        routes.add(route1);
        routes.add(route2);

        Collection<Job> jobs = ruinFarthest.ruinRoutes(routes);
        assertTrue(jobs.size() == 2);
        assertTrue(jobs.contains(s4));
        assertTrue(jobs.contains(s3));
    }

}
