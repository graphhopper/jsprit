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
package com.graphhopper.jsprit.core.problem.vehicle;

import com.graphhopper.jsprit.core.problem.AbstractVehicle;
import com.graphhopper.jsprit.core.problem.Skills;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;

import java.util.*;

/**
 * Key to identify similar vehicles
 * <p>
 * <p>Two vehicles are equal if they share the same type, the same start and end-location and the same earliestStart and latestStart.
 *
 * @author stefan
 */
public class VehicleTypeKey extends AbstractVehicle.AbstractTypeKey {

    private final static String VEHICLE_ID_SKILL_PREFIX = "#vehicle_id#_";

    public final String type;
    public final String startLocationId;
    public final String endLocationId;
    public final double earliestStart;
    public final double latestEnd;
    public final Skills skills;
    public final boolean returnToDepot;
    public final Object userData;
    public final double earliestBreakStart;
    public final double latestBreakStart;
    public final double breakDuration;
    private Set<String> prohibitedTasks = new HashSet<>();

    public VehicleTypeKey(String typeId, String startLocationId, String endLocationId, double earliestStart, double latestEnd, Skills skills, Collection<String> prohibitedTasks, boolean returnToDepot, Object userData, double earliestBreakStart, double latestBreakStart, double breakDuration) {
        this.type = typeId;
        this.startLocationId = startLocationId;
        this.endLocationId = endLocationId;
        this.earliestStart = earliestStart;
        this.latestEnd = latestEnd;
        this.skills = getSkillsWithoutVehicleId(skills);
        this.returnToDepot = returnToDepot;
        this.userData = userData;
        this.earliestBreakStart = earliestBreakStart;
        this.latestBreakStart = latestBreakStart;
        this.breakDuration = breakDuration;
        this.prohibitedTasks.addAll(prohibitedTasks);
    }

    private Skills getSkillsWithoutVehicleId(Skills skills){
        final Set<String> skillsWithoutVehicleId = new HashSet<>();
        for (String skill : skills.values()) {
            if (!skill.toLowerCase().startsWith(VEHICLE_ID_SKILL_PREFIX)) {
                skillsWithoutVehicleId.add(skill);
            }
        }
        return Skills.Builder.newInstance().addAllSkills(skillsWithoutVehicleId).build();
    }

    public VehicleTypeKey(String typeId, String startLocationId, String endLocationId, double earliestStart, double latestEnd, Skills skills, Collection<String> prohibitedTasks, boolean returnToDepot, Object userData) {
        this(typeId, startLocationId, endLocationId, earliestStart, latestEnd, skills, prohibitedTasks, returnToDepot, userData, 0, Double.MAX_VALUE, 0);
    }

    public VehicleTypeKey(String typeId, String startLocationId, String endLocationId, double earliestStart, double latestEnd, Skills skills, Collection<String> prohibitedTasks, boolean returnToDepot) {
        this(typeId, startLocationId, endLocationId, earliestStart, latestEnd, skills, prohibitedTasks, returnToDepot, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VehicleTypeKey that = (VehicleTypeKey) o;

        if (Double.compare(that.earliestStart, earliestStart) != 0) return false;
        if (Double.compare(that.latestEnd, latestEnd) != 0) return false;
        if (returnToDepot != that.returnToDepot) return false;
        if (!endLocationId.equals(that.endLocationId)) return false;
        if (!skills.equals(that.skills)) return false;
        if (!startLocationId.equals(that.startLocationId)) return false;
        if (!type.equals(that.type)) return false;
        if (!Objects.equals(that.userData, this.userData)) return false;
        if (Double.compare(that.breakDuration, breakDuration) != 0) return false;
        if (Double.compare(that.earliestBreakStart, earliestBreakStart) != 0) return false;
        if (Double.compare(that.latestBreakStart, latestBreakStart) != 0) return false;
        if (prohibitedTasks.size() != that.prohibitedTasks.size() || !prohibitedTasks.containsAll(that.prohibitedTasks)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = type.hashCode();
        result = 31 * result + startLocationId.hashCode();
        result = 31 * result + endLocationId.hashCode();
        temp = Double.doubleToLongBits(earliestStart);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(latestEnd);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + skills.hashCode();
        result = 31 * result + (returnToDepot ? 1 : 0);
        if (userData != null)
            result = 31 * result + userData.hashCode();

        temp = Double.doubleToLongBits(breakDuration);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(earliestBreakStart);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(latestBreakStart);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + prohibitedTasks.hashCode();

        return result;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(type).append("_").append(startLocationId).append("_").append(endLocationId)
            .append("_").append(Double.toString(earliestStart)).append("_").append(Double.toString(latestEnd));
        if (userData != null)
            stringBuilder.append("_").append(userData.toString()).append("_");

        for (String t : prohibitedTasks)
            stringBuilder.append(t).append("_");

        return stringBuilder.toString();
    }


}
