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
package jsprit.core.problem.io;

import jsprit.core.problem.Location;
import jsprit.core.problem.Skills;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.solution.route.activity.TourActivity.JobActivity;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleType;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class VrpXMLWriter {
	
	static class XMLConf extends XMLConfiguration {
		
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Document createDoc() throws ConfigurationException{
			return createDocument();
		}
	}
	
	private Logger log = LogManager.getLogger(VrpXMLWriter.class);
	
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
	
	private static Logger logger = LogManager.getLogger(VrpXMLWriter.class);
	
	public void write(String filename){
		if(!filename.endsWith(".xml")) filename+=".xml";
		log.info("write vrp: " + filename);
		XMLConf xmlConfig = new XMLConf();
		xmlConfig.setFileName(filename);
		xmlConfig.setRootElementName("problem");
		xmlConfig.setAttributeSplittingDisabled(true);
		xmlConfig.setDelimiterParsingDisabled(true);
		
		writeProblemType(xmlConfig);
		writeVehiclesAndTheirTypes(xmlConfig);
		
		//might be sorted?
		List<Job> jobs = new ArrayList<Job>();
		jobs.addAll(vrp.getJobs().values());
		for(VehicleRoute r : vrp.getInitialVehicleRoutes()){
			jobs.addAll(r.getTourActivities().getJobs());
		}
		
		writeServices(xmlConfig,jobs);
		writeShipments(xmlConfig,jobs);
		
		writeInitialRoutes(xmlConfig);
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
			logger.error("Exception:", e);
			e.printStackTrace();
			System.exit(1);
		} 
		
		try {
			Writer out = new FileWriter(filename);
			XMLSerializer serializer = new XMLSerializer(out, format);
			serializer.serialize(xmlConfig.getDocument());
			out.close();
		} catch (IOException e) {
			logger.error("Exception:", e);
			e.printStackTrace();
			System.exit(1);
		}
		
		
	}

	private void writeInitialRoutes(XMLConf xmlConfig) {
		if(vrp.getInitialVehicleRoutes().isEmpty()) return;
		String path = "initialRoutes.route";
		int routeCounter = 0;
		for(VehicleRoute route : vrp.getInitialVehicleRoutes()){
			xmlConfig.setProperty(path + "(" + routeCounter + ").driverId", route.getDriver().getId());
			xmlConfig.setProperty(path + "(" + routeCounter + ").vehicleId", route.getVehicle().getId());
			xmlConfig.setProperty(path + "(" + routeCounter + ").start", route.getStart().getEndTime());
			int actCounter = 0;
			for(TourActivity act : route.getTourActivities().getActivities()){
				xmlConfig.setProperty(path + "(" + routeCounter + ").act("+actCounter+")[@type]", act.getName());
				if(act instanceof JobActivity){
					Job job = ((JobActivity) act).getJob();
					if(job instanceof Service){
						xmlConfig.setProperty(path + "(" + routeCounter + ").act("+actCounter+").serviceId", job.getId());
					}
					else if(job instanceof Shipment){
						xmlConfig.setProperty(path + "(" + routeCounter + ").act("+actCounter+").shipmentId", job.getId());
					}
					else{
						throw new IllegalStateException("cannot write solution correctly since job-type is not know. make sure you use either service or shipment, or another writer");
					}
				}
				xmlConfig.setProperty(path + "(" + routeCounter + ").act("+actCounter+").arrTime", act.getArrTime());
				xmlConfig.setProperty(path + "(" + routeCounter + ").act("+actCounter+").endTime", act.getEndTime());
				actCounter++;
			}
			xmlConfig.setProperty(path + "(" + routeCounter + ").end", route.getEnd().getArrTime());
			routeCounter++;
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
            int unassignedJobCounter = 0;
            for(Job unassignedJob : solution.getUnassignedJobs()){
                xmlConfig.setProperty(solutionPath + "(" + counter + ").unassignedJobs.job(" + unassignedJobCounter + ")[@id]", unassignedJob.getId());
                unassignedJobCounter++;
            }
			counter++;
		}
	}

	private void writeServices(XMLConf xmlConfig, List<Job> jobs) {
		String shipmentPathString = "services.service";
		int counter = 0;
		for(Job j : jobs){
			if(!(j instanceof Service)) continue;
			Service service = (Service) j;
			xmlConfig.setProperty(shipmentPathString + "("+counter+")[@id]", service.getId());
			xmlConfig.setProperty(shipmentPathString + "("+counter+")[@type]", service.getType());
			if(service.getLocation().getId() != null) xmlConfig.setProperty(shipmentPathString + "("+counter+").location.id", service.getLocation().getId());
			if(service.getLocation().getCoordinate() != null) {
				xmlConfig.setProperty(shipmentPathString + "(" + counter + ").location.coord[@x]", service.getLocation().getCoordinate().getX());
				xmlConfig.setProperty(shipmentPathString + "("+counter+").location.coord[@y]", service.getLocation().getCoordinate().getY());
			}
            if(service.getLocation().getIndex() != Location.NO_INDEX){
                xmlConfig.setProperty(shipmentPathString + "(" + counter + ").location.index", service.getLocation().getIndex());
            }
			for(int i=0;i<service.getSize().getNuOfDimensions();i++){
				xmlConfig.setProperty(shipmentPathString + "("+counter+").capacity-dimensions.dimension("+i+")[@index]", i);
				xmlConfig.setProperty(shipmentPathString + "("+counter+").capacity-dimensions.dimension("+i+")", service.getSize().get(i));
			}
			xmlConfig.setProperty(shipmentPathString + "("+counter+").duration", service.getServiceDuration());
			xmlConfig.setProperty(shipmentPathString + "("+counter+").timeWindows.timeWindow(0).start", service.getTimeWindow().getStart());
			xmlConfig.setProperty(shipmentPathString + "("+counter+").timeWindows.timeWindow(0).end", service.getTimeWindow().getEnd());

            //skills
            String skillString = getSkillString(service);
            xmlConfig.setProperty(shipmentPathString + "("+counter+").requiredSkills", skillString);

            //name
            if(service.getName() != null){
                if(!service.getName().equals("no-name")){
                    xmlConfig.setProperty(shipmentPathString + "("+counter+").name", service.getName());
                }
            }
			counter++;
		}
	}
	
	private void writeShipments(XMLConf xmlConfig, List<Job> jobs) {
		String shipmentPathString = "shipments.shipment";
		int counter = 0;
		for(Job j : jobs){
			if(!(j instanceof Shipment)) continue;
			Shipment shipment = (Shipment) j;
			xmlConfig.setProperty(shipmentPathString + "("+counter+")[@id]", shipment.getId());
//			xmlConfig.setProperty(shipmentPathString + "("+counter+")[@type]", service.getType());
			if(shipment.getPickupLocation().getId() != null) xmlConfig.setProperty(shipmentPathString + "("+counter+").pickup.location.id", shipment.getPickupLocation().getId());
			if(shipment.getPickupLocation().getCoordinate() != null) {
				xmlConfig.setProperty(shipmentPathString + "("+counter+").pickup.location.coord[@x]", shipment.getPickupLocation().getCoordinate().getX());
				xmlConfig.setProperty(shipmentPathString + "("+counter+").pickup.location.coord[@y]", shipment.getPickupLocation().getCoordinate().getY());
			}
            if(shipment.getPickupLocation().getIndex() != Location.NO_INDEX){
                xmlConfig.setProperty(shipmentPathString + "(" + counter + ").pickup.location.index", shipment.getPickupLocation().getIndex());
            }
			
			xmlConfig.setProperty(shipmentPathString + "("+counter+").pickup.duration", shipment.getPickupServiceTime());
			xmlConfig.setProperty(shipmentPathString + "("+counter+").pickup.timeWindows.timeWindow(0).start", shipment.getPickupTimeWindow().getStart());
			xmlConfig.setProperty(shipmentPathString + "("+counter+").pickup.timeWindows.timeWindow(0).end", shipment.getPickupTimeWindow().getEnd());
			
			
			if(shipment.getDeliveryLocation().getId() != null) xmlConfig.setProperty(shipmentPathString + "("+counter+").delivery.location.id", shipment.getDeliveryLocation().getId());
			if(shipment.getDeliveryLocation().getCoordinate() != null) {
				xmlConfig.setProperty(shipmentPathString + "(" + counter + ").delivery.location.coord[@x]", shipment.getDeliveryLocation().getCoordinate().getX());
				xmlConfig.setProperty(shipmentPathString + "(" + counter + ").delivery.location.coord[@y]", shipment.getDeliveryLocation().getCoordinate().getY());
			}
            if(shipment.getDeliveryLocation().getIndex() != Location.NO_INDEX){
                xmlConfig.setProperty(shipmentPathString + "("+counter+").delivery.location.index", shipment.getDeliveryLocation().getIndex());
            }
			
			xmlConfig.setProperty(shipmentPathString + "("+counter+").delivery.duration", shipment.getDeliveryServiceTime());
			xmlConfig.setProperty(shipmentPathString + "("+counter+").delivery.timeWindows.timeWindow(0).start", shipment.getDeliveryTimeWindow().getStart());
			xmlConfig.setProperty(shipmentPathString + "("+counter+").delivery.timeWindows.timeWindow(0).end", shipment.getDeliveryTimeWindow().getEnd());
			
			for(int i=0;i<shipment.getSize().getNuOfDimensions();i++){
				xmlConfig.setProperty(shipmentPathString + "("+counter+").capacity-dimensions.dimension("+i+")[@index]", i);
				xmlConfig.setProperty(shipmentPathString + "("+counter+").capacity-dimensions.dimension("+i+")", shipment.getSize().get(i));
			}

            //skills
            String skillString = getSkillString(shipment);
            xmlConfig.setProperty(shipmentPathString + "("+counter+").requiredSkills", skillString);

            //name
            if(shipment.getName() != null){
                if(!shipment.getName().equals("no-name")){
                    xmlConfig.setProperty(shipmentPathString + "("+counter+").name", shipment.getName());
                }
            }
			counter++;
		}
	}
	
	private void writeProblemType(XMLConfiguration xmlConfig){
		xmlConfig.setProperty("problemType.fleetSize", vrp.getFleetSize());
	}

	private void writeVehiclesAndTheirTypes(XMLConfiguration xmlConfig) {

		//vehicles
		String vehiclePathString = Schema.VEHICLES + "." + Schema.VEHICLE;
		int counter = 0;
		for(Vehicle vehicle : vrp.getVehicles()){
			xmlConfig.setProperty(vehiclePathString + "("+counter+").id", vehicle.getId());
			xmlConfig.setProperty(vehiclePathString + "("+counter+").typeId", vehicle.getType().getTypeId());
			xmlConfig.setProperty(vehiclePathString + "("+counter+").startLocation.id", vehicle.getStartLocation().getId());
			if(vehicle.getStartLocation().getCoordinate() != null){
				xmlConfig.setProperty(vehiclePathString + "("+counter+").startLocation.coord[@x]", vehicle.getStartLocation().getCoordinate().getX());
				xmlConfig.setProperty(vehiclePathString + "("+counter+").startLocation.coord[@y]", vehicle.getStartLocation().getCoordinate().getY());
			}
            if(vehicle.getStartLocation().getIndex() != Location.NO_INDEX){
                xmlConfig.setProperty(vehiclePathString + "("+counter+").startLocation.index", vehicle.getStartLocation().getIndex());
            }

			xmlConfig.setProperty(vehiclePathString + "("+counter+").endLocation.id", vehicle.getEndLocation().getId());
			if(vehicle.getEndLocation().getCoordinate() != null){
				xmlConfig.setProperty(vehiclePathString + "("+counter+").endLocation.coord[@x]", vehicle.getEndLocation().getCoordinate().getX());
				xmlConfig.setProperty(vehiclePathString + "("+counter+").endLocation.coord[@y]", vehicle.getEndLocation().getCoordinate().getY());
			}
            if(vehicle.getEndLocation().getIndex() != Location.NO_INDEX){
                xmlConfig.setProperty(vehiclePathString + "("+counter+").endLocation.index", vehicle.getEndLocation().getId());
            }
			xmlConfig.setProperty(vehiclePathString + "("+counter+").timeSchedule.start", vehicle.getEarliestDeparture());
			xmlConfig.setProperty(vehiclePathString + "("+counter+").timeSchedule.end", vehicle.getLatestArrival());

			xmlConfig.setProperty(vehiclePathString + "("+counter+").returnToDepot", vehicle.isReturnToDepot());

            //write skills
            String skillString = getSkillString(vehicle);
            xmlConfig.setProperty(vehiclePathString + "("+counter+").skills", skillString);

			counter++;
		}

		//types
		String typePathString = Schema.builder().append(Schema.TYPES).dot(Schema.TYPE).build();
		int typeCounter = 0;
		for(VehicleType type : vrp.getTypes()){
			xmlConfig.setProperty(typePathString + "("+typeCounter+").id", type.getTypeId());
			
			for(int i=0;i<type.getCapacityDimensions().getNuOfDimensions();i++){
				xmlConfig.setProperty(typePathString + "("+typeCounter+").capacity-dimensions.dimension("+i+")[@index]", i);
				xmlConfig.setProperty(typePathString + "("+typeCounter+").capacity-dimensions.dimension("+i+")", type.getCapacityDimensions().get(i));
			}
			
			xmlConfig.setProperty(typePathString + "("+typeCounter+").costs.fixed", type.getVehicleCostParams().fix);
			xmlConfig.setProperty(typePathString + "("+typeCounter+").costs.distance", type.getVehicleCostParams().perDistanceUnit);
			xmlConfig.setProperty(typePathString + "("+typeCounter+").costs.time", type.getVehicleCostParams().perTimeUnit);
			typeCounter++;
		}


		
		
	}

    private String getSkillString(Vehicle vehicle) {
        return createSkillString(vehicle.getSkills());
    }

    private String getSkillString(Job job){
        return createSkillString(job.getRequiredSkills());
    }

    private String createSkillString(Skills skills) {
        if(skills.values().size() == 0) return null;
        String skillString = null;
        for(String skill : skills.values()){
            if(skillString == null) skillString = skill;
            else skillString += ", " + skill;
        }
        return skillString;
    }


}
