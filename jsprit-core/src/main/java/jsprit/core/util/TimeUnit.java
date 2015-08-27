/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package jsprit.core.util;

/**
 * Created by schroeder on 04.12.14.
 */
public enum TimeUnit {

    SEC("sec"), MIN("min"), HOUR("hour");

    String abbr;

    TimeUnit(String abbr) {
        this.abbr = abbr;
    }

    public String abbreviation() {
        return abbr;
    }
}
