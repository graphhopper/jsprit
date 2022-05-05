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
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.driver.DriverImpl;
import com.graphhopper.jsprit.core.problem.job.*;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl.Builder;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.Resource;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class VrpXMLReader {

    public interface ServiceBuilderFactory {
        Service.Builder createBuilder(String serviceType, String id, Integer size);
    }

    static class DefaultServiceBuilderFactory implements ServiceBuilderFactory {

        @Override
        public Service.Builder createBuilder(String serviceType, String id, Integer size) {
            if (serviceType.equals("pickup")) {
                if (size != null) return Pickup.Builder.newInstance(id).addSizeDimension(0, size);
                else return Pickup.Builder.newInstance(id);
            } else if (serviceType.equals("delivery")) {
                if (size != null) return Delivery.Builder.newInstance(id).addSizeDimension(0, size);
                else return Delivery.Builder.newInstance(id);
            } else {
                if (size != null) return Service.Builder.newInstance(id).addSizeDimension(0, size);
                else return Service.Builder.newInstance(id);

            }
        }
    }

    private static Logger logger = LoggerFactory.getLogger(VrpXMLReader.class);

    private VehicleRoutingProblem.Builder vrpBuilder;

    private Map<String, Vehicle> vehicleMap;

    private Map<String, Service> serviceMap;

    private Map<String, Shipment> shipmentMap;

    private Set<String> freezedJobIds = new HashSet<String>();

    private boolean schemaValidation = true;

    private Collection<VehicleRoutingProblemSolution> solutions;

    private ServiceBuilderFactory serviceBuilderFactory = new DefaultServiceBuilderFactory();



    /**
     * @param schemaValidation the schemaValidation to set
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setSchemaValidation(boolean schemaValidation) {
        this.schemaValidation = schemaValidation;
    }

    public VrpXMLReader(VehicleRoutingProblem.Builder vrpBuilder, Collection<VehicleRoutingProblemSolution> solutions) {
        this.vrpBuilder = vrpBuilder;
        this.vehicleMap = new LinkedHashMap<String, Vehicle>();
        this.serviceMap = new LinkedHashMap<String, Service>();
        this.shipmentMap = new LinkedHashMap<String, Shipment>();
        this.solutions = solutions;
    }

    public VrpXMLReader(VehicleRoutingProblem.Builder vrpBuilder) {
        this.vrpBuilder = vrpBuilder;
        this.vehicleMap = new LinkedHashMap<String, Vehicle>();
        this.serviceMap = new LinkedHashMap<String, Service>();
        this.shipmentMap = new LinkedHashMap<String, Shipment>();
        this.solutions = null;
    }

    public void read(String filename) {
        logger.debug("read vrp: {}", filename);
        XMLConfiguration xmlConfig = createXMLConfiguration();
        try {
            xmlConfig.load(filename);
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
        read(xmlConfig);
    }

    public void read(InputStream fileContents) {
        XMLConfiguration xmlConfig = createXMLConfiguration();
        try {
            xmlConfig.load(fileContents);
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
        read(xmlConfig);
    }

    private XMLConfiguration createXMLConfiguration() {
        XMLConfiguration xmlConfig = new XMLConfiguration();
        xmlConfig.setAttributeSplittingDisabled(true);
        xmlConfig.setDelimiterParsingDisabled(true);

        if (schemaValidation) {
            final InputStream resource = Resource.getAsInputStream("vrp_xml_schema.xsd");
            if (resource != null) {
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
            } else {
                logger.debug("cannot find schema-xsd file (vrp_xml_schema.xsd). try to read xml without xml-file-validation.");
            }
        }
        return xmlConfig;
    }

    private void read(XMLConfiguration xmlConfig) {
        readProblemType(xmlConfig);
        readVehiclesAndTheirTypes(xmlConfig);

        readShipments(xmlConfig);
        readServices(xmlConfig);

        readInitialRoutes(xmlConfig);
        readSolutions(xmlConfig);

        addJobsAndTheirLocationsToVrp();
    }

    private void addJobsAndTheirLocationsToVrp() {
        for (Service service : serviceMap.values()) {
            if (!freezedJobIds.contains(service.getId())) {
                vrpBuilder.addJob(service);
            }
        }
        for (Shipment shipment : shipmentMap.values()) {
            if (!freezedJobIds.contains(shipment.getId())) {
                vrpBuilder.addJob(shipment);
            }
        }
    }

    private void readInitialRoutes(XMLConfiguration xmlConfig) {
        List<HierarchicalConfiguration> initialRouteConfigs = xmlConfig.configurationsAt("initialRoutes.route");
        for (HierarchicalConfiguration routeConfig : initialRouteConfigs) {
            Driver driver = DriverImpl.noDriver();
            String vehicleId = routeConfig.getString("vehicleId");
            Vehicle vehicle = getVehicle(vehicleId);
            if (vehicle == null) throw new IllegalArgumentException("vehicle is missing.");
            String start = routeConfig.getString("start");
            if (start == null) throw new IllegalArgumentException("route start-time is missing.");
            double departureTime = Double.parseDouble(start);

            VehicleRoute.Builder routeBuilder = VehicleRoute.Builder.newInstance(vehicle, driver);
            routeBuilder.setDepartureTime(departureTime);

            List<HierarchicalConfiguration> actConfigs = routeConfig.configurationsAt("act");
            for (HierarchicalConfiguration actConfig : actConfigs) {
                String type = actConfig.getString("[@type]");
                if (type == null) throw new IllegalArgumentException("act[@type] is missing.");
                double arrTime = 0.;
                double endTime = 0.;
                String arrTimeS = actConfig.getString("arrTime");
                if (arrTimeS != null) arrTime = Double.parseDouble(arrTimeS);
                String endTimeS = actConfig.getString("endTime");
                if (endTimeS != null) endTime = Double.parseDouble(endTimeS);

                String serviceId = actConfig.getString("serviceId");
                if(type.equals("break")) {
                    Break currentbreak = getBreak(vehicleId);
                    routeBuilder.addBreak(currentbreak);
                }
                else {
                    if (serviceId != null) {
                        Service service = getService(serviceId);
                        if (service == null)
                            throw new IllegalArgumentException("service to serviceId " + serviceId + " is missing (reference in one of your initial routes). make sure you define the service you refer to here in <services> </services>.");
                        //!!!since job is part of initial route, it does not belong to jobs in problem, i.e. variable jobs that can be assigned/scheduled
                        freezedJobIds.add(serviceId);
                        routeBuilder.addService(service);
                    } else {
                        String shipmentId = actConfig.getString("shipmentId");
                        if (shipmentId == null)
                            throw new IllegalArgumentException("either serviceId or shipmentId is missing");
                        Shipment shipment = getShipment(shipmentId);
                        if (shipment == null)
                            throw new IllegalArgumentException("shipment to shipmentId " + shipmentId + " is missing (reference in one of your initial routes). make sure you define the shipment you refer to here in <shipments> </shipments>.");
                        freezedJobIds.add(shipmentId);
                        if (type.equals("pickupShipment")) {
                            routeBuilder.addPickup(shipment);
                        } else if (type.equals("deliverShipment")) {
                            routeBuilder.addDelivery(shipment);
                        } else
                            throw new IllegalArgumentException("type " + type + " is not supported. Use 'pickupShipment' or 'deliverShipment' here");
                    }
                }
            }
            VehicleRoute route = routeBuilder.build();
            vrpBuilder.addInitialVehicleRoute(route);
        }

    }

    private void readSolutions(XMLConfiguration vrpProblem) {
        if (solutions == null) return;
        List<HierarchicalConfiguration> solutionConfigs = vrpProblem.configurationsAt("solutions.solution");
        for (HierarchicalConfiguration solutionConfig : solutionConfigs) {
            String totalCost = solutionConfig.getString("cost");
            double cost = -1;
            if (totalCost != null) cost = Double.parseDouble(totalCost);
            List<HierarchicalConfiguration> routeConfigs = solutionConfig.configurationsAt("routes.route");
            List<VehicleRoute> routes = new ArrayList<VehicleRoute>();
            for (HierarchicalConfiguration routeConfig : routeConfigs) {
                //! here, driverId is set to noDriver, no matter whats in driverId.
                Driver driver = DriverImpl.noDriver();
                String vehicleId = routeConfig.getString("vehicleId");
                Vehicle vehicle = getVehicle(vehicleId);
                if (vehicle == null) throw new IllegalArgumentException("vehicle is missing.");
                String start = routeConfig.getString("start");
                if (start == null) throw new IllegalArgumentException("route start-time is missing.");
                double departureTime = Double.parseDouble(start);

                String end = routeConfig.getString("end");
                if (end == null) throw new IllegalArgumentException("route end-time is missing.");

                VehicleRoute.Builder routeBuilder = VehicleRoute.Builder.newInstance(vehicle, driver);
                routeBuilder.setDepartureTime(departureTime);
                List<HierarchicalConfiguration> actConfigs = routeConfig.configurationsAt("act");
                for (HierarchicalConfiguration actConfig : actConfigs) {
                    String type = actConfig.getString("[@type]");
                    if (type == null) throw new IllegalArgumentException("act[@type] is missing.");
                    double arrTime = 0.;
                    double endTime = 0.;
                    String arrTimeS = actConfig.getString("arrTime");
                    if (arrTimeS != null) arrTime = Double.parseDouble(arrTimeS);
                    String endTimeS = actConfig.getString("endTime");
                    if (endTimeS != null) endTime = Double.parseDouble(endTimeS);
                    if(type.equals("break")) {
                        Break currentbreak = getBreak(vehicleId);
                        routeBuilder.addBreak(currentbreak);
                    }
                    else {
                        String serviceId = actConfig.getString("serviceId");
                        if (serviceId != null) {
                            Service service = getService(serviceId);
                            routeBuilder.addService(service);
                        } else {
                            String shipmentId = actConfig.getString("shipmentId");
                            if (shipmentId == null)
                                throw new IllegalArgumentException("either serviceId or shipmentId is missing");
                            Shipment shipment = getShipment(shipmentId);
                            if (shipment == null)
                                throw new IllegalArgumentException("shipment with id " + shipmentId + " does not exist.");
                            if (type.equals("pickupShipment")) {
                                routeBuilder.addPickup(shipment);
                            } else if (type.equals("deliverShipment")) {
                                routeBuilder.addDelivery(shipment);
                            } else
                                throw new IllegalArgumentException("type " + type + " is not supported. Use 'pickupShipment' or 'deliverShipment' here");
                        }
                    }
                }
                routes.add(routeBuilder.build());
            }
            VehicleRoutingProblemSolution solution = new VehicleRoutingProblemSolution(routes, cost);
            List<HierarchicalConfiguration> unassignedJobConfigs = solutionConfig.configurationsAt("unassignedJobs.job");
            for (HierarchicalConfiguration unassignedJobConfig : unassignedJobConfigs) {
                String jobId = unassignedJobConfig.getString("[@id]");
                Job job = getShipment(jobId);
                if (job == null) job = getService(jobId);
                if (job == null) throw new IllegalArgumentException("cannot find unassignedJob with id " + jobId);
                solution.getUnassignedJobs().add(job);
            }

            solutions.add(solution);
        }
    }

    private Shipment getShipment(String shipmentId) {
        return shipmentMap.get(shipmentId);
    }

    private Service getService(String serviceId) {
        return serviceMap.get(serviceId);
    }

    private Vehicle getVehicle(String vehicleId) {
        return vehicleMap.get(vehicleId);
    }

    private Break getBreak(String vehicleId) {
        return vehicleMap.get(vehicleId).getBreak();
    }

    private void readProblemType(XMLConfiguration vrpProblem) {
        String fleetSize = vrpProblem.getString("problemType.fleetSize");
        if (fleetSize == null) vrpBuilder.setFleetSize(FleetSize.INFINITE);
        else if (fleetSize.toUpperCase().equals(FleetSize.INFINITE.toString()))
            vrpBuilder.setFleetSize(FleetSize.INFINITE);
        else vrpBuilder.setFleetSize(FleetSize.FINITE);
    }

    private void readShipments(XMLConfiguration config) {
        List<HierarchicalConfiguration> shipmentConfigs = config.configurationsAt("shipments.shipment");
        for (HierarchicalConfiguration shipmentConfig : shipmentConfigs) {
            String id = shipmentConfig.getString("[@id]");
            if (id == null) throw new IllegalArgumentException("shipment[@id] is missing.");

            String capacityString = shipmentConfig.getString("capacity-demand");
            boolean capacityDimensionsExist = shipmentConfig.containsKey("capacity-dimensions.dimension(0)");
            if (capacityString == null && !capacityDimensionsExist) {
                throw new IllegalArgumentException("capacity of shipment is not set. use 'capacity-dimensions'");
            }
            if (capacityString != null && capacityDimensionsExist) {
                throw new IllegalArgumentException("either use capacity or capacity-dimension, not both. prefer the use of 'capacity-dimensions' over 'capacity'.");
            }

            Shipment.Builder builder;
            if (capacityString != null) {
                builder = Shipment.Builder.newInstance(id).addSizeDimension(0, Integer.parseInt(capacityString));
            } else {
                builder = Shipment.Builder.newInstance(id);
                List<HierarchicalConfiguration> dimensionConfigs = shipmentConfig.configurationsAt("capacity-dimensions.dimension");
                for (HierarchicalConfiguration dimension : dimensionConfigs) {
                    Integer index = dimension.getInt("[@index]");
                    Integer value = dimension.getInt("");
                    builder.addSizeDimension(index, value);
                }
            }

            //name
            String name = shipmentConfig.getString("name");
            if (name != null) builder.setName(name);

            //pickup location
            //pickup-locationId
            Location.Builder pickupLocationBuilder = Location.Builder.newInstance();
            String pickupLocationId = shipmentConfig.getString("pickup.locationId");
            if (pickupLocationId == null) pickupLocationId = shipmentConfig.getString("pickup.location.id");
            if (pickupLocationId != null) {
                pickupLocationBuilder.setId(pickupLocationId);
            }

            //pickup-coord
            Coordinate pickupCoord = getCoord(shipmentConfig, "pickup.");
            if (pickupCoord == null) pickupCoord = getCoord(shipmentConfig, "pickup.location.");
            if (pickupCoord != null) {
                pickupLocationBuilder.setCoordinate(pickupCoord);
            }

            //pickup.location.index
            String pickupLocationIndex = shipmentConfig.getString("pickup.location.index");
            if (pickupLocationIndex != null) pickupLocationBuilder.setIndex(Integer.parseInt(pickupLocationIndex));
            builder.setPickupLocation(pickupLocationBuilder.build());

            //pickup-serviceTime
            String pickupServiceTime = shipmentConfig.getString("pickup.duration");
            if (pickupServiceTime != null) builder.setPickupServiceTime(Double.parseDouble(pickupServiceTime));

            //pickup-tw
            List<HierarchicalConfiguration> pickupTWConfigs = shipmentConfig.configurationsAt("pickup.timeWindows.timeWindow");
            if (!pickupTWConfigs.isEmpty()) {
                for (HierarchicalConfiguration pu_twConfig : pickupTWConfigs) {
                    builder.addPickupTimeWindow(TimeWindow.newInstance(pu_twConfig.getDouble("start"), pu_twConfig.getDouble("end")));
                }
            }

            //delivery location
            //delivery-locationId
            Location.Builder deliveryLocationBuilder = Location.Builder.newInstance();
            String deliveryLocationId = shipmentConfig.getString("delivery.locationId");
            if (deliveryLocationId == null) deliveryLocationId = shipmentConfig.getString("delivery.location.id");
            if (deliveryLocationId != null) {
                deliveryLocationBuilder.setId(deliveryLocationId);
//				builder.setDeliveryLocationId(deliveryLocationId);
            }

            //delivery-coord
            Coordinate deliveryCoord = getCoord(shipmentConfig, "delivery.");
            if (deliveryCoord == null) deliveryCoord = getCoord(shipmentConfig, "delivery.location.");
            if (deliveryCoord != null) {
                deliveryLocationBuilder.setCoordinate(deliveryCoord);
            }

            String deliveryLocationIndex = shipmentConfig.getString("delivery.location.index");
            if (deliveryLocationIndex != null)
                deliveryLocationBuilder.setIndex(Integer.parseInt(deliveryLocationIndex));
            builder.setDeliveryLocation(deliveryLocationBuilder.build());

            //delivery-serviceTime
            String deliveryServiceTime = shipmentConfig.getString("delivery.duration");
            if (deliveryServiceTime != null) builder.setDeliveryServiceTime(Double.parseDouble(deliveryServiceTime));

            //delivery-tw
            List<HierarchicalConfiguration> deliveryTWConfigs = shipmentConfig.configurationsAt("delivery.timeWindows.timeWindow");
            if (!deliveryTWConfigs.isEmpty()) {
                for (HierarchicalConfiguration dl_twConfig : deliveryTWConfigs) {
                    builder.addDeliveryTimeWindow(TimeWindow.newInstance(dl_twConfig.getDouble("start"), dl_twConfig.getDouble("end")));
                }
            }

            //read skills
            String skillString = shipmentConfig.getString("requiredSkills");
            if (skillString != null) {
                String cleaned = skillString.replaceAll("\\s", "");
                String[] skillTokens = cleaned.split("[,;]");
                for (String skill : skillTokens) builder.addRequiredSkill(skill.toLowerCase());
            }

            //build shipment
            Shipment shipment = builder.build();
//			vrpBuilder.addJob(shipment);
            shipmentMap.put(shipment.getId(), shipment);
        }
    }

    private static Coordinate getCoord(HierarchicalConfiguration serviceConfig, String prefix) {
        Coordinate pickupCoord = null;
        if (serviceConfig.getString(prefix + "coord[@x]") != null && serviceConfig.getString(prefix + "coord[@y]") != null) {
            double x = Double.parseDouble(serviceConfig.getString(prefix + "coord[@x]"));
            double y = Double.parseDouble(serviceConfig.getString(prefix + "coord[@y]"));
            pickupCoord = Coordinate.newInstance(x, y);
        }
        return pickupCoord;
    }

    private void readServices(XMLConfiguration vrpProblem) {
        List<HierarchicalConfiguration> serviceConfigs = vrpProblem.configurationsAt("services.service");
        for (HierarchicalConfiguration serviceConfig : serviceConfigs) {
            String id = serviceConfig.getString("[@id]");
            if (id == null) throw new IllegalArgumentException("service[@id] is missing.");
            String type = serviceConfig.getString("[@type]");
            if (type == null) type = "service";

            String capacityString = serviceConfig.getString("capacity-demand");
            boolean capacityDimensionsExist = serviceConfig.containsKey("capacity-dimensions.dimension(0)");
            if (capacityString == null && !capacityDimensionsExist) {
                throw new IllegalArgumentException("capacity of service is not set. use 'capacity-dimensions'");
            }
            if (capacityString != null && capacityDimensionsExist) {
                throw new IllegalArgumentException("either use capacity or capacity-dimension, not both. prefer the use of 'capacity-dimensions' over 'capacity'.");
            }

            Service.Builder builder;
            if (capacityString != null) {
                builder = serviceBuilderFactory.createBuilder(type, id, Integer.parseInt(capacityString));
            } else {
                builder = serviceBuilderFactory.createBuilder(type, id, null);
                List<HierarchicalConfiguration> dimensionConfigs = serviceConfig.configurationsAt("capacity-dimensions.dimension");
                for (HierarchicalConfiguration dimension : dimensionConfigs) {
                    Integer index = dimension.getInt("[@index]");
                    Integer value = dimension.getInt("");
                    builder.addSizeDimension(index, value);
                }
            }

            //name
            String name = serviceConfig.getString("name");
            if (name != null) builder.setName(name);

            //location
            Location.Builder locationBuilder = Location.Builder.newInstance();
            String serviceLocationId = serviceConfig.getString("locationId");
            if (serviceLocationId == null) {
                serviceLocationId = serviceConfig.getString("location.id");
            }
            if (serviceLocationId != null) locationBuilder.setId(serviceLocationId);

            Coordinate serviceCoord = getCoord(serviceConfig, "");
            if (serviceCoord == null) serviceCoord = getCoord(serviceConfig, "location.");
            if (serviceCoord != null) {
                locationBuilder.setCoordinate(serviceCoord);
            }

            String locationIndex = serviceConfig.getString("location.index");
            if (locationIndex != null) locationBuilder.setIndex(Integer.parseInt(locationIndex));
            builder.setLocation(locationBuilder.build());

            if (serviceConfig.containsKey("duration")) {
                builder.setServiceTime(serviceConfig.getDouble("duration"));
            }
            List<HierarchicalConfiguration> deliveryTWConfigs = serviceConfig.configurationsAt("timeWindows.timeWindow");
            if (!deliveryTWConfigs.isEmpty()) {
                for (HierarchicalConfiguration twConfig : deliveryTWConfigs) {
                    builder.addTimeWindow(TimeWindow.newInstance(twConfig.getDouble("start"), twConfig.getDouble("end")));
                }
            }

            //read skills
            String skillString = serviceConfig.getString("requiredSkills");
            if (skillString != null) {
                String cleaned = skillString.replaceAll("\\s", "");
                String[] skillTokens = cleaned.split("[,;]");
                for (String skill : skillTokens) builder.addRequiredSkill(skill.toLowerCase());
            }

            //build service
            Service service = builder.build();
            serviceMap.put(service.getId(), service);
//			vrpBuilder.addJob(service);

        }
    }

    private void readVehiclesAndTheirTypes(XMLConfiguration vrpProblem) {

        //read vehicle-types
        Map<String, VehicleType> types = new HashMap<String, VehicleType>();
        List<HierarchicalConfiguration> typeConfigs = vrpProblem.configurationsAt("vehicleTypes.type");
        for (HierarchicalConfiguration typeConfig : typeConfigs) {
            String typeId = typeConfig.getString("id");
            if (typeId == null) throw new IllegalArgumentException("typeId is missing.");

            String capacityString = typeConfig.getString("capacity");
            boolean capacityDimensionsExist = typeConfig.containsKey("capacity-dimensions.dimension(0)");
            if (capacityString == null && !capacityDimensionsExist) {
                throw new IllegalArgumentException("capacity of type is not set. use 'capacity-dimensions'");
            }
            if (capacityString != null && capacityDimensionsExist) {
                throw new IllegalArgumentException("either use capacity or capacity-dimension, not both. prefer the use of 'capacity-dimensions' over 'capacity'.");
            }

            VehicleTypeImpl.Builder typeBuilder;
            if (capacityString != null) {
                typeBuilder = VehicleTypeImpl.Builder.newInstance(typeId).addCapacityDimension(0, Integer.parseInt(capacityString));
            } else {
                typeBuilder = VehicleTypeImpl.Builder.newInstance(typeId);
                List<HierarchicalConfiguration> dimensionConfigs = typeConfig.configurationsAt("capacity-dimensions.dimension");
                for (HierarchicalConfiguration dimension : dimensionConfigs) {
                    Integer index = dimension.getInt("[@index]");
                    Integer value = dimension.getInt("");
                    typeBuilder.addCapacityDimension(index, value);
                }
            }

            Double fix = typeConfig.getDouble("costs.fixed");
            Double timeC = typeConfig.getDouble("costs.time");
            Double distC = typeConfig.getDouble("costs.distance");
            if(typeConfig.containsKey("costs.service")){
                Double serviceC = typeConfig.getDouble("costs.service");
                if (serviceC != null) typeBuilder.setCostPerServiceTime(serviceC);
            }

            if(typeConfig.containsKey("costs.wait")){
                Double waitC = typeConfig.getDouble("costs.wait");
                if (waitC != null) typeBuilder.setCostPerWaitingTime(waitC);
            }

            if (fix != null) typeBuilder.setFixedCost(fix);
            if (timeC != null) typeBuilder.setCostPerTransportTime(timeC);
            if (distC != null) typeBuilder.setCostPerDistance(distC);
            VehicleType type = typeBuilder.build();
            String id = type.getTypeId();
            types.put(id, type);
        }

        //read vehicles
        List<HierarchicalConfiguration> vehicleConfigs = vrpProblem.configurationsAt("vehicles.vehicle");
        boolean doNotWarnAgain = false;
        for (HierarchicalConfiguration vehicleConfig : vehicleConfigs) {
            String vehicleId = vehicleConfig.getString("id");
            if (vehicleId == null) throw new IllegalArgumentException("vehicleId is missing.");
            Builder builder = VehicleImpl.Builder.newInstance(vehicleId);
            String typeId = vehicleConfig.getString("typeId");
            if (typeId == null) throw new IllegalArgumentException("typeId is missing.");
            String vType = vehicleConfig.getString("[@type]");
            if (vType != null) {
                if (vType.equals("penalty")) {
                    typeId += "_penalty";
                }
            }
            VehicleType type = types.get(typeId);
            if (type == null) throw new IllegalArgumentException("vehicleType with typeId " + typeId + " is missing.");
            builder.setType(type);

            //read startlocation
            Location.Builder startLocationBuilder = Location.Builder.newInstance();
            String locationId = vehicleConfig.getString("location.id");
            if (locationId == null) {
                locationId = vehicleConfig.getString("startLocation.id");
            }
            startLocationBuilder.setId(locationId);
            String coordX = vehicleConfig.getString("location.coord[@x]");
            String coordY = vehicleConfig.getString("location.coord[@y]");
            if (coordX == null || coordY == null) {
                coordX = vehicleConfig.getString("startLocation.coord[@x]");
                coordY = vehicleConfig.getString("startLocation.coord[@y]");
            }
            if (coordX == null || coordY == null) {
                if (!doNotWarnAgain) {
                    logger.debug("location.coord is missing. will not warn you again.");
                    doNotWarnAgain = true;
                }
            } else {
                Coordinate coordinate = Coordinate.newInstance(Double.parseDouble(coordX), Double.parseDouble(coordY));
                startLocationBuilder.setCoordinate(coordinate);
            }
            String index = vehicleConfig.getString("startLocation.index");
            if (index == null) index = vehicleConfig.getString("location.index");
            if (index != null) {
                startLocationBuilder.setIndex(Integer.parseInt(index));
            }
            builder.setStartLocation(startLocationBuilder.build());

            //read endlocation
            Location.Builder endLocationBuilder = Location.Builder.newInstance();
            boolean hasEndLocation = false;
            String endLocationId = vehicleConfig.getString("endLocation.id");
            if (endLocationId != null) {
                hasEndLocation = true;
                endLocationBuilder.setId(endLocationId);
            }
            String endCoordX = vehicleConfig.getString("endLocation.coord[@x]");
            String endCoordY = vehicleConfig.getString("endLocation.coord[@y]");
            if (endCoordX == null || endCoordY == null) {
                if (!doNotWarnAgain) {
                    logger.debug("endLocation.coord is missing. will not warn you again.");
                    doNotWarnAgain = true;
                }
            } else {
                Coordinate coordinate = Coordinate.newInstance(Double.parseDouble(endCoordX), Double.parseDouble(endCoordY));
                hasEndLocation = true;
                endLocationBuilder.setCoordinate(coordinate);
            }
            String endLocationIndex = vehicleConfig.getString("endLocation.index");
            if (endLocationIndex != null) {
                hasEndLocation = true;
                endLocationBuilder.setIndex(Integer.parseInt(endLocationIndex));
            }
            if (hasEndLocation) builder.setEndLocation(endLocationBuilder.build());

            //read timeSchedule
            String start = vehicleConfig.getString("timeSchedule.start");
            String end = vehicleConfig.getString("timeSchedule.end");
            if (start != null) builder.setEarliestStart(Double.parseDouble(start));
            if (end != null) builder.setLatestArrival(Double.parseDouble(end));

            //read return2depot
            String returnToDepot = vehicleConfig.getString("returnToDepot");
            if (returnToDepot != null) {
                builder.setReturnToDepot(vehicleConfig.getBoolean("returnToDepot"));
            }

            //read skills
            String skillString = vehicleConfig.getString("skills");
            if (skillString != null) {
                String cleaned = skillString.replaceAll("\\s", "");
                String[] skillTokens = cleaned.split("[,;]");
                for (String skill : skillTokens) builder.addSkill(skill.toLowerCase());
            }

            // read break
            List<HierarchicalConfiguration> breakTWConfigs = vehicleConfig.configurationsAt("breaks.timeWindows.timeWindow");
            if (!breakTWConfigs.isEmpty()) {
                String breakDurationString = vehicleConfig.getString("breaks.duration");
                String id = vehicleConfig.getString("breaks.id");
                Break.Builder current_break = Break.Builder.newInstance(id);
                current_break.setServiceTime(Double.parseDouble(breakDurationString));
                for (HierarchicalConfiguration twConfig : breakTWConfigs) {
                	current_break.addTimeWindow(TimeWindow.newInstance(twConfig.getDouble("start"), twConfig.getDouble("end")));
                }
                builder.setBreak(current_break.build());
            }


            //build vehicle
            VehicleImpl vehicle = builder.build();
            vrpBuilder.addVehicle(vehicle);
            vehicleMap.put(vehicleId, vehicle);
        }

    }


}
