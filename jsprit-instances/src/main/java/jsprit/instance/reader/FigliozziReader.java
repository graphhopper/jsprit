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
package jsprit.instance.reader;

import java.util.List;

import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.util.CrowFlyCosts;
import jsprit.core.util.Locations;


public class FigliozziReader {
	
	public static class TDCosts implements VehicleRoutingTransportCosts {
		
		private List<Double> timeBins;
		
		private List<Double> speed;
		
		private CrowFlyCosts crowFly;
		
		public TDCosts(Locations locations, List<Double> timeBins, List<Double> speedValues) {
			super();
			speed = speedValues;
			this.timeBins = timeBins;
			crowFly = new CrowFlyCosts(locations);
		}
			
		@Override
		public double getTransportCost(String fromId, String toId, double departureTime, Driver driver, Vehicle vehicle) {
			return 1.0*crowFly.getTransportCost(fromId, toId, departureTime, null, null) + 
					1.0*getTransportTime(fromId,toId,departureTime, null, null);
//			return getTransportTime(fromId, toId, departureTime, driver, vehicle);
//			return crowFly.getTransportCost(fromId, toId, departureTime, null, null);
		}
		
		@Override
		public double getBackwardTransportCost(String fromId, String toId,double arrivalTime, Driver driver, Vehicle vehicle) {
//			return crowFly.getTransportCost(fromId, toId, arrivalTime, null,null) + getBackwardTransportTime(fromId, toId, arrivalTime,null,null);
			return getBackwardTransportTime(fromId, toId, arrivalTime, driver, vehicle);
//			return crowFly.getTransportCost(fromId, toId, arrivalTime, null, null);
		}

		
		@Override
		public double getTransportTime(String fromId, String toId, double departureTime, Driver driver, Vehicle vehicle) {
			if(fromId.equals(toId)){
				return 0.0;
			}
			double totalTravelTime = 0.0;
			double distanceToTravel = crowFly.getTransportCost(fromId, toId, departureTime, null, null);
			double currentTime = departureTime;
			for(int i=0;i<timeBins.size();i++){
				double timeThreshold = timeBins.get(i);
				if(currentTime < timeThreshold){
					double maxReachableDistance = (timeThreshold-currentTime)*speed.get(i);
					if(distanceToTravel > maxReachableDistance){
						distanceToTravel = distanceToTravel - maxReachableDistance;
						totalTravelTime += (timeThreshold-currentTime);
						currentTime = timeThreshold;
						continue;
					}
					else{ //<= maxReachableDistance
						totalTravelTime += distanceToTravel/speed.get(i);
						return totalTravelTime;
					}
				}
			}
			return Double.MAX_VALUE;
		}


		@Override
		public double getBackwardTransportTime(String fromId, String toId,double arrivalTime, Driver driver, Vehicle vehicle) {
			if(fromId.equals(toId)){
				return 0.0;
			}
			double totalTravelTime = 0.0;
			double distanceToTravel = crowFly.getTransportCost(fromId, toId, arrivalTime, null, null);
			double currentTime = arrivalTime;
			for(int i=timeBins.size()-1;i>=0;i--){
				double nextLowerTimeThreshold;
				if(i>0){
					nextLowerTimeThreshold = timeBins.get(i-1);
				}
				else{
					nextLowerTimeThreshold = 0;
				}
				if(currentTime > nextLowerTimeThreshold){
					double maxReachableDistance = (currentTime - nextLowerTimeThreshold)*speed.get(i);
					if(distanceToTravel > maxReachableDistance){
						distanceToTravel = distanceToTravel - maxReachableDistance;
						totalTravelTime += (currentTime-nextLowerTimeThreshold);
						currentTime = nextLowerTimeThreshold;
						continue;
					}
					else{ //<= maxReachableDistance
						totalTravelTime += distanceToTravel/speed.get(i);
						return totalTravelTime;
					}
				}
			}
			return Double.MAX_VALUE;
		}

		

	}
	
//	private VehicleRoutingProblem.Builder builder;
//
//	public FigliozziReader(Builder builder) {
//		super();
//		this.builder = builder;
//	}
//	
//	public void read(String instanceFile, String speedScenarioFile, String speedScenario){
//		
//		
//	}
//	

}
