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

import jsprit.core.problem.constraint.ConstraintManager.Priority;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.activity.TourActivity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


class HardActivityLevelConstraintManager implements HardActivityConstraint {

	private Collection<HardActivityConstraint> criticalConstraints = new ArrayList<HardActivityConstraint>();
	
	private Collection<HardActivityConstraint> highPrioConstraints = new ArrayList<HardActivityConstraint>();
	
	private Collection<HardActivityConstraint> lowPrioConstraints = new ArrayList<HardActivityConstraint>();
	
	public void addConstraint(HardActivityConstraint constraint, Priority priority){
		if(priority.equals(Priority.CRITICAL)){
			criticalConstraints.add(constraint);
		}
		else if(priority.equals(Priority.HIGH)){
			highPrioConstraints.add(constraint);
		}
		else{
			lowPrioConstraints.add(constraint);
		}
	}
	
	Collection<HardActivityConstraint> getCriticalConstraints(){ return Collections.unmodifiableCollection(criticalConstraints); }
	
	Collection<HardActivityConstraint> getHighPrioConstraints(){ return Collections.unmodifiableCollection(highPrioConstraints); }
	
	Collection<HardActivityConstraint> getLowPrioConstraints(){ return Collections.unmodifiableCollection(lowPrioConstraints); }
	
	Collection<HardActivityConstraint> getAllConstraints(){
		List<HardActivityConstraint> c = new ArrayList<HardActivityConstraint>();
		c.addAll(criticalConstraints);
		c.addAll(highPrioConstraints);
		c.addAll(lowPrioConstraints);
		return Collections.unmodifiableCollection(c); 
	}
	
	@Override
	public ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
		ConstraintsStatus notFulfilled = null;
		for(HardActivityConstraint c : criticalConstraints){
			ConstraintsStatus status = c.fulfilled(iFacts, prevAct, newAct, nextAct, prevActDepTime);
			if(status.equals(ConstraintsStatus.NOT_FULFILLED_BREAK)){
				return status;
			}
			else{
				if(status.equals(ConstraintsStatus.NOT_FULFILLED)){
					notFulfilled = status;
				}
			}
		}
		if(notFulfilled != null) return notFulfilled;
		
		for(HardActivityConstraint c : highPrioConstraints){
			ConstraintsStatus status = c.fulfilled(iFacts, prevAct, newAct, nextAct, prevActDepTime);
			if(status.equals(ConstraintsStatus.NOT_FULFILLED_BREAK)){
				return status;
			}
			else{
				if(status.equals(ConstraintsStatus.NOT_FULFILLED)){
					notFulfilled = status;
				}
			}
		}
		if(notFulfilled != null) return notFulfilled;
		
		for(HardActivityConstraint constraint : lowPrioConstraints){
			ConstraintsStatus status = constraint.fulfilled(iFacts, prevAct, newAct, nextAct, prevActDepTime);
			if(status.equals(ConstraintsStatus.NOT_FULFILLED_BREAK) || status.equals(ConstraintsStatus.NOT_FULFILLED)){
				return status;
			}
		}
		
		return ConstraintsStatus.FULFILLED;
	}
	
}
