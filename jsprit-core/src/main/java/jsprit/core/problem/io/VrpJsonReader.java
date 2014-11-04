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
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.problem.vehicle.VehicleTypeImpl;

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
            vehicleBuilder.setStartLocationId(vehicleNode.path(JsonConstants.Vehicle.START_ADDRESS).path(JsonConstants.Address.ID).asText());

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


}
