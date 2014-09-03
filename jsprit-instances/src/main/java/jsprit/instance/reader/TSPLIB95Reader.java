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

import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.util.Coordinate;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class TSPLIB95Reader {

    private VehicleRoutingProblem.Builder vrpBuilder;

    public TSPLIB95Reader(VehicleRoutingProblem.Builder vrpBuilder) {
        this.vrpBuilder = vrpBuilder;
    }

    public void read(String filename){
        BufferedReader reader = getBufferedReader(filename);
        String line;
        Coordinate[] coords = null;
        Integer[] demands = null;
        Integer capacity = null;
        List<Integer> depotIds = new ArrayList<Integer>();
        boolean isCoordSection = false;
        boolean isDemandSection = false;
        boolean isDepotSection = false;
        while( ( line = getLine(reader) ) != null ){
            if(line.startsWith("DIMENSION")){
                String[] tokens = line.split(":");
                String dim = tokens[1].trim();
                coords = new Coordinate[Integer.parseInt(dim)];
                demands = new Integer[Integer.parseInt(dim)];
                continue;
            }
            if(line.startsWith("CAPACITY")){
                String[] tokens = line.split(":");
                capacity = Integer.parseInt(tokens[1].trim());
                continue;
            }
            if(line.startsWith("NODE_COORD_SECTION")){
                isCoordSection = true;
                isDemandSection = false;
                isDepotSection = false;
                continue;
            }
            if(line.startsWith("DEMAND_SECTION")){
                isDemandSection = true;
                isCoordSection = false;
                isDepotSection = false;
                continue;
            }
            if(line.startsWith("DEPOT_SECTION")){
                isDepotSection = true;
                isDemandSection = false;
                isCoordSection = false;
                continue;
            }
            if(isCoordSection){
                if(coords == null) throw new IllegalStateException("DIMENSION tag missing");
                String[] tokens = line.split("\\s+");
                coords[Integer.parseInt(tokens[0]) - 1] = Coordinate.newInstance(Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2]));
                continue;
            }
            if(isDemandSection){
                if(demands == null) throw new IllegalStateException("DIMENSION tag missing");
                String[] tokens = line.split("\\s+");
                demands[Integer.parseInt(tokens[0]) - 1] = Integer.parseInt(tokens[1]);
                continue;
            }
            if(isDepotSection){
                if(line.equals("-1")){
                    isDepotSection = false;
                }
                else{
                    depotIds.add(Integer.parseInt(line));
                }
            }
        }
        close(reader);
        vrpBuilder.setFleetSize(VehicleRoutingProblem.FleetSize.INFINITE);
        for(Integer depotId : depotIds){
            VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("typeId").addCapacityDimension(0,capacity).build();
            VehicleImpl vehicle = VehicleImpl.Builder.newInstance("vehicle").setStartLocationId(depotId.toString())
                    .setStartLocationCoordinate(coords[depotId - 1]).setType(type).build();
            vrpBuilder.addVehicle(vehicle);
        }
        for(int i=0;i<demands.length;i++){
            if(demands[i] == 0) continue;
            String id = "" + (i+1);
            Service service = Service.Builder.newInstance(id).setLocationId(id).setCoord(coords[i]).addSizeDimension(0,demands[i]).build();
            vrpBuilder.addJob(service);
        }

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
