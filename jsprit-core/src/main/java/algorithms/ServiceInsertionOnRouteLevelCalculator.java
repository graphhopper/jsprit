/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
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
package algorithms;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.apache.log4j.Logger;

import util.Neighborhood;
import algorithms.ActivityInsertionCostsCalculator.ActivityInsertionCosts;
import basics.Job;
import basics.Service;
import basics.costs.VehicleRoutingActivityCosts;
import basics.costs.VehicleRoutingTransportCosts;
import basics.route.DefaultTourActivityFactory;
import basics.route.Driver;
import basics.route.End;
import basics.route.Start;
import basics.route.TourActivities;
import basics.route.TourActivity;
import basics.route.TourActivityFactory;
import basics.route.Vehicle;
import basics.route.VehicleImpl.NoVehicle;
import basics.route.VehicleRoute;



final class ServiceInsertionOnRouteLevelCalculator implements JobInsertionCalculator{
	
	private static final Logger logger = Logger.getLogger(ServiceInsertionOnRouteLevelCalculator.class);
	
	private final VehicleRoutingTransportCosts transportCosts;
	
	private final VehicleRoutingActivityCosts activityCosts;

	private AuxilliaryCostCalculator auxilliaryPathCostCalculator;
	
	private TourActivityFactory tourActivityFactory = new DefaultTourActivityFactory();
	
	private StateGetter stateManager;
	
	private HardRouteLevelConstraint hardRouteLevelConstraint;
	
	private HardActivityLevelConstraint hardActivityLevelConstraint;
	
	private ActivityInsertionCostsCalculator activityInsertionCostsCalculator;
	
	private int nuOfActsForwardLooking = 0;
	
	private int memorySize = 2;
	
	private Start start;
	
	private End end;
	
	private Neighborhood neighborhood = new Neighborhood() {
		
		@Override
		public boolean areNeighbors(String location1, String location2) {
			return true;
		}
		
	};
	
	public void setTourActivityFactory(TourActivityFactory tourActivityFactory){
		this.tourActivityFactory=tourActivityFactory;
	}
	
	public void setNeighborhood(Neighborhood neighborhood) {
		this.neighborhood = neighborhood;
		logger.info("initialise neighborhood " + neighborhood);
	}
	
	public void setMemorySize(int memorySize) {
		this.memorySize = memorySize;
		logger.info("set [solutionMemory="+memorySize+"]");
	}

	public ServiceInsertionOnRouteLevelCalculator(VehicleRoutingTransportCosts vehicleRoutingCosts, VehicleRoutingActivityCosts costFunc, ActivityInsertionCostsCalculator activityInsertionCostsCalculator, HardRouteLevelConstraint hardRouteLevelConstraint, HardActivityLevelConstraint hardActivityLevelConstraint) {
			super();
			this.transportCosts = vehicleRoutingCosts;
			this.activityCosts = costFunc;
			this.activityInsertionCostsCalculator = activityInsertionCostsCalculator;
			this.hardRouteLevelConstraint = hardRouteLevelConstraint;
			this.hardActivityLevelConstraint = hardActivityLevelConstraint;
			auxilliaryPathCostCalculator = new AuxilliaryCostCalculator(transportCosts, activityCosts);
			logger.info("initialise " + this);
		}


	public void setStates(StateGetter stateManager){
		this.stateManager = stateManager;
	}
	
	void setNuOfActsForwardLooking(int nOfActsForwardLooking) {
		this.nuOfActsForwardLooking = nOfActsForwardLooking;
		logger.info("set [forwardLooking="+nOfActsForwardLooking+"]");
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
	public InsertionData calculate(final VehicleRoute currentRoute, final Job jobToInsert, final Vehicle newVehicle, double newVehicleDepartureTime, final Driver newDriver, final double best_known_insertion_costs) {
		if(jobToInsert == null) throw new IllegalStateException("job is null. cannot calculate the insertion of a null-job.");
		if(newVehicle == null || newVehicle instanceof NoVehicle) throw new IllegalStateException("no vehicle given. set para vehicle!");
		
		InsertionContext insertionContext = new InsertionContext(currentRoute, jobToInsert, newVehicle, newDriver, newVehicleDepartureTime);
		if(!hardRouteLevelConstraint.fulfilled(insertionContext)){
			return InsertionData.noInsertionFound();
		}
		
		/**
		 * map that memorizes the costs with newVehicle, which is a cost-snapshot at tour-activities. 
		 */
		Map<TourActivity,Double> activity2costWithNewVehicle = new HashMap<TourActivity,Double>();
		
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
		TourActivity serviceAct2Insert = tourActivityFactory.createActivity(service);
		int best_insertion_index = InsertionData.NO_INDEX;
		
		initialiseStartAndEnd(newVehicle, newVehicleDepartureTime);
		
		TourActivity prevAct = start;
		int actIndex = 0;
		double sumOf_prevCosts_newVehicle = 0.0;
		double prevActDepTime_newVehicle = start.getEndTime();

		/**
		 * inserting serviceAct2Insert in route r={0,1,...,i-1,i,j,j+1,...,n(r),n(r)+1}
		 * i=prevAct
		 * j=nextAct
		 * k=serviceAct2Insert
		 */
		for(TourActivity nextAct : tour.getActivities()){
			if(neighborhood.areNeighbors(serviceAct2Insert.getLocationId(), prevAct.getLocationId()) && neighborhood.areNeighbors(serviceAct2Insert.getLocationId(), nextAct.getLocationId())){
				if(hardActivityLevelConstraint.fulfilled(insertionContext, prevAct, serviceAct2Insert, nextAct, prevActDepTime_newVehicle)){
					/**
					 * builds a path on this route forwardPath={i,k,j,j+1,j+2,...,j+nuOfActsForwardLooking}
					 */		
					ActivityInsertionCosts actInsertionCosts = activityInsertionCostsCalculator.calculate(insertionContext, prevAct, nextAct, serviceAct2Insert, prevActDepTime_newVehicle);

					/**
					 * insertion_cost_approximation = c({0,1,...,i},newVehicle) + c({i,k,j,j+1,j+2,...,j+nuOfActsForwardLooking},newVehicle) - c({0,1,...,i,j,j+1,...,j+nuOfActsForwardLooking},oldVehicle)
					 */
					double insertion_cost_approximation = sumOf_prevCosts_newVehicle - sumOf_prevCosts_oldVehicle(currentRoute,prevAct) + actInsertionCosts.getAdditionalCosts(); 

					/**
					 * memorize it in insertion-queue
					 */
					if(insertion_cost_approximation < best_known_insertion_costs){
						bestInsertionsQueue.add(new InsertionData(insertion_cost_approximation, InsertionData.NO_INDEX, actIndex, newVehicle, newDriver));
					}
				}

			}

			/**
			 * calculate transport and activity costs with new vehicle (without inserting k)
			 */
			double transportCost_prevAct_nextAct_newVehicle = transportCosts.getTransportCost(prevAct.getLocationId(), nextAct.getLocationId(), prevActDepTime_newVehicle, newDriver, newVehicle);
			double transportTime_prevAct_nextAct_newVehicle = transportCosts.getTransportTime(prevAct.getLocationId(), nextAct.getLocationId(), prevActDepTime_newVehicle, newDriver, newVehicle);
			double arrTime_nextAct_newVehicle = prevActDepTime_newVehicle + transportTime_prevAct_nextAct_newVehicle;
			double activityCost_nextAct = activityCosts.getActivityCost(nextAct, arrTime_nextAct_newVehicle, newDriver, newVehicle);

			/**
			 * memorize transport and activity costs with new vehicle without inserting k
			 */
			sumOf_prevCosts_newVehicle += transportCost_prevAct_nextAct_newVehicle + activityCost_nextAct;
			activity2costWithNewVehicle.put(nextAct, sumOf_prevCosts_newVehicle);

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
		End nextAct = end;
		if(neighborhood.areNeighbors(serviceAct2Insert.getLocationId(), prevAct.getLocationId()) && neighborhood.areNeighbors(serviceAct2Insert.getLocationId(), nextAct.getLocationId())){
			if(hardActivityLevelConstraint.fulfilled(insertionContext, prevAct, serviceAct2Insert, nextAct, prevActDepTime_newVehicle)){
				ActivityInsertionCosts actInsertionCosts = activityInsertionCostsCalculator.calculate(insertionContext, prevAct, nextAct, serviceAct2Insert, prevActDepTime_newVehicle);
				if(actInsertionCosts != null){
					/**
					 * insertion_cost_approximation = c({0,1,...,i},newVehicle) + c({i,k,j,j+1,j+2,...,j+nuOfActsForwardLooking},newVehicle) - c({0,1,...,i,j,j+1,...,j+nuOfActsForwardLooking},oldVehicle)
					 */
					double insertion_cost_approximation = sumOf_prevCosts_newVehicle - sumOf_prevCosts_oldVehicle(currentRoute,prevAct) + actInsertionCosts.getAdditionalCosts(); 

					/**
					 * memorize it in insertion-queue
					 */
					if(insertion_cost_approximation < best_known_insertion_costs){
						bestInsertionsQueue.add(new InsertionData(insertion_cost_approximation, InsertionData.NO_INDEX, actIndex, newVehicle, newDriver));
					}
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
				double insertion_costs = auxilliaryPathCostCalculator.costOfPath(wholeTour, start.getEndTime(), newDriver, newVehicle) - stateManager.getRouteState(currentRoute,StateIdFactory.COSTS).toDouble();

				/**
				 * if better than best known, make it the best known
				 */
				if(insertion_costs < best_insertion_costs){
					best_insertion_index = data.getDeliveryInsertionIndex();
					best_insertion_costs = insertion_costs;
				}
			}
		}
		if(best_insertion_index == InsertionData.NO_INDEX) return InsertionData.noInsertionFound();
		return new InsertionData(best_insertion_costs, InsertionData.NO_INDEX, best_insertion_index, newVehicle, newDriver);
	}
	
	/**
	 * initialize start and end of tour.
	 * 
	 * @param newVehicle
	 * @param newVehicleDepartureTime
	 */
	private void initialiseStartAndEnd(final Vehicle newVehicle, double newVehicleDepartureTime) {
		if(start == null){
			start = Start.newInstance(newVehicle.getLocationId(), newVehicle.getEarliestDeparture(), newVehicle.getLatestArrival());
			start.setEndTime(newVehicleDepartureTime);
		}
		else{
			start.setLocationId(newVehicle.getLocationId());
			start.setTheoreticalEarliestOperationStartTime(newVehicle.getEarliestDeparture());
			start.setTheoreticalLatestOperationStartTime(newVehicle.getLatestArrival());
			start.setEndTime(newVehicleDepartureTime);
		}
		
		if(end == null){
			end = End.newInstance(newVehicle.getLocationId(), 0.0, newVehicle.getLatestArrival());
		}
		else{
			end.setLocationId(newVehicle.getLocationId());
			end.setTheoreticalEarliestOperationStartTime(newVehicleDepartureTime);
			end.setTheoreticalLatestOperationStartTime(newVehicle.getLatestArrival());
		}
	}

	private double sumOf_prevCosts_oldVehicle(VehicleRoute vehicleRoute, TourActivity act) {
		if(act instanceof End){
			return stateManager.getRouteState(vehicleRoute,StateIdFactory.COSTS).toDouble();
		}
		return stateManager.getActivityState(act,StateIdFactory.COSTS).toDouble();
	}

	/**
	 * creates a comparator to sort insertion-data in insertionQueue in ascending order according insertion costs.
	 * @return
	 */
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
