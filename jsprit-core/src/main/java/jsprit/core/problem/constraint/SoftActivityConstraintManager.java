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
import jsprit.core.problem.solution.route.activity.TourActivity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

class SoftActivityConstraintManager implements SoftActivityConstraint{

	private Collection<SoftActivityConstraint> softConstraints = new ArrayList<SoftActivityConstraint>();
	
	public void addConstraint(SoftActivityConstraint constraint){
		softConstraints.add(constraint);
	}
	
	Collection<SoftActivityConstraint> getConstraints(){ return Collections.unmodifiableCollection(softConstraints); }
	
	@Override
	public double getCosts(JobInsertionContext iFacts, TourActivity prevAct,TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
		double sumCosts = 0.0;
		for(SoftActivityConstraint c : softConstraints){
			sumCosts += c.getCosts(iFacts, prevAct, newAct, nextAct, prevActDepTime);
		}
		return sumCosts;
	}

}
