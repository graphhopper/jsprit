/*******************************************************************************
 * Copyright (c) 2014 Stefan Schroeder.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package jsprit.core.util;

import jsprit.core.problem.cost.VehicleRoutingTransportCosts;

public class CostFactory {

    /**
     * Return manhattanCosts.
     * <p/>
     * This retrieves coordinates from locationIds. LocationId has to be locId="{x},{y}". For example,
     * locId="10,10" is interpreted such that x=10 and y=10.
     *
     * @return manhattanCosts
     */
    public static VehicleRoutingTransportCosts createManhattanCosts() {
        Locations locations = new Locations() {

            @Override
            public Coordinate getCoord(String id) {
                //assume: locationId="x,y"
                String[] splitted = id.split(",");
                return Coordinate.newInstance(Double.parseDouble(splitted[0]),
                    Double.parseDouble(splitted[1]));
            }

        };
        return new ManhattanCosts(locations);
    }

    /**
     * Return euclideanCosts.
     * <p/>
     * This retrieves coordinates from locationIds. LocationId has to be locId="{x},{y}". For example,
     * locId="10,10" is interpreted such that x=10 and y=10.
     *
     * @return euclidean
     */
    public static VehicleRoutingTransportCosts createEuclideanCosts() {
        Locations locations = new Locations() {

            @Override
            public Coordinate getCoord(String id) {
                //assume: locationId="x,y"
                String[] splitted = id.split(",");
                return Coordinate.newInstance(Double.parseDouble(splitted[0]),
                    Double.parseDouble(splitted[1]));
            }

        };
        return new CrowFlyCosts(locations);
    }
}
