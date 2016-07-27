/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
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
package com.graphhopper.jsprit.core.util;


import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.job.Pickup;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


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

    private JobType jobType = JobType.SERVICE;

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
     * {@link com.graphhopper.jsprit.core.problem.VehicleRoutingProblem.Builder}.
     *
     * @param inputStream
     */
    public void read(InputStream inputStream) {
        vrpBuilder.setFleetSize(FleetSize.INFINITE);
        BufferedReader reader = getReader(inputStream);
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
                if(jobType.equals(JobType.SERVICE)) {
                    Service service = Service.Builder.newInstance(customer).addSizeDimension(0, demand).setServiceTime(serviceTime).setLocation(Location.newInstance(customerCoord.getX(), customerCoord.getY())).build();
                    vrpBuilder.addJob(service);
                }
                else if(jobType.equals(JobType.DELIVERY)){
                    Delivery service = Delivery.Builder.newInstance(customer).addSizeDimension(0, demand).setServiceTime(serviceTime).setLocation(Location.newInstance(customerCoord.getX(), customerCoord.getY())).build();
                    vrpBuilder.addJob(service);
                }
                else if(jobType.equals(JobType.PICKUP)){
                    Pickup service = Pickup.Builder.newInstance(customer).addSizeDimension(0, demand).setServiceTime(serviceTime).setLocation(Location.newInstance(customerCoord.getX(), customerCoord.getY())).build();
                    vrpBuilder.addJob(service);
                }
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

    private BufferedReader getReader(InputStream inputStream) {
        return new BufferedReader(new InputStreamReader(inputStream));
    }

    public ChristofidesReader setJobType(JobType jobType) {
        this.jobType = jobType;
        return this;
    }
}
