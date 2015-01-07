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
package jsprit.core.algorithm.module;

import jsprit.core.algorithm.SearchStrategyModule;
import jsprit.core.algorithm.listener.SearchStrategyModuleListener;
import jsprit.core.algorithm.recreate.InsertionStrategy;
import jsprit.core.algorithm.recreate.listener.InsertionListener;
import jsprit.core.algorithm.ruin.RuinStrategy;
import jsprit.core.algorithm.ruin.listener.RuinListener;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


public class RuinAndRecreateModule implements SearchStrategyModule{

	private InsertionStrategy insertion;
	
	private RuinStrategy ruin;
	
	private String moduleName;
	
	public RuinAndRecreateModule(String moduleName, InsertionStrategy insertion, RuinStrategy ruin) {
		super();
		this.insertion = insertion;
		this.ruin = ruin;
		this.moduleName = moduleName;
	}

	@Override
	public VehicleRoutingProblemSolution runAndGetSolution(VehicleRoutingProblemSolution vrpSolution) {
		Collection<Job> ruinedJobs = ruin.ruin(vrpSolution.getRoutes());
        Set<Job> ruinedJobSet = new HashSet<Job>();
        ruinedJobSet.addAll(ruinedJobs);
        ruinedJobSet.addAll(vrpSolution.getUnassignedJobs());
		Collection<Job> unassignedJobs = insertion.insertJobs(vrpSolution.getRoutes(), ruinedJobSet);
        vrpSolution.getUnassignedJobs().clear();
        vrpSolution.getUnassignedJobs().addAll(unassignedJobs);
		return vrpSolution;

	}

	@Override
	public String getName() {
		return moduleName;
	}

	@Override
	public void addModuleListener(SearchStrategyModuleListener moduleListener) {
		if(moduleListener instanceof InsertionListener){
			InsertionListener iListener = (InsertionListener) moduleListener; 
			if(!insertion.getListeners().contains(iListener)){
				insertion.addListener(iListener);
			}
		}
		if(moduleListener instanceof RuinListener){
			RuinListener rListener = (RuinListener) moduleListener;
			if(!ruin.getListeners().contains(rListener)){
				ruin.addListener(rListener);
			}
		}
		
	}

	public InsertionStrategy getInsertion() {
		return insertion;
	}

	public RuinStrategy getRuin() {
		return ruin;
	}
}
