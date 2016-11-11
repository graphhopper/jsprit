package com.graphhopper.jsprit.core.distance;

import com.graphhopper.jsprit.core.util.Coordinate;

public class ManhattanDistanceCalculator implements DistanceCalculator {

    private static ManhattanDistanceCalculator INSTANCE = new ManhattanDistanceCalculator();

    public static ManhattanDistanceCalculator getInstance() {
        return INSTANCE;
    }

    @Override
    public double calculateDistance(Coordinate from, Coordinate to) {
        double xDiff = from.getX() - to.getX();
        double yDiff = from.getY() - to.getY();

        return Math.abs(xDiff) + Math.abs(yDiff);
    }

    @Override
    public String getName() {
        return "Manhattan";
    }


}
