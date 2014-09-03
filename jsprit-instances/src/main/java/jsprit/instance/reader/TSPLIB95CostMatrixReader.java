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


import jsprit.core.util.VehicleRoutingTransportCostsMatrix;

import java.io.*;

public class TSPLIB95CostMatrixReader {

    private VehicleRoutingTransportCostsMatrix.Builder costMatrixBuilder;

    public TSPLIB95CostMatrixReader(VehicleRoutingTransportCostsMatrix.Builder costMatrixBuilder) {
        this.costMatrixBuilder = costMatrixBuilder;
    }

    public void read(String matrixFile){
        BufferedReader reader = getBufferedReader(matrixFile);
        String line;
        boolean isEdgeWeights = false;
        int fromIndex = 0;
        while( ( line = getLine(reader) ) != null ){
            if(line.startsWith("EDGE_WEIGHT_SECTION")){
                isEdgeWeights = true;
                continue;
            }
            if(line.startsWith("DEMAND_SECTION")){
                isEdgeWeights = false;
                continue;
            }
            if(isEdgeWeights){
                String[] tokens = line.split("\\s+");
                String fromId = "" + (fromIndex + 1);
                for(int i=0;i<tokens.length;i++){
                    double distance = Double.parseDouble(tokens[i]);
                    String toId = "" + (i+1);
                    costMatrixBuilder.addTransportDistance(fromId,toId,distance);
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
