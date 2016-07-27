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
package com.graphhopper.jsprit.instance.reader;


import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl.Builder;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Reader that reads instances developed by:
 * <p>
 * <p>Cordeau, J.-F., Gendreau, M. and Laporte, G. (1997), A tabu search heuristic for periodic and multi-depot vehicle routing problems.
 * Networks, 30: 105–119. doi: 10.1002/(SICI)1097-0037(199709)30:2<105::AID-NET5>3.0.CO;2-G
 * <p>
 * <p>Files and file-description can be found <a href="http://neo.lcc.uma.es/vrp/vrp-instances/multiple-depot-vrp-instances/">here</a>.
 *
 * @author stefan schroeder
 */
public class CordeauReader {

    private static Logger logger = LoggerFactory.getLogger(CordeauReader.class);

    private final VehicleRoutingProblem.Builder vrpBuilder;

    private double coordProjectionFactor = 1;


    public CordeauReader(VehicleRoutingProblem.Builder vrpBuilder) {
        super();
        this.vrpBuilder = vrpBuilder;
    }

    public void read(String fileName) {
        vrpBuilder.setFleetSize(FleetSize.FINITE);
        BufferedReader reader = getReader(fileName);
        int vrpType;
        int nOfDepots = 0;
        int nOfCustomers = 0;
        int nOfVehiclesAtEachDepot = 0;

        int counter = 0;
        String line;
        List<List<Builder>> vehiclesAtDepot = new ArrayList<List<Builder>>();
        int depotCounter = 0;
        while ((line = readLine(reader)) != null) {
            line = line.replace("\r", "");
            line = line.trim();
            String[] tokens = line.split("\\s+");
            if (counter == 0) {
                vrpType = Integer.parseInt(tokens[0].trim());
                if (vrpType != 2)
                    throw new IllegalStateException("expect vrpType to be equal to 2 and thus to be MDVRP");
                nOfVehiclesAtEachDepot = Integer.parseInt(tokens[1].trim());
                nOfCustomers = Integer.parseInt(tokens[2].trim());
                nOfDepots = Integer.parseInt(tokens[3].trim());
            } else if (counter <= nOfDepots) {
                String depot = Integer.valueOf(counter).toString();
                int duration = Integer.parseInt(tokens[0].trim());
                if (duration == 0) duration = 999999;
                int capacity = Integer.parseInt(tokens[1].trim());
                VehicleTypeImpl vehicleType = VehicleTypeImpl.Builder.newInstance(counter + "_cordeauType").addCapacityDimension(0, capacity).
                    setCostPerDistance(1.0).setFixedCost(0).build();
                List<Builder> builders = new ArrayList<VehicleImpl.Builder>();
                for (int vehicleCounter = 0; vehicleCounter < nOfVehiclesAtEachDepot; vehicleCounter++) {
                    Builder vBuilder = VehicleImpl.Builder.newInstance(depot + "_" + (vehicleCounter + 1) + "_cordeauVehicle");
                    vBuilder.setLatestArrival(duration).setType(vehicleType);
                    builders.add(vBuilder);
                }
                vehiclesAtDepot.add(builders);
            } else if (counter <= (nOfCustomers + nOfDepots)) {
                String id = tokens[0].trim();
                Coordinate customerCoord = makeCoord(tokens[1].trim(), tokens[2].trim());
                double serviceTime = Double.parseDouble(tokens[3].trim());
                int demand = Integer.parseInt(tokens[4].trim());
                Service service = Service.Builder.newInstance(id).addSizeDimension(0, demand).setServiceTime(serviceTime)
                    .setLocation(Location.Builder.newInstance().setId(id).setCoordinate(customerCoord).build()).build();
                vrpBuilder.addJob(service);
            } else if (counter <= (nOfCustomers + nOfDepots + nOfDepots)) {
                Coordinate depotCoord = makeCoord(tokens[1].trim(), tokens[2].trim());
                List<Builder> vBuilders = vehiclesAtDepot.get(depotCounter);
                for (Builder vBuilder : vBuilders) {
                    vBuilder.setStartLocation(Location.newInstance(depotCoord.getX(), depotCoord.getY()));
                    VehicleImpl vehicle = vBuilder.build();
                    vrpBuilder.addVehicle(vehicle);
                }
                depotCounter++;
            } else {
                throw new IllegalStateException("there are more lines than expected in file.");
            }
            counter++;
        }
        close(reader);
    }

    public void setCoordProjectionFactor(double coordProjectionFactor) {
        this.coordProjectionFactor = coordProjectionFactor;
    }

    private void close(BufferedReader reader) {
        try {
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String readLine(BufferedReader reader) {
        try {
            return reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Coordinate makeCoord(String xString, String yString) {
        double x = Double.parseDouble(xString);
        double y = Double.parseDouble(yString);
        return new Coordinate(x * coordProjectionFactor, y * coordProjectionFactor);
    }

    private BufferedReader getReader(String solomonFile) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(solomonFile));
        } catch (FileNotFoundException e1) {
            throw new RuntimeException(e1);
        }
        return reader;
    }
}
