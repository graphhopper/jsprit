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
            for(String skill : skills) this.skills.add(skill);
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

    /**
     * Not case sensitive.
     *
     * @param skill which is checked whether it is in skill container or not
     * @return true if skill is included, false otherwise
     */
    public boolean containsSkill(String skill){
        return skills.contains(skill.toLowerCase());
    }




}
