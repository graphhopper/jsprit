package com.graphhopper.jsprit.core.algorithm.recreate.multipickup;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupLocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MultiPickupEdgeCasesTest {

    @Test
    @DisplayName("Should throw exception when adding a null pickup location")
    void shouldThrowExceptionWhenAddingNullPickup() {
        // Arrange
        Shipment.Builder builder = Shipment.Builder.newInstance("s1");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> builder.addPickupLocation((PickupLocation) null),
                "Should throw IllegalArgumentException when adding a null PickupLocation.");
    }

    @Test
    @DisplayName("Should handle mixed setPickupLocation and addPickupLocation calls correctly")
    void shouldHandleMixedPickupLocationMethods() {
        // Arrange
        PickupLocation pickup1 = PickupLocation.newInstance(Location.Builder.newInstance().setId("pickup_1").build());
        PickupLocation pickup2 = PickupLocation.newInstance(Location.Builder.newInstance().setId("pickup_2").build());
        PickupLocation pickup3 = PickupLocation.newInstance(Location.Builder.newInstance().setId("pickup_3").build());

        // Act
        Shipment shipment = Shipment.Builder.newInstance("s1")
                .setPickupLocation(pickup1)      // Collection has 1
                .addPickupLocation(pickup2)      // Collection has 2
                .addPickupLocation(pickup3)      // Collection has 3
                .setDeliveryLocation(Location.Builder.newInstance().setId("delivery").build())
                .build();

        // Assert
        assertEquals(3, shipment.getPickupLocations().size(), "Should have three pickup locations.");
    }

    @Test
    @DisplayName("Should handle many pickup options added in a loop")
    void shouldHandleManyPickupOptionsEfficiently() {
        // Arrange
        Shipment.Builder shipmentBuilder = Shipment.Builder.newInstance("s1");

        // Act
        for (int i = 0; i < 10; i++) {
            shipmentBuilder.addPickupLocation(PickupLocation.newInstance(Location.Builder.newInstance().setId("loc-" + i).build()));
        }
        Shipment shipment = shipmentBuilder.setDeliveryLocation(Location.newInstance("delivery")).build();

        // Assert
        assertEquals(10, shipment.getPickupLocations().size(), "Should have 10 pickup locations.");
    }

    @Test
    @DisplayName("Calling addPickupLocation first should work as expected")
    void addPickupLocationFirstShouldWork() {
        // Arrange
        PickupLocation pickup1 = PickupLocation.newInstance(Location.Builder.newInstance().setId("pickup_1").build());
        PickupLocation pickup2 = PickupLocation.newInstance(Location.Builder.newInstance().setId("pickup_2").build());

        // Act
        Shipment shipment = Shipment.Builder.newInstance("s1")
                .addPickupLocation(pickup1)
                .addPickupLocation(pickup2)
                .setDeliveryLocation(Location.Builder.newInstance().setId("delivery").build())
                .build();

        // Assert
        assertEquals(2, shipment.getPickupLocations().size(), "Should have two pickup locations.");
    }
}
