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

    public static class Solution {

        public static final String COSTS = "costs";

        public static final String DISTANCE = "distance";

        public static final String TIME = "time";

        public static final String ROUTES = "routes";

        public static final String NO_ROUTES = "no_routes";

        public static final String NO_UNASSIGNED = "no_unassigned_jobs";

        public static final String FIXED_COSTS = "fixed_costs";

        public static final String VARIABLE_COSTS = "variable_costs";

        public static class Route {

            public static final String COSTS = "costs";

            public static final String DISTANCE = "distance";

            public static final String TIME = "time";

            public static final String VEHICLE_ID = "vehicle_id";

            public static final String START_TIME = "start_time";

            public static final String END_TIME = "end_time";

            public static final String ACTIVITY = "act";

            public static class Activity {

                public static final String TYPE = "type";

                public static final String SERVICE_ID = "job_id";

                public static final String ARR_TIME = "arr_time";

                public static final String END_TIME = "end_time";

            }

        }

    }

    public static class Address {

        public static String ID = "id";

        public static String LON = "lon";

        public static String LAT = "lat";

    }

    public static class TimeWindow {

        public static final String START = "start";

        public static final String END = "end";
    }

    public static class Job {

        public static final String SIZE = "size";

        public static final String ADDRESS = "address";

        public static final String ID = "id";


        public static final String SERVICE_DURATION = "service_duration";

        public static final String NAME = "name";

        public static final String SKILLS = "required_skills";

        public static final String TIME_WINDOW = "time_window";

        public static final String TYPE = "type";

        public static final String PICKUP = "pickup";

        public static final String DELIVERY = "delivery";

        public static final String SERVICE = "service";
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

        public static final String RETURN_TO_DEPOT = "return_to_depot";

        public static class Type {

            public static final String ID = "id";

            public static final String CAPACITY = "capacity";

            public static final String FIXED_COSTS = "fixed_costs";

            public static final String DISTANCE = "distance_dependent_costs";

            public static final String TIME = "time_dependent_costs";
        }
    }
}
