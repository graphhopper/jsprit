package com.graphhopper.jsprit.core.distance;

import com.graphhopper.jsprit.core.util.Coordinate;

public class SphericalDistanceCalculator implements DistanceCalculator {

    private static final double R = 6372.8; // km

    private static SphericalDistanceCalculator INSTANCE = new SphericalDistanceCalculator();

    public static SphericalDistanceCalculator getInstance() {
        return INSTANCE;
    }

    @Override
    public double calculateDistance(Coordinate from, Coordinate to) {
        double lon1 = from.getX();
        double lon2 = to.getX();
        double lat1 = from.getY();
        double lat2 = to.getY();

        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double deltaLatSin = Math.sin(deltaLat / 2);
        double deltaLonSin = Math.sin(deltaLon / 2);
        double a = deltaLatSin * deltaLatSin
                        + deltaLonSin * deltaLonSin * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double distance = R * c;
        return distance;
    }

    @Override
    public String getName() {
        return "Spherical";
    }

}
