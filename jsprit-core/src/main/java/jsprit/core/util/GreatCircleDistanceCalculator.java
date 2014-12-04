/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package jsprit.core.util;

/**
 * Created by schroeder on 28.11.14.
 */
public class GreatCircleDistanceCalculator {

    private static final double R = 6372.8; // km

    /**
     * Harversine method.
     *
     * double lon1 = coord1.getX();
     * double lon2 = coord2.getX();
     * double lat1 = coord1.getY();
     * double lat2 = coord2.getY();
     *
     * @param coord1 - from coord
     * @param coord2 - to coord
     * @return great circle distance
     */
    public  static double calculateDistance(Coordinate coord1, Coordinate coord2, DistanceUnit distanceUnit){
        double lon1 = coord1.getX();
        double lon2 = coord2.getX();
        double lat1 = coord1.getY();
        double lat2 = coord2.getY();

        double delta_Lat = Math.toRadians(lat2 - lat1);
        double delta_Lon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.sin(delta_Lat / 2) * Math.sin(delta_Lat / 2) + Math.sin(delta_Lon / 2) * Math.sin(delta_Lon / 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double distance = R * c;
        if(distanceUnit.equals(DistanceUnit.Meter)){
            distance = distance * 1000.;
        }
        return distance;
    }

}
