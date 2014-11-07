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
import jsprit.core.problem.job.Delivery;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Pickup;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.route.activity.TimeWindow;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.util.Coordinate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Reads vehicle routing problem from json file.
 */
public class VrpJsonReader {

    private final VehicleRoutingProblem.Builder vrpBuilder;

    private final Map<String, VehicleType> vehicle_type_map = new HashMap<String,VehicleType>();

    public VrpJsonReader(VehicleRoutingProblem.Builder vrpBuilder) {
        this.vrpBuilder = vrpBuilder;
    }

    /**
     * Reads json file.
     *
     * @param jsonFile to be read
     * @throws java.lang.IllegalStateException if there is a service without id and proper location spec OR
     * if there is a vehicle without id and proper location spec OR if there is a vehicle type without id
     */
    public void read(File jsonFile){
        JsonNode root = buildTree_and_getRoot_fromFile(jsonFile);
        parse(root);
    }

    public void read(String jsonContent){
        JsonNode root = buildTree_and_getRoot_fromContent(jsonContent);
        parse(root);
    }

    private void parse(JsonNode root) {
        setFleetSize(root);
        parse_and_map_vehicle_types(root);
        parse_vehicles(root);
        parse_services(root);
    }

    private JsonNode buildTree_and_getRoot_fromContent(String jsonContent){
        JsonNode node = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            node = objectMapper.readTree(jsonContent);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return node;
    }

    private JsonNode buildTree_and_getRoot_fromFile(File jsonFile){
        JsonNode node = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonContent = "";
            BufferedReader reader = new BufferedReader(new FileReader(jsonFile));
            String line;
            while((line = reader.readLine()) != null) jsonContent += line;
            reader.close();
            node = objectMapper.readTree(jsonContent);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return node;
    }

    /**
     * @param root node
     * @throws java.lang.IllegalStateException if service id is missing OR neither location id nor location coordinate are set
     */
    private void parse_services(JsonNode root) {
        JsonNode services = root.path(JsonConstants.SERVICES);
        for(JsonNode serviceNode : services){
            //type
            JsonNode typeNode = serviceNode.path(JsonConstants.Job.TYPE);
            //service id
            JsonNode jobIdNode = serviceNode.path(JsonConstants.Job.ID);
            if(jobIdNode.isMissingNode()) throw new IllegalStateException("service-id is missing");

            Service.Builder serviceBuilder;
            if(typeNode.isMissingNode()) serviceBuilder = Service.Builder.newInstance(jobIdNode.asText());
            else if(typeNode.asText().equals(JsonConstants.Job.SERVICE)) serviceBuilder = Service.Builder.newInstance(jobIdNode.asText());
            else if(typeNode.asText().equals(JsonConstants.Job.PICKUP)) serviceBuilder = Pickup.Builder.newInstance(jobIdNode.asText());
            else if(typeNode.asText().equals(JsonConstants.Job.DELIVERY)) serviceBuilder = Delivery.Builder.newInstance(jobIdNode.asText());
            else throw new IllegalStateException("type of service ("+typeNode.asText()+") is not supported");

            //service address
            JsonNode addressIdNode = serviceNode.path(JsonConstants.Job.ADDRESS).path(JsonConstants.Address.ID);
            boolean either_locationId_or_coord = false;
            if(!addressIdNode.isMissingNode()){
                serviceBuilder.setLocationId(addressIdNode.asText());
                either_locationId_or_coord = true;
            }
            {
                JsonNode lonNode = serviceNode.path(JsonConstants.Job.ADDRESS).path(JsonConstants.Address.LON);
                JsonNode latNode = serviceNode.path(JsonConstants.Job.ADDRESS).path(JsonConstants.Address.LAT);
                if (!lonNode.isMissingNode() && !latNode.isMissingNode()) {
                    serviceBuilder.setCoord(Coordinate.newInstance(lonNode.asDouble(), latNode.asDouble()));
                    either_locationId_or_coord = true;
                }
            }
            if(!either_locationId_or_coord) throw new IllegalStateException("location missing. either locationId or locationCoordinate is required");
            //service name
            JsonNode nameNode = serviceNode.path(JsonConstants.Job.NAME);
            if(!nameNode.isMissingNode()) serviceBuilder.setName(nameNode.asText());
            //service duration
            serviceBuilder.setServiceTime(serviceNode.path(JsonConstants.Job.SERVICE_DURATION).asDouble());
            //service tw
            JsonNode start_tw_node = serviceNode.path(JsonConstants.Job.TIME_WINDOW).path(JsonConstants.TimeWindow.START);
            JsonNode end_tw_node = serviceNode.path(JsonConstants.Job.TIME_WINDOW).path(JsonConstants.TimeWindow.END);
            if(!start_tw_node.isMissingNode() && !end_tw_node.isMissingNode()){
                serviceBuilder.setTimeWindow(TimeWindow.newInstance(start_tw_node.asDouble(),end_tw_node.asDouble()));
            }
            //service size
            JsonNode sizeNode = serviceNode.path(JsonConstants.Job.SIZE);
            int size_index = 0;
            for(JsonNode sizeValNode : sizeNode){
                int size_value = sizeValNode.intValue();
                serviceBuilder.addSizeDimension(size_index,size_value);
                size_index++;
            }
            //service skills
            JsonNode reqSkills = serviceNode.path(JsonConstants.Job.SKILLS);
            for(JsonNode skillNode : reqSkills){
                serviceBuilder.addRequiredSkill(skillNode.asText());
            }
            //add service
            vrpBuilder.addJob(serviceBuilder.build());
        }
    }

    /**
     * @param root node
     * @throws java.lang.IllegalStateException if vehicle id is missing OR if neither start location id nor start location
     * coordinate are set
     */
    private void parse_vehicles(JsonNode root) {
        JsonNode vehicles = root.path(JsonConstants.VEHICLES);
        for(JsonNode vehicleNode : vehicles){
            //vehicle id
            JsonNode vehicle_id_node = vehicleNode.path(JsonConstants.Vehicle.ID);
            if(vehicle_id_node.isMissingNode()) throw new IllegalStateException("vehicle id missing");
            VehicleImpl.Builder vehicleBuilder = VehicleImpl.Builder.newInstance(vehicle_id_node.asText());
            //vehicle type
            VehicleType type = vehicle_type_map.get(vehicleNode.path(JsonConstants.Vehicle.TYPE_ID).asText());
            vehicleBuilder.setType(type);
            //earliest start
            JsonNode earliestStartNode = vehicleNode.path(JsonConstants.Vehicle.EARLIEST_START);
            if(!earliestStartNode.isMissingNode()) vehicleBuilder.setEarliestStart(earliestStartNode.asDouble());
            //latest end
            JsonNode latestEndNode = vehicleNode.path(JsonConstants.Vehicle.LATEST_END);
            if(!latestEndNode.isMissingNode()) vehicleBuilder.setLatestArrival(latestEndNode.asDouble());
            //start
                //location id
            boolean either_id_or_coord = false;
            JsonNode startAddressId = vehicleNode.path(JsonConstants.Vehicle.START_ADDRESS).path(JsonConstants.Address.ID);
            if(!startAddressId.isMissingNode()){
                vehicleBuilder.setStartLocationId(startAddressId.asText());
                either_id_or_coord = true;
            }
                //location coordinate
            {
                JsonNode lonNode = vehicleNode.path(JsonConstants.Vehicle.START_ADDRESS).path(JsonConstants.Address.LON);
                JsonNode latNode = vehicleNode.path(JsonConstants.Vehicle.START_ADDRESS).path(JsonConstants.Address.LAT);
                if (!lonNode.isMissingNode() && !latNode.isMissingNode()) {
                    vehicleBuilder.setStartLocationCoordinate(Coordinate.newInstance(lonNode.asDouble(), latNode.asDouble()));
                    either_id_or_coord = true;
                }
            }
            if(!either_id_or_coord) throw new IllegalStateException("start location of vehicle missing. either id or coordinate required");
            //end
                //location id
            JsonNode endAddressId = vehicleNode.path(JsonConstants.Vehicle.END_ADDRESS).path(JsonConstants.Address.ID);
            if(!endAddressId.isMissingNode()){
                if(!startAddressId.asText().equals(endAddressId.asText())){
                    vehicleBuilder.setEndLocationId(endAddressId.asText());
                }
            }
                //location coordinate
            {
                JsonNode lonNode = vehicleNode.path(JsonConstants.Vehicle.END_ADDRESS).path(JsonConstants.Address.LON);
                JsonNode latNode = vehicleNode.path(JsonConstants.Vehicle.END_ADDRESS).path(JsonConstants.Address.LAT);
                if (!lonNode.isMissingNode() && !latNode.isMissingNode()) {
                    vehicleBuilder.setEndLocationCoordinate(Coordinate.newInstance(lonNode.asDouble(), latNode.asDouble()));
                }
            }
            //skills
            JsonNode skillsNode = vehicleNode.path(JsonConstants.Vehicle.SKILLS);
            for(JsonNode skillNode : skillsNode){
                String skill = skillNode.asText();
                vehicleBuilder.addSkill(skill);
            }

            vrpBuilder.addVehicle(vehicleBuilder.build());

        }
    }

    /**
     * @param root node
     * @throws java.lang.IllegalStateException if type id is missing
     */
    private void parse_and_map_vehicle_types(JsonNode root) {
        JsonNode types = root.path(JsonConstants.VEHICLE_TYPES);
        for(JsonNode typeNode : types){
            JsonNode typeIdNode = typeNode.path(JsonConstants.Vehicle.Type.ID);
            if(typeIdNode.isMissingNode()) throw new IllegalStateException("type id missing");
            VehicleTypeImpl.Builder typeBuilder = VehicleTypeImpl.Builder.newInstance(typeIdNode.asText());
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
        new VrpJsonReader(vrpBuilder).read(new File("output/vrp.json"));
        VehicleRoutingProblem problem = vrpBuilder.build();
        for(Job j : problem.getJobs().values()) System.out.println(j);
        for(Vehicle v : problem.getVehicles()) System.out.println(v);
    }


}
