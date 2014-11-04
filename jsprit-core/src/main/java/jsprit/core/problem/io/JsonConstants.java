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

/**
 * Created by stefan on 03.11.14.
 */
public class JsonConstants {

    public static final String SERVICES = "services";

    public static final String FLEET = "fleet_size";

    public static final String VEHICLES = "vehicles";

    public static final String VEHICLE_TYPES = "vehicle_types";

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

    public static class Vehicle {

        public static final String ID = "id";

        public static final String START_ADDRESS = "start_address";

        public static final String END_ADDRESS = "end_address";

        public static final String EARLIEST_START = "earliest_start";

        public static final String LATEST_END = "latest_end";

        public static final String SKILLS = "skills";

        public static final String CAPACITY = "capacity";

        public static final String TYPE_ID = "type_id";

        public static class Type {

            public static final String ID = "id";

            public static final String CAPACITY = "capacity";

            public static final String FIXED_COSTS = "fixed_costs";

            public static final String DISTANCE = "distance_dependent_costs";

            public static final String TIME = "time_dependent_costs";
        }
    }
}
