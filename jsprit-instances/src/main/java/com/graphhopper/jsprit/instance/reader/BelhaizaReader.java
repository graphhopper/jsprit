/*
 * Licensed to GraphHopper GmbH under one or more contributor
 * license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * GraphHopper GmbH licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.graphhopper.jsprit.instance.reader;


import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


/**
 * Reader that reads the well-known solomon-instances.
 *
 * <p>See: <a href="http://neo.lcc.uma.es/vrp/vrp-instances/capacitated-vrp-with-time-windows-instances/">neo.org</a>
 *
 * @author stefan
 *
 */

public class BelhaizaReader {

    private int fixedCosts;

    /**
	 * @param costProjectionFactor the costProjectionFactor to set
	 */
	public void setVariableCostProjectionFactor(double costProjectionFactor) {
		this.variableCostProjectionFactor = costProjectionFactor;
	}

	private static Logger logger = LoggerFactory.getLogger(BelhaizaReader.class);

	private final VehicleRoutingProblem.Builder vrpBuilder;

	private double coordProjectionFactor = 1;

	private double timeProjectionFactor = 1;

	private double variableCostProjectionFactor = 1;

	private double fixedCostPerVehicle = 0.0;

	public BelhaizaReader(VehicleRoutingProblem.Builder vrpBuilder) {
		super();
		this.vrpBuilder = vrpBuilder;
	}

	public BelhaizaReader(VehicleRoutingProblem.Builder vrpBuilder, double fixedCostPerVehicle) {
		super();
		this.vrpBuilder = vrpBuilder;
		this.fixedCostPerVehicle=fixedCostPerVehicle;
	}

	public void read(String solomonFile){
		vrpBuilder.setFleetSize(FleetSize.INFINITE);
		BufferedReader reader = getReader(solomonFile);
		int vehicleCapacity = 0;
		int counter = 0;
		String line;
		while((line = readLine(reader)) != null){
			String[] tokens = line.replace("\r", "").trim().split("\\s+");
			counter++;
			if(counter == 2){
				vehicleCapacity = Integer.parseInt(tokens[1]);
				continue;
			}
			if(counter > 2){
				if(tokens.length < 7) continue;
                Coordinate coord = makeCoord(tokens[1],tokens[2]);
				String customerId = tokens[0];
				int demand = Integer.parseInt(tokens[4]);
				double serviceTime = Double.parseDouble(tokens[3])*timeProjectionFactor;
				if(counter == 3){
					VehicleTypeImpl.Builder typeBuilder = VehicleTypeImpl.Builder.newInstance("solomonType").addCapacityDimension(0, vehicleCapacity);
					typeBuilder.setCostPerDistance(1.0*variableCostProjectionFactor).setFixedCost(fixedCostPerVehicle)
                    .setCostPerWaitingTime(0.8);
                    System.out.println("fix: " + fixedCostPerVehicle + "; perDistance: 1.0; perWaitingTime: 0.8");
                    VehicleTypeImpl vehicleType = typeBuilder.build();
					double end = Double.parseDouble(tokens[8])*timeProjectionFactor;
					VehicleImpl vehicle = VehicleImpl.Builder.newInstance("solomonVehicle").setEarliestStart(0.).setLatestArrival(end)
							.setStartLocation(Location.Builder.newInstance().setId(customerId)
										.setCoordinate(coord).build()).setType(vehicleType).build();
					vrpBuilder.addVehicle(vehicle);
				}
				else{
					Service.Builder serviceBuilder = Service.Builder.newInstance(customerId);
					serviceBuilder.addSizeDimension(0, demand).setLocation(Location.Builder.newInstance().setCoordinate(coord).setId(customerId).build()).setServiceTime(serviceTime);
					int noTimeWindows = Integer.parseInt(tokens[7]);
					for(int i=0;i<noTimeWindows*2;i=i+2){
						double earliest = Double.parseDouble(tokens[8+i]);
						double latest = Double.parseDouble(tokens[8+i+1]);
						serviceBuilder.addTimeWindow(earliest,latest);
					}
					vrpBuilder.addJob(serviceBuilder.build());
				}
			}
		}
		close(reader);
	}

	public void setCoordProjectionFactor(double coordProjectionFactor) {
		this.coordProjectionFactor = coordProjectionFactor;
	}

	private void close(BufferedReader reader)  {
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e.toString());
			System.exit(1);
		}
	}

	private String readLine(BufferedReader reader) {
		try {
			return reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e.toString());
			System.exit(1);
			return null;
		}
	}

	private Coordinate makeCoord(String xString, String yString) {
		double x = Double.parseDouble(xString);
		double y = Double.parseDouble(yString);
		return new Coordinate(x*coordProjectionFactor,y*coordProjectionFactor);
	}

	private BufferedReader getReader(String solomonFile) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(solomonFile));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			logger.error(e1.toString());
			System.exit(1);
		}
		return reader;
	}

	public void setTimeProjectionFactor(double timeProjection) {
		this.timeProjectionFactor=timeProjection;

	}

    public void setFixedCosts(int fixedCosts) {
        this.fixedCostPerVehicle = fixedCosts;
    }
}
