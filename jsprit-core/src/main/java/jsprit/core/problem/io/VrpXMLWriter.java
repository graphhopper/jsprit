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
package jsprit.core.problem.io;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.solution.route.activity.TourActivity.JobActivity;
import jsprit.core.problem.vehicle.PenaltyVehicleType;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleType;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class VrpXMLWriter {
	
	static class XMLConf extends XMLConfiguration {
		
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Document createDoc() throws ConfigurationException{
			Document doc = createDocument();
			return doc;
		}
	}
	
	private Logger log = Logger.getLogger(VrpXMLWriter.class);
	
	private VehicleRoutingProblem vrp;
	
	private Collection<VehicleRoutingProblemSolution> solutions;
	
	public VrpXMLWriter(VehicleRoutingProblem vrp, Collection<VehicleRoutingProblemSolution> solutions) {
		this.vrp = vrp;
		this.solutions = solutions;
	}
	
	public VrpXMLWriter(VehicleRoutingProblem vrp) {
		this.vrp = vrp;
		this.solutions = null;
	}
	
	private static Logger logger = Logger.getLogger(VrpXMLWriter.class);
	
	public void write(String filename){
		if(!filename.endsWith(".xml")) filename+=".xml";
		log.info("write vrp to " + filename);
		XMLConf xmlConfig = new XMLConf();
		xmlConfig.setFileName(filename);
		xmlConfig.setRootElementName("problem");
		xmlConfig.setAttributeSplittingDisabled(true);
		xmlConfig.setDelimiterParsingDisabled(true);
		
		
		writeProblemType(xmlConfig);
		writeVehiclesAndTheirTypes(xmlConfig);
		writeServices(xmlConfig);
		writeShipments(xmlConfig);
		writeSolutions(xmlConfig);
		
		OutputFormat format = new OutputFormat();
		format.setIndenting(true);
		format.setIndent(5);
		
		try {
			Document document = xmlConfig.createDoc();
			
			Element element = document.getDocumentElement();
			element.setAttribute("xmlns", "http://www.w3schools.com");
			element.setAttribute("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance");
			element.setAttribute("xsi:schemaLocation","http://www.w3schools.com vrp_xml_schema.xsd");
						
		} catch (ConfigurationException e) {
			logger.error(e);
			e.printStackTrace();
			System.exit(1);
		} 
		
		try {
			Writer out = new FileWriter(filename);
			XMLSerializer serializer = new XMLSerializer(out, format);
			serializer.serialize(xmlConfig.getDocument());
		} catch (IOException e) {
			logger.error(e);
			e.printStackTrace();
			System.exit(1);
		}
		
		
	}

	private void writeSolutions(XMLConf xmlConfig) {
		if(solutions == null) return;
		String solutionPath = "solutions.solution";
		int counter = 0;
		for(VehicleRoutingProblemSolution solution : solutions){
			xmlConfig.setProperty(solutionPath + "(" + counter + ").cost", solution.getCost());
			int routeCounter = 0;
			for(VehicleRoute route : solution.getRoutes()){
//				xmlConfig.setProperty(solutionPath + "(" + counter + ").routes.route(" + routeCounter + ").cost", route.getCost());
				xmlConfig.setProperty(solutionPath + "(" + counter + ").routes.route(" + routeCounter + ").driverId", route.getDriver().getId());
				xmlConfig.setProperty(solutionPath + "(" + counter + ").routes.route(" + routeCounter + ").vehicleId", route.getVehicle().getId());
				xmlConfig.setProperty(solutionPath + "(" + counter + ").routes.route(" + routeCounter + ").start", route.getStart().getEndTime());
				int actCounter = 0;
				for(TourActivity act : route.getTourActivities().getActivities()){
					xmlConfig.setProperty(solutionPath + "(" + counter + ").routes.route(" + routeCounter + ").act("+actCounter+")[@type]", act.getName());
					if(act instanceof JobActivity){
						Job job = ((JobActivity) act).getJob();
						if(job instanceof Service){
							xmlConfig.setProperty(solutionPath + "(" + counter + ").routes.route(" + routeCounter + ").act("+actCounter+").serviceId", job.getId());
						}
						else if(job instanceof Shipment){
							xmlConfig.setProperty(solutionPath + "(" + counter + ").routes.route(" + routeCounter + ").act("+actCounter+").shipmentId", job.getId());
						}
						else{
							throw new IllegalStateException("cannot write solution correctly since job-type is not know. make sure you use either service or shipment, or another writer");
						}
					}
					xmlConfig.setProperty(solutionPath + "(" + counter + ").routes.route(" + routeCounter + ").act("+actCounter+").arrTime", act.getArrTime());
					xmlConfig.setProperty(solutionPath + "(" + counter + ").routes.route(" + routeCounter + ").act("+actCounter+").endTime", act.getEndTime());
					actCounter++;
				}
				xmlConfig.setProperty(solutionPath + "(" + counter + ").routes.route(" + routeCounter + ").end", route.getEnd().getArrTime());
				routeCounter++;
			}
			counter++;
		}
	}

	private void writeServices(XMLConf xmlConfig) {
		String shipmentPathString = "services.service";
		int counter = 0;
		for(Job j : vrp.getJobs().values()){
			if(!(j instanceof Service)) continue;
			Service service = (Service) j;
			xmlConfig.setProperty(shipmentPathString + "("+counter+")[@id]", service.getId());
			xmlConfig.setProperty(shipmentPathString + "("+counter+")[@type]", service.getType());
			if(service.getLocationId() != null) xmlConfig.setProperty(shipmentPathString + "("+counter+").locationId", service.getLocationId());
			if(service.getCoord() != null) {
				xmlConfig.setProperty(shipmentPathString + "("+counter+").coord[@x]", service.getCoord().getX());
				xmlConfig.setProperty(shipmentPathString + "("+counter+").coord[@y]", service.getCoord().getY());
			}
			xmlConfig.setProperty(shipmentPathString + "("+counter+").capacity-demand", service.getCapacityDemand());
			xmlConfig.setProperty(shipmentPathString + "("+counter+").duration", service.getServiceDuration());
			xmlConfig.setProperty(shipmentPathString + "("+counter+").timeWindows.timeWindow(0).start", service.getTimeWindow().getStart());
			xmlConfig.setProperty(shipmentPathString + "("+counter+").timeWindows.timeWindow(0).end", service.getTimeWindow().getEnd());
			
			counter++;
		}
	}
	
	private void writeShipments(XMLConf xmlConfig) {
		String shipmentPathString = "shipments.shipment";
		int counter = 0;
		for(Job j : vrp.getJobs().values()){
			if(!(j instanceof Shipment)) continue;
			Shipment shipment = (Shipment) j;
			xmlConfig.setProperty(shipmentPathString + "("+counter+")[@id]", shipment.getId());
//			xmlConfig.setProperty(shipmentPathString + "("+counter+")[@type]", service.getType());
			if(shipment.getPickupLocation() != null) xmlConfig.setProperty(shipmentPathString + "("+counter+").pickup.locationId", shipment.getPickupLocation());
			if(shipment.getPickupCoord() != null) {
				xmlConfig.setProperty(shipmentPathString + "("+counter+").pickup.coord[@x]", shipment.getPickupCoord().getX());
				xmlConfig.setProperty(shipmentPathString + "("+counter+").pickup.coord[@y]", shipment.getPickupCoord().getY());
			}
			
			xmlConfig.setProperty(shipmentPathString + "("+counter+").pickup.duration", shipment.getPickupServiceTime());
			xmlConfig.setProperty(shipmentPathString + "("+counter+").pickup.timeWindows.timeWindow(0).start", shipment.getPickupTimeWindow().getStart());
			xmlConfig.setProperty(shipmentPathString + "("+counter+").pickup.timeWindows.timeWindow(0).end", shipment.getPickupTimeWindow().getEnd());
			
			
			if(shipment.getDeliveryLocation() != null) xmlConfig.setProperty(shipmentPathString + "("+counter+").delivery.locationId", shipment.getDeliveryLocation());
			if(shipment.getDeliveryCoord() != null) {
				xmlConfig.setProperty(shipmentPathString + "("+counter+").delivery.coord[@x]", shipment.getDeliveryCoord().getX());
				xmlConfig.setProperty(shipmentPathString + "("+counter+").delivery.coord[@y]", shipment.getDeliveryCoord().getY());
			}
			
			xmlConfig.setProperty(shipmentPathString + "("+counter+").delivery.duration", shipment.getDeliveryServiceTime());
			xmlConfig.setProperty(shipmentPathString + "("+counter+").delivery.timeWindows.timeWindow(0).start", shipment.getDeliveryTimeWindow().getStart());
			xmlConfig.setProperty(shipmentPathString + "("+counter+").delivery.timeWindows.timeWindow(0).end", shipment.getDeliveryTimeWindow().getEnd());
			
			
			xmlConfig.setProperty(shipmentPathString + "("+counter+").capacity-demand", shipment.getCapacityDemand());
			counter++;
		}
	}
	
	private void writeProblemType(XMLConfiguration xmlConfig){
		xmlConfig.setProperty("problemType.fleetSize", vrp.getFleetSize());
		xmlConfig.setProperty("problemType.fleetComposition", vrp.getFleetComposition());
	}

	private void writeVehiclesAndTheirTypes(XMLConfiguration xmlConfig) {

		//vehicles
		String vehiclePathString = new StringBuilder().append(Schema.VEHICLES).append(".").
				append(Schema.VEHICLE).toString();
		int counter = 0;
		for(Vehicle vehicle : vrp.getVehicles()){
			if(vehicle.getType() instanceof PenaltyVehicleType){
				xmlConfig.setProperty(vehiclePathString + "("+counter+")[@type]", "penalty");
			}
			xmlConfig.setProperty(vehiclePathString + "("+counter+").id", vehicle.getId());
			xmlConfig.setProperty(vehiclePathString + "("+counter+").typeId", vehicle.getType().getTypeId());
			xmlConfig.setProperty(vehiclePathString + "("+counter+").location.id", vehicle.getLocationId());
			if(vehicle.getCoord() != null){
				xmlConfig.setProperty(vehiclePathString + "("+counter+").location.coord[@x]", vehicle.getCoord().getX());
				xmlConfig.setProperty(vehiclePathString + "("+counter+").location.coord[@y]", vehicle.getCoord().getY());
			}
			xmlConfig.setProperty(vehiclePathString + "("+counter+").timeSchedule.start", vehicle.getEarliestDeparture());
			xmlConfig.setProperty(vehiclePathString + "("+counter+").timeSchedule.end", vehicle.getLatestArrival());

			xmlConfig.setProperty(vehiclePathString + "("+counter+").returnToDepot", vehicle.isReturnToDepot());
			counter++;
		}

		//types
		String typePathString = Schema.builder().append(Schema.TYPES).dot(Schema.TYPE).build();
		int typeCounter = 0;
		for(VehicleType type : vrp.getTypes()){
			if(type instanceof PenaltyVehicleType){
				xmlConfig.setProperty(typePathString + "("+typeCounter+")[@type]", "penalty");
				xmlConfig.setProperty(typePathString + "("+typeCounter+")[@penaltyFactor]", ((PenaltyVehicleType)type).getPenaltyFactor());
			}
			xmlConfig.setProperty(typePathString + "("+typeCounter+").id", type.getTypeId());
			xmlConfig.setProperty(typePathString + "("+typeCounter+").capacity", type.getCapacity());
			xmlConfig.setProperty(typePathString + "("+typeCounter+").costs.fixed", type.getVehicleCostParams().fix);
			xmlConfig.setProperty(typePathString + "("+typeCounter+").costs.distance", type.getVehicleCostParams().perDistanceUnit);
			xmlConfig.setProperty(typePathString + "("+typeCounter+").costs.time", type.getVehicleCostParams().perTimeUnit);
			typeCounter++;
		}


		
		
	}

}
