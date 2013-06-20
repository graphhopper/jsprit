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
package basics.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import util.Coordinate;
import util.Resource;
import basics.Service;
import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblem.FleetComposition;
import basics.VehicleRoutingProblem.FleetSize;
import basics.VehicleRoutingProblemSolution;
import basics.route.Driver;
import basics.route.DriverImpl;
import basics.route.End;
import basics.route.ServiceActivity;
import basics.route.Start;
import basics.route.TimeWindow;
import basics.route.Vehicle;
import basics.route.VehicleImpl;
import basics.route.VehicleImpl.VehicleBuilder;
import basics.route.VehicleRoute;
import basics.route.VehicleType;
import basics.route.VehicleTypeImpl;

public class VrpXMLReader{

	private static Logger logger = Logger.getLogger(VrpXMLReader.class);
	
	private VehicleRoutingProblem.Builder vrpBuilder;
	
	private Map<String,Vehicle> vehicleMap;

	private Map<String, Service> serviceMap; 
	
	private boolean schemaValidation = true;

	private Collection<VehicleRoutingProblemSolution> solutions;
	
	/**
	 * @param schemaValidation the schemaValidation to set
	 */
	public void setSchemaValidation(boolean schemaValidation) {
		this.schemaValidation = schemaValidation;
	}

	public VrpXMLReader(VehicleRoutingProblem.Builder vrpBuilder, Collection<VehicleRoutingProblemSolution> solutions){
		this.vrpBuilder = vrpBuilder;
		this.vehicleMap = new HashMap<String, Vehicle>();
		this.serviceMap = new HashMap<String, Service>();
		this.solutions = solutions;
	}
	
	public VrpXMLReader(VehicleRoutingProblem.Builder vrpBuilder){
		this.vrpBuilder = vrpBuilder;
		this.vehicleMap = new HashMap<String, Vehicle>();
		this.serviceMap = new HashMap<String, Service>();
		this.solutions = null;
	}
	
	public void read(String filename) {
		logger.info("read vrp from file " + filename);
		XMLConfiguration xmlConfig = new XMLConfiguration();
		xmlConfig.setFileName(filename);
		xmlConfig.setAttributeSplittingDisabled(true);
		xmlConfig.setDelimiterParsingDisabled(true);
		
		if(schemaValidation){
			final InputStream resource = Resource.getAsInputStream("vrp_xml_schema.xsd");
			if(resource != null) {
				EntityResolver resolver = new EntityResolver() {

					@Override
					public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
						{
							InputSource is = new InputSource(resource);
							return is;
						}
					}
				};
				xmlConfig.setEntityResolver(resolver);
				xmlConfig.setSchemaValidation(true);
				logger.info("validating " + filename + " with xsd-schema");
			}
			else{
				logger.warn("cannot find schema-xsd file (vrp_xml_schema.xsd). try to read xml without xml-file-validation.");
			}		
		}
		try {
			xmlConfig.load();
		} catch (ConfigurationException e) {
			logger.error(e);
			e.printStackTrace();
			System.exit(1);
		}
		readProblemType(xmlConfig);
		readVehiclesAndTheirTypes(xmlConfig);
		readServices(xmlConfig);
		readSolutions(xmlConfig);
	}

	private void readSolutions(XMLConfiguration vrpProblem) {
		if(solutions == null) return;
		List<HierarchicalConfiguration> solutionConfigs = vrpProblem.configurationsAt("solutions.solution");
		for(HierarchicalConfiguration solutionConfig : solutionConfigs){
			String totalCost = solutionConfig.getString("cost");
			double cost = -1;
			if(totalCost != null) cost = Double.parseDouble(totalCost);
			List<HierarchicalConfiguration> routeConfigs = solutionConfig.configurationsAt("routes.route");
			List<VehicleRoute> routes = new ArrayList<VehicleRoute>();
			for(HierarchicalConfiguration routeConfig : routeConfigs){
				String driverId = routeConfig.getString("driverId");
				//! here, driverId is set to noDriver, no matter whats in driverId.
				Driver driver = DriverImpl.noDriver();
				String vehicleId = routeConfig.getString("vehicleId");
				Vehicle vehicle = getVehicle(vehicleId);
				if(vehicle == null) throw new IllegalStateException("vehicle is missing.");
				String start = routeConfig.getString("start");
				if(start == null) throw new IllegalStateException("route start-time is missing.");
				String end = routeConfig.getString("end");
				if(end == null) throw new IllegalStateException("route end-time is missing.");
				Start startAct = Start.newInstance(vehicle.getLocationId(), vehicle.getEarliestDeparture(), vehicle.getLatestArrival());
				startAct.setEndTime(Double.parseDouble(start));
				End endAct = End.newInstance(vehicle.getLocationId(), vehicle.getEarliestDeparture(), vehicle.getLatestArrival());
				endAct.setArrTime(Double.parseDouble(end));
				VehicleRoute.Builder routeBuilder = VehicleRoute.Builder.newInstance(startAct, endAct);
				routeBuilder.setDriver(driver);
				routeBuilder.setVehicle(vehicle);
				List<HierarchicalConfiguration> actConfigs = routeConfig.configurationsAt("act");
				for(HierarchicalConfiguration actConfig : actConfigs){
					String type = actConfig.getString("[@type]");
					if(type == null) throw new IllegalStateException("act[@type] is missing.");
					String serviceId = actConfig.getString("serviceId");
					if(serviceId == null) throw new IllegalStateException("act.serviceId is missing.");
					Service service = getService(serviceId);
					String arrTime = actConfig.getString("arrTime");
					if(arrTime == null) throw new IllegalStateException("act.arrTime is missing.");
					String endTime = actConfig.getString("endTime");
					if(endTime == null) throw new IllegalStateException("act.endTime is missing.");
					ServiceActivity serviceActivity = ServiceActivity.newInstance(service);
					serviceActivity.setArrTime(Double.parseDouble(arrTime));
					serviceActivity.setEndTime(Double.parseDouble(endTime));
					routeBuilder.addActivity(serviceActivity);
				}
				routes.add(routeBuilder.build());
			}
			VehicleRoutingProblemSolution solution = new VehicleRoutingProblemSolution(routes, cost);
			solutions.add(solution);
		}
	}
	
	private Service getService(String serviceId) {
		return serviceMap.get(serviceId);
	}

	private Vehicle getVehicle(String vehicleId) {
		return vehicleMap.get(vehicleId);
	}

	private void readProblemType(XMLConfiguration vrpProblem) {
		String fleetSize = vrpProblem.getString("problemType.fleetSize");
		if(fleetSize == null) vrpBuilder.setFleetSize(FleetSize.INFINITE);
		else if(fleetSize.toUpperCase().equals(FleetSize.INFINITE.toString())) vrpBuilder.setFleetSize(FleetSize.INFINITE);
		else vrpBuilder.setFleetSize(FleetSize.FINITE);
		
		String fleetComposition = vrpProblem.getString("problemType.fleetComposition");
		if(fleetComposition == null) vrpBuilder.setFleetComposition(FleetComposition.HOMOGENEOUS);
		else if(fleetComposition.toUpperCase().equals(FleetComposition.HETEROGENEOUS.toString())){
			vrpBuilder.setFleetComposition(FleetComposition.HETEROGENEOUS);
		}
		else vrpBuilder.setFleetComposition(FleetComposition.HOMOGENEOUS);
		
	}

	private void readServices(XMLConfiguration vrpProblem) {
		List<HierarchicalConfiguration> serviceConfigs = vrpProblem.configurationsAt("services.service");
		for(HierarchicalConfiguration serviceConfig : serviceConfigs){
			String id = serviceConfig.getString("[@id]");
			if(id == null) throw new IllegalStateException("service[@id] is missing.");
			String name = serviceConfig.getString("[@type]");
			if(name == null) name = "service";
			String capacityDemand = serviceConfig.getString("capacity-demand");
			int cap = 0;
			if(capacityDemand != null) cap = Integer.parseInt(capacityDemand);
			Service.Builder builder = Service.Builder.newInstance(id, cap);
			builder.setName(name);
			String serviceLocationId = serviceConfig.getString("locationId");
			builder.setLocationId(serviceLocationId);
			Coordinate serviceCoord = null;
			if(serviceConfig.getString("coord[@x]") != null && serviceConfig.getString("coord[@y]") != null){
				double x = Double.parseDouble(serviceConfig.getString("coord[@x]"));
				double y = Double.parseDouble(serviceConfig.getString("coord[@y]"));
				serviceCoord = Coordinate.newInstance(x,y);
			}
			builder.setCoord(serviceCoord);
			if(serviceCoord != null){
				if(serviceLocationId != null){
					vrpBuilder.addLocation(serviceLocationId,serviceCoord);
				}
				else{
					vrpBuilder.addLocation(serviceCoord.toString(),serviceCoord);
					builder.setLocationId(serviceCoord.toString());
				}
			}
			
			
			if(serviceConfig.containsKey("duration")){
				builder.setServiceTime(serviceConfig.getDouble("duration"));
			}
			List<HierarchicalConfiguration> deliveryTWConfigs = serviceConfig.configurationsAt("timeWindows.timeWindow");
			if(!deliveryTWConfigs.isEmpty()){
				for(HierarchicalConfiguration twConfig : deliveryTWConfigs){
					builder.setTimeWindow(TimeWindow.newInstance(twConfig.getDouble("start"), twConfig.getDouble("end")));
				}
			}
			Service service = builder.build();
			serviceMap.put(service.getId(),service);
			vrpBuilder.addJob(service);

		}
	}

	private void readVehiclesAndTheirTypes(XMLConfiguration vrpProblem) {

		//read vehicle-types
		Map<String, VehicleTypeImpl> types = new HashMap<String, VehicleTypeImpl>();
		List<HierarchicalConfiguration> typeConfigs = vrpProblem.configurationsAt("vehicleTypes.type");
		for(HierarchicalConfiguration typeConfig : typeConfigs){
			String typeId = typeConfig.getString("id");
			Integer capacity = typeConfig.getInt("capacity");
			Double fix = typeConfig.getDouble("costs.fixed");
			Double timeC = typeConfig.getDouble("costs.time");
			Double distC = typeConfig.getDouble("costs.distance");
//			Double start = typeConfig.getDouble("timeSchedule.start");
//			Double end = typeConfig.getDouble("timeSchedule.end");
			if(typeId == null) throw new IllegalStateException("typeId is missing.");
			if(capacity == null) throw new IllegalStateException("capacity is missing.");
			VehicleTypeImpl.Builder typeBuilder = VehicleTypeImpl.Builder.newInstance(typeId, capacity);
			if(fix != null) typeBuilder.setFixedCost(fix);
			if(timeC != null) typeBuilder.setCostPerTime(timeC);
			if(distC != null) typeBuilder.setCostPerDistance(distC);
//			if(start != null && end != null) typeBuilder.setTimeSchedule(new TimeSchedule(start, end));
			VehicleTypeImpl type = typeBuilder.build();
			types.put(type.typeId, type);
			vrpBuilder.addVehicleType(type);
		}
		
		//read vehicles
		List<HierarchicalConfiguration> vehicleConfigs = vrpProblem.configurationsAt("vehicles.vehicle");
		boolean doNotWarnAgain = false;
		for(HierarchicalConfiguration vehicleConfig : vehicleConfigs){
			String vehicleId = vehicleConfig.getString("id");
			if(vehicleId == null) throw new IllegalStateException("vehicleId is missing.");
			VehicleBuilder builder = VehicleImpl.VehicleBuilder.newInstance(vehicleId);
			String typeId = vehicleConfig.getString("typeId");
			if(typeId == null) throw new IllegalStateException("typeId is missing.");
			VehicleTypeImpl type = types.get(typeId);
			if(type == null) throw new IllegalStateException("vehicleType with typeId " + typeId + " is missing.");
			builder.setType(type);
			String locationId = vehicleConfig.getString("location.id");
			if(locationId == null) throw new IllegalStateException("location.id is missing.");
			builder.setLocationId(locationId);
			String coordX = vehicleConfig.getString("location.coord[@x]");
			String coordY = vehicleConfig.getString("location.coord[@y]");
			if(coordX == null || coordY == null) {
				if(!doNotWarnAgain) {
					logger.warn("location.coord is missing. do not warn you again.");
					doNotWarnAgain = true;
				}
			}
			else{
				Coordinate coordinate = Coordinate.newInstance(Double.parseDouble(coordX), Double.parseDouble(coordY));
				builder.setLocationCoord(coordinate);
			}
			String start = vehicleConfig.getString("timeSchedule.start");
			String end = vehicleConfig.getString("timeSchedule.end");
			if(start != null) builder.setEarliestStart(Double.parseDouble(start));
			if(end != null) builder.setLatestArrival(Double.parseDouble(end));
			VehicleImpl vehicle = builder.build();
//			vehicleMap.put(vehicle.getId(), vehicle);
			vrpBuilder.addVehicle(vehicle);
			vehicleMap.put(vehicleId, vehicle);
		}

	}
	
	

}
