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
package jsprit.core.problem.job;


import jsprit.core.problem.Capacity;
import jsprit.core.problem.Skills;

/**
 * Pickup extends Service and is intended to model a Service where smth is LOADED (i.e. picked up) to a transport unit.
 *
 * @author schroeder
 */
public class Break extends Service {

    public static class Builder extends Service.Builder<Break> {

        /**
         * Returns a new instance of builder that builds a pickup.
         *
         * @param id the id of the pickup
         * @return the builder
         */
        public static Builder newInstance(String id) {
            return new Builder(id);
        }

        private boolean variableLocation = true;

        Builder(String id) {
            super(id);
        }

        /**
         * Builds Pickup.
         * <p/>
         * <p>Pickup type is "pickup"
         *
         * @return pickup
         * @throws IllegalStateException if neither locationId nor coordinate has been set
         */
        public Break build() {
            if (location != null) {
                variableLocation = false;
            }
            this.setType("break");
            super.capacity = Capacity.Builder.newInstance().build();
            super.skills = Skills.Builder.newInstance().build();
            return new Break(this);
        }

    }

    private boolean variableLocation = true;

    Break(Builder builder) {
        super(builder);
        this.variableLocation = builder.variableLocation;
    }

    public boolean hasVariableLocation() {
        return variableLocation;
    }

}
