package jsprit.core.problem.io;

/**
 * Created by stefan on 03.11.14.
 */
public class JsonConstants {

    public static class Address {

        public static String ID = "id";

        public static String LON = "lon";

        public static String LAT = "lat";

    }

    public static class TimeWindow {

        public static final String START = "start";

        public static final String END = "end";
    }

    public static String JOB = "job";

    public static class Job {

        public static final String SIZE = "size";

        public static final String ADDRESS = "address";

        public static final String ID = "id";


        public static final String SERVICE_DURATION = "service_duration";

        public static final String NAME = "name";

        public static final String SKILLS = "required_skills";

        public static final String TIME_WINDOW = "time_window";

    }
}
