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

package jsprit.core.problem;

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
        public static Builder newInstance(){
            return new Builder();
        }

        private Set<String> skills = new HashSet<String>();

        /**
         * Adds skill. Skill is transformed into lowerCase.
         *
         * @param skill skill to be added
         * @return builder
         */
        public Builder addSkill(String skill){
            skills.add(skill.toLowerCase());
            return this;
        }

        /**
         * Adds a collection of skills.
         *
         * @param skills collection of skills to be added
         * @return builder
         */
        public Builder addAllSkills(Collection<String> skills){
            for(String skill : skills) addSkill(skill);
            return this;
        }

        /**
         * Builds the skill container and returns it.
         *
         * @return skills
         */
        public Skills build(){
            return new Skills(this);
        }

    }

    private Set<String> skills = new HashSet<String>();

    private Skills(Builder builder){
        skills.addAll(builder.skills);
    }

    /**
     * Returns an unmodifiable set of skills. All skills are inLowerCase.
     *
     * @return set of skills in this containter
     */
    public Set<String> values(){
        return Collections.unmodifiableSet(skills);
    }

    public String toString(){
        String s = "[";
        boolean first = true;
        for(String skill : values()){
            if(first){
                s += skill;
                first = false;
            }
            else s += ", " + skill;
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
    public boolean containsSkill(String skill){
        return skills.contains(skill.toLowerCase());
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
