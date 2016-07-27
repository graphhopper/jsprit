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

package com.graphhopper.jsprit.core.algorithm.state;

public class InternalStates {


    public final static StateId MAXLOAD = new StateFactory.StateIdImpl("max_load", 0);

    public final static StateId LOAD = new StateFactory.StateIdImpl("load", 1);

    public final static StateId COSTS = new StateFactory.StateIdImpl("costs", 2);

    public final static StateId LOAD_AT_BEGINNING = new StateFactory.StateIdImpl("load_at_beginning", 3);

    public final static StateId LOAD_AT_END = new StateFactory.StateIdImpl("load_at_end", 4);

    public final static StateId DURATION = new StateFactory.StateIdImpl("duration", 5);

    public final static StateId LATEST_OPERATION_START_TIME = new StateFactory.StateIdImpl("latest_operation_start_time", 6);

    public final static StateId EARLIEST_OPERATION_START_TIME = new StateFactory.StateIdImpl("earliest_operation_start_time", 7);

    public final static StateId FUTURE_MAXLOAD = new StateFactory.StateIdImpl("future_max_load", 8);

    public final static StateId PAST_MAXLOAD = new StateFactory.StateIdImpl("past_max_load", 9);

    public static final StateId SKILLS = new StateFactory.StateIdImpl("skills", 10);

    public static final StateId WAITING = new StateFactory.StateIdImpl("waiting", 11);

    public static final StateId TIME_SLACK = new StateFactory.StateIdImpl("time_slack", 12);

    public static final StateId FUTURE_WAITING = new StateFactory.StateIdImpl("future_waiting", 13);

    public static final StateId EARLIEST_WITHOUT_WAITING = new StateFactory.StateIdImpl("earliest_without_waiting", 14);

    public static final StateId SWITCH_NOT_FEASIBLE = new StateFactory.StateIdImpl("switch_not_feasible", 15);
}
