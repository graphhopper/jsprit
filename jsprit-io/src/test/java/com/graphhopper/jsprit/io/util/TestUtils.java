package com.graphhopper.jsprit.io.util;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.util.Coordinate;

/**
 * Created by schroeder on 19/12/14.
 */
public class TestUtils {

    public static Location loc(String id, Coordinate coordinate) {
        return Location.Builder.newInstance().setId(id).setCoordinate(coordinate).build();
    }

    public static Location loc(String id) {
        return Location.Builder.newInstance().setId(id).build();
    }

    public static Location loc(Coordinate coordinate) {
        return Location.Builder.newInstance().setCoordinate(coordinate).build();
    }
}
