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

package jsprit.core.problem.io;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.route.activity.TimeWindow;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.util.Coordinate;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by stefan on 03.11.14.
 */
public class VrpJsonWriter {

    private final VehicleRoutingProblem vrp;

    public VrpJsonWriter(VehicleRoutingProblem vrp) {
        this.vrp = vrp;
    }

    public void write(String filename){
        try {
            JsonGenerator jsonGenerator = new JsonFactory().createGenerator(new FileOutputStream(filename));
            jsonGenerator.setPrettyPrinter(new DefaultPrettyPrinter());
            jsonGenerator.writeStartObject();

            jsonGenerator.writeStringField(JsonConstants.FLEET,vrp.getFleetSize().toString());
            writeVehicles(jsonGenerator);
//            writeVehicleTypes(jsonGenerator);
            writeServices(jsonGenerator);




            jsonGenerator.writeEndObject();

            jsonGenerator.flush();
            jsonGenerator.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeVehicles(JsonGenerator jsonGenerator) {
        try {
            jsonGenerator.writeArrayFieldStart(JsonConstants.VEHICLES);
            for(Vehicle vehicle : vrp.getVehicles()){
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField(JsonConstants.Vehicle.ID,vehicle.getId());
                jsonGenerator.writeObjectFieldStart(JsonConstants.Vehicle.START_ADDRESS);
                    jsonGenerator.writeStringField(JsonConstants.Address.ID, vehicle.getStartLocationId());
                    jsonGenerator.writeNumberField(JsonConstants.Address.LON, vehicle.getStartLocationCoordinate().getX());
                    jsonGenerator.writeNumberField(JsonConstants.Address.LAT,vehicle.getStartLocationCoordinate().getY());
                jsonGenerator.writeEndObject();
                if(!(vehicle.getStartLocationCoordinate().equals(vehicle.getEndLocationCoordinate())
                        && vehicle.getStartLocationId().equals(vehicle.getEndLocationId()))){
                    jsonGenerator.writeObjectFieldStart(JsonConstants.Vehicle.END_ADDRESS);
                    jsonGenerator.writeStringField(JsonConstants.Address.ID, vehicle.getEndLocationId());
                    jsonGenerator.writeNumberField(JsonConstants.Address.LON, vehicle.getEndLocationCoordinate().getX());
                    jsonGenerator.writeNumberField(JsonConstants.Address.LAT,vehicle.getEndLocationCoordinate().getY());
                    jsonGenerator.writeEndObject();
                }

                jsonGenerator.writeEndObject();
            }
            jsonGenerator.writeEndArray();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void writeServices(JsonGenerator jsonGenerator) {
        try {
            jsonGenerator.writeArrayFieldStart(JsonConstants.SERVICES);
            for(Job job : vrp.getJobs().values()){
                if(!(job instanceof Service)) continue;
                Service service = (Service)job;
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField(JsonConstants.Job.ID,service.getId());
                jsonGenerator.writeStringField(JsonConstants.Job.NAME,service.getName());
                jsonGenerator.writeObjectFieldStart(JsonConstants.Job.ADDRESS);
                jsonGenerator.writeStringField(JsonConstants.Address.ID, service.getLocationId());
                jsonGenerator.writeNumberField(JsonConstants.Address.LON, service.getCoord().getX());
                jsonGenerator.writeNumberField(JsonConstants.Address.LAT,service.getCoord().getY());
                jsonGenerator.writeEndObject();
                jsonGenerator.writeNumberField(JsonConstants.Job.SERVICE_DURATION, service.getServiceDuration());

                jsonGenerator.writeObjectFieldStart(JsonConstants.Job.TIME_WINDOW);
                jsonGenerator.writeNumberField(JsonConstants.TimeWindow.START,service.getTimeWindow().getStart());
                jsonGenerator.writeNumberField(JsonConstants.TimeWindow.END,service.getTimeWindow().getEnd());
                jsonGenerator.writeEndObject();

                jsonGenerator.writeArrayFieldStart(JsonConstants.Job.SIZE);
                for(int i=0;i<service.getSize().getNuOfDimensions();i++){
                    jsonGenerator.writeNumber(service.getSize().get(i));
                }
                jsonGenerator.writeEndArray();

                jsonGenerator.writeArrayFieldStart(JsonConstants.Job.SKILLS);
                for(String skill : service.getRequiredSkills().values()){
                    jsonGenerator.writeString(skill);
                }
                jsonGenerator.writeEndArray();
                jsonGenerator.writeEndObject();
            }
            jsonGenerator.writeEndArray();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        Service service = Service.Builder.newInstance("s1").setLocationId("s1_loc").setCoord(Coordinate.newInstance(40, 10))
                .addSizeDimension(0, 20).addSizeDimension(1, 40)
                .setServiceTime(100.)
                .setTimeWindow(TimeWindow.newInstance(10, 20))
                .addRequiredSkill("drilling-machine")
                .addRequiredSkill("screw-driver").build();
        Service service2 = Service.Builder.newInstance("s2").setLocationId("s2_loc").setCoord(Coordinate.newInstance(40, 10))
                .addSizeDimension(0, 20).addSizeDimension(1, 40)
                .setServiceTime(100.)
                .setTimeWindow(TimeWindow.newInstance(10, 20))
                .addRequiredSkill("screw-driver").build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocationId("startLoc").setStartLocationCoordinate(Coordinate.newInstance(0,0))
                .setEndLocationId("endLoc").setEndLocationCoordinate(Coordinate.newInstance(12, 12)).build();

        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocationId("startLoc").setStartLocationCoordinate(Coordinate.newInstance(0,0))
                .build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(service).addJob(service2)
                .addVehicle(v1).addVehicle(v2).build();
        new VrpJsonWriter(vrp).write("output/vrp.json");
    }
}
