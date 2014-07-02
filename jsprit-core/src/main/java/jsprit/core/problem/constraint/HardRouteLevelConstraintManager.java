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
package jsprit.core.problem.constraint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import jsprit.core.problem.misc.JobInsertionContext;


class HardRouteLevelConstraintManager implements HardRouteStateLevelConstraint {

	private Collection<HardRouteStateLevelConstraint> hardConstraints = new ArrayList<HardRouteStateLevelConstraint>();
	
	public void addConstraint(HardRouteStateLevelConstraint constraint){
		hardConstraints.add(constraint);
	}
	
	Collection<HardRouteStateLevelConstraint> getConstraints(){ return Collections.unmodifiableCollection(hardConstraints); }

	@Override
	public boolean fulfilled(JobInsertionContext insertionContext) {
		for(HardRouteStateLevelConstraint constraint : hardConstraints){
			if(!constraint.fulfilled(insertionContext)){
				return false;
			}
		}
		return true;
	}
	
}
