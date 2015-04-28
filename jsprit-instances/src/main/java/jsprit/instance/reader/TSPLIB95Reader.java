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
import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.util.Coordinate;
import jsprit.core.util.FastVehicleRoutingTransportCostsMatrix;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TSPLIB95Reader {

    private VehicleRoutingProblem.Builder vrpBuilder;

    private boolean switchCoordinates = false;

    public void setSwitchCoordinates(boolean switchCoordinates) {
        this.switchCoordinates = switchCoordinates;
    }

    public TSPLIB95Reader(VehicleRoutingProblem.Builder vrpBuilder) {
        this.vrpBuilder = vrpBuilder;
    }

    public void read(String filename){
        BufferedReader reader = getBufferedReader(filename);
        String line_;
        Coordinate[] coords = null;
        int[] demands = null;
        Integer capacity = null;
        String edgeType = null;
        String edgeWeightFormat = null;
        List<Integer> depotIds = new ArrayList<Integer>();
        boolean isCoordSection = false;
        boolean isDemandSection = false;
        boolean isDepotSection = false;
        boolean isEdgeWeightSection = false;
        List<Double> edgeWeights = new ArrayList<Double>();
        int dimensions = 0;
        int coordIndex = 0;
        Map<Integer,Integer> indexMap = new HashMap<Integer, Integer>();
        while( ( line_ = getLine(reader) ) != null ){
            String line = line_.trim();
            if(line.startsWith("EOF") || line.contains("EOF")){
                break;
            }
            if(line.startsWith("DIMENSION")){
                String[] tokens = line.split(":");
                String dim = tokens[1].trim();
                dimensions = Integer.parseInt(dim);
                coords = new Coordinate[dimensions];
                demands = new int[dimensions];
                continue;
            }
            if(line.startsWith("CAPACITY")){
                String[] tokens = line.trim().split(":");
                capacity = Integer.parseInt(tokens[1].trim());
                continue;
            }
            if(line.startsWith("EDGE_WEIGHT_TYPE")){
                String[] tokens = line.trim().split(":");
                edgeType = tokens[1].trim();
                continue;
            }
            if(line.startsWith("EDGE_WEIGHT_FORMAT")){
                String[] tokens = line.trim().split(":");
                edgeWeightFormat = tokens[1].trim();
                continue;
            }
            if(line.startsWith("NODE_COORD_SECTION")){
                isCoordSection = true;
                isDemandSection = false;
                isDepotSection = false;
                isEdgeWeightSection = false;
                continue;
            }
            if(line.startsWith("DEMAND_SECTION")){
                isDemandSection = true;
                isCoordSection = false;
                isDepotSection = false;
                isEdgeWeightSection = false;
                continue;
            }
            if(line.startsWith("DEPOT_SECTION")){
                isDepotSection = true;
                isDemandSection = false;
                isCoordSection = false;
                isEdgeWeightSection = false;
                continue;
            }
            if(line.startsWith("EDGE_WEIGHT_SECTION")){
                isDepotSection = false;
                isCoordSection = false;
                isDemandSection = false;
                isEdgeWeightSection = true;
                continue;
            }
            if(line.startsWith("DISPLAY_DATA_SECTION")){
                isDepotSection = false;
                isCoordSection = true;
                isDemandSection = false;
                isEdgeWeightSection = false;
                continue;
            }
            if(isCoordSection){
                if(coords == null) throw new IllegalStateException("DIMENSION tag missing");
                String[] tokens = line.trim().split("\\s+");
                Integer id = Integer.parseInt(tokens[0]);
                if(switchCoordinates){
                    coords[coordIndex] = Coordinate.newInstance(Double.parseDouble(tokens[2]), Double.parseDouble(tokens[1]));
                }
                else coords[coordIndex] = Coordinate.newInstance(Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2]));
                indexMap.put(id,coordIndex);
                coordIndex++;
                continue;
            }
            if(isDemandSection){
                if(demands == null) throw new IllegalStateException("DIMENSION tag missing");
                String[] tokens = line.trim().split("\\s+");
                Integer id = Integer.parseInt(tokens[0]);
                int index = indexMap.get(id);
                demands[index] = Integer.parseInt(tokens[1]);
                continue;
            }
            if(isDepotSection){
                if(line.equals("-1")){
                    isDepotSection = false;
                }
                else{
                    depotIds.add(Integer.parseInt(line));
                }
                continue;
            }
            if(isEdgeWeightSection){
                String[] tokens = line.trim().split("\\s+");
                for (String s : tokens) edgeWeights.add(Double.parseDouble(s));
                continue;
            }
        }
        close(reader);
        vrpBuilder.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);
        for(Integer depotId : depotIds){
            VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0,capacity).build();
            VehicleImpl vehicle = VehicleImpl.Builder.newInstance("vehicle")
                    .setStartLocation(Location.Builder.newInstance().setId(depotId.toString()).setCoordinate(coords[depotId - 1]).build())
                    .setType(type).build();
            vrpBuilder.addVehicle(vehicle);
        }

        for (Integer id_ : indexMap.keySet()) {
            String id = id_.toString();
            int index = indexMap.get(id_);
            if(depotIds.isEmpty()){
                if(index == 0) {
                    VehicleImpl vehicle = VehicleImpl.Builder.newInstance("traveling_salesman")
                            .setStartLocation(Location.Builder.newInstance().setId(id)
                                    .setCoordinate(coords[index]).setIndex(index).build())
                                    .build();
                    vrpBuilder.addVehicle(vehicle);
                    continue;
                }
            }
            Service service = Service.Builder.newInstance(id)
                    .setLocation(Location.Builder.newInstance().setId(id)
                            .setCoordinate(coords[index]).setIndex(index).build())
                    .addSizeDimension(0, demands[index]).build();
            vrpBuilder.addJob(service);
        }
        if(edgeType.equals("GEO")){
            List<Location> locations = new ArrayList<Location>();
            for(Vehicle v : vrpBuilder.getAddedVehicles()) locations.add(v.getStartLocation());
            for(Job j : vrpBuilder.getAddedJobs()) locations.add(((Service)j).getLocation());
            vrpBuilder.setRoutingCost(getGEOMatrix(locations));
        }
        else if(edgeType.equals("EXPLICIT")){
            if(edgeWeightFormat.equals("UPPER_ROW")){
                FastVehicleRoutingTransportCostsMatrix.Builder matrixBuilder = FastVehicleRoutingTransportCostsMatrix.Builder.newInstance(dimensions,true);
                int fromIndex = 0;
                int toIndex = 1;
                for(int i=0;i<edgeWeights.size();i++){
                    if(toIndex == dimensions){
                        fromIndex++;
                        toIndex = fromIndex + 1;
                    }
                    matrixBuilder.addTransportDistance(fromIndex, toIndex, edgeWeights.get(i));
                    matrixBuilder.addTransportTime(fromIndex,toIndex,edgeWeights.get(i));
                    toIndex++;
                }
                vrpBuilder.setRoutingCost(matrixBuilder.build());
            }
            else if(edgeWeightFormat.equals("UPPER_DIAG_ROW")){
                FastVehicleRoutingTransportCostsMatrix.Builder matrixBuilder = FastVehicleRoutingTransportCostsMatrix.Builder.newInstance(dimensions,true);
                int fromIndex = 0;
                int toIndex = 0;
                for(int i=0;i<edgeWeights.size();i++){
                    if(toIndex == dimensions){
                        fromIndex++;
                        toIndex = fromIndex;
                    }
                    matrixBuilder.addTransportDistance(fromIndex,toIndex,edgeWeights.get(i));
                    matrixBuilder.addTransportTime(fromIndex,toIndex,edgeWeights.get(i));
                    toIndex++;
                }
                vrpBuilder.setRoutingCost(matrixBuilder.build());
            }
            else if(edgeWeightFormat.equals("LOWER_DIAG_ROW")){
                FastVehicleRoutingTransportCostsMatrix.Builder matrixBuilder = FastVehicleRoutingTransportCostsMatrix.Builder.newInstance(dimensions,true);
                int fromIndex = 0;
                int toIndex = 0;
                for(int i=0;i<edgeWeights.size();i++){
                    if(toIndex > fromIndex){
                        fromIndex++;
                        toIndex = 0;
                    }
                    matrixBuilder.addTransportDistance(fromIndex,toIndex,edgeWeights.get(i));
                    matrixBuilder.addTransportTime(fromIndex,toIndex,edgeWeights.get(i));
                    toIndex++;
                }
                vrpBuilder.setRoutingCost(matrixBuilder.build());
            }
            else if(edgeWeightFormat.equals("FULL_MATRIX")){
                FastVehicleRoutingTransportCostsMatrix.Builder matrixBuilder = FastVehicleRoutingTransportCostsMatrix.Builder.newInstance(dimensions,false);
                int fromIndex = 0;
                int toIndex = 0;
                for(int i=0;i<edgeWeights.size();i++){
                    if(toIndex == dimensions){
                        fromIndex++;
                        toIndex = 0;
                    }
                    matrixBuilder.addTransportDistance(fromIndex,toIndex,edgeWeights.get(i));
                    matrixBuilder.addTransportTime(fromIndex,toIndex,edgeWeights.get(i));
                    toIndex++;
                }
                vrpBuilder.setRoutingCost(matrixBuilder.build());
            }



        }


    }

    private VehicleRoutingTransportCosts getGEOMatrix(List<Location> noLocations) {
        FastVehicleRoutingTransportCostsMatrix.Builder matrixBuilder = FastVehicleRoutingTransportCostsMatrix.Builder.newInstance(noLocations.size(),true);
        for(Location i : noLocations){
            for(Location j : noLocations){
                matrixBuilder.addTransportDistance(i.getIndex(),j.getIndex(),getDistance(i,j));
                matrixBuilder.addTransportTime(i.getIndex(), j.getIndex(), getDistance(i, j));
            }
        }
        return matrixBuilder.build();
    }

    private double getDistance(Location from, Location to) {
        double longitude_from = getLongitude(from);
        double longitude_to = getLongitude(to);
        double latitude_from = getLatitude(from);
        double latitude_to = getLatitude(to);
        double q1 = Math.cos( longitude_from - longitude_to);
        double q2 = Math.cos( latitude_from - latitude_to);
        double q3 = Math.cos( latitude_from + latitude_to);
        return 6378.388 * Math.acos( .5 * ( ( 1. + q1 ) * q2 - ( 1. - q1 ) * q3 ) ) + 1.;
    }

    private double getLatitude(Location loc) {
        int deg = (int) loc.getCoordinate().getX();
        double min = loc.getCoordinate().getX() - deg;
        return Math.PI * (deg + 5. * min / 3.) / 180.;
    }

    private double getLongitude(Location loc) {
        int deg = (int) loc.getCoordinate().getY();
        double min = loc.getCoordinate().getY() - deg;
        return Math.PI * (deg + 5. * min / 3.) / 180.;
    }



    private void close(BufferedReader reader) {
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ;
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
