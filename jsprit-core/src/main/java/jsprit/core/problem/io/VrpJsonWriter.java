package jsprit.core.problem.io;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.route.activity.TimeWindow;
import jsprit.core.util.Coordinate;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by stefan on 03.11.14.
 */
public class VrpJsonWriter {

    private Service service;

    public VrpJsonWriter(Service service) {
        this.service = service;
    }

    public void write(String filename){
        try {
            JsonGenerator jsonGenerator = new JsonFactory().createGenerator(new FileOutputStream(filename));
            jsonGenerator.setPrettyPrinter(new DefaultPrettyPrinter());
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

            jsonGenerator.flush();
            jsonGenerator.close();

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
        new VrpJsonWriter(service).write("output/jsonwriter.json");
    }
}
