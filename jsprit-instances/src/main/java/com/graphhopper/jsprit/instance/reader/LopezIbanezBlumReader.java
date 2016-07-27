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
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.util.FastVehicleRoutingTransportCostsMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by schroeder on 18/02/15.
 */
public class LopezIbanezBlumReader {

    private static Logger logger = LoggerFactory.getLogger(LopezIbanezBlumReader.class);

    private VehicleRoutingProblem.Builder builder;

    public LopezIbanezBlumReader(VehicleRoutingProblem.Builder builder) {
        this.builder = builder;
    }

    public void read(String instanceFile) {
        builder.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);
        BufferedReader reader = getReader(instanceFile);
        String line;
        int noNodes = 0;
        int lineCount = 1;
        FastVehicleRoutingTransportCostsMatrix.Builder matrixBuilder = null;
        while ((line = readLine(reader)) != null) {
            if (line.startsWith("#")) continue;
            if (lineCount == 1) {
                noNodes = Integer.parseInt(line);
                matrixBuilder = FastVehicleRoutingTransportCostsMatrix.Builder.newInstance(noNodes, false);
                lineCount++;
                continue;
            } else if (lineCount <= 1 + noNodes) {
                String[] wimaTokens = line.split("\\s+");
                int nodeIndex = lineCount - 2;
                for (int toIndex = 0; toIndex < wimaTokens.length; toIndex++) {
                    matrixBuilder.addTransportDistance(nodeIndex, toIndex, Double.parseDouble(wimaTokens[toIndex]));
                    matrixBuilder.addTransportTime(nodeIndex, toIndex, Double.parseDouble(wimaTokens[toIndex]));
                }
                lineCount++;
                continue;
            } else {
                int nodeIndex = lineCount - 2 - noNodes;
                String[] twTokens = line.split("\\s+");
                if (nodeIndex == 0) {
                    VehicleImpl travelingSalesman = VehicleImpl.Builder.newInstance("traveling_salesman").setStartLocation(Location.newInstance(nodeIndex))
                        .setEarliestStart(Double.parseDouble(twTokens[0])).setLatestArrival(Double.parseDouble(twTokens[1])).build();
                    builder.addVehicle(travelingSalesman);
                } else {
                    Service s = Service.Builder.newInstance("" + nodeIndex).setLocation(Location.newInstance(nodeIndex))
                        .setTimeWindow(TimeWindow.newInstance(Double.parseDouble(twTokens[0]), Double.parseDouble(twTokens[1]))).build();
                    builder.addJob(s);
                }
                lineCount++;
            }
        }
        builder.setRoutingCost(matrixBuilder.build());
        close(reader);
    }

    public static void main(String[] args) {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new LopezIbanezBlumReader(builder).read("input/Dumas/n20w20.001.txt");
        VehicleRoutingProblem vrp = builder.build();
        System.out.println("0->1: " + vrp.getTransportCosts().getTransportCost(Location.newInstance(0), Location.newInstance(1), 0, null, null));
        System.out.println("0->20: " + vrp.getTransportCosts().getTransportCost(Location.newInstance(0), Location.newInstance(20), 0, null, null));
        System.out.println("4->18: " + vrp.getTransportCosts().getTransportCost(Location.newInstance(4), Location.newInstance(18), 0, null, null));
        System.out.println("20->8: " + vrp.getTransportCosts().getTransportCost(Location.newInstance(20), Location.newInstance(8), 0, null, null));
        System.out.println("18: " + ((Service) vrp.getJobs().get("" + 18)).getTimeWindow().getStart() + " " + ((Service) vrp.getJobs().get("" + 18)).getTimeWindow().getEnd());
        System.out.println("20: " + ((Service) vrp.getJobs().get("" + 20)).getTimeWindow().getStart() + " " + ((Service) vrp.getJobs().get("" + 20)).getTimeWindow().getEnd());
        System.out.println("1: " + ((Service) vrp.getJobs().get("" + 1)).getTimeWindow().getStart() + " " + ((Service) vrp.getJobs().get("" + 1)).getTimeWindow().getEnd());
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
