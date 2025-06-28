package com.graphhopper.jsprit.core.problem.solution.route.activity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class PickupLocationsImpl implements PickupLocations {
    private Collection<PickupLocation> pickupLocations = new ArrayList<PickupLocation>();

    public void add(PickupLocation pickupLocation){
        pickupLocations.add(pickupLocation);
    }

    public void clear(){
        pickupLocations.clear();
    }

    @Override
    public Collection<PickupLocation> getPickupLocations() {
        return Collections.unmodifiableCollection(pickupLocations);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(pickupLocations.size() * 60);
        for (PickupLocation pl : pickupLocations) {
            sb.append("[location=").append(pl).append("]");
        }
        return sb.toString();
    }

}
