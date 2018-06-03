package com.graphhopper.jsprit.core.problem.job;


import com.graphhopper.jsprit.core.problem.Location;

import java.util.ArrayList;
import java.util.List;

public class ShipmentWithMutablePickupDeliverOptions extends Shipment {
    List<Location> pickupPossibleLocations = new ArrayList<>();
    List<Location> deliverPossibleLocations = new ArrayList<>();

    public ShipmentWithMutablePickupDeliverOptions(Builder builder, List<Location> pickupPossibleLocations, List<Location> deliverPossibleLocations) {
        super(builder);
        this.pickupPossibleLocations.addAll(pickupPossibleLocations);
        this.deliverPossibleLocations.addAll(deliverPossibleLocations);
    }

    public List<Location> getDeliverPossibleLocations() {
        return deliverPossibleLocations;
    }

    public List<Location> getPickupPossibleLocations() {
        return pickupPossibleLocations;
    }
}
