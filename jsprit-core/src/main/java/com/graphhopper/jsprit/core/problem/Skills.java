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

package com.graphhopper.jsprit.core.problem;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Skill container managing skills
 */
public class Skills {

    public static class Builder {

        /**
         * Returns new instance of skill-builder.
         *
         * @return builder
         */
        public static Builder newInstance() {
            return new Builder();
        }

        private Set<String> skills = new HashSet<String>();

        /**
         * Adds skill. Skill is transformed into lowerCase.
         *
         * @param skill skill to be added
         * @return builder
         */
        public Builder addSkill(String skill) {
            skills.add(skill.trim().toLowerCase());
            return this;
        }

        /**
         * Adds a collection of skills.
         *
         * @param skills collection of skills to be added
         * @return builder
         */
        public Builder addAllSkills(Collection<String> skills) {
            for (String skill : skills) addSkill(skill);
            return this;
        }

        /**
         * Builds the skill container and returns it.
         *
         * @return skills
         */
        public Skills build() {
            return new Skills(this);
        }

    }

    private Set<String> skills = new HashSet<String>();

    private Skills(Builder builder) {
        skills.addAll(builder.skills);
    }

    /**
     * Returns an unmodifiable set of skills. All skills are inLowerCase.
     *
     * @return set of skills in this containter
     */
    public Set<String> values() {
        return Collections.unmodifiableSet(skills);
    }

    public String toString() {
        String s = "[";
        boolean first = true;
        for (String skill : values()) {
            if (first) {
                s += skill;
                first = false;
            } else s += ", " + skill;
        }
        s += "]";
        return s;
    }

    /**
     * Not case sensitive.
     *
     * @param skill which is checked whether it is in skill container or not
     * @return true if skill is included, false otherwise
     */
    public boolean containsSkill(String skill) {
        return skills.contains(skill.trim().toLowerCase());// trim to be consistent with addSkill()
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Skills skills1 = (Skills) o;

        if (skills != null ? !skills.equals(skills1.skills) : skills1.skills != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return skills != null ? skills.hashCode() : 0;
    }
}
