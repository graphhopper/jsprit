/*******************************************************************************
 * Copyright (c) 2011 Stefan Schroeder.
 * eMail: stefan.schroeder@kit.edu
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
/* *********************************************************************** *
 * project: org.matsim.*
 * IniSolution.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2011 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package algorithms;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import basics.Job;
import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblemSolution;
import basics.route.DriverImpl;
import basics.route.TourActivities;
import basics.route.Vehicle;
import basics.route.VehicleRoute;



public final class BestInsertionInitialSolutionFactory implements InitialSolutionFactory {

	private static final Logger logger = Logger.getLogger(BestInsertionInitialSolutionFactory.class);

	private final InsertionStrategy insertion;
	
	private boolean generateAsMuchAsRoutesAsVehiclesExist = false;
	
	public void setGenerateAsMuchAsRoutesAsVehiclesExist(boolean generateAsMuchAsRoutesAsVehiclesExist) {
		this.generateAsMuchAsRoutesAsVehiclesExist = generateAsMuchAsRoutesAsVehiclesExist;
	}

	public BestInsertionInitialSolutionFactory(InsertionStrategy insertionStrategy) {
		super();
		this.insertion = insertionStrategy;
	}

	@Override
	public VehicleRoutingProblemSolution createSolution(final VehicleRoutingProblem vrp) {
		logger.info("create initial solution.");
		List<VehicleRoute> vehicleRoutes = new ArrayList<VehicleRoute>();
		if(generateAsMuchAsRoutesAsVehiclesExist){
			for(Vehicle vehicle : vrp.getVehicles()){
				vehicleRoutes.add(VehicleRoute.newInstance(TourActivities.emptyTour(), DriverImpl.noDriver(), vehicle));
			}
		}
		insertion.insertJobs(vehicleRoutes, getUnassignedJobs(vrp));
		double totalCost = getTotalCost(vehicleRoutes);
		logger.info("creation done");
		return new VehicleRoutingProblemSolution(vehicleRoutes, totalCost);
	}

	private double getTotalCost(List<VehicleRoute> serviceProviders) {
		double c = 0.0;
		for(VehicleRoute a : serviceProviders){
			c += a.getCost();
		}
		return c;
	}

	private List<Job> getUnassignedJobs(VehicleRoutingProblem vrp) {
		List<Job> jobs = new ArrayList<Job>(vrp.getJobs().values());
		return jobs;
	}

}
