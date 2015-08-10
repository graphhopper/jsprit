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
package jsprit.core.algorithm.recreate;

import jsprit.core.problem.JobActivityFactory;
import jsprit.core.problem.constraint.*;
import jsprit.core.problem.constraint.HardActivityConstraint.ConstraintsStatus;
import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.End;
import jsprit.core.problem.solution.route.activity.Start;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.util.CalculationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;

/**
 * Calculator that calculates the best insertion position for a {@link Service}.
 * 
 * @author schroeder
 *
 */
final class ServiceInsertionCalculator implements JobInsertionCostsCalculator{
	
	private static final Logger logger = LogManager.getLogger(ServiceInsertionCalculator.class);

	private HardRouteConstraint hardRouteLevelConstraint;
	
	private HardActivityConstraint hardActivityLevelConstraint;
	
	private SoftRouteConstraint softRouteConstraint;
	
	private SoftActivityConstraint softActivityConstraint;
	
	private VehicleRoutingTransportCosts transportCosts;
	
	private ActivityInsertionCostsCalculator additionalTransportCostsCalculator;
	
	private JobActivityFactory activityFactory;
	
	private AdditionalAccessEgressCalculator additionalAccessEgressCalculator;

	public ServiceInsertionCalculator(VehicleRoutingTransportCosts routingCosts, ActivityInsertionCostsCalculator additionalTransportCostsCalculator, ConstraintManager constraintManager) {
		super();
		this.transportCosts = routingCosts;
		hardRouteLevelConstraint = constraintManager;
		hardActivityLevelConstraint = constraintManager;
		softActivityConstraint = constraintManager;
		softRouteConstraint = constraintManager;
		this.additionalTransportCostsCalculator = additionalTransportCostsCalculator;
		additionalAccessEgressCalculator = new AdditionalAccessEgressCalculator(routingCosts);
		logger.debug("initialise {}", this);
	}

    public void setJobActivityFactory(JobActivityFactory jobActivityFactory){
        this.activityFactory = jobActivityFactory;
    }
	
	@Override
	public String toString() {
		return "[name=calculatesServiceInsertion]";
	}
	
	/**
	 * Calculates the marginal cost of inserting job i locally. This is based on the
	 * assumption that cost changes can entirely covered by only looking at the predecessor i-1 and its successor i+1.
	 *  
	 */
	@Override
	public InsertionData getInsertionData(final VehicleRoute currentRoute, final Job jobToInsert, final Vehicle newVehicle, double newVehicleDepartureTime, final Driver newDriver, final double bestKnownCosts) {
		JobInsertionContext insertionContext = new JobInsertionContext(currentRoute, jobToInsert, newVehicle, newDriver, newVehicleDepartureTime);
        Service service = (Service)jobToInsert;
        int insertionIndex = InsertionData.NO_INDEX;

		TourActivity deliveryAct2Insert = activityFactory.createActivities(service).get(0);
        insertionContext.getAssociatedActivities().add(deliveryAct2Insert);

        /*
        check hard constraints at route level
         */
        if(!hardRouteLevelConstraint.fulfilled(insertionContext)){
			return InsertionData.createEmptyInsertionData();
		}

        /*
        check soft constraints at route level
         */
        double additionalICostsAtRouteLevel = softRouteConstraint.getCosts(insertionContext);

		double bestCost = bestKnownCosts;
        additionalICostsAtRouteLevel += additionalAccessEgressCalculator.getCosts(insertionContext);

        /*
        generate new start and end for new vehicle
         */
        Start start = new Start(newVehicle.getStartLocation(), newVehicle.getEarliestDeparture(), Double.MAX_VALUE);
		start.setEndTime(newVehicleDepartureTime);
		End end = new End(newVehicle.getEndLocation(), 0.0, newVehicle.getLatestArrival());
		
		TourActivity prevAct = start;
		double prevActStartTime = newVehicleDepartureTime;
		int actIndex = 0;
		Iterator<TourActivity> activityIterator = currentRoute.getActivities().iterator();
		boolean tourEnd = false;
		while(!tourEnd){
			TourActivity nextAct;
			if(activityIterator.hasNext()) nextAct = activityIterator.next();
			else{
				nextAct = end;
				tourEnd = true;
			}
			ConstraintsStatus status = hardActivityLevelConstraint.fulfilled(insertionContext, prevAct, deliveryAct2Insert, nextAct, prevActStartTime);
			if(status.equals(ConstraintsStatus.FULFILLED)){
				//from job2insert induced costs at activity level
				double additionalICostsAtActLevel = softActivityConstraint.getCosts(insertionContext, prevAct, deliveryAct2Insert, nextAct, prevActStartTime);
				double additionalTransportationCosts = additionalTransportCostsCalculator.getCosts(insertionContext, prevAct, nextAct, deliveryAct2Insert, prevActStartTime);
				if(additionalICostsAtRouteLevel + additionalICostsAtActLevel + additionalTransportationCosts < bestCost){
					bestCost = additionalICostsAtRouteLevel + additionalICostsAtActLevel + additionalTransportationCosts;
					insertionIndex = actIndex;
				}
			}
			else if(status.equals(ConstraintsStatus.NOT_FULFILLED_BREAK)){
				break;
			}
			double nextActArrTime = prevActStartTime + transportCosts.getTransportTime(prevAct.getLocation(), nextAct.getLocation(), prevActStartTime, newDriver, newVehicle);
			prevActStartTime = CalculationUtils.getActivityEndTime(nextActArrTime, nextAct);
			prevAct = nextAct;
			actIndex++;
		}
		if(insertionIndex == InsertionData.NO_INDEX) {
			return InsertionData.createEmptyInsertionData();
		}
		InsertionData insertionData = new InsertionData(bestCost, InsertionData.NO_INDEX, insertionIndex, newVehicle, newDriver);
		insertionData.getEvents().add(new InsertActivity(currentRoute,newVehicle,deliveryAct2Insert,insertionIndex));
		insertionData.getEvents().add(new SwitchVehicle(currentRoute,newVehicle,newVehicleDepartureTime));
		insertionData.setVehicleDepartureTime(newVehicleDepartureTime);
		return insertionData;
	}

}
