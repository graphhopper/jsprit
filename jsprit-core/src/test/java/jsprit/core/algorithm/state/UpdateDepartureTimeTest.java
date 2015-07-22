package jsprit.core.algorithm.state;

import jsprit.core.problem.Location;
import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TimeWindow;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.util.CostFactory;
import junit.framework.Assert;
import org.junit.Test;

/**
 * Created by schroeder on 22/07/15.
 */
public class UpdateDepartureTimeTest {

    @Test
    public void whenNoTimeWindow_departureTimeShouldBeEarliestStartTime(){
        VehicleRoutingTransportCosts routingCosts = CostFactory.createManhattanCosts();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0,0)).build();
        Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance(10,0)).build();
        VehicleRoute route = VehicleRoute.Builder.newInstance(v).addService(s).build();

        UpdateDepartureTime udt = new UpdateDepartureTime(routingCosts);
        udt.visit(route);

        Assert.assertEquals(0.,route.getDepartureTime());
    }

    @Test
    public void whenNoTimeWindow_departureTimeShouldBeEarliestStartTime2(){
        VehicleRoutingTransportCosts routingCosts = CostFactory.createManhattanCosts();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setEarliestStart(10).setStartLocation(Location.newInstance(0, 0)).build();
        Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance(10,0)).build();
        VehicleRoute route = VehicleRoute.Builder.newInstance(v).addService(s).build();

        UpdateDepartureTime udt = new UpdateDepartureTime(routingCosts);
        udt.visit(route);

        Assert.assertEquals(10.,route.getDepartureTime());
    }

    @Test
    public void whenTimeWindowAndNoWaitingTime_departureTimeShouldBeEarliestStartTime1(){
        VehicleRoutingTransportCosts routingCosts = CostFactory.createManhattanCosts();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setEarliestStart(0).setStartLocation(Location.newInstance(0,0)).build();
        Service s = Service.Builder.newInstance("s").setTimeWindow(TimeWindow.newInstance(5,20)).setLocation(Location.newInstance(10, 0)).build();
        VehicleRoute route = VehicleRoute.Builder.newInstance(v).addService(s).build();

        UpdateDepartureTime udt = new UpdateDepartureTime(routingCosts);
        udt.visit(route);

        Assert.assertEquals(0.,route.getDepartureTime());
    }

    @Test
    public void whenTimeWindowAndNoWaitingTime_departureTimeShouldBeEarliestStartTime2(){
        VehicleRoutingTransportCosts routingCosts = CostFactory.createManhattanCosts();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setEarliestStart(10).setStartLocation(Location.newInstance(0,0)).build();
        Service s = Service.Builder.newInstance("s").setTimeWindow(TimeWindow.newInstance(5,20)).setLocation(Location.newInstance(10, 0)).build();
        VehicleRoute route = VehicleRoute.Builder.newInstance(v).addService(s).build();

        UpdateDepartureTime udt = new UpdateDepartureTime(routingCosts);
        udt.visit(route);

        Assert.assertEquals(10.,route.getDepartureTime());
    }

    @Test
    public void whenTimeWindowAndWaitingTime_departureTimeShouldChange(){
        VehicleRoutingTransportCosts routingCosts = CostFactory.createManhattanCosts();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setEarliestStart(0)
                .setHasVariableDepartureTime(true)
                .setStartLocation(Location.newInstance(0,0)).build();
        Service s = Service.Builder.newInstance("s").setTimeWindow(TimeWindow.newInstance(15,20)).setLocation(Location.newInstance(10, 0)).build();
        VehicleRoute route = VehicleRoute.Builder.newInstance(v).addService(s).build();

        UpdateDepartureTime udt = new UpdateDepartureTime(routingCosts);
        udt.visit(route);

        Assert.assertEquals(5.,route.getDepartureTime());
    }

    @Test
    public void whenTimeWindowAndWaitingTime_departureTimeShouldChange2(){
        VehicleRoutingTransportCosts routingCosts = CostFactory.createManhattanCosts();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setHasVariableDepartureTime(true).setEarliestStart(5).setStartLocation(Location.newInstance(0,0)).build();
        Service s = Service.Builder.newInstance("s").setTimeWindow(TimeWindow.newInstance(18,20)).setLocation(Location.newInstance(10, 0)).build();
        VehicleRoute route = VehicleRoute.Builder.newInstance(v).addService(s).build();

        UpdateDepartureTime udt = new UpdateDepartureTime(routingCosts);
        udt.visit(route);

        Assert.assertEquals(8.,route.getDepartureTime());
    }
}
