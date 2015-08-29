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
package jsprit.core.problem.constraint;

import jsprit.core.problem.misc.JobInsertionContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;


class HardRouteLevelConstraintManager implements HardRouteConstraint {

    private Collection<HardRouteConstraint> hardConstraints = new ArrayList<HardRouteConstraint>();

    public void addConstraint(HardRouteConstraint constraint) {
        hardConstraints.add(constraint);
    }

    Collection<HardRouteConstraint> getConstraints() {
        return Collections.unmodifiableCollection(hardConstraints);
    }

    @Override
    public boolean fulfilled(JobInsertionContext insertionContext) {
        for (HardRouteConstraint constraint : hardConstraints) {
            if (!constraint.fulfilled(insertionContext)) {
                return false;
            }
        }
        return true;
    }

}
