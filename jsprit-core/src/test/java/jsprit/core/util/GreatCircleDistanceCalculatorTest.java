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

import junit.framework.Assert;
import org.junit.Test;

/**
 * Created by schroeder on 28.11.14.
 */
public class GreatCircleDistanceCalculatorTest {

    @Test
    public void test() {
        double lon1 = 8.3858333;
        double lat1 = 49.0047222;

        double lon2 = 12.1333333;
        double lat2 = 54.0833333;

        double greatCircle = GreatCircleDistanceCalculator.calculateDistance(
            Coordinate.newInstance(lon1, lat1),
            Coordinate.newInstance(lon2, lat2),
            DistanceUnit.Kilometer
        );
        Assert.assertEquals(600, greatCircle, 30.);
    }

    @Test
    public void testMeter() {
        double lon1 = 8.3858333;
        double lat1 = 49.0047222;

        double lon2 = 12.1333333;
        double lat2 = 54.0833333;

        double greatCircle = GreatCircleDistanceCalculator.calculateDistance(
            Coordinate.newInstance(lon1, lat1),
            Coordinate.newInstance(lon2, lat2),
            DistanceUnit.Meter
        );
        Assert.assertEquals(600000, greatCircle, 30000.);
    }

}
