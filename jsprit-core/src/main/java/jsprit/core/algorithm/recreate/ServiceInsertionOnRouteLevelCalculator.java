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

import jsprit.core.algorithm.state.InternalStates;
import jsprit.core.problem.JobActivityFactory;
import jsprit.core.problem.Location;
import jsprit.core.problem.constraint.HardActivityConstraint;
import jsprit.core.problem.constraint.HardActivityConstraint.ConstraintsStatus;
import jsprit.core.problem.constraint.HardRouteConstraint;
import jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.End;
import jsprit.core.problem.solution.route.activity.Start;
import jsprit.core.problem.solution.route.activity.TourActivities;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl.NoVehicle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;




final class ServiceInsertionOnRouteLevelCalculator implements JobInsertionCostsCalculator{
	
	private static final Logger logger = LogManager.getLogger(ServiceInsertionOnRouteLevelCalculator.class);
	
	private final VehicleRoutingTransportCosts transportCosts;
	
	private final VehicleRoutingActivityCosts activityCosts;

	private AuxilliaryCostCalculator auxilliaryPathCostCalculator;
	
	private JobActivityFactory activityFactory;
	
	private RouteAndActivityStateGetter stateManager;
	
	private HardRouteConstraint hardRouteLevelConstraint;
	
	private HardActivityConstraint hardActivityLevelConstraint;
	
	private ActivityInsertionCostsCalculator activityInsertionCostsCalculator;
	
	private int nuOfActsForwardLooking = 0;
//
	private int memorySize = 2;
	
	private Start start;
	
	private End end;

	public void setJobActivityFactory(JobActivityFactory jobActivityFactory){
		this.activityFactory=jobActivityFactory;
	}

	public void setMemorySize(int memorySize) {
		this.memorySize = memorySize;
		logger.debug("set [solutionMemory={}]", memorySize);
	}

	public ServiceInsertionOnRouteLevelCalculator(VehicleRoutingTransportCosts vehicleRoutingCosts, VehicleRoutingActivityCosts costFunc, ActivityInsertionCostsCalculator activityInsertionCostsCalculator, HardRouteConstraint hardRouteLevelConstraint, HardActivityConstraint hardActivityLevelConstraint) {
			super();
			this.transportCosts = vehicleRoutingCosts;
			this.activityCosts = costFunc;
			this.activityInsertionCostsCalculator = activityInsertionCostsCalculator;
			this.hardRouteLevelConstraint = hardRouteLevelConstraint;
			this.hardActivityLevelConstraint = hardActivityLevelConstraint;
			auxilliaryPathCostCalculator = new AuxilliaryCostCalculator(transportCosts, activityCosts);
			logger.debug("initialise {}", this);
		}


	public void setStates(RouteAndActivityStateGetter stateManager){
		this.stateManager = stateManager;
	}
	
	void setNuOfActsForwardLooking(int nOfActsForwardLooking) {
		this.nuOfActsForwardLooking = nOfActsForwardLooking;
		logger.debug("set [forwardLooking={}]", nOfActsForwardLooking);
	}

	@Override
	public String toString() {
		return "[name=calculatesServiceInsertionOnRouteLevel][solutionMemory="+memorySize+"][forwardLooking="+nuOfActsForwardLooking+"]";
	}
	
	/**
	 * Calculates the insertion costs of job i on route level (which is based on the assumption that inserting job i does not only
	 * have local effects but affects the entire route).
	 * Calculation is conducted by two steps. In the first step, promising insertion positions are identified by appromiximating their 
	 * marginal insertion cost. In the second step, marginal cost of the best M positions are calculated exactly.
	 * 
	 * 
	 */
	@Override
	public InsertionData getInsertionData(final VehicleRoute currentRoute, final Job jobToInsert, final Vehicle newVehicle, double newVehicleDepartureTime, final Driver newDriver, final double best_known_insertion_costs) {
		if(jobToInsert == null) throw new IllegalStateException("job is null. cannot calculate the insertion of a null-job.");
		if(newVehicle == null || newVehicle instanceof NoVehicle) throw new IllegalStateException("no vehicle given. set para vehicle!");
		
		JobInsertionContext insertionContext = new JobInsertionContext(currentRoute, jobToInsert, newVehicle, newDriver, newVehicleDepartureTime);
		if(!hardRouteLevelConstraint.fulfilled(insertionContext)){
			return InsertionData.createEmptyInsertionData();
		}
		
		/**
		 * map that memorizes the costs with newVehicle, which is a cost-snapshot at tour-activities. 
		 */
//		Map<TourActivity,Double> activity2costWithNewVehicle = new HashMap<TourActivity,Double>();
		
		/**
		 * priority queue that stores insertion-data by insertion-costs in ascending order.
		 */
		PriorityQueue<InsertionData> bestInsertionsQueue = new PriorityQueue<InsertionData>(Math.max(2, currentRoute.getTourActivities().getActivities().size()), getComparator());
		
		TourActivities tour = currentRoute.getTourActivities();
		double best_insertion_costs = best_known_insertion_costs;
		Service service = (Service)jobToInsert;

		
		/**
		 * some inis
		 */
		TourActivity serviceAct2Insert = activityFactory.createActivities(service).get(0);
		int best_insertion_index = InsertionData.NO_INDEX;
		
		initialiseStartAndEnd(newVehicle, newVehicleDepartureTime);
		
		TourActivity prevAct = start;
		int actIndex = 0;
		double sumOf_prevCosts_newVehicle = 0.0;
		double prevActDepTime_newVehicle = start.getEndTime();

        boolean loopBroken = false;
		/**
		 * inserting serviceAct2Insert in route r={0,1,...,i-1,i,j,j+1,...,n(r),n(r)+1}
		 * i=prevAct
		 * j=nextAct
		 * k=serviceAct2Insert
		 */
		for(TourActivity nextAct : tour.getActivities()){
            ConstraintsStatus hardActivityConstraintsStatus = hardActivityLevelConstraint.fulfilled(insertionContext, prevAct, serviceAct2Insert, nextAct, prevActDepTime_newVehicle);
            if(hardActivityConstraintsStatus.equals(ConstraintsStatus.FULFILLED)){
                /**
                 * builds a path on this route forwardPath={i,k,j,j+1,j+2,...,j+nuOfActsForwardLooking}
                 */
                double actInsertionCosts = activityInsertionCostsCalculator.getCosts(insertionContext, prevAct, nextAct, serviceAct2Insert, prevActDepTime_newVehicle);

                /**
                 * insertion_cost_approximation = c({0,1,...,i},newVehicle) + c({i,k,j,j+1,j+2,...,j+nuOfActsForwardLooking},newVehicle) - c({0,1,...,i,j,j+1,...,j+nuOfActsForwardLooking},oldVehicle)
                 */
                double insertion_cost_approximation = sumOf_prevCosts_newVehicle - sumOf_prevCosts_oldVehicle(currentRoute,prevAct) + actInsertionCosts;

                /**
                 * memorize it in insertion-queue
                 */
                if(insertion_cost_approximation < best_known_insertion_costs){
                    bestInsertionsQueue.add(new InsertionData(insertion_cost_approximation, InsertionData.NO_INDEX, actIndex, newVehicle, newDriver));
                }
            }
            else if(hardActivityConstraintsStatus.equals(ConstraintsStatus.NOT_FULFILLED_BREAK)){
                loopBroken = true;
                break;
            }


            /**
             * calculate transport and activity costs with new vehicle (without inserting k)
             */
			double transportCost_prevAct_nextAct_newVehicle = transportCosts.getTransportCost(prevAct.getLocation(), nextAct.getLocation(), prevActDepTime_newVehicle, newDriver, newVehicle);
			double transportTime_prevAct_nextAct_newVehicle = transportCosts.getTransportTime(prevAct.getLocation(), nextAct.getLocation(), prevActDepTime_newVehicle, newDriver, newVehicle);
			double arrTime_nextAct_newVehicle = prevActDepTime_newVehicle + transportTime_prevAct_nextAct_newVehicle;
			double activityCost_nextAct = activityCosts.getActivityCost(nextAct, arrTime_nextAct_newVehicle, newDriver, newVehicle);

			/**
			 * memorize transport and activity costs with new vehicle without inserting k
			 */
			sumOf_prevCosts_newVehicle += transportCost_prevAct_nextAct_newVehicle + activityCost_nextAct;
//			activity2costWithNewVehicle.put(nextAct, sumOf_prevCosts_newVehicle);

			/**
			 * departure time at nextAct with new vehicle
			 */
			double depTime_nextAct_newVehicle = Math.max(arrTime_nextAct_newVehicle, nextAct.getTheoreticalEarliestOperationStartTime()) + nextAct.getOperationTime();

			/**
			 * set previous to next
			 */
			prevAct = nextAct;
			prevActDepTime_newVehicle = depTime_nextAct_newVehicle;

			actIndex++;
		}
		if(!loopBroken){
			End nextAct = end;
            ConstraintsStatus hardActivityConstraintsStatus = hardActivityLevelConstraint.fulfilled(insertionContext, prevAct, serviceAct2Insert, nextAct, prevActDepTime_newVehicle);
            if(hardActivityConstraintsStatus.equals(ConstraintsStatus.FULFILLED)){
                double actInsertionCosts = activityInsertionCostsCalculator.getCosts(insertionContext, prevAct, nextAct, serviceAct2Insert, prevActDepTime_newVehicle);

                    /**
                     * insertion_cost_approximation = c({0,1,...,i},newVehicle) + c({i,k,j,j+1,j+2,...,j+nuOfActsForwardLooking},newVehicle) - c({0,1,...,i,j,j+1,...,j+nuOfActsForwardLooking},oldVehicle)
                     */
                    double insertion_cost_approximation = sumOf_prevCosts_newVehicle - sumOf_prevCosts_oldVehicle(currentRoute,prevAct) + actInsertionCosts;

                    /**
                     * memorize it in insertion-queue
                     */
                    if(insertion_cost_approximation < best_known_insertion_costs){
                        bestInsertionsQueue.add(new InsertionData(insertion_cost_approximation, InsertionData.NO_INDEX, actIndex, newVehicle, newDriver));
                    }

            }
        }

		
		/**
		 * the above calculations approximate insertion costs. now calculate the exact insertion costs for the most promising (according to the approximation)
		 * insertion positions.
		 *  
		 */
		
		if(memorySize==0){ // return bestInsertion
			InsertionData insertion = bestInsertionsQueue.poll();
			if(insertion != null){
				best_insertion_index = insertion.getDeliveryInsertionIndex();
				best_insertion_costs = insertion.getInsertionCost();
			}
		}
		else{
		
			for(int i=0;i<memorySize;i++){
				InsertionData data = bestInsertionsQueue.poll();
				if(data == null){
					continue;
				}
				/**
				 * build tour with new activity.
				 */
				List<TourActivity> wholeTour = new ArrayList<TourActivity>();
				wholeTour.add(start);
				wholeTour.addAll(currentRoute.getTourActivities().getActivities());
				wholeTour.add(end);
				wholeTour.add(data.getDeliveryInsertionIndex()+1, serviceAct2Insert);

				/**
				 * compute cost-diff of tour with and without new activity --> insertion_costs
				 */
                Double currentRouteCosts = stateManager.getRouteState(currentRoute, InternalStates.COSTS, Double.class);
                if(currentRouteCosts == null) currentRouteCosts = 0.;
                double insertion_costs = auxilliaryPathCostCalculator.costOfPath(wholeTour, start.getEndTime(), newDriver, newVehicle) - currentRouteCosts;

				/**
				 * if better than best known, make it the best known
				 */
				if(insertion_costs < best_insertion_costs){
					best_insertion_index = data.getDeliveryInsertionIndex();
					best_insertion_costs = insertion_costs;
				}
			}
		}
		if(best_insertion_index == InsertionData.NO_INDEX) return InsertionData.createEmptyInsertionData();
		InsertionData insertionData = new InsertionData(best_insertion_costs, InsertionData.NO_INDEX, best_insertion_index, newVehicle, newDriver);
		insertionData.setVehicleDepartureTime(start.getEndTime());
		return insertionData;
	}

	private void initialiseStartAndEnd(final Vehicle newVehicle, double newVehicleDepartureTime) {
		if(start == null){
			start = new Start(newVehicle.getStartLocation(), newVehicle.getEarliestDeparture(), Double.MAX_VALUE);
			start.setEndTime(newVehicleDepartureTime);
		}
		else{
			start.setLocation(Location.newInstance(newVehicle.getStartLocation().getId()));
			start.setTheoreticalEarliestOperationStartTime(newVehicle.getEarliestDeparture());
			start.setTheoreticalLatestOperationStartTime(Double.MAX_VALUE);
			start.setEndTime(newVehicleDepartureTime);
		}
		
		if(end == null){
			end = new End(newVehicle.getEndLocation(), 0.0, newVehicle.getLatestArrival());
		}
		else{
			end.setLocation(Location.newInstance(newVehicle.getEndLocation().getId()));
			end.setTheoreticalEarliestOperationStartTime(0.0);
			end.setTheoreticalLatestOperationStartTime(newVehicle.getLatestArrival());
		}
	}

	private double sumOf_prevCosts_oldVehicle(VehicleRoute vehicleRoute, TourActivity act) {
		Double prevCost;
        if(act instanceof End){
			prevCost = stateManager.getRouteState(vehicleRoute, InternalStates.COSTS,Double.class);
		}
        else prevCost = stateManager.getActivityState(act, InternalStates.COSTS,Double.class);
        if(prevCost == null) prevCost = 0.;
		return prevCost;
	}

	private Comparator<InsertionData> getComparator() {
		return new Comparator<InsertionData>() {

			@Override
			public int compare(InsertionData o1, InsertionData o2) {
				if(o1.getInsertionCost() < o2.getInsertionCost()){
					return -1;
				}
				else {
					return 1;
				}

			}
		};
	}
}
