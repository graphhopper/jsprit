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
package jsprit.instance.reader;

import jsprit.core.problem.Location;
import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.util.EuclideanDistanceCalculator;
import jsprit.core.util.Locations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class Figliozzi {

    public static class TimeDependentTransportCostsFactory {

        public static enum SpeedDistribution {

            TD1a, TD1b, TD1c, TD2a, TD2b, TD2c, TD3a, TD3b, TD3c, TD1d, TD2d, TD3d, TD4, TD5, TD6, CLASSIC

        }


        public static TDCosts createCosts(Locations locations, SpeedDistribution speedDistribution, double depotClosingTime){
            List<Double> timeBins = createTimeBins(depotClosingTime);
            List<Double> speedValues = createSpeedValues(speedDistribution);
            return new TDCosts(locations,timeBins,speedValues);
        }

        static List<Double> createSpeedValues(SpeedDistribution speedDistribution) {
            List<Double> speedValues = Collections.emptyList();
            switch(speedDistribution){
                case TD1a: speedValues = Arrays.asList(1.,1.6,1.05,1.6,1.); break;
                case TD2a: speedValues = Arrays.asList(1.,2.,1.5,2.,1.); break;
                case TD3a: speedValues = Arrays.asList(1.,2.5,1.75,2.5,1.); break;

                case TD1b: speedValues = Arrays.asList(1.6,1.,1.05,1.,1.6); break;
                case TD2b: speedValues = Arrays.asList(2.,1.,1.5,1.,2.); break;
                case TD3b: speedValues = Arrays.asList(2.5,1.,1.75,1.,2.5); break;

                case TD1c: speedValues = Arrays.asList(1.6,1.6,1.05,1.,1.); break;
                case TD2c: speedValues = Arrays.asList(2.,2.,1.5,1.,1.); break;
                case TD3c: speedValues = Arrays.asList(2.5,2.5,1.75,1.,1.); break;

                case TD1d: speedValues = Arrays.asList(1.,1.,1.05,1.6,1.6); break;
                case TD2d: speedValues = Arrays.asList(1.,1.,1.5,2.,2.); break;
                case TD3d: speedValues = Arrays.asList(1.,1.,1.75,2.5,2.5); break;

                case TD4: speedValues = Arrays.asList(1.1,0.85,1.1,0.85,1.1); break;
                case TD5: speedValues = Arrays.asList(1.2,0.8,1.,0.8,1.2); break;
                case TD6: speedValues = Arrays.asList(1.2,0.7,1.2,0.7,1.2); break;

                case CLASSIC: speedValues = Arrays.asList(1.,1.,1.,1.,1.); break;
            }
            return speedValues;
        }

        private static List<Double> createTimeBins(double depotClosingTime) {
            List<Double> timeBins = new ArrayList<Double>();
            timeBins.add(.2 * depotClosingTime);
            timeBins.add(.4 * depotClosingTime);
            timeBins.add(.6 * depotClosingTime);
            timeBins.add(.8 * depotClosingTime);
            timeBins.add(depotClosingTime);
            return timeBins;
        }

    }


    public static class TDCosts implements VehicleRoutingTransportCosts {
		
		private List<Double> timeBins;
		
		private List<Double> speed;

        private Locations locations;

        private double transportDistanceParameter = 1.;

        private double transportTimeParameter = 1.;
		
		public TDCosts(Locations locations, List<Double> timeBins, List<Double> speedValues) {
			super();
			speed = speedValues;
			this.timeBins = timeBins;
            this.locations = locations;
		}

        public void setTransportDistanceParameter(double transportDistanceParameter) {
            this.transportDistanceParameter = transportDistanceParameter;
        }

        public void setTransportTimeParameter(double transportTimeParameter) {
            this.transportTimeParameter = transportTimeParameter;
        }

        @Override
		public double getTransportCost(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle) {
			return transportDistanceParameter * EuclideanDistanceCalculator.calculateDistance(locations.getCoord(from.getId()),locations.getCoord(to.getId())) +
					transportTimeParameter * getTransportTime(from, to,departureTime, driver, vehicle);
        }
		
		@Override
		public double getBackwardTransportCost(Location from, Location to,double arrivalTime, Driver driver, Vehicle vehicle) {
            return transportDistanceParameter * EuclideanDistanceCalculator.calculateDistance(locations.getCoord(from.getId()),locations.getCoord(to.getId())) +
                    transportTimeParameter * getBackwardTransportTime(from, to, arrivalTime, driver, vehicle);
        }

		
		@Override
		public double getTransportTime(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle) {
			if(from.equals(to)){
				return 0.0;
			}
			double totalTravelTime = 0.0;
			double distanceToTravel = EuclideanDistanceCalculator.calculateDistance(locations.getCoord(from.getId()), locations.getCoord(to.getId()));
			double currentTime = departureTime;
			for(int i=0;i<timeBins.size();i++){
				double timeThreshold = timeBins.get(i);
				if(currentTime < timeThreshold){
					double maxReachableDistance = (timeThreshold-currentTime)*speed.get(i);
					if(distanceToTravel > maxReachableDistance){
						distanceToTravel = distanceToTravel - maxReachableDistance;
						totalTravelTime += (timeThreshold-currentTime);
						currentTime = timeThreshold;
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
		public double getBackwardTransportTime(Location from, Location to,double arrivalTime, Driver driver, Vehicle vehicle) {
			if(from.equals(to)){
				return 0.0;
			}
			double totalTravelTime = 0.0;
			double distanceToTravel = EuclideanDistanceCalculator.calculateDistance(locations.getCoord(from.getId()), locations.getCoord(to.getId()));
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

}
