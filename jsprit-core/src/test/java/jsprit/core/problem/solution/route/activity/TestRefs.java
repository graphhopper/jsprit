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
package jsprit.core.problem.solution.route.activity;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;


public class TestRefs {

    @Test
    public void testReferencs() {
        List<Start> starts = new ArrayList<Start>();
        starts.add(Start.newInstance("foo0", 0.0, 0.0));
        starts.add(Start.newInstance("foo1", 1.0, 1.0));

        doSmth(starts);

        assertTrue(starts.get(0).getLocation().getId().startsWith("foo"));
        assertTrue(starts.get(1).getLocation().getId().startsWith("foo"));
    }

    private void doSmth(List<Start> starts) {
        int count = 0;
        for (@SuppressWarnings("unused") Start s : starts) {
            s = Start.newInstance("yo_" + count, 0.0, 0.0);
            count++;
        }

    }

}
