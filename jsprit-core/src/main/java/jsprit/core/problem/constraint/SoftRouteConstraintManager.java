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

import jsprit.core.problem.misc.JobInsertionContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

class SoftRouteConstraintManager implements SoftRouteConstraint{

	private Collection<SoftRouteConstraint> softConstraints = new ArrayList<SoftRouteConstraint>();
	
	public void addConstraint(SoftRouteConstraint constraint){
		softConstraints.add(constraint);
	}
	
	Collection<SoftRouteConstraint> getConstraints(){ return Collections.unmodifiableCollection(softConstraints); }
	
	@Override
	public double getCosts(JobInsertionContext insertionContext) {
		double sumCosts = 0.0;
		for(SoftRouteConstraint c : softConstraints){
			sumCosts += c.getCosts(insertionContext);
		}
		return sumCosts;
	}

}
