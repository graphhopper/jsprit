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

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import basics.Job;
import basics.Service;
import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblemSolution;
import basics.route.ServiceActivity;
import basics.route.TourActivity;
import basics.route.TourActivity.JobActivity;
import basics.route.Vehicle;
import basics.route.VehicleRoute;
import basics.route.VehicleType;

public class VrpXMLWriter {
	
	static class XMLConf extends XMLConfiguration {
		
		
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
		log.info("write vrp to " + filename);
		XMLConf xmlConfig = new XMLConf();
		xmlConfig.setFileName(filename);
		xmlConfig.setRootElementName("problem");
		xmlConfig.setAttributeSplittingDisabled(true);
		xmlConfig.setDelimiterParsingDisabled(true);
		
		
		writeProblemType(xmlConfig);
		writeVehiclesAndTheirTypes(xmlConfig);
		writerServices(xmlConfig);
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
				xmlConfig.setProperty(solutionPath + "(" + counter + ").routes.route(" + routeCounter + ").cost", route.getCost());
				xmlConfig.setProperty(solutionPath + "(" + counter + ").routes.route(" + routeCounter + ").driverId", route.getDriver().getId());
				xmlConfig.setProperty(solutionPath + "(" + counter + ").routes.route(" + routeCounter + ").vehicleId", route.getVehicle().getId());
				xmlConfig.setProperty(solutionPath + "(" + counter + ").routes.route(" + routeCounter + ").start", route.getStart().getEndTime());
				int actCounter = 0;
				for(TourActivity act : route.getTourActivities().getActivities()){
					xmlConfig.setProperty(solutionPath + "(" + counter + ").routes.route(" + routeCounter + ").act("+actCounter+")[@type]", act.getName());
					if(act instanceof JobActivity){
						xmlConfig.setProperty(solutionPath + "(" + counter + ").routes.route(" + routeCounter + ").act("+actCounter+").serviceId", ((JobActivity) act).getJob().getId());
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

	private void writerServices(XMLConf xmlConfig) {
		String shipmentPathString = "services.service";
		int counter = 0;
		for(Job j : vrp.getJobs().values()){
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
			xmlConfig.setProperty(vehiclePathString + "("+counter+").id", vehicle.getId());
			xmlConfig.setProperty(vehiclePathString + "("+counter+").typeId", vehicle.getType().getTypeId());
			xmlConfig.setProperty(vehiclePathString + "("+counter+").location.id", vehicle.getLocationId());
			if(vehicle.getCoord() != null){
				xmlConfig.setProperty(vehiclePathString + "("+counter+").location.coord[@x]", vehicle.getCoord().getX());
				xmlConfig.setProperty(vehiclePathString + "("+counter+").location.coord[@y]", vehicle.getCoord().getY());
			}
			xmlConfig.setProperty(vehiclePathString + "("+counter+").timeSchedule.start", vehicle.getEarliestDeparture());
			xmlConfig.setProperty(vehiclePathString + "("+counter+").timeSchedule.end", vehicle.getLatestArrival());

			counter++;
		}

		//types
		String typePathString = Schema.builder().append(Schema.TYPES).dot(Schema.TYPE).build();
		int typeCounter = 0;
		for(VehicleType type : vrp.getTypes()){
			xmlConfig.setProperty(typePathString + "("+typeCounter+").id", type.getTypeId());
			xmlConfig.setProperty(typePathString + "("+typeCounter+").capacity", type.getCapacity());
			xmlConfig.setProperty(typePathString + "("+typeCounter+").costs.fixed", type.getVehicleCostParams().fix);
			xmlConfig.setProperty(typePathString + "("+typeCounter+").costs.distance", type.getVehicleCostParams().perDistanceUnit);
			xmlConfig.setProperty(typePathString + "("+typeCounter+").costs.time", type.getVehicleCostParams().perTimeUnit);
			typeCounter++;
		}


		
		
	}

}
