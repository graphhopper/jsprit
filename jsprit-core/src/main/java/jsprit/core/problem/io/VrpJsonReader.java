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


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.route.activity.TimeWindow;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.util.Coordinate;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by schroeder on 04.11.14.
 */
public class VrpJsonReader {

    private final VehicleRoutingProblem.Builder vrpBuilder;

    private final Map<String, VehicleType> vehicle_type_map = new HashMap<String,VehicleType>();

    public VrpJsonReader(VehicleRoutingProblem.Builder vrpBuilder) {
        this.vrpBuilder = vrpBuilder;
    }

    public void read(String jsonFile){
        JsonNode root = buildTree_and_getRoot(jsonFile);
        setFleetSize(root);
        parse_and_map_vehicle_types(root);
        parse_vehicles(root);
        parse_services(root);
    }

    private JsonNode buildTree_and_getRoot(String jsonFile){
        JsonNode node = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            node = objectMapper.readTree(new File(jsonFile));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return node;
    }

    private void parse_services(JsonNode root) {
        JsonNode services = root.path(JsonConstants.SERVICES);
        for(JsonNode serviceNode : services){
            JsonNode jobIdNode = serviceNode.path(JsonConstants.Job.ID);
            if(jobIdNode.isMissingNode()){
                throw new IllegalStateException("service-id is missing");
            }
            Service.Builder serviceBuilder = Service.Builder.newInstance(jobIdNode.asText());

            JsonNode addressIdNode = serviceNode.path(JsonConstants.Job.ADDRESS).path(JsonConstants.Address.ID);
            boolean either_locationId_or_coord = false;
            if(!addressIdNode.isMissingNode()){
                serviceBuilder.setLocationId(addressIdNode.asText());
                either_locationId_or_coord = true;
            }
            {
                Double lon = serviceNode.path(JsonConstants.Job.ADDRESS).path(JsonConstants.Address.LON).asDouble();
                Double lat = serviceNode.path(JsonConstants.Job.ADDRESS).path(JsonConstants.Address.LAT).asDouble();
                if (lon != null && lat != null) {
                    serviceBuilder.setCoord(Coordinate.newInstance(lon, lat));
                    either_locationId_or_coord = true;
                }
            }
            if(!either_locationId_or_coord) throw new IllegalStateException("location missing. either locationId or locationCoordinate is required");

            serviceBuilder.setName(serviceNode.path(JsonConstants.Job.NAME).asText());
            serviceBuilder.setServiceTime(serviceNode.path(JsonConstants.Job.SERVICE_DURATION).asDouble());

            Double tw_start = serviceNode.path(JsonConstants.Job.TIME_WINDOW).path(JsonConstants.TimeWindow.START).asDouble();
            Double tw_end = serviceNode.path(JsonConstants.Job.TIME_WINDOW).path(JsonConstants.TimeWindow.END).asDouble();
            if(tw_start != null && tw_end != null){
                serviceBuilder.setTimeWindow(TimeWindow.newInstance(tw_start,tw_end));
            }

            JsonNode sizeNode = serviceNode.path(JsonConstants.Job.SIZE);
            int size_index = 0;
            for(JsonNode sizeValNode : sizeNode){
                int size_value = sizeValNode.intValue();
                serviceBuilder.addSizeDimension(size_index,size_value);
                size_index++;
            }

            JsonNode reqSkills = serviceNode.path(JsonConstants.Job.SKILLS);
            for(JsonNode skillNode : reqSkills){
                serviceBuilder.addRequiredSkill(skillNode.asText());
            }

            vrpBuilder.addJob(serviceBuilder.build());
        }
    }

    private void parse_vehicles(JsonNode root) {
        JsonNode vehicles = root.path(JsonConstants.VEHICLES);

        for(JsonNode vehicleNode : vehicles){
//            JsonNode vehicleNode = vehicle_iterator.next();
            VehicleImpl.Builder vehicleBuilder = VehicleImpl.Builder.newInstance(vehicleNode.path(JsonConstants.Vehicle.ID).asText());
            VehicleType type = vehicle_type_map.get(vehicleNode.path(JsonConstants.Vehicle.TYPE_ID).asText());
            vehicleBuilder.setType(type);

            JsonNode earliestStartNode = vehicleNode.path(JsonConstants.Vehicle.EARLIEST_START);
            if(!earliestStartNode.isMissingNode()) vehicleBuilder.setEarliestStart(earliestStartNode.asDouble());

            JsonNode latestEndNode = vehicleNode.path(JsonConstants.Vehicle.LATEST_END);
            if(!latestEndNode.isMissingNode()) vehicleBuilder.setLatestArrival(latestEndNode.asDouble());

            String startLocationId = vehicleNode.path(JsonConstants.Vehicle.START_ADDRESS).path(JsonConstants.Address.ID).asText();
            vehicleBuilder.setStartLocationId(startLocationId);
            JsonNode endAddressId = vehicleNode.path(JsonConstants.Vehicle.END_ADDRESS).path(JsonConstants.Address.ID);
            if(!endAddressId.isMissingNode()){
                if(!startLocationId.equals(endAddressId.asText())){
                    vehicleBuilder.setEndLocationId(endAddressId.asText());
                }
            }
            {
                JsonNode lonNode = vehicleNode.path(JsonConstants.Vehicle.START_ADDRESS).path(JsonConstants.Address.LON);
                JsonNode latNode = vehicleNode.path(JsonConstants.Vehicle.START_ADDRESS).path(JsonConstants.Address.LAT);
                if (!lonNode.isMissingNode() && !latNode.isMissingNode()) {
                    vehicleBuilder.setStartLocationCoordinate(Coordinate.newInstance(lonNode.asDouble(), latNode.asDouble()));
                }
            }
            {
                JsonNode lonNode = vehicleNode.path(JsonConstants.Vehicle.END_ADDRESS).path(JsonConstants.Address.LON);
                JsonNode latNode = vehicleNode.path(JsonConstants.Vehicle.END_ADDRESS).path(JsonConstants.Address.LAT);
                if (!lonNode.isMissingNode() && !latNode.isMissingNode()) {
                    vehicleBuilder.setEndLocationCoordinate(Coordinate.newInstance(lonNode.asDouble(), latNode.asDouble()));
                }
            }

            JsonNode skillsNode = vehicleNode.path(JsonConstants.Vehicle.SKILLS);
            for(JsonNode skillNode : skillsNode){
                String skill = skillNode.asText();
                vehicleBuilder.addSkill(skill);
            }

            VehicleImpl vehicle = vehicleBuilder.build();
            vrpBuilder.addVehicle(vehicle);

        }
    }

    private void parse_and_map_vehicle_types(JsonNode root) {
        JsonNode types = root.path(JsonConstants.VEHICLE_TYPES);
        for(JsonNode typeNode : types){
            VehicleTypeImpl.Builder typeBuilder = VehicleTypeImpl.Builder.newInstance(typeNode.path(JsonConstants.Vehicle.Type.ID).asText());
            typeBuilder.setFixedCost(typeNode.path(JsonConstants.Vehicle.Type.FIXED_COSTS).asDouble());
            typeBuilder.setCostPerDistance(typeNode.path(JsonConstants.Vehicle.Type.DISTANCE).asDouble());
            typeBuilder.setCostPerTime(typeNode.path(JsonConstants.Vehicle.Type.TIME).asDouble());
            JsonNode capacity = typeNode.path(JsonConstants.Vehicle.Type.CAPACITY);
            Iterator<JsonNode> capacity_dimension_iterator = capacity.iterator();
            int capacity_index = 0;
            while(capacity_dimension_iterator.hasNext()){
                JsonNode capacity_value = capacity_dimension_iterator.next();
                int capacity_val = capacity_value.asInt();
                typeBuilder.addCapacityDimension(capacity_index,capacity_val);
                capacity_index++;
            }
            VehicleTypeImpl type = typeBuilder.build();
            vehicle_type_map.put(type.getTypeId(),type);
        }
    }

    private void setFleetSize(JsonNode root) {
        String fleetsize = root.path(JsonConstants.FLEET).asText();
        if(fleetsize.equals("INFINITE")) vrpBuilder.setFleetSize(VehicleRoutingProblem.FleetSize.INFINITE);
        else vrpBuilder.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);
    }

    public static void main(String[] args) {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpJsonReader(vrpBuilder).read("output/vrp.json");
        VehicleRoutingProblem problem = vrpBuilder.build();
        for(Job j : problem.getJobs().values()) System.out.println(j);
        for(Vehicle v : problem.getVehicles()) System.out.println(v);
    }


}
