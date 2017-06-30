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
package com.graphhopper.jsprit.io.problem;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.Skills;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Break;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.core.util.VehicleIndexComparator;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class VrpXMLWriter {

    static class XMLConf extends XMLConfiguration {


        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public Document createDoc() throws ConfigurationException {
            return createDocument();
        }
    }

    private Logger log = LoggerFactory.getLogger(VrpXMLWriter.class);

    private VehicleRoutingProblem vrp;

    private Collection<VehicleRoutingProblemSolution> solutions;

    private boolean onlyBestSolution = false;

    public VrpXMLWriter(VehicleRoutingProblem vrp, Collection<VehicleRoutingProblemSolution> solutions, boolean onlyBestSolution) {
        this.vrp = vrp;
        this.solutions = new ArrayList<VehicleRoutingProblemSolution>(solutions);
        this.onlyBestSolution = onlyBestSolution;
    }

    public VrpXMLWriter(VehicleRoutingProblem vrp, Collection<VehicleRoutingProblemSolution> solutions) {
        this.vrp = vrp;
        this.solutions = solutions;
    }

    public VrpXMLWriter(VehicleRoutingProblem vrp) {
        this.vrp = vrp;
        this.solutions = null;
    }

    private static Logger logger = LoggerFactory.getLogger(VrpXMLWriter.class);

    public void write(String filename) {
        if (!filename.endsWith(".xml")) filename += ".xml";
        log.info("write vrp: " + filename);
        XMLConf xmlConfig = createXMLConfiguration();

        try {
            xmlConfig.setFileName(filename);
            Writer out = new FileWriter(filename);
            XMLSerializer serializer = new XMLSerializer(out, createOutputFormat());
            serializer.serialize(xmlConfig.getDocument());
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public OutputStream write() {
        XMLConf xmlConfig = createXMLConfiguration();
        OutputStream out = new ByteArrayOutputStream();

        try {
            XMLSerializer serializer = new XMLSerializer(out, createOutputFormat());
            serializer.serialize(xmlConfig.getDocument());
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return out;
    }

    private XMLConf createXMLConfiguration() {
        XMLConf xmlConfig = new XMLConf();
        xmlConfig.setRootElementName("problem");
        xmlConfig.setAttributeSplittingDisabled(true);
        xmlConfig.setDelimiterParsingDisabled(true);

        writeProblemType(xmlConfig);
        writeVehiclesAndTheirTypes(xmlConfig);

        //might be sorted?
        List<Job> jobs = new ArrayList<Job>();
        jobs.addAll(vrp.getJobs().values());
        for (VehicleRoute r : vrp.getInitialVehicleRoutes()) {
            jobs.addAll(r.getTourActivities().getJobs());
        }

        writeServices(xmlConfig, jobs);
        writeShipments(xmlConfig, jobs);

        writeInitialRoutes(xmlConfig);
        if(onlyBestSolution && solutions != null) {
            VehicleRoutingProblemSolution solution = Solutions.bestOf(solutions);
            solutions.clear();
            solutions.add(solution);
        }

        writeSolutions(xmlConfig);


        try {
            Document document = xmlConfig.createDoc();

            Element element = document.getDocumentElement();
            element.setAttribute("xmlns", "http://www.w3schools.com");
            element.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            element.setAttribute("xsi:schemaLocation", "http://www.w3schools.com vrp_xml_schema.xsd");

        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
        return xmlConfig;
    }

    private OutputFormat createOutputFormat() {
        OutputFormat format = new OutputFormat();
        format.setIndenting(true);
        format.setIndent(5);
        return format;
    }

    private void writeInitialRoutes(XMLConf xmlConfig) {
        if (vrp.getInitialVehicleRoutes().isEmpty()) return;
        String path = "initialRoutes.route";
        int routeCounter = 0;
        for (VehicleRoute route : vrp.getInitialVehicleRoutes()) {
            xmlConfig.setProperty(path + "(" + routeCounter + ").driverId", route.getDriver().getId());
            xmlConfig.setProperty(path + "(" + routeCounter + ").vehicleId", route.getVehicle().getId());
            xmlConfig.setProperty(path + "(" + routeCounter + ").start", route.getStart().getEndTime());
            int actCounter = 0;
            for (TourActivity act : route.getTourActivities().getActivities()) {
                xmlConfig.setProperty(path + "(" + routeCounter + ").act(" + actCounter + ")[@type]", act.getName());
                if (act instanceof TourActivity.JobActivity) {
                    Job job = ((TourActivity.JobActivity) act).getJob();
                    if (job instanceof Service) {
                        xmlConfig.setProperty(path + "(" + routeCounter + ").act(" + actCounter + ").serviceId", job.getId());
                    } else if (job instanceof Shipment) {
                        xmlConfig.setProperty(path + "(" + routeCounter + ").act(" + actCounter + ").shipmentId", job.getId());
                    } else if (job instanceof Break) {
                    	xmlConfig.setProperty(path + "(" + routeCounter + ").act(" + actCounter + ").breakId", job.getId());
                    } else {
                        throw new IllegalStateException("cannot write solution correctly since job-type is not know. make sure you use either service or shipment, or another writer");
                    }
                }
                xmlConfig.setProperty(path + "(" + routeCounter + ").act(" + actCounter + ").arrTime", act.getArrTime());
                xmlConfig.setProperty(path + "(" + routeCounter + ").act(" + actCounter + ").endTime", act.getEndTime());
                actCounter++;
            }
            xmlConfig.setProperty(path + "(" + routeCounter + ").end", route.getEnd().getArrTime());
            routeCounter++;
        }

    }

    private void writeSolutions(XMLConf xmlConfig) {
        if (solutions == null) return;
        String solutionPath = "solutions.solution";
        int counter = 0;
        for (VehicleRoutingProblemSolution solution : solutions) {
            xmlConfig.setProperty(solutionPath + "(" + counter + ").cost", solution.getCost());
            int routeCounter = 0;
            List<VehicleRoute> list = new ArrayList<VehicleRoute>(solution.getRoutes());
            Collections.sort(list , new VehicleIndexComparator());
            for (VehicleRoute route : list) {
//				xmlConfig.setProperty(solutionPath + "(" + counter + ").routes.route(" + routeCounter + ").cost", route.getCost());
                xmlConfig.setProperty(solutionPath + "(" + counter + ").routes.route(" + routeCounter + ").driverId", route.getDriver().getId());
                xmlConfig.setProperty(solutionPath + "(" + counter + ").routes.route(" + routeCounter + ").vehicleId", route.getVehicle().getId());
                xmlConfig.setProperty(solutionPath + "(" + counter + ").routes.route(" + routeCounter + ").start", route.getStart().getEndTime());
                int actCounter = 0;
                for (TourActivity act : route.getTourActivities().getActivities()) {
                    xmlConfig.setProperty(solutionPath + "(" + counter + ").routes.route(" + routeCounter + ").act(" + actCounter + ")[@type]", act.getName());
                    if (act instanceof TourActivity.JobActivity) {
                        Job job = ((TourActivity.JobActivity) act).getJob();
                        if (job instanceof Break) {
                            xmlConfig.setProperty(solutionPath + "(" + counter + ").routes.route(" + routeCounter + ").act(" + actCounter + ").breakId", job.getId());
                        } else if (job instanceof Service) {
                            xmlConfig.setProperty(solutionPath + "(" + counter + ").routes.route(" + routeCounter + ").act(" + actCounter + ").serviceId", job.getId());
                        } else if (job instanceof Shipment) {
                            xmlConfig.setProperty(solutionPath + "(" + counter + ").routes.route(" + routeCounter + ").act(" + actCounter + ").shipmentId", job.getId());
                        } else {
                            throw new IllegalStateException("cannot write solution correctly since job-type is not know. make sure you use either service or shipment, or another writer");
                        }
                    }
                    xmlConfig.setProperty(solutionPath + "(" + counter + ").routes.route(" + routeCounter + ").act(" + actCounter + ").arrTime", act.getArrTime());
                    xmlConfig.setProperty(solutionPath + "(" + counter + ").routes.route(" + routeCounter + ").act(" + actCounter + ").endTime", act.getEndTime());
                    actCounter++;
                }
                xmlConfig.setProperty(solutionPath + "(" + counter + ").routes.route(" + routeCounter + ").end", route.getEnd().getArrTime());
                routeCounter++;
            }
            int unassignedJobCounter = 0;
            for (Job unassignedJob : solution.getUnassignedJobs()) {
                xmlConfig.setProperty(solutionPath + "(" + counter + ").unassignedJobs.job(" + unassignedJobCounter + ")[@id]", unassignedJob.getId());
                unassignedJobCounter++;
            }
            counter++;
        }
    }

    private void writeServices(XMLConf xmlConfig, List<Job> jobs) {
        String shipmentPathString = "services.service";
        int counter = 0;
        for (Job j : jobs) {
            if (!(j instanceof Service)) continue;
            Service service = (Service) j;
            xmlConfig.setProperty(shipmentPathString + "(" + counter + ")[@id]", service.getId());
            xmlConfig.setProperty(shipmentPathString + "(" + counter + ")[@type]", service.getType());
            if (service.getLocation().getId() != null)
                xmlConfig.setProperty(shipmentPathString + "(" + counter + ").location.id", service.getLocation().getId());
            if (service.getLocation().getCoordinate() != null) {
                xmlConfig.setProperty(shipmentPathString + "(" + counter + ").location.coord[@x]", service.getLocation().getCoordinate().getX());
                xmlConfig.setProperty(shipmentPathString + "(" + counter + ").location.coord[@y]", service.getLocation().getCoordinate().getY());
            }
            if (service.getLocation().getIndex() != Location.NO_INDEX) {
                xmlConfig.setProperty(shipmentPathString + "(" + counter + ").location.index", service.getLocation().getIndex());
            }
            for (int i = 0; i < service.getSize().getNuOfDimensions(); i++) {
                xmlConfig.setProperty(shipmentPathString + "(" + counter + ").capacity-dimensions.dimension(" + i + ")[@index]", i);
                xmlConfig.setProperty(shipmentPathString + "(" + counter + ").capacity-dimensions.dimension(" + i + ")", service.getSize().get(i));
            }

            Collection<TimeWindow> tws = service.getTimeWindows();
            int index = 0;
            xmlConfig.setProperty(shipmentPathString + "(" + counter + ").duration", service.getServiceDuration());
            for(TimeWindow tw : tws) {
	            xmlConfig.setProperty(shipmentPathString + "(" + counter + ").timeWindows.timeWindow(" + index + ").start", tw.getStart());
	            xmlConfig.setProperty(shipmentPathString + "(" + counter + ").timeWindows.timeWindow(" + index + ").end", tw.getEnd());
	            ++index;
            }

            //skills
            String skillString = getSkillString(service);
            xmlConfig.setProperty(shipmentPathString + "(" + counter + ").requiredSkills", skillString);

            //name
            if (service.getName() != null) {
                if (!service.getName().equals("no-name")) {
                    xmlConfig.setProperty(shipmentPathString + "(" + counter + ").name", service.getName());
                }
            }
            counter++;
        }
    }

    private void writeShipments(XMLConf xmlConfig, List<Job> jobs) {
        String shipmentPathString = "shipments.shipment";
        int counter = 0;
        for (Job j : jobs) {
            if (!(j instanceof Shipment)) continue;
            Shipment shipment = (Shipment) j;
            xmlConfig.setProperty(shipmentPathString + "(" + counter + ")[@id]", shipment.getId());
//			xmlConfig.setProperty(shipmentPathString + "("+counter+")[@type]", service.getType());
            if (shipment.getPickupLocation().getId() != null)
                xmlConfig.setProperty(shipmentPathString + "(" + counter + ").pickup.location.id", shipment.getPickupLocation().getId());
            if (shipment.getPickupLocation().getCoordinate() != null) {
                xmlConfig.setProperty(shipmentPathString + "(" + counter + ").pickup.location.coord[@x]", shipment.getPickupLocation().getCoordinate().getX());
                xmlConfig.setProperty(shipmentPathString + "(" + counter + ").pickup.location.coord[@y]", shipment.getPickupLocation().getCoordinate().getY());
            }
            if (shipment.getPickupLocation().getIndex() != Location.NO_INDEX) {
                xmlConfig.setProperty(shipmentPathString + "(" + counter + ").pickup.location.index", shipment.getPickupLocation().getIndex());
            }

            Collection<TimeWindow> pu_tws = shipment.getPickupTimeWindows();
            int index = 0;
            xmlConfig.setProperty(shipmentPathString + "(" + counter + ").pickup.duration", shipment.getPickupServiceTime());
            for(TimeWindow tw : pu_tws) {
	            xmlConfig.setProperty(shipmentPathString + "(" + counter + ").pickup.timeWindows.timeWindow(" + index + ").start", tw.getStart());
	            xmlConfig.setProperty(shipmentPathString + "(" + counter + ").pickup.timeWindows.timeWindow(" + index + ").end", tw.getEnd());
	            ++index;
	        }

            if (shipment.getDeliveryLocation().getId() != null)
                xmlConfig.setProperty(shipmentPathString + "(" + counter + ").delivery.location.id", shipment.getDeliveryLocation().getId());
            if (shipment.getDeliveryLocation().getCoordinate() != null) {
                xmlConfig.setProperty(shipmentPathString + "(" + counter + ").delivery.location.coord[@x]", shipment.getDeliveryLocation().getCoordinate().getX());
                xmlConfig.setProperty(shipmentPathString + "(" + counter + ").delivery.location.coord[@y]", shipment.getDeliveryLocation().getCoordinate().getY());
            }
            if (shipment.getDeliveryLocation().getIndex() != Location.NO_INDEX) {
                xmlConfig.setProperty(shipmentPathString + "(" + counter + ").delivery.location.index", shipment.getDeliveryLocation().getIndex());
            }

            Collection<TimeWindow> del_tws = shipment.getDeliveryTimeWindows();
        	xmlConfig.setProperty(shipmentPathString + "(" + counter + ").delivery.duration", shipment.getDeliveryServiceTime());
        	index = 0;
            for(TimeWindow tw : del_tws) {
            	xmlConfig.setProperty(shipmentPathString + "(" + counter + ").delivery.timeWindows.timeWindow(" + index + ").start", tw.getStart());
            	xmlConfig.setProperty(shipmentPathString + "(" + counter + ").delivery.timeWindows.timeWindow(" + index + ").end", tw.getEnd());
            	++index;
            }

            for (int i = 0; i < shipment.getSize().getNuOfDimensions(); i++) {
                xmlConfig.setProperty(shipmentPathString + "(" + counter + ").capacity-dimensions.dimension(" + i + ")[@index]", i);
                xmlConfig.setProperty(shipmentPathString + "(" + counter + ").capacity-dimensions.dimension(" + i + ")", shipment.getSize().get(i));
            }

            //skills
            String skillString = getSkillString(shipment);
            xmlConfig.setProperty(shipmentPathString + "(" + counter + ").requiredSkills", skillString);

            //name
            if (shipment.getName() != null) {
                if (!shipment.getName().equals("no-name")) {
                    xmlConfig.setProperty(shipmentPathString + "(" + counter + ").name", shipment.getName());
                }
            }
            counter++;
        }
    }

    private void writeProblemType(XMLConfiguration xmlConfig) {
        xmlConfig.setProperty("problemType.fleetSize", vrp.getFleetSize());
    }

    private void writeVehiclesAndTheirTypes(XMLConfiguration xmlConfig) {

        //vehicles
        String vehiclePathString = Schema.VEHICLES + "." + Schema.VEHICLE;
        int counter = 0;
        for (Vehicle vehicle : vrp.getVehicles()) {
            xmlConfig.setProperty(vehiclePathString + "(" + counter + ").id", vehicle.getId());
            xmlConfig.setProperty(vehiclePathString + "(" + counter + ").typeId", vehicle.getType().getTypeId());
            xmlConfig.setProperty(vehiclePathString + "(" + counter + ").startLocation.id", vehicle.getStartLocation().getId());
            if (vehicle.getStartLocation().getCoordinate() != null) {
                xmlConfig.setProperty(vehiclePathString + "(" + counter + ").startLocation.coord[@x]", vehicle.getStartLocation().getCoordinate().getX());
                xmlConfig.setProperty(vehiclePathString + "(" + counter + ").startLocation.coord[@y]", vehicle.getStartLocation().getCoordinate().getY());
            }
            if (vehicle.getStartLocation().getIndex() != Location.NO_INDEX) {
                xmlConfig.setProperty(vehiclePathString + "(" + counter + ").startLocation.index", vehicle.getStartLocation().getIndex());
            }

            xmlConfig.setProperty(vehiclePathString + "(" + counter + ").endLocation.id", vehicle.getEndLocation().getId());
            if (vehicle.getEndLocation().getCoordinate() != null) {
                xmlConfig.setProperty(vehiclePathString + "(" + counter + ").endLocation.coord[@x]", vehicle.getEndLocation().getCoordinate().getX());
                xmlConfig.setProperty(vehiclePathString + "(" + counter + ").endLocation.coord[@y]", vehicle.getEndLocation().getCoordinate().getY());
            }
            if (vehicle.getEndLocation().getIndex() != Location.NO_INDEX) {
                xmlConfig.setProperty(vehiclePathString + "(" + counter + ").endLocation.index", vehicle.getEndLocation().getId());
            }
            xmlConfig.setProperty(vehiclePathString + "(" + counter + ").timeSchedule.start", vehicle.getEarliestDeparture());
            xmlConfig.setProperty(vehiclePathString + "(" + counter + ").timeSchedule.end", vehicle.getLatestArrival());

            if (vehicle.getBreak() != null) {
                Collection<TimeWindow> tws = vehicle.getBreak().getTimeWindows();
                int index = 0;
	            xmlConfig.setProperty(vehiclePathString + "(" + counter + ").breaks.duration", vehicle.getBreak().getServiceDuration());
                for(TimeWindow tw : tws) {
		            xmlConfig.setProperty(vehiclePathString + "(" + counter + ").breaks.timeWindows.timeWindow(" + index + ").start", tw.getStart());
		            xmlConfig.setProperty(vehiclePathString + "(" + counter + ").breaks.timeWindows.timeWindow(" + index + ").end", tw.getEnd());
		            ++index;
                }
	        }
            xmlConfig.setProperty(vehiclePathString + "(" + counter + ").returnToDepot", vehicle.isReturnToDepot());

            //write skills
            String skillString = getSkillString(vehicle);
            xmlConfig.setProperty(vehiclePathString + "(" + counter + ").skills", skillString);

            counter++;
        }

        //types
        String typePathString = Schema.builder().append(Schema.TYPES).dot(Schema.TYPE).build();
        int typeCounter = 0;
        for (VehicleType type : vrp.getTypes()) {
            xmlConfig.setProperty(typePathString + "(" + typeCounter + ").id", type.getTypeId());

            for (int i = 0; i < type.getCapacityDimensions().getNuOfDimensions(); i++) {
                xmlConfig.setProperty(typePathString + "(" + typeCounter + ").capacity-dimensions.dimension(" + i + ")[@index]", i);
                xmlConfig.setProperty(typePathString + "(" + typeCounter + ").capacity-dimensions.dimension(" + i + ")", type.getCapacityDimensions().get(i));
            }

            xmlConfig.setProperty(typePathString + "(" + typeCounter + ").costs.fixed", type.getVehicleCostParams().fix);
            xmlConfig.setProperty(typePathString + "(" + typeCounter + ").costs.distance", type.getVehicleCostParams().perDistanceUnit);
            xmlConfig.setProperty(typePathString + "(" + typeCounter + ").costs.time", type.getVehicleCostParams().perTransportTimeUnit);
            xmlConfig.setProperty(typePathString + "(" + typeCounter + ").costs.service", type.getVehicleCostParams().perServiceTimeUnit);
            xmlConfig.setProperty(typePathString + "(" + typeCounter + ").costs.wait", type.getVehicleCostParams().perWaitingTimeUnit);
            typeCounter++;
        }


    }

    private String getSkillString(Vehicle vehicle) {
        return createSkillString(vehicle.getSkills());
    }

    private String getSkillString(Job job) {
        return createSkillString(job.getRequiredSkills());
    }

    private String createSkillString(Skills skills) {
        if (skills.values().size() == 0) return null;
        String skillString = null;
        for (String skill : skills.values()) {
            if (skillString == null) skillString = skill;
            else skillString += ", " + skill;
        }
        return skillString;
    }


}

