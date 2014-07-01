package jsprit.core.problem;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by schroeder on 01.07.14.
 */
public class Skills {

    public static class Builder {

        /**
         * Returns new instance of skill-builder.
         *
         * @return
         */
        public static Builder newInstance(){
            return new Builder();
        }

        private Set<String> skills = new HashSet<String>();

        /**
         * Adds skill. Skill is transformed into lowerCase.
         *
         * @param skill
         * @return
         */
        public Builder addSkill(String skill){
            skills.add(skill.toLowerCase());
            return this;
        }

        public Builder addAllSkills(Collection<String> skills){
            for(String skill : skills) this.skills.add(skill);
            return this;
        }

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
     * @return
     */
    public Set<String> values(){
        return Collections.unmodifiableSet(skills);
    }

    /**
     * Not case sensitive.
     *
     * @param skill
     * @return
     */
    public boolean containsSkill(String skill){
        return skills.contains(skill.toLowerCase());
    }




}
