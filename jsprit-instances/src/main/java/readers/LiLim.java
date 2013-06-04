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
package readers;
//package instances;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.Map;
//
//import org.apache.log4j.Level;
//import org.apache.log4j.Logger;
//import org.matsim.contrib.freight.vrp.algorithms.rr.RuinAndRecreate;
//import org.matsim.contrib.freight.vrp.basics.Job;
//import org.matsim.contrib.freight.vrp.basics.Shipment;
//import org.matsim.contrib.freight.vrp.basics.VehicleRoutingProblem;
//import org.matsim.contrib.freight.vrp.basics.VrpBuilder;
//import org.matsim.contrib.freight.vrp.utils.Coordinate;
//import org.matsim.contrib.freight.vrp.utils.CrowFlyCosts;
//import org.matsim.contrib.freight.vrp.utils.Locations;
//import org.matsim.core.utils.io.IOUtils;
//
///**
// * test instances for the capacitated vrp with pickup and deliveries and time windows. 
// * instances are from li and lim and can be found at:
// * http://www.top.sintef.no/vrp/benchmarks.html
// * @author stefan schroeder
// *
// */
//
//
//public class LiLim {
//	
//	static class MyLocations implements Locations{
//
//		private Map<String,Coordinate> locations = new HashMap<String, Coordinate>();
//
//		public void addLocation(String id, Coordinate coord){
//			locations.put(id, coord);
//		}
//
//		@Override
//		public Coordinate getCoord(String id) {
//			return locations.get(id);
//		}	
//	}
//	
//	static class CustomerData{
//		public double start;
//		public double end;
//		public double serviceTime;
//		public CustomerData(double start, double end, double serviceTime) {
//			super();
//			this.start = start;
//			this.end = end;
//			this.serviceTime = serviceTime;
//		}
//	}
//	
//	static class Relation{
//		public String from;
//		public String to;
//		public int demand;
//		public Relation(String from, String to, int demand) {
//			super();
//			this.from = from;
//			this.to = to;
//			this.demand = demand;
//		}
//		
//	}
//	
//	private static Logger logger = Logger.getLogger(Christophides.class);
//	
//	private VrpBuilder vrpBuilder;
//	
////	private Locations locations;
//	
//	private String fileNameOfInstance;
//	
//	private int vehicleCapacity;
//	
//	private String depotId;
//	
//	private Map<String,CustomerData> data;
//	
//	private Collection<Relation> relations;
//	
//	private String instanceName;
//	public LiLim(String fileNameOfInstance, String instanceName) {
//		this.fileNameOfInstance = fileNameOfInstance;
//		this.instanceName = instanceName;
//		data = new HashMap<String, LiLim.CustomerData>();
//		relations = new ArrayList<LiLim.Relation>();
//	}
//	
//	public static void main(String[] args) {
//		Logger.getRootLogger().setLevel(Level.INFO);
//		LiLim liLim = new LiLim("/Users/stefan/Documents/workspace/VehicleRouting/instances/cvrppdtw_lilim/pdp100/lc205.txt", "lc205");
//		liLim.run();
//	}
//	
//	public void run(){
//		MyLocations myLocations = new MyLocations();
//		Collection<Job> jobs = new ArrayList<Job>();
//		readLocationsAndJobs(myLocations);
//		buildJobs(jobs);
//		VrpBuilder vrpBuilder = new VrpBuilder(new CrowFlyCosts(myLocations));
//		for(Job j : jobs){
//			vrpBuilder.addJob(j);
//		}
//		for(int i=0;i<20;i++){
//			vrpBuilder.addVehicle(VrpUtils.createVehicle("" + (i+1), depotId, vehicleCapacity, "standard",100.0,1.0,1.0));
//		}
//		RuinAndRecreate algo = createAlgo(vrpBuilder.build());
//		algo.run();
//	}
//	
//	private void buildJobs(Collection<Job> jobs) {
//		Integer counter = 0;
//		for(Relation rel : relations){
//			counter++;
//			String from = rel.from;
//			String to = rel.to;
//			Shipment s = VrpUtils.createShipment(counter.toString(), from, to, rel.demand, 
//					VrpUtils.createTimeWindow(data.get(from).start, data.get(from).end), 
//					VrpUtils.createTimeWindow(data.get(to).start, data.get(to).end));
//			s.setPickupServiceTime(data.get(from).serviceTime);
//			s.setDeliveryServiceTime(data.get(to).serviceTime);
//			jobs.add(s);
//		}
//		
//	}
//
//	private RuinAndRecreate createAlgo(VehicleRoutingProblem vrp) {
////		PickupAndDeliveryTourWithTimeWindowsAlgoFactory factory = new PickupAndDeliveryTourWithTimeWindowsAlgoFactory();
////		factory.setIterations(100);
////		factory.setWarmUp(10);
////		RuinAndRecreateChartListener chartListener = new RuinAndRecreateChartListener();
////		chartListener.setFilename("vrp/liLim/"+instanceName+".png");
////		RuinAndRecreateReport report = new RuinAndRecreateReport();
////		factory.addRuinAndRecreateListener(chartListener);
////		factory.addRuinAndRecreateListener(report);
////		return factory.createAlgorithm(vrp);
//		return null;
//	}
//
//	private void readLocationsAndJobs(MyLocations locs) {
//		BufferedReader reader = IOUtils.getBufferedReader(fileNameOfInstance);
//		String line = null;
//		boolean firstLine = true;
//		try {
//			while((line = reader.readLine()) != null){
//				line = line.replace("\r", "");
//				line = line.trim();
//				String[] tokens = line.split("\t");
//				if(firstLine){
//					int vehicleCapacity = getInt(tokens[1]);
//					this.vehicleCapacity = vehicleCapacity;
//					firstLine = false;
//					continue;
//				}
//				else{
//					String customerId = tokens[0];
//					Coordinate coord = makeCoord(tokens[1], tokens[2]);
//					if(customerId.equals("0")){
//						depotId = customerId;
//					}
//					int demand = getInt(tokens[3]);
//					double startTimeWindow = getDouble(tokens[4]);
//					double endTimeWindow = getDouble(tokens[5]);
//					double serviceTime = getDouble(tokens[6]);
//					locs.addLocation(customerId, coord);
//					data.put(customerId, new CustomerData(startTimeWindow,endTimeWindow,serviceTime));
//					if(demand > 0){
//						relations.add(new Relation(customerId,tokens[8],demand));
//					}
//				}
//			}
//			reader.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//	}
//	
//	private Coordinate makeCoord(String xString, String yString) {
//		double x = Double.parseDouble(xString);
//		double y = Double.parseDouble(yString);
//		return new Coordinate(x,y);
//	}
//	
//	private double getDouble(String string) {
//		return Double.parseDouble(string);
//	}
//
//	private int getInt(String string) {
//		return Integer.parseInt(string);
//	}
//
//
//}
