package com.graphhopper.jsprit.core.distance;

import com.graphhopper.jsprit.core.util.Coordinate;

public class EuclideanDistanceCalculator implements DistanceCalculator {

    private static EuclideanDistanceCalculator INSTANCE = new EuclideanDistanceCalculator();

    public static EuclideanDistanceCalculator getInstance() {
        return INSTANCE;
    }

    private EuclideanDistanceCalculator() {
    }

    @Override
    public double calculateDistance(Coordinate from, Coordinate to) {
        double xDiff = from.getX() - to.getX();
        double yDiff = from.getY() - to.getY();
        return Math.sqrt((xDiff * xDiff) + (yDiff * yDiff));
    }

    @Override
    public String getName() {
        return "Euclidean";
    }

}
