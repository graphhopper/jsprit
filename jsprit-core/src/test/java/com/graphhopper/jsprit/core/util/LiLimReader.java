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
package com.graphhopper.jsprit.core.util;


import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem.Builder;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * test instances for the capacitated vrp with pickup and deliveries and time windows.
 * instances are from li and lim and can be found at:
 * http://www.top.sintef.no/vrp/benchmarks.html
 *
 * @author stefan schroeder
 */


public class LiLimReader {

    static class CustomerData {
        public Coordinate coord;
        public double start;
        public double end;
        public double serviceTime;

        public CustomerData(Coordinate coord, double start, double end, double serviceTime) {
            super();
            this.coord = coord;
            this.start = start;
            this.end = end;
            this.serviceTime = serviceTime;
        }
    }

    static class Relation {
        public String from;
        public String to;
        public int demand;

        public Relation(String from, String to, int demand) {
            super();
            this.from = from;
            this.to = to;
            this.demand = demand;
        }

    }

    private static Logger logger = LoggerFactory.getLogger(LiLimReader.class);

    private Builder vrpBuilder;

    private int vehicleCapacity;

    private String depotId;

    private Map<String, CustomerData> customers;

    private Collection<Relation> relations;

    private double depotOpeningTime;

    private double depotClosingTime;

    private int fixCosts = 0;

    public LiLimReader(Builder vrpBuilder) {
        customers = new HashMap<String, CustomerData>();
        relations = new ArrayList<Relation>();
        this.vrpBuilder = vrpBuilder;
    }

    public LiLimReader(Builder builder, int fixCosts) {
        customers = new HashMap<String, CustomerData>();
        relations = new ArrayList<Relation>();
        this.vrpBuilder = builder;
        this.fixCosts = fixCosts;
    }

    public void read(InputStream inputStream) {
        readShipments(inputStream);
        buildShipments();
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0, vehicleCapacity)
            .setCostPerDistance(1.0).setFixedCost(fixCosts).build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("vehicle")
            .setEarliestStart(depotOpeningTime).setLatestArrival(depotClosingTime)
            .setStartLocation(Location.Builder.newInstance().setCoordinate(customers.get(depotId).coord).build()).setType(type).build();
        vrpBuilder.addVehicle(vehicle);
    }

    private void buildShipments() {
        Integer counter = 0;
        for (Relation rel : relations) {
            counter++;
            String from = rel.from;
            String to = rel.to;
            int demand = rel.demand;
            Shipment s = Shipment.Builder.newInstance(counter.toString()).addSizeDimension(0, demand)
                .setPickupLocation(Location.Builder.newInstance().setCoordinate(customers.get(from).coord).build()).setPickupServiceTime(customers.get(from).serviceTime)
                .setPickupTimeWindow(TimeWindow.newInstance(customers.get(from).start, customers.get(from).end))
                .setDeliveryLocation(Location.Builder.newInstance().setCoordinate(customers.get(to).coord).build()).setDeliveryServiceTime(customers.get(to).serviceTime)
                .setDeliveryTimeWindow(TimeWindow.newInstance(customers.get(to).start, customers.get(to).end)).build();
            vrpBuilder.addJob(s);
        }

    }

    private BufferedReader getReader(InputStream inputStream) {
        return new BufferedReader(new InputStreamReader(inputStream));
    }

    private void readShipments(InputStream inputStream) {
        BufferedReader reader = getReader(inputStream);
        String line = null;
        boolean firstLine = true;
        try {
            while ((line = reader.readLine()) != null) {
                line = line.replace("\r", "");
                line = line.trim();
                String[] tokens = line.split("\t");
                if (firstLine) {
                    int vehicleCapacity = getInt(tokens[1]);
                    this.vehicleCapacity = vehicleCapacity;
                    firstLine = false;
                    continue;
                } else {
                    String customerId = tokens[0];
                    Coordinate coord = makeCoord(tokens[1], tokens[2]);
                    int demand = getInt(tokens[3]);
                    double startTimeWindow = getDouble(tokens[4]);
                    double endTimeWindow = getDouble(tokens[5]);
                    double serviceTime = getDouble(tokens[6]);
//					vrpBuilder.addLocation(customerId, coord);
                    customers.put(customerId, new CustomerData(coord, startTimeWindow, endTimeWindow, serviceTime));
                    if (customerId.equals("0")) {
                        depotId = customerId;
                        depotOpeningTime = startTimeWindow;
                        depotClosingTime = endTimeWindow;
                    }
                    if (demand > 0) {
                        relations.add(new Relation(customerId, tokens[8], demand));
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private Coordinate makeCoord(String xString, String yString) {
        double x = Double.parseDouble(xString);
        double y = Double.parseDouble(yString);
        return new Coordinate(x, y);
    }

    private double getDouble(String string) {
        return Double.parseDouble(string);
    }

    private int getInt(String string) {
        return Integer.parseInt(string);
    }


}
