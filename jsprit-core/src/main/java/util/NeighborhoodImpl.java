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
package util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import basics.Service;
import basics.route.Vehicle;



public class NeighborhoodImpl implements Neighborhood{
	
	private static Logger log = Logger.getLogger(NeighborhoodImpl.class);
	
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
				double dist2depot = EuclideanDistanceCalculator.calculateDistance(v.getCoord(), i.getCoord());
				if(dist2depot <= threshold){
					neighborsToAll.add(((Service)i).getLocationId());
				}
			}
			for(Service j : services){
				double crowFlyDistance = EuclideanDistanceCalculator.calculateDistance(i.getCoord(), j.getCoord());
				if(crowFlyDistance <= threshold) {
					neigh.add(((Service)j).getLocationId());
				}
			}
			neighbors.put(((Service)i).getLocationId(), neigh);
		}
		
	}

	private void makeNeighborsToAll(Collection<Vehicle> vehicles) {
		for(Vehicle v : vehicles){
			neighborsToAll.add(v.getLocationId());
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
