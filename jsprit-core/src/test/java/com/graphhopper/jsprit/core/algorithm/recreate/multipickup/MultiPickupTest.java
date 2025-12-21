package com.graphhopper.jsprit.core.algorithm.recreate.multipickup;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupLocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MultiPickupTest {

    @Test
    @DisplayName("Should create a shipment with a single pickup location using setPickupLocation")
    void shouldCreateShipmentWithSinglePickup() {
        // Arrange
        Location pickupLoc = Location.Builder.newInstance().setId("pickup-loc").build();
        PickupLocation pickup = PickupLocation.newInstance(pickupLoc);

        // Act
        Shipment shipment = Shipment.Builder.newInstance("s1")
                .setPickupLocation(pickup)
                .setDeliveryLocation(Location.Builder.newInstance().setId("delivery-loc").build())
                .build();

        // Assert
        assertNotNull(shipment.getPickupLocations(), "Pickup locations should not be null.");
        assertEquals(1, shipment.getPickupLocations().size(), "Should have one pickup location.");
        assertTrue(shipment.getPickupLocations().contains(pickup), "The correct pickup location should be present.");
    }

    @Test
    @DisplayName("Should create a shipment with multiple pickup locations using addPickupLocation")
    void shouldCreateShipmentWithMultiplePickups() {
        // Arrange
        PickupLocation pickup1 = PickupLocation.newInstance(Location.Builder.newInstance().setId("pickup-1").build());
        PickupLocation pickup2 = PickupLocation.newInstance(Location.Builder.newInstance().setId("pickup-2").build());

        // Act
        Shipment shipment = Shipment.Builder.newInstance("s1")
                .addPickupLocation(pickup1)
                .addPickupLocation(pickup2)
                .setDeliveryLocation(Location.Builder.newInstance().setId("delivery-loc").build())
                .build();

        // Assert
        assertNotNull(shipment.getPickupLocations());
        assertEquals(2, shipment.getPickupLocations().size(), "Should have two pickup locations.");
        assertTrue(shipment.getPickupLocations().contains(pickup1), "Pickup 1 should be present.");
        assertTrue(shipment.getPickupLocations().contains(pickup2), "Pickup 2 should be present.");
    }

    @Test
    @DisplayName("setPickupLocation should overwrite previously added locations")
    void setShouldOverwriteAddedLocations() {
        // Arrange
        PickupLocation initialPickup = PickupLocation.newInstance(Location.Builder.newInstance().setId("initial").build());
        PickupLocation overwritingPickup = PickupLocation.newInstance(Location.Builder.newInstance().setId("overwrite").build());

        // Act
        Shipment shipment = Shipment.Builder.newInstance("s1")
                .addPickupLocation(initialPickup)
                .setPickupLocation(overwritingPickup) // This should replace the initial one
                .setDeliveryLocation(Location.Builder.newInstance().setId("delivery-loc").build())
                .build();

        // Assert
        assertNotNull(shipment.getPickupLocations());
        assertEquals(1, shipment.getPickupLocations().size(), "Should only have one pickup location after overwrite.");
        assertTrue(shipment.getPickupLocations().contains(overwritingPickup), "The overwriting pickup should be present.");
        assertFalse(shipment.getPickupLocations().contains(initialPickup), "The initial pickup should have been removed.");
    }

    @Test
    @DisplayName("Should throw exception if build is called with no pickup locations")
    void shouldThrowExceptionWhenNoPickupIsSet() {
        // Arrange
        Shipment.Builder builder = Shipment.Builder.newInstance("s1")
                .setDeliveryLocation(Location.newInstance("delivery"));

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, builder::build,
                "Should throw IllegalArgumentException when no pickup location is provided.");
        assertTrue(exception.getMessage().contains("At least one pickup location must be provided"));
    }
}
