package jsprit.core.algorithm.state;

import jsprit.core.problem.Location;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.route.RouteVisitor;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TimeWindow;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.util.CostFactory;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by schroeder on 22/07/15.
 */
public class UpdateDepartureTimeTest {

    RouteVisitor revVisitors;

    @Before
    public void doBefore(){
        revVisitors = new UpdateDepartureTime(CostFactory.createManhattanCosts());
    }

    @Test
    public void whenNoTimeWindow_departureTimeShouldBeEarliestStartTime(){
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0,0)).build();
        Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance(10,0)).build();
        VehicleRoute route = VehicleRoute.Builder.newInstance(v).addService(s).build();
        revVisitors.visit(route);

        Assert.assertEquals(0.,route.getDepartureTime());
    }

    @Test
    public void whenNoTimeWindow_departureTimeShouldBeEarliestStartTime2(){
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setEarliestStart(10).setStartLocation(Location.newInstance(0, 0)).build();
        Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance(10,0)).build();
        VehicleRoute route = VehicleRoute.Builder.newInstance(v).addService(s).build();
        revVisitors.visit(route);

        Assert.assertEquals(10.,route.getDepartureTime());
    }

    @Test
    public void whenTimeWindowAndNoWaitingTime_departureTimeShouldBeEarliestStartTime1(){
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setEarliestStart(0).setStartLocation(Location.newInstance(0,0)).build();
        Service s = Service.Builder.newInstance("s").setTimeWindow(TimeWindow.newInstance(5,20)).setLocation(Location.newInstance(10, 0)).build();
        VehicleRoute route = VehicleRoute.Builder.newInstance(v).addService(s).build();
        revVisitors.visit(route);

        Assert.assertEquals(0.,route.getDepartureTime());
    }

    @Test
    public void whenTimeWindowAndNoWaitingTime_departureTimeShouldBeEarliestStartTime2(){
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setEarliestStart(10).setStartLocation(Location.newInstance(0,0)).build();
        Service s = Service.Builder.newInstance("s").setTimeWindow(TimeWindow.newInstance(5,20)).setLocation(Location.newInstance(10, 0)).build();
        VehicleRoute route = VehicleRoute.Builder.newInstance(v).addService(s).build();
        revVisitors.visit(route);

        Assert.assertEquals(10.,route.getDepartureTime());
    }

    @Test
    public void whenTimeWindowAndWaitingTime_departureTimeShouldChange(){
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setEarliestStart(0)
                .setHasVariableDepartureTime(true)
                .setStartLocation(Location.newInstance(0,0)).build();
        Service s = Service.Builder.newInstance("s").setTimeWindow(TimeWindow.newInstance(15,20)).setLocation(Location.newInstance(10, 0)).build();
        VehicleRoute route = VehicleRoute.Builder.newInstance(v).addService(s).build();
        revVisitors.visit(route);

        Assert.assertEquals(5.,route.getDepartureTime());
    }

    @Test
    public void whenTimeWindowAndWaitingTime_departureTimeShouldChange2(){
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setHasVariableDepartureTime(true).setEarliestStart(5).setStartLocation(Location.newInstance(0,0)).build();
        Service s = Service.Builder.newInstance("s").setTimeWindow(TimeWindow.newInstance(18,20)).setLocation(Location.newInstance(10, 0)).build();
        VehicleRoute route = VehicleRoute.Builder.newInstance(v).addService(s).build();
        revVisitors.visit(route);

        Assert.assertEquals(8.,route.getDepartureTime());
    }

    @Test
    public void whenTimeWindowAndWaitingTime_departureTimeShouldChange3(){
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setHasVariableDepartureTime(true).setEarliestStart(0).setStartLocation(Location.newInstance(0,0)).build();
        Service s = Service.Builder.newInstance("s")
                .setTimeWindow(TimeWindow.newInstance(15, 20)).setLocation(Location.newInstance(10, 0)).build();
        Service s2 = Service.Builder.newInstance("s2")
                .setTimeWindow(TimeWindow.newInstance(10,40)).setLocation(Location.newInstance(20, 0)).build();
        VehicleRoute route = VehicleRoute.Builder.newInstance(v).addService(s).addService(s2).build();
        revVisitors.visit(route);

        Assert.assertEquals(5.,route.getDepartureTime());
    }

    @Test
    public void whenTimeWindowAndWaitingTime_departureTimeShouldChange4(){
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setHasVariableDepartureTime(true).setEarliestStart(0).setStartLocation(Location.newInstance(0,0)).build();
        Service s = Service.Builder.newInstance("s")
                .setTimeWindow(TimeWindow.newInstance(15, 20)).setLocation(Location.newInstance(10, 0)).build();
        Service s2 = Service.Builder.newInstance("s2")
                .setTimeWindow(TimeWindow.newInstance(29,40)).setLocation(Location.newInstance(20, 0)).build();
        VehicleRoute route = VehicleRoute.Builder.newInstance(v).addService(s).addService(s2).build();
        revVisitors.visit(route);

        Assert.assertEquals(9.,route.getDepartureTime());
    }

    @Test
    public void whenTimeWindowAndWaitingTime_departureTimeShouldChange5(){
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setHasVariableDepartureTime(true).setEarliestStart(0).setStartLocation(Location.newInstance(0,0)).build();
        Service s = Service.Builder.newInstance("s")
                .setTimeWindow(TimeWindow.newInstance(15, 20)).setLocation(Location.newInstance(10, 0)).build();
        Service s2 = Service.Builder.newInstance("s2")
                .setTimeWindow(TimeWindow.newInstance(35,40)).setLocation(Location.newInstance(20, 0)).build();
        VehicleRoute route = VehicleRoute.Builder.newInstance(v).addService(s).addService(s2).build();
        revVisitors.visit(route);

        Assert.assertEquals(10.,route.getDepartureTime());
    }
}
