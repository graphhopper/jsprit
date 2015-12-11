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
package jsprit.instance.reader;


import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.route.activity.TimeWindow;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.util.Coordinate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


/**
 * Reader that reads the well-known solomon-instances.
 * <p/>
 * <p>See: <a href="http://neo.lcc.uma.es/vrp/vrp-instances/capacitated-vrp-with-time-windows-instances/">neo.org</a>
 *
 * @author stefan
 */

public class SolomonReader {

    /**
     * @param costProjectionFactor the costProjectionFactor to set
     */
    public void setVariableCostProjectionFactor(double costProjectionFactor) {
        this.variableCostProjectionFactor = costProjectionFactor;
    }

    private static Logger logger = LogManager.getLogger(SolomonReader.class);

    private final VehicleRoutingProblem.Builder vrpBuilder;

    private double coordProjectionFactor = 1;

    private double timeProjectionFactor = 1;

    private double variableCostProjectionFactor = 1;

    private double fixedCostPerVehicle = 0.0;

    public SolomonReader(VehicleRoutingProblem.Builder vrpBuilder) {
        super();
        this.vrpBuilder = vrpBuilder;
    }

    public SolomonReader(VehicleRoutingProblem.Builder vrpBuilder, double fixedCostPerVehicle) {
        super();
        this.vrpBuilder = vrpBuilder;
        this.fixedCostPerVehicle = fixedCostPerVehicle;
    }

    public void read(String solomonFile) {
        vrpBuilder.setFleetSize(FleetSize.INFINITE);
        BufferedReader reader = getReader(solomonFile);
        int vehicleCapacity = 0;

        int counter = 0;
        String line;
        while ((line = readLine(reader)) != null) {
            line = line.replace("\r", "");
            line = line.trim();
            String[] tokens = line.split(" +");
            counter++;
            if (counter == 5) {
                vehicleCapacity = Integer.parseInt(tokens[1]);
                continue;
            }
            if (counter > 9) {
                if (tokens.length < 7) continue;
                Coordinate coord = makeCoord(tokens[1], tokens[2]);
                String customerId = tokens[0];
                int demand = Integer.parseInt(tokens[3]);
                double start = Double.parseDouble(tokens[4]) * timeProjectionFactor;
                double end = Double.parseDouble(tokens[5]) * timeProjectionFactor;
                double serviceTime = Double.parseDouble(tokens[6]) * timeProjectionFactor;
                if (counter == 10) {
                    VehicleTypeImpl.Builder typeBuilder = VehicleTypeImpl.Builder.newInstance("solomonType").addCapacityDimension(0, vehicleCapacity);
                    typeBuilder.setCostPerDistance(1.0 * variableCostProjectionFactor).setFixedCost(fixedCostPerVehicle);
                    VehicleTypeImpl vehicleType = typeBuilder.build();

                    VehicleImpl vehicle = VehicleImpl.Builder.newInstance("solomonVehicle").setEarliestStart(start).setLatestArrival(end)
                        .setStartLocation(Location.Builder.newInstance().setId(customerId)
                            .setCoordinate(coord).build()).setType(vehicleType).build();
                    vrpBuilder.addVehicle(vehicle);

                } else {
                    Service service = Service.Builder.newInstance(customerId).addSizeDimension(0, demand)
                        .setLocation(Location.Builder.newInstance().setCoordinate(coord).setId(customerId).build()).setServiceTime(serviceTime)
                        .setTimeWindow(TimeWindow.newInstance(start, end)).build();
                    vrpBuilder.addJob(service);
                }
            }
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

    public void setTimeProjectionFactor(double timeProjection) {
        this.timeProjectionFactor = timeProjection;

    }
}
