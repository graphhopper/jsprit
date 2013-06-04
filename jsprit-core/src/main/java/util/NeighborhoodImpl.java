/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;

import basics.Job;
import basics.Service;
import basics.VehicleRoutingProblem;
import basics.route.Vehicle;



public class NeighborhoodImpl implements Neighborhood{
	
	private static Logger log = Logger.getLogger(NeighborhoodImpl.class);
	
	private Set<String> neighborsToAll;
	
	private double threshold = Double.MAX_VALUE;
	
	private boolean initialised = false;
	
	private VehicleRoutingProblem vrp;
	
	public void setThreshold(double threshold) {
		this.threshold = threshold;
		log.info("set threshold to " + threshold);
	}

	private Map<String,Set<String>> neighbors;
	
	public NeighborhoodImpl(VehicleRoutingProblem vrp) {
		neighborsToAll = new HashSet<String>();
		this.vrp = vrp;
		neighbors = new HashMap<String, Set<String>>();
//		makeNeighbors();
	}

	private void makeNeighbors() {
		for(Job i : vrp.getJobs().values()){
			Set<String> neigh = new HashSet<String>();
			for(Vehicle v : vrp.getVehicles()){
				double dist2depot = EuclideanDistanceCalculator.calculateDistance(v.getCoord(), ((Service)i).getCoord());
				if(dist2depot <= threshold){
					neighborsToAll.add(((Service)i).getLocationId());
				}
			}
			for(Job j : vrp.getJobs().values()){
				double crowFlyDistance = EuclideanDistanceCalculator.calculateDistance(((Service)i).getCoord(), ((Service)j).getCoord());
				
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
//		for(Job j : vrp.getJobs().values()){
//			
//		}
	}

	public void initialise(){
		log.info("initialise neighboorhood [threshold="+ this.threshold + "]");
		makeNeighborsToAll(vrp.getVehicles());
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
