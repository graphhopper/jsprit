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
package jsprit.core.algorithm;

import java.util.Collection;

import jsprit.core.algorithm.io.VehicleRoutingAlgorithms;
import jsprit.core.algorithm.listener.AlgorithmStartsListener;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.util.CrowFlyCosts;
import jsprit.core.util.EuclideanDistanceCalculator;
import jsprit.core.util.Locations;
import jsprit.core.util.NeighborhoodImpl;
import jsprit.core.util.Solutions;

import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.apache.log4j.Logger;


class NeighborhoodThresholdInitialiser implements AlgorithmStartsListener{

	private static Logger log = Logger.getLogger(NeighborhoodThresholdInitialiser.class);
	
	private NeighborhoodImpl neighborhood;
	
	private VehicleRoutingAlgorithmFactory routingAlgorithmFactory = new VehicleRoutingAlgorithmFactory() {
		
		@Override
		public VehicleRoutingAlgorithm createAlgorithm(VehicleRoutingProblem vrp) {
			VehicleRoutingAlgorithm algorithm = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "resources/config.xml");
			return algorithm;
		}
	};
	
	private int crowFlySpeed = 20;
	
	public NeighborhoodThresholdInitialiser(NeighborhoodImpl neighborhood) {
		this.neighborhood = neighborhood;
	}
	
	
	/**
	 * @param crowFlySpeed the crowFlySpeed to set
	 */
	public void setCrowFlySpeed(int crowFlySpeed) {
		this.crowFlySpeed = crowFlySpeed;
	}


	/**
	 * @param routingAlgorithmFactory the routingAlgorithm to set
	 */
	public void setRoutingAlgorithmFactory(VehicleRoutingAlgorithmFactory routingAlgorithmFactory) {
		this.routingAlgorithmFactory = routingAlgorithmFactory;
	}

	public void initialise(VehicleRoutingProblem problem){
		informAlgorithmStarts(problem, null, null);
	}
	
	@Override
	public void informAlgorithmStarts(VehicleRoutingProblem problem, VehicleRoutingAlgorithm algorithm, Collection<VehicleRoutingProblemSolution> solutions) {
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		builder.addAllJobs(problem.getJobs().values());
		builder.addAllVehicles(problem.getVehicles());
		CrowFlyCosts crowFly = new CrowFlyCosts(builder.getLocations());
		crowFly.speed = crowFlySpeed;
		builder.setRoutingCost(crowFly);
		VehicleRoutingProblem pblm = builder.build();
		
		VehicleRoutingAlgorithm algo = routingAlgorithmFactory.createAlgorithm(pblm);
		Collection<VehicleRoutingProblemSolution> mySolutions = algo.searchSolutions(); 
		
		double threshold = determineThreshold(pblm,builder.getLocations(), mySolutions);
		neighborhood.setThreshold(threshold);
		neighborhood.initialise();
	}

	private double determineThreshold(VehicleRoutingProblem pblm, Locations locations, Collection<VehicleRoutingProblemSolution> mySolutions) {
		VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(mySolutions);
		double[] distances = new double[bestSolution.getRoutes().size()+pblm.getJobs().size()];
		getDistances(distances,bestSolution,locations);
		Mean mean = new Mean();
		double meanValue = mean.evaluate(distances);
		StandardDeviation dev = new StandardDeviation();
		double devValue = dev.evaluate(distances, meanValue);
		log.info("mean="+meanValue+", dev="+devValue);
		return meanValue + devValue; 
//				+ 2*devValue;
//		return Double.MAX_VALUE;
	}

	private void getDistances(double[] distances, VehicleRoutingProblemSolution bestSolution, Locations locations) {
		int index = 0;
		for(VehicleRoute route : bestSolution.getRoutes()){
			TourActivity prev = null;
			for(TourActivity act : route.getTourActivities().getActivities()){
				if(prev == null){ prev = act; continue; }
				double dist = EuclideanDistanceCalculator.calculateDistance(locations.getCoord(prev.getLocationId()), locations.getCoord(act.getLocationId()));
//				log.info("dist="+dist);
				distances[index] = dist;
				index++;
				prev = act;
			}
//			double dist = EuclideanDistanceCalculator.calculateDistance(locations.getCoord(prev.getLocationId()), locations.getCoord(route.getEnd().getLocationId()));
//			distances[index] = dist;
//			index++;
		}
	}

}
