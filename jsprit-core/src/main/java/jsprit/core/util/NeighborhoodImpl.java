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
package jsprit.core.util;

import jsprit.core.problem.job.Service;
import jsprit.core.problem.vehicle.Vehicle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

@Deprecated
public class NeighborhoodImpl implements Neighborhood{
	
	private static Logger log = LogManager.getLogger(NeighborhoodImpl.class);
	
	private Set<String> neighborsToAll;
	
	private double threshold = Double.MAX_VALUE;
	
	private boolean initialised = false;
	
	public void setThreshold(double threshold) {
		this.threshold = threshold;
		log.info("set threshold to " + threshold);
	}

	private Map<String,Set<String>> neighbors;

	private Collection<Vehicle> vehicles;

	private Collection<Service> services;
	
	public NeighborhoodImpl(Collection<Vehicle> vehicles, Collection<Service> services) {
		neighborsToAll = new HashSet<String>();
		this.vehicles = vehicles;
		this.services = services;
		neighbors = new HashMap<String, Set<String>>();
	}

	private void makeNeighbors() {
		for(Service i : services){
			Set<String> neigh = new HashSet<String>();
			for(Vehicle v : vehicles){
				double dist2depot = EuclideanDistanceCalculator.calculateDistance(v.getStartLocationCoordinate(), i.getLocation().getCoordinate());
				if(dist2depot <= threshold){
					neighborsToAll.add(((Service)i).getLocation().getId());
				}
			}
			for(Service j : services){
				double crowFlyDistance = EuclideanDistanceCalculator.calculateDistance(i.getLocation().getCoordinate(), j.getLocation().getCoordinate());
				if(crowFlyDistance <= threshold) {
					neigh.add(((Service)j).getLocation().getId());
				}
			}
			neighbors.put(((Service)i).getLocation().getId(), neigh);
		}
		
	}

	private void makeNeighborsToAll(Collection<Vehicle> vehicles) {
		for(Vehicle v : vehicles){
			neighborsToAll.add(v.getStartLocationId());
		}
	}

	public void initialise(){
		log.info("initialise neighboorhood [threshold="+ this.threshold + "]");
		makeNeighborsToAll(vehicles);
		makeNeighbors();
		initialised = true;
	}
	
	public boolean areNeighbors(String location1, String location2){
		if(!initialised) {
//			initialise();
			return true;
		}
		if(neighborsToAll.contains(location1) || neighborsToAll.contains(location2)){
			return true; 
		}
		if(neighbors.get(location1).contains(location2)){
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "[name=euclideanNeighborhood][threshold="+threshold+"]";
	}

}
