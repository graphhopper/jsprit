package com.graphhopper.jsprit.core.algorithm.state;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.cost.ForwardTransportTime;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.*;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.CrowFlyCosts;
import com.graphhopper.jsprit.core.util.Locations;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;


class ConsolidateSameStopTimeActivityCost implements VehicleRoutingActivityCosts {
    @Override
    public double getActivityCost(TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle, TourActivity prevAct) {
        if (isSameLocation(tourAct, prevAct)) {
            return 0.0;
        }
        if (vehicle != null) {
            double waiting = vehicle.getType().getVehicleCostParams().perWaitingTimeUnit * Math.max(0., tourAct.getTheoreticalEarliestOperationStartTime() - arrivalTime);
            double servicing = vehicle.getType().getVehicleCostParams().perServiceTimeUnit * getActivityDuration(tourAct, arrivalTime, driver, vehicle, prevAct);
            return waiting + servicing;
        }
        return 0;
    }

    @Override
    public double getActivityDuration(TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle, TourActivity prevAct) {
        if (isSameLocation(tourAct, prevAct)) {
            return 0.0;
        }
        return tourAct.getOperationTime();
    }

    private boolean isSameLocation(TourActivity tourAct, TourActivity prevAct) {
        if (tourAct != null && tourAct.getLocation() != null && prevAct != null && prevAct.getLocation() != null) {
            if (tourAct.getLocation().equals(prevAct.getLocation())) {
                return true;
            }
            return false;
        }
        return false;
    }

}


public class UpdateActivityTimesTest {
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
        end = End.newInstance("end", 0, 12);
        start.setLocation(startLocation);
        end.setLocation(endLocation);
        transportTime = new CrowFlyCosts(locations);
        activityCosts = new ConsolidateSameStopTimeActivityCost();
        stateUpdater = new UpdateActivityTimes(transportTime, activityCosts);
        tourActivityFactory = new DefaultTourActivityFactory();
        tourActivities = new ArrayList<>();
        route = mock(VehicleRoute.class);
        when(route.getStart()).thenReturn(start);
        when(route.getEnd()).thenReturn(end);
        when(route.getActivities()).thenReturn(tourActivities);
    }

    @Test
    public void shouldAdjustActivityTimes_WhenAllActivitiesHappenAtSameLocationAndTimeAndDifferentServiceTimes() {
        Coordinate coordinateOne = Coordinate.newInstance(2, 0);
        Coordinate coordinateTwo = Coordinate.newInstance(3, 0);
        Coordinate coordinateThree = Coordinate.newInstance(4, 0);
        Location locationOne = Location.Builder.newInstance().setId("one").setCoordinate(coordinateOne).build();
        Location locationTwo = Location.Builder.newInstance().setId("two").setCoordinate(coordinateTwo).build();
        Location locationThree = Location.Builder.newInstance().setId("three").setCoordinate(coordinateThree).build();
        coordinates.put("one", coordinateOne);
        coordinates.put("two", coordinateTwo);
        coordinates.put("three", coordinateThree);
        TourActivity activityOne = tourActivityFactory.createActivity(Service.Builder.newInstance("lone").setLocation(locationOne).setServiceTime(1D).build());
        TourActivity activityTwo = tourActivityFactory.createActivity(Service.Builder.newInstance("ltwo").setLocation(locationOne).setServiceTime(1D).build());
        TourActivity activityThree = tourActivityFactory.createActivity(Service.Builder.newInstance("lthree").setLocation(locationOne).setServiceTime(1D).build());
        TourActivity activityFour = tourActivityFactory.createActivity(Service.Builder.newInstance("lfour").setLocation(locationOne).setServiceTime(1D).build());
        TourActivity activityFive = tourActivityFactory.createActivity(Service.Builder.newInstance("lfive").setLocation(locationOne).setServiceTime(1D).build());
        TourActivity activitySix = tourActivityFactory.createActivity(Service.Builder.newInstance("oone").setLocation(locationTwo).setServiceTime(1D).build());
        TourActivity activitySeven = tourActivityFactory.createActivity(Service.Builder.newInstance("otwo").setLocation(locationTwo).setServiceTime(1D).build());
        TourActivity activityEight = tourActivityFactory.createActivity(Service.Builder.newInstance("othree").setLocation(locationTwo).setServiceTime(1D).build());
        TourActivity activityNine = tourActivityFactory.createActivity(Service.Builder.newInstance("ofour").setLocation(locationThree).setServiceTime(1D).build());
        TourActivity activityTen = tourActivityFactory.createActivity(Service.Builder.newInstance("ofive").setLocation(locationThree).setServiceTime(1D).build());
        tourActivities.addAll(Arrays.asList(
            activityOne,
            activityTwo,
            activityThree,
            activityFour,
            activityFive,
            activitySix,
            activitySeven,
            activityEight,
            activityNine,
            activityTen
        ));
        stateUpdater.begin(route);
        stateUpdater.visit(activityOne);
        stateUpdater.visit(activityTwo);
        stateUpdater.visit(activityThree);
        stateUpdater.visit(activityFour);
        stateUpdater.visit(activityFive);
        stateUpdater.visit(activitySix);
        stateUpdater.visit(activitySeven);
        stateUpdater.visit(activityEight);
        stateUpdater.visit(activityNine);
        stateUpdater.visit(activityTen);
        stateUpdater.finish();

        assertEquals(activityOne.getArrTime(), 2D, 0.001);
        assertEquals(activityOne.getEndTime(), 3D, 0.001);
        assertEquals(activityTwo.getArrTime(), 3D, 0.001);
        assertEquals(activityTwo.getEndTime(), 3D, 0.001);
        assertEquals(activityThree.getArrTime(), 3D, 0.001);
        assertEquals(activityThree.getEndTime(), 3D, 0.001);
        assertEquals(activityFour.getArrTime(), 3D, 0.001);
        assertEquals(activityFour.getEndTime(), 3D, 0.001);
        assertEquals(activityFive.getArrTime(), 3D, 0.001);
        assertEquals(activitySix.getArrTime(), 4D, 0.001);
        assertEquals(activitySix.getEndTime(), 5D, 0.001);
        assertEquals(activitySeven.getArrTime(), 5D, 0.001);
        assertEquals(activityEight.getArrTime(), 5D, 0.001);
        assertEquals(activityNine.getArrTime(), 6D, 0.001);
        assertEquals(activityNine.getEndTime(), 7D, 0.001);
        assertEquals(activityTen.getArrTime(), 7D, 0.001);
        assertEquals(end.getArrTime(), 13D, 0.001);
        assertEquals(end.getEndTime(), 12D, 0.001);
        // This needs to be constrained by an added VehicleTimeConstraint by default.
        // assertTrue("Arrival Time must be before End Time", end.getArrTime() < end.getEndTime());
    }
}


