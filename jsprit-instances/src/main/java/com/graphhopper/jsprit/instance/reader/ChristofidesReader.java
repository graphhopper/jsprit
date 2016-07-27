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
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


/**
 * Reader that reads Christophides, Mingozzi and Toth instances.
 * <p>
 * <p>Files and file-description can be found <a href="http://neo.lcc.uma.es/vrp/vrp-instances/capacitated-vrp-instances/">here</a>.
 *
 * @author stefan schroeder
 */
public class ChristofidesReader {

    private static Logger logger = LoggerFactory.getLogger(ChristofidesReader.class);

    private final VehicleRoutingProblem.Builder vrpBuilder;

    private double coordProjectionFactor = 1;

    /**
     * Constructs the reader.
     *
     * @param vrpBuilder the builder
     */
    public ChristofidesReader(VehicleRoutingProblem.Builder vrpBuilder) {
        super();
        this.vrpBuilder = vrpBuilder;
    }

    /**
     * Reads instance-file and memorizes vehicles, customers and so forth in
     * {@link VehicleRoutingProblem.Builder}.
     *
     * @param fileName the filename to read
     */
    public void read(String fileName) {
        vrpBuilder.setFleetSize(FleetSize.INFINITE);
        BufferedReader reader = getReader(fileName);
        int vehicleCapacity = 0;
        double serviceTime = 0.0;
        double endTime = Double.MAX_VALUE;
        int counter = 0;
        String line;
        while ((line = readLine(reader)) != null) {
            line = line.replace("\r", "");
            line = line.trim();
            String[] tokens = line.split(" ");
            if (counter == 0) {
                vehicleCapacity = Integer.parseInt(tokens[1].trim());
                endTime = Double.parseDouble(tokens[2].trim());
                serviceTime = Double.parseDouble(tokens[3].trim());
            } else if (counter == 1) {
                Coordinate depotCoord = makeCoord(tokens[0].trim(), tokens[1].trim());
                VehicleTypeImpl vehicleType = VehicleTypeImpl.Builder.newInstance("christophidesType").addCapacityDimension(0, vehicleCapacity).
                    setCostPerDistance(1.0).build();
                VehicleImpl vehicle = VehicleImpl.Builder.newInstance("christophidesVehicle").setLatestArrival(endTime).setStartLocation(Location.newInstance(depotCoord.getX(), depotCoord.getY())).
                    setType(vehicleType).build();
                vrpBuilder.addVehicle(vehicle);
            } else {
                Coordinate customerCoord = makeCoord(tokens[0].trim(), tokens[1].trim());
                int demand = Integer.parseInt(tokens[2].trim());
                String customer = Integer.valueOf(counter - 1).toString();
                Service service = Service.Builder.newInstance(customer).addSizeDimension(0, demand).setServiceTime(serviceTime).setLocation(Location.newInstance(customerCoord.getX(), customerCoord.getY())).build();
                vrpBuilder.addJob(service);
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
