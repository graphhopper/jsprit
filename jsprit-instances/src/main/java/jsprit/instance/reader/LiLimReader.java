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
package jsprit.instance.reader;


import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.VehicleRoutingProblem.Builder;
import jsprit.core.problem.job.Shipment;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * test instances for the capacitated vrp with pickup and deliveries and time windows. 
 * instances are from li and lim and can be found at:
 * http://www.top.sintef.no/vrp/benchmarks.html
 * @author stefan schroeder
 *
 */


public class LiLimReader {
	
	static class CustomerData{
		public Coordinate coord;
		public double start;
		public double end;
		public double serviceTime;
		
		public CustomerData(Coordinate coord, double start, double end, double serviceTime) {
			super();
			this.coord = coord;
			this.start = start;
			this.end = end;
			this.serviceTime = serviceTime;
		}
	}
	
	static class Relation{
		public String from;
		public String to;
		public int demand;
		public Relation(String from, String to, int demand) {
			super();
			this.from = from;
			this.to = to;
			this.demand = demand;
		}
		
	}
	
	private static Logger logger = LogManager.getLogger(LiLimReader.class);
	
	private VehicleRoutingProblem.Builder vrpBuilder;
	
	private int vehicleCapacity;
	
	private String depotId;
	
	private Map<String,CustomerData> customers;
	
	private Collection<Relation> relations;
	
	private double depotOpeningTime;

	private double depotClosingTime;

	private int fixCosts = 0;
	
	public LiLimReader(Builder vrpBuilder) {
		customers = new HashMap<String, LiLimReader.CustomerData>();
		relations = new ArrayList<LiLimReader.Relation>();
		this.vrpBuilder = vrpBuilder;
	}
	
	public LiLimReader(Builder builder, int fixCosts) {
		customers = new HashMap<String, LiLimReader.CustomerData>();
		relations = new ArrayList<LiLimReader.Relation>();
		this.vrpBuilder = builder;
		this.fixCosts = fixCosts;
	}

	public void read(String filename){
		readShipments(filename);
		buildShipments();
		VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0, vehicleCapacity)
				.setCostPerDistance(1.0).setFixedCost(fixCosts).build();
		VehicleImpl vehicle = VehicleImpl.Builder.newInstance("vehicle")
				.setEarliestStart(depotOpeningTime).setLatestArrival(depotClosingTime)
				.setStartLocation(Location.Builder.newInstance().setCoordinate(customers.get(depotId).coord).build()).setType(type).build();
		vrpBuilder.addVehicle(vehicle);
	}
	
	private void buildShipments() {
		Integer counter = 0;
		for(Relation rel : relations){
			counter++;
			String from = rel.from;
			String to = rel.to;
			int demand = rel.demand;
			Shipment s = Shipment.Builder.newInstance(counter.toString()).addSizeDimension(0, demand)
					.setPickupLocation(Location.Builder.newInstance().setCoordinate(customers.get(from).coord).build()).setPickupServiceTime(customers.get(from).serviceTime)
					.setPickupTimeWindow(TimeWindow.newInstance(customers.get(from).start, customers.get(from).end))
					.setDeliveryLocation(Location.Builder.newInstance().setCoordinate(customers.get(to).coord).build()).setDeliveryServiceTime(customers.get(to).serviceTime)
					.setDeliveryTimeWindow(TimeWindow.newInstance(customers.get(to).start, customers.get(to).end)).build();
			vrpBuilder.addJob(s);
		}
		
	}
	
	private BufferedReader getReader(String file) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			logger.error("Exception:", e1);
			System.exit(1);
		}
		return reader;
	}

	private void readShipments(String file) {
		BufferedReader reader = getReader(file);
		String line = null;
		boolean firstLine = true;
		try {
			while((line = reader.readLine()) != null){
				line = line.replace("\r", "");
				line = line.trim();
				String[] tokens = line.split("\t");
				if(firstLine){
					int vehicleCapacity = getInt(tokens[1]);
					this.vehicleCapacity = vehicleCapacity;
					firstLine = false;
					continue;
				}
				else{
					String customerId = tokens[0];
					Coordinate coord = makeCoord(tokens[1], tokens[2]);
					int demand = getInt(tokens[3]);
					double startTimeWindow = getDouble(tokens[4]);
					double endTimeWindow = getDouble(tokens[5]);
					double serviceTime = getDouble(tokens[6]);
//					vrpBuilder.addLocation(customerId, coord);
					customers.put(customerId, new CustomerData(coord,startTimeWindow,endTimeWindow, serviceTime));
					if(customerId.equals("0")){
						depotId = customerId;
						depotOpeningTime = startTimeWindow;
						depotClosingTime = endTimeWindow;
					}
					if(demand > 0){
						relations.add(new Relation(customerId,tokens[8],demand));
					}
				}
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private Coordinate makeCoord(String xString, String yString) {
		double x = Double.parseDouble(xString);
		double y = Double.parseDouble(yString);
		return new Coordinate(x,y);
	}
	
	private double getDouble(String string) {
		return Double.parseDouble(string);
	}

	private int getInt(String string) {
		return Integer.parseInt(string);
	}


}
