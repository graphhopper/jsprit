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

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(new File(jsonFile));
            setFleetSize(root);
            parse_and_map_vehicle_types(root);
            parse_vehicles(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parse_vehicles(JsonNode root) {
        JsonNode vehicles = root.path(JsonConstants.VEHICLES);
        Iterator<JsonNode> vehicle_iterator = vehicles.iterator();
        while(vehicle_iterator.hasNext()){
            JsonNode vehicleNode = vehicle_iterator.next();
            VehicleImpl.Builder vehicleBuilder = VehicleImpl.Builder.newInstance(vehicleNode.path(JsonConstants.Vehicle.ID).asText());
            VehicleType type = vehicle_type_map.get(vehicleNode.path(JsonConstants.Vehicle.TYPE_ID).asText());
            vehicleBuilder.setType(type);
            vehicleBuilder.setEarliestStart(vehicleNode.path(JsonConstants.Vehicle.EARLIEST_START).asDouble());
            vehicleBuilder.setLatestArrival(vehicleNode.path(JsonConstants.Vehicle.LATEST_END).asDouble());

            String startLocationId = vehicleNode.path(JsonConstants.Vehicle.START_ADDRESS).path(JsonConstants.Address.ID).asText();
            vehicleBuilder.setStartLocationId(startLocationId);
            String endLocationId = vehicleNode.path(JsonConstants.Vehicle.END_ADDRESS).path(JsonConstants.Address.ID).asText();
            if(!startLocationId.equals(endLocationId)){
                vehicleBuilder.setEndLocationId(endLocationId);
            }
            {
                Double lon = vehicleNode.path(JsonConstants.Vehicle.START_ADDRESS).path(JsonConstants.Address.LON).asDouble();
                Double lat = vehicleNode.path(JsonConstants.Vehicle.START_ADDRESS).path(JsonConstants.Address.LAT).asDouble();
                if (lon != null && lat != null) {
                    vehicleBuilder.setStartLocationCoordinate(Coordinate.newInstance(lon, lat));
                }
            }
            {
                Double lon = vehicleNode.path(JsonConstants.Vehicle.END_ADDRESS).path(JsonConstants.Address.LON).asDouble();
                Double lat = vehicleNode.path(JsonConstants.Vehicle.END_ADDRESS).path(JsonConstants.Address.LAT).asDouble();
                if (lon != null && lat != null) {
                    vehicleBuilder.setEndLocationCoordinate(Coordinate.newInstance(lon, lat));
                }
            }

            JsonNode skillsNode = vehicleNode.path(JsonConstants.Vehicle.SKILLS);
            Iterator<JsonNode> skill_iterator = skillsNode.iterator();
            while(skill_iterator.hasNext()){
                JsonNode skillNode = skill_iterator.next();
                String skill = skillNode.asText();
                vehicleBuilder.addSkill(skill);
            }

            VehicleImpl vehicle = vehicleBuilder.build();
            vrpBuilder.addVehicle(vehicle);

        }
    }

    private void parse_and_map_vehicle_types(JsonNode root) {
        JsonNode types = root.path(JsonConstants.VEHICLE_TYPES);
        Iterator<JsonNode> typeIterator = types.iterator();
        while(typeIterator.hasNext()){
            JsonNode typeNode = typeIterator.next();
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
