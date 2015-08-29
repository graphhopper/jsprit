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

package jsprit.core.problem;

import jsprit.core.util.Coordinate;
import junit.framework.Assert;
import org.junit.Test;

/**
 * Created by schroeder on 16.12.14.
 */
public class LocationTest {

    @Test
    public void whenIndexSet_buildLocation() {
        Location l = Location.Builder.newInstance().setIndex(1).build();
        Assert.assertEquals(1, l.getIndex());
        Assert.assertTrue(true);
    }

    @Test
    public void whenIndexSetWitFactory_returnCorrectLocation() {
        Location l = Location.newInstance(1);
        Assert.assertEquals(1, l.getIndex());
        Assert.assertTrue(true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenIndexSmallerZero_throwException() {
        Location l = Location.Builder.newInstance().setIndex(-1).build();
    }

    @Test(expected = IllegalStateException.class)
    public void whenCoordinateAndIdAndIndexNotSet_throwException() {
        Location l = Location.Builder.newInstance().build();
    }

    @Test
    public void whenIdSet_build() {
        Location l = Location.Builder.newInstance().setId("id").build();
        Assert.assertEquals("id", l.getId());
        Assert.assertTrue(true);
    }

    @Test
    public void whenIdSetWithFactory_returnCorrectLocation() {
        Location l = Location.newInstance("id");
        Assert.assertEquals("id", l.getId());
        Assert.assertTrue(true);
    }

    @Test
    public void whenCoordinateSet_build() {
        Location l = Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(10, 20)).build();
        Assert.assertEquals(10., l.getCoordinate().getX());
        Assert.assertEquals(20., l.getCoordinate().getY());
        Assert.assertTrue(true);
    }

    @Test
    public void whenCoordinateSetWithFactory_returnCorrectLocation() {
//        Location l = Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(10,20)).build();
        Location l = Location.newInstance(10, 20);
        Assert.assertEquals(10., l.getCoordinate().getX());
        Assert.assertEquals(20., l.getCoordinate().getY());
        Assert.assertTrue(true);
    }


}
