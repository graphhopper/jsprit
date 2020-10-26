package com.graphhopper.jsprit.core.algorithm.state;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.cost.ForwardTransportTime;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.WaitingTimeCosts;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.*;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.CrowFlyCosts;
import com.graphhopper.jsprit.core.util.Locations;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UpdateActivityTimesTest{
    ForwardTransportTime transportTime;
    VehicleRoutingActivityCosts activityCosts;
    VehicleRoute route;
    List<TourActivity> tourActivities;
    Location startLocation;
    Location endLocation;
    Start start;
    End end;
    Map<String, Coordinate> coordinates;
    Locations locations;
    TourActivityFactory tourActivityFactory;
    UpdateActivityTimes stateUpdater;

    @Before
    public void setUp() throws Exception {
        coordinates = new HashMap<>();
        locations = new Locations() {
            @Override
            public Coordinate getCoord(String id) {
                return coordinates.get(id);
            }
        };
        Coordinate startCoordinate = Coordinate.newInstance(0, 0);
        Coordinate endCoordinate = Coordinate.newInstance(10, 0);
        coordinates.put("start", startCoordinate);
        coordinates.put("end", endCoordinate);
        startLocation = Location.Builder.newInstance().setId("start").setCoordinate(startCoordinate).build();
        endLocation = Location.Builder.newInstance().setId("end").setCoordinate(endCoordinate).build();
        start = Start.newInstance("start", 0, 0);
        end = End.newInstance("end", 0, 30);
        start.setLocation(startLocation);
        end.setLocation(endLocation);
        transportTime = new CrowFlyCosts(locations);
        activityCosts = new WaitingTimeCosts();
        stateUpdater = new UpdateActivityTimes(transportTime, activityCosts);
        tourActivityFactory = new DefaultTourActivityFactory();
        tourActivities = new ArrayList<>();
        route = mock(VehicleRoute.class);
        when(route.getStart()).thenReturn(start);
        when(route.getEnd()).thenReturn(end);
        when(route.getActivities()).thenReturn(tourActivities);
    }

    @Test
    public void shouldNotAdjustActivityTimes_WhenActivitiesHappenAtDifferentLocations() {
        Coordinate coordinateOne = Coordinate.newInstance(2, 0);
        Coordinate coordinateTwo = Coordinate.newInstance(4, 0);
        Coordinate coordinateThree = Coordinate.newInstance(6, 0);
        Coordinate coordinateFour = Coordinate.newInstance(8, 0);
        Location locationOne = Location.Builder.newInstance().setId("one").setCoordinate(coordinateOne).build();
        Location locationTwo = Location.Builder.newInstance().setId("two").setCoordinate(coordinateTwo).build();
        Location locationThree = Location.Builder.newInstance().setId("three").setCoordinate(coordinateThree).build();
        Location locationFour = Location.Builder.newInstance().setId("four").setCoordinate(coordinateFour).build();
        coordinates.put("one", coordinateOne);
        coordinates.put("two", coordinateTwo);
        coordinates.put("three", coordinateThree);
        coordinates.put("four", coordinateFour);
        TourActivity activityOne = tourActivityFactory.createActivity(Service.Builder.newInstance("one").setLocation(locationOne).setServiceTime(1D).build());
        TourActivity activityTwo = tourActivityFactory.createActivity(Service.Builder.newInstance("two").setLocation(locationTwo).setServiceTime(1D).build());
        TourActivity activityThree = tourActivityFactory.createActivity(Service.Builder.newInstance("three").setLocation(locationThree).setServiceTime(1D).build());
        TourActivity activityFour = tourActivityFactory.createActivity(Service.Builder.newInstance("four").setLocation(locationFour).setServiceTime(1D).build());
        tourActivities.addAll(Arrays.asList(activityOne, activityTwo, activityThree, activityFour));
        stateUpdater.begin(route);
        stateUpdater.visit(activityOne);
        stateUpdater.visit(activityTwo);
        stateUpdater.visit(activityThree);
        stateUpdater.visit(activityFour);
        stateUpdater.finish();

        assertEquals(activityOne.getArrTime(), 2D, 0.001);
        assertEquals(activityOne.getEndTime(), 3D, 0.001);
        assertEquals(activityTwo.getArrTime(), 5D, 0.001);
        assertEquals(activityTwo.getEndTime(), 6D, 0.001);
        assertEquals(activityThree.getArrTime(), 8D, 0.001);
        assertEquals(activityThree.getEndTime(), 9D, 0.001);
        assertEquals(activityFour.getArrTime(), 11D, 0.001);
        assertEquals(activityFour.getEndTime(), 12D, 0.001);
        assertEquals(end.getArrTime(), 14D, 0.001);
        assertEquals(end.getEndTime(), 30D, 0.001);
    }

    @Test
    public void shouldNotAdjustActivityTimes_WhenActivitiesHappenAtDifferentTimes() {
        Coordinate coordinateOne = Coordinate.newInstance(2, 0);
        Coordinate coordinateTwo = Coordinate.newInstance(2, 0);
        Coordinate coordinateThree = Coordinate.newInstance(2, 0);
        Coordinate coordinateFour = Coordinate.newInstance(2, 0);
        Location locationOne = Location.Builder.newInstance().setId("one").setCoordinate(coordinateOne).build();
        Location locationTwo = Location.Builder.newInstance().setId("two").setCoordinate(coordinateTwo).build();
        Location locationThree = Location.Builder.newInstance().setId("three").setCoordinate(coordinateThree).build();
        Location locationFour = Location.Builder.newInstance().setId("four").setCoordinate(coordinateFour).build();
        coordinates.put("one", coordinateOne);
        coordinates.put("two", coordinateTwo);
        coordinates.put("three", coordinateThree);
        coordinates.put("four", coordinateFour);
        TourActivity activityOne = tourActivityFactory.createActivity(Service.Builder.newInstance("one").setLocation(locationOne).setServiceTime(1D).build());
        TourActivity activityTwo = tourActivityFactory.createActivity(Service.Builder.newInstance("two").setLocation(locationTwo).setServiceTime(1D).build());
        TourActivity activityThree = tourActivityFactory.createActivity(Service.Builder.newInstance("three").setLocation(locationThree).setServiceTime(1D).build());
        TourActivity activityFour = tourActivityFactory.createActivity(Service.Builder.newInstance("four").setLocation(locationFour).setServiceTime(1D).build());
        activityOne.setTheoreticalEarliestOperationStartTime(0);
        activityTwo.setTheoreticalEarliestOperationStartTime(6);
        activityThree.setTheoreticalEarliestOperationStartTime(10);
        activityFour.setTheoreticalEarliestOperationStartTime(15);
        tourActivities.addAll(Arrays.asList(activityOne, activityTwo, activityThree, activityFour));
        stateUpdater.begin(route);
        stateUpdater.visit(activityOne);
        stateUpdater.visit(activityTwo);
        stateUpdater.visit(activityThree);
        stateUpdater.visit(activityFour);
        stateUpdater.finish();

        assertEquals(activityOne.getArrTime(), 2D, 0.001);
        assertEquals(activityOne.getEndTime(), 3D, 0.001);
        assertEquals(activityTwo.getArrTime(), 3D, 0.001);
        assertEquals(activityTwo.getEndTime(), 7D, 0.001);
        assertEquals(activityThree.getArrTime(), 7D, 0.001);
        assertEquals(activityThree.getEndTime(), 11D, 0.001);
        assertEquals(activityFour.getArrTime(), 11D, 0.001);
        assertEquals(activityFour.getEndTime(), 16D, 0.001);
        assertEquals(end.getArrTime(), 24D, 0.001);
        assertEquals(end.getEndTime(), 30D, 0.001);
    }

    @Test
    public void shouldAdjustActivityTimes_WhenAllActivitiesHappenAtSameLocationAndTime() {
        Coordinate coordinateOne = Coordinate.newInstance(2, 0);
        Coordinate coordinateTwo = Coordinate.newInstance(2, 0);
        Coordinate coordinateThree = Coordinate.newInstance(2, 0);
        Coordinate coordinateFour = Coordinate.newInstance(2, 0);
        Location locationOne = Location.Builder.newInstance().setId("one").setCoordinate(coordinateOne).build();
        Location locationTwo = Location.Builder.newInstance().setId("two").setCoordinate(coordinateTwo).build();
        Location locationThree = Location.Builder.newInstance().setId("three").setCoordinate(coordinateThree).build();
        Location locationFour = Location.Builder.newInstance().setId("four").setCoordinate(coordinateFour).build();
        coordinates.put("one", coordinateOne);
        coordinates.put("two", coordinateTwo);
        coordinates.put("three", coordinateThree);
        coordinates.put("four", coordinateFour);
        TourActivity activityOne = tourActivityFactory.createActivity(Service.Builder.newInstance("one").setLocation(locationOne).setServiceTime(1D).build());
        TourActivity activityTwo = tourActivityFactory.createActivity(Service.Builder.newInstance("two").setLocation(locationTwo).setServiceTime(1D).build());
        TourActivity activityThree = tourActivityFactory.createActivity(Service.Builder.newInstance("three").setLocation(locationThree).setServiceTime(1D).build());
        TourActivity activityFour = tourActivityFactory.createActivity(Service.Builder.newInstance("four").setLocation(locationFour).setServiceTime(1D).build());
        activityOne.setTheoreticalEarliestOperationStartTime(0);
        activityTwo.setTheoreticalEarliestOperationStartTime(0);
        activityThree.setTheoreticalEarliestOperationStartTime(0);
        activityFour.setTheoreticalEarliestOperationStartTime(0);
        tourActivities.addAll(Arrays.asList(activityOne, activityTwo, activityThree, activityFour));
        stateUpdater.begin(route);
        stateUpdater.visit(activityOne);
        stateUpdater.visit(activityTwo);
        stateUpdater.visit(activityThree);
        stateUpdater.visit(activityFour);
        stateUpdater.finish();

        assertEquals(activityOne.getArrTime(), 2D, 0.001);
        assertEquals(activityOne.getEndTime(), 3D, 0.001);
        assertEquals(activityTwo.getArrTime(), 2D, 0.001);
        assertEquals(activityTwo.getEndTime(), 3D, 0.001);
        assertEquals(activityThree.getArrTime(), 2D, 0.001);
        assertEquals(activityThree.getEndTime(), 3D, 0.001);
        assertEquals(activityFour.getArrTime(), 2D, 0.001);
        assertEquals(activityFour.getEndTime(), 3D, 0.001);
        assertEquals(end.getArrTime(), 11D, 0.001);
        assertEquals(end.getEndTime(), 30D, 0.001);
    }

    @Test
    public void shouldAdjustActivityTimes_WhenSomeActivitiesHappenAtSameLocationAndTime() {
        Coordinate coordinateOne = Coordinate.newInstance(2, 0);
        Coordinate coordinateTwo = Coordinate.newInstance(2, 0);
        Coordinate coordinateThree = Coordinate.newInstance(4, 0);
        Coordinate coordinateFour = Coordinate.newInstance(4, 0);
        Location locationOne = Location.Builder.newInstance().setId("one").setCoordinate(coordinateOne).build();
        Location locationTwo = Location.Builder.newInstance().setId("two").setCoordinate(coordinateTwo).build();
        Location locationThree = Location.Builder.newInstance().setId("three").setCoordinate(coordinateThree).build();
        Location locationFour = Location.Builder.newInstance().setId("four").setCoordinate(coordinateFour).build();
        coordinates.put("one", coordinateOne);
        coordinates.put("two", coordinateTwo);
        coordinates.put("three", coordinateThree);
        coordinates.put("four", coordinateFour);
        TourActivity activityOne = tourActivityFactory.createActivity(Service.Builder.newInstance("one").setLocation(locationOne).setServiceTime(1D).build());
        TourActivity activityTwo = tourActivityFactory.createActivity(Service.Builder.newInstance("two").setLocation(locationTwo).setServiceTime(1D).build());
        TourActivity activityThree = tourActivityFactory.createActivity(Service.Builder.newInstance("three").setLocation(locationThree).setServiceTime(1D).build());
        TourActivity activityFour = tourActivityFactory.createActivity(Service.Builder.newInstance("four").setLocation(locationFour).setServiceTime(1D).build());
        activityOne.setTheoreticalEarliestOperationStartTime(0);
        activityTwo.setTheoreticalEarliestOperationStartTime(0);
        activityThree.setTheoreticalEarliestOperationStartTime(0);
        activityFour.setTheoreticalEarliestOperationStartTime(0);
        tourActivities.addAll(Arrays.asList(activityOne, activityTwo, activityThree, activityFour));
        stateUpdater.begin(route);
        stateUpdater.visit(activityOne);
        stateUpdater.visit(activityTwo);
        stateUpdater.visit(activityThree);
        stateUpdater.visit(activityFour);
        stateUpdater.finish();

        assertEquals(activityOne.getArrTime(), 2D, 0.001);
        assertEquals(activityOne.getEndTime(), 3D, 0.001);
        assertEquals(activityTwo.getArrTime(), 2D, 0.001);
        assertEquals(activityTwo.getEndTime(), 3D, 0.001);
        assertEquals(activityThree.getArrTime(), 5D, 0.001);
        assertEquals(activityThree.getEndTime(), 6D, 0.001);
        assertEquals(activityFour.getArrTime(), 5D, 0.001);
        assertEquals(activityFour.getEndTime(), 6D, 0.001);
        assertEquals(end.getArrTime(), 12D, 0.001);
        assertEquals(end.getEndTime(), 30D, 0.001);
    }

    @Test
    public void shouldAdjustActivityTimes_WhenAllActivitiesHappenAtSameLocationAndTimeAndDifferentServiceTimes() {
        Coordinate coordinateOne = Coordinate.newInstance(2, 0);
        Coordinate coordinateTwo = Coordinate.newInstance(2, 0);
        Coordinate coordinateThree = Coordinate.newInstance(2, 0);
        Coordinate coordinateFour = Coordinate.newInstance(2, 0);
        Location locationOne = Location.Builder.newInstance().setId("one").setCoordinate(coordinateOne).build();
        Location locationTwo = Location.Builder.newInstance().setId("two").setCoordinate(coordinateTwo).build();
        Location locationThree = Location.Builder.newInstance().setId("three").setCoordinate(coordinateThree).build();
        Location locationFour = Location.Builder.newInstance().setId("four").setCoordinate(coordinateFour).build();
        coordinates.put("one", coordinateOne);
        coordinates.put("two", coordinateTwo);
        coordinates.put("three", coordinateThree);
        coordinates.put("four", coordinateFour);
        TourActivity activityOne = tourActivityFactory.createActivity(Service.Builder.newInstance("one").setLocation(locationOne).setServiceTime(1D).build());
        TourActivity activityTwo = tourActivityFactory.createActivity(Service.Builder.newInstance("two").setLocation(locationTwo).setServiceTime(4D).build());
        TourActivity activityThree = tourActivityFactory.createActivity(Service.Builder.newInstance("three").setLocation(locationThree).setServiceTime(2D).build());
        TourActivity activityFour = tourActivityFactory.createActivity(Service.Builder.newInstance("four").setLocation(locationFour).setServiceTime(3D).build());
        activityOne.setTheoreticalEarliestOperationStartTime(0);
        activityTwo.setTheoreticalEarliestOperationStartTime(0);
        activityThree.setTheoreticalEarliestOperationStartTime(0);
        activityFour.setTheoreticalEarliestOperationStartTime(0);
        tourActivities.addAll(Arrays.asList(activityOne, activityTwo, activityThree, activityFour));
        stateUpdater.begin(route);
        stateUpdater.visit(activityOne);
        stateUpdater.visit(activityTwo);
        stateUpdater.visit(activityThree);
        stateUpdater.visit(activityFour);
        stateUpdater.finish();

        assertEquals(activityOne.getArrTime(), 2D, 0.001);
        assertEquals(activityOne.getEndTime(), 6D, 0.001);
        assertEquals(activityTwo.getArrTime(), 2D, 0.001);
        assertEquals(activityTwo.getEndTime(), 6D, 0.001);
        assertEquals(activityThree.getArrTime(), 2D, 0.001);
        assertEquals(activityThree.getEndTime(), 6D, 0.001);
        assertEquals(activityFour.getArrTime(), 2D, 0.001);
        assertEquals(activityFour.getEndTime(), 6D, 0.001);
        assertEquals(end.getArrTime(), 14D, 0.001);
        assertEquals(end.getEndTime(), 30D, 0.001);
    }

    @Test
    public void shouldAdjustActivityTimes_WhenAllActivitiesHappenAtSameLocationAndTimeAndOneDifferentServiceTime() {
        Coordinate coordinateOne = Coordinate.newInstance(2, 0);
        Coordinate coordinateTwo = Coordinate.newInstance(2, 0);
        Coordinate coordinateThree = Coordinate.newInstance(2, 0);
        Coordinate coordinateFour = Coordinate.newInstance(2, 0);
        Location locationOne = Location.Builder.newInstance().setId("one").setCoordinate(coordinateOne).build();
        Location locationTwo = Location.Builder.newInstance().setId("two").setCoordinate(coordinateTwo).build();
        Location locationThree = Location.Builder.newInstance().setId("three").setCoordinate(coordinateThree).build();
        Location locationFour = Location.Builder.newInstance().setId("four").setCoordinate(coordinateFour).build();
        coordinates.put("one", coordinateOne);
        coordinates.put("two", coordinateTwo);
        coordinates.put("three", coordinateThree);
        coordinates.put("four", coordinateFour);
        TourActivity activityOne = tourActivityFactory.createActivity(Service.Builder.newInstance("one").setLocation(locationOne).setServiceTime(1D).build());
        TourActivity activityTwo = tourActivityFactory.createActivity(Service.Builder.newInstance("two").setLocation(locationTwo).setServiceTime(4D).build());
        TourActivity activityThree = tourActivityFactory.createActivity(Service.Builder.newInstance("three").setLocation(locationThree).setServiceTime(1D).build());
        TourActivity activityFour = tourActivityFactory.createActivity(Service.Builder.newInstance("four").setLocation(locationFour).setServiceTime(1D).build());
        activityOne.setTheoreticalEarliestOperationStartTime(0);
        activityTwo.setTheoreticalEarliestOperationStartTime(0);
        activityThree.setTheoreticalEarliestOperationStartTime(0);
        activityFour.setTheoreticalEarliestOperationStartTime(0);
        tourActivities.addAll(Arrays.asList(activityOne, activityTwo, activityThree, activityFour));
        stateUpdater.begin(route);
        stateUpdater.visit(activityOne);
        stateUpdater.visit(activityTwo);
        stateUpdater.visit(activityThree);
        stateUpdater.visit(activityFour);
        stateUpdater.finish();

        assertEquals(activityOne.getArrTime(), 2D, 0.001);
        assertEquals(activityOne.getEndTime(), 6D, 0.001);
        assertEquals(activityTwo.getArrTime(), 2D, 0.001);
        assertEquals(activityTwo.getEndTime(), 6D, 0.001);
        assertEquals(activityThree.getArrTime(), 2D, 0.001);
        assertEquals(activityThree.getEndTime(), 6D, 0.001);
        assertEquals(activityFour.getArrTime(), 2D, 0.001);
        assertEquals(activityFour.getEndTime(), 6D, 0.001);
        assertEquals(end.getArrTime(), 14D, 0.001);
        assertEquals(end.getEndTime(), 30D, 0.001);
    }
}
