package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import org.junit.Test;

import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class AbstractInsertionStrategyTest {
    Random random = new Random();

    @Test
    public void updateNewRouteInsertionDataNoInsertion() {
        InsertionData iData = new InsertionData.NoInsertionFound();
        AbstractInsertionStrategy.updateNewRouteInsertionData(iData);
        assertEquals(iData.getInsertionCost(), Double.MAX_VALUE, .001);
    }

    @Test
    public void updateNewRouteInsertionDataNewInsertionNoVehicle() {
        double insertionCost = random.nextDouble();
        Vehicle vehicle = new VehicleImpl.NoVehicle();
        InsertionData iData = new InsertionData(insertionCost, -1, -1, vehicle, null);
        AbstractInsertionStrategy.updateNewRouteInsertionData(iData);
        assertEquals(iData.getInsertionCost(), insertionCost, .001);
    }

    @Test
    public void updateNewRouteInsertionDataNewInsertionVehicleWithFixedCost() {
        double insertionCost = random.nextDouble();
        double fixedCost = random.nextDouble();
        Vehicle vehicle = VehicleImpl.Builder.newInstance(UUID.randomUUID().toString())
            .setType(VehicleTypeImpl.Builder.newInstance(UUID.randomUUID().toString()).setFixedCost(fixedCost).build())
            .setStartLocation(Location.newInstance(random.nextDouble(), random.nextDouble()))
            .build();

        InsertionData iData = new InsertionData(insertionCost, -1, -1, vehicle, null);
        AbstractInsertionStrategy.updateNewRouteInsertionData(iData);
        assertEquals(iData.getInsertionCost(), insertionCost + fixedCost, .001);
    }

    @Test
    public void updateNewRouteInsertionDataNewInsertionVehicleWithoutType() {
        double insertionCost = random.nextDouble();
        Vehicle vehicle = VehicleImpl.Builder.newInstance(UUID.randomUUID().toString())
            .setStartLocation(Location.newInstance(random.nextDouble(), random.nextDouble()))
            .build();

        InsertionData iData = new InsertionData(insertionCost, -1, -1, vehicle, null);
        AbstractInsertionStrategy.updateNewRouteInsertionData(iData);
        assertEquals(iData.getInsertionCost(), insertionCost, .001);
    }
}
