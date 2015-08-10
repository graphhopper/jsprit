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
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.route.activity.TimeWindow;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.util.Coordinate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class LuiShenReader {

	private static Logger logger = LogManager.getLogger(LuiShenReader.class);
	
	private final VehicleRoutingProblem.Builder vrpBuilder;

	private double coordProjectionFactor = 1;

	public LuiShenReader(VehicleRoutingProblem.Builder vrpBuilder) {
		super();
		this.vrpBuilder = vrpBuilder;
	}
	
	/**
	 * Reads input files to build Liu Shen problem.
	 * 
	 * <p>The instance-file is a solomon file. The vehicle-file is a
	 * txt-file that has the following columns:
	 * <p>Vehicle;Capacity;Cost_a;Cost_b;Cost_c
	 * <p>Concrete vehicleType:
	 * <p>A;100;300;60;30
	 * 
	 * <p>In the example above, the vehicle-type with typeId A has
	 * a capacity of 100, and fixed costs of 100 in cost scenario "a",
	 * 300 in "b" and 30 in "c". 

	 * @param instanceFile is a solomon-instance-file
	 * @param vehicleFile
	 * @param costScenario is either "a", "b" or "c"
	 */
	public void read(String instanceFile, String vehicleFile, String costScenario){
		vrpBuilder.setFleetSize(FleetSize.INFINITE);
		BufferedReader reader = getReader(instanceFile);
		int counter = 0;
		String line = null;
		while((line = readLine(reader)) != null){
			line = line.replace("\r", "");
			line = line.trim();
			String[] tokens = line.split(" +");
			counter++;
			if(counter > 9){
                if(tokens.length < 7) continue;
				Coordinate coord = makeCoord(tokens[1],tokens[2]);
				String customerId = tokens[0];
				int demand = Integer.parseInt(tokens[3]);
				double start = Double.parseDouble(tokens[4])*coordProjectionFactor ;
				double end = Double.parseDouble(tokens[5])*coordProjectionFactor;
				double serviceTime = Double.parseDouble(tokens[6])*coordProjectionFactor;
				if(counter == 10){
					createVehicles(vehicleFile,costScenario,customerId,coord,start,end);
				}
				else{
					Service service = Service.Builder.newInstance("" + counter).addSizeDimension(0, demand)
							.setLocation(Location.Builder.newInstance().setCoordinate(coord).setId(customerId).build()).setServiceTime(serviceTime)
							.setTimeWindow(TimeWindow.newInstance(start, end)).build();
					vrpBuilder.addJob(service);
				}
			}
		}
		close(reader);
	}

	private void createVehicles(String vehicleFileName, String costScenario, String locationId, Coordinate coord, double start, double end) {
		BufferedReader reader = getReader(vehicleFileName);
		
		int costScenarioColumn = getCostScenarioColumn(costScenario);
		int vehicleIdColumn = 0;
		int capacityColumn = 1;
		
		
		
		boolean firstLine = true;
		String line = null;
		while((line = readLine(reader)) != null){
			if(firstLine){
				firstLine = false;
				continue;
			}
			String[] tokens = line.split(";");
			String vehicleId = tokens[vehicleIdColumn];
			int capacity = Integer.parseInt(tokens[capacityColumn]);
			int fixCost = Integer.parseInt(tokens[costScenarioColumn]);
			
			VehicleTypeImpl.Builder typeBuilder = VehicleTypeImpl.Builder.newInstance(vehicleId).addCapacityDimension(0, capacity);
			typeBuilder.setFixedCost(fixCost).setCostPerDistance(1.0);
			
			VehicleTypeImpl type = typeBuilder.build();
			
			VehicleImpl reprVehicle = VehicleImpl.Builder.newInstance(vehicleId).setEarliestStart(start).setLatestArrival(end).
					setStartLocation(Location.Builder.newInstance().setId(locationId).setCoordinate(coord).build())
					.setType(type).build();
			
			vrpBuilder.addVehicle(reprVehicle);
			
		}
		close(reader);	
	}
	
	private int getCostScenarioColumn(String costScenario) {
		if(costScenario.equals("a")){
			return 2;
		}
		else if(costScenario.equals("b")){
			return 3;
		}
		else if(costScenario.equals("c")){
			return 4;
		}
		throw new IllegalStateException("costScenario " + costScenario + " not known");
	}
	
	public void setCoordProjectionFactor(double coordProjectionFactor) {
		this.coordProjectionFactor = coordProjectionFactor;
	}

	private void close(BufferedReader reader)  {
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Exception:", e);
			System.exit(1);
		}
	}

	private String readLine(BufferedReader reader) {
		try {
			return reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Exception:", e);
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
			logger.error("Exception:", e1);
			System.exit(1);
		}
		return reader;
	}
}
