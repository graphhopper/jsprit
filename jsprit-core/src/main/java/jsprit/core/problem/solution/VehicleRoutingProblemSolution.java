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
package jsprit.core.problem.solution;

import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.route.VehicleRoute;

import java.util.ArrayList;
import java.util.Collection;


/**
 * Contains the solution of a vehicle routing problem and its corresponding costs.
 * 
 * @author stefan schroeder
 *
 */
public class VehicleRoutingProblemSolution {

	/**
	 * Makes a deep copy of the solution to be copied.
	 * 
	 * @param solution2copy solution to be copied
	 * @return solution
	 */
	public static VehicleRoutingProblemSolution copyOf(VehicleRoutingProblemSolution solution2copy){
		 return new VehicleRoutingProblemSolution(solution2copy);
	}
	
	private final Collection<VehicleRoute> routes;

    private Collection<Job> unassignedJobs = new ArrayList<Job>();

	private double cost;

	private VehicleRoutingProblemSolution(VehicleRoutingProblemSolution solution){
		routes = new ArrayList<VehicleRoute>();
		for(VehicleRoute r : solution.getRoutes()){
			VehicleRoute route = VehicleRoute.copyOf(r);
			routes.add(route);
		}
		this.cost = solution.getCost();
        unassignedJobs.addAll(solution.getUnassignedJobs());
	}
	
	/**
	 * Constructs a solution with a number of {@link VehicleRoute}s and their corresponding aggregate cost value.
	 * 
	 * @param routes routes being part of the solution
	 * @param cost total costs of solution
	 */
	public VehicleRoutingProblemSolution(Collection<VehicleRoute> routes, double cost) {
		super();
		this.routes = routes;
		this.cost = cost;
	}

    /**
     * Constructs a solution with a number of {@link VehicleRoute}s, bad jobs and their corresponding aggregate cost value.
     *
     * @param routes routes being part of the solution
     * @param unassignedJobs jobs that could not be assigned to any vehicle
     * @param cost total costs of solution
     *
     */
    public VehicleRoutingProblemSolution(Collection<VehicleRoute> routes, Collection<Job> unassignedJobs, double cost) {
        super();
        this.routes = routes;
        this.unassignedJobs = unassignedJobs;
        this.cost = cost;
    }

	/**
	 * Returns a collection of vehicle-routes.
	 * 
	 * @return collection of vehicle-routes
	 */
	public Collection<VehicleRoute> getRoutes() {
		return routes;
	}

	/**
	 * Returns cost of this solution.
	 * 
	 * @return costs
	 */
	public double getCost() {
		return cost;
	}
	
	/**
	 * Sets the costs of this solution.
	 * 
	 * @param cost the cost to assigned to this solution
	 */
	public void setCost(double cost){
		this.cost = cost;
	}

    /**
     * Returns bad jobs, i.e. jobs that are not assigned to any vehicle route.
     *
     * @return bad jobs
     */
    public Collection<Job> getUnassignedJobs(){
        return unassignedJobs;
    }

	@Override
	public String toString() {
		return "[costs=" + cost + "][routes="+routes.size()+"][unassigned="+unassignedJobs.size()+"]";
	}
}
