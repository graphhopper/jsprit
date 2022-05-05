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


import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;

import java.io.*;

public class TSPLIB95CostMatrixReader {

    private VehicleRoutingTransportCostsMatrix.Builder costMatrixBuilder;

    public TSPLIB95CostMatrixReader(VehicleRoutingTransportCostsMatrix.Builder costMatrixBuilder) {
        this.costMatrixBuilder = costMatrixBuilder;
    }

    public void read(String matrixFile) {
        BufferedReader reader = getBufferedReader(matrixFile);
        String line;
        boolean isEdgeWeights = false;
        int fromIndex = 0;
        while ((line = getLine(reader)) != null) {
            if (line.startsWith("EDGE_WEIGHT_SECTION")) {
                isEdgeWeights = true;
                continue;
            }
            if (line.startsWith("DEMAND_SECTION")) {
                isEdgeWeights = false;
                continue;
            }
            if (isEdgeWeights) {
                String[] tokens = line.split("\\s+");
                String fromId = "" + (fromIndex + 1);
                for (int i = 0; i < tokens.length; i++) {
                    double distance = Double.parseDouble(tokens[i]);
                    String toId = "" + (i + 1);
                    costMatrixBuilder.addTransportDistance(fromId, toId, distance);
                    costMatrixBuilder.addTransportTime(fromId, toId, distance);
                }
                fromIndex++;
            }
        }
        close(reader);
    }

    private void close(BufferedReader reader) {
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getLine(BufferedReader reader) {
        String s = null;
        try {
            s = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }

    private BufferedReader getBufferedReader(String filename) {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(new File(filename)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bufferedReader;
    }
}
