package com.graphhopper.jsprit.core.distance;

import com.graphhopper.jsprit.core.util.Coordinate;

public interface DistanceCalculator {

    public double calculateDistance(Coordinate from, Coordinate to);

    public String getName();
}
