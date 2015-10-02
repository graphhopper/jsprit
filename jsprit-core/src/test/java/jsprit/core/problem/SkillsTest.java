package jsprit.core.problem;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * Created by schroeder on 01.07.14.
 */
public class SkillsTest {

    @Test
    public void whenSkillsAdded_theyShouldBeinSkillSet() {
        Skills skills = Skills.Builder.newInstance().addSkill("skill1").addSkill("skill2").build();
        assertTrue(skills.containsSkill("skill1"));
        assertTrue(skills.containsSkill("skill2"));
    }

    @Test
    public void whenSkillsAddedCaseInsensitive_theyShouldBeinSkillSet() {
        Skills skills = Skills.Builder.newInstance().addSkill("skill1").addSkill("skill2").build();
        assertTrue(skills.containsSkill("skilL1"));
        assertTrue(skills.containsSkill("skIll2"));
    }

    @Test
    public void whenSkillsAddedCaseInsensitive2_theyShouldBeinSkillSet() {
        Skills skills = Skills.Builder.newInstance().addSkill("Skill1").addSkill("skill2").build();
        assertTrue(skills.containsSkill("skilL1"));
        assertTrue(skills.containsSkill("skIll2"));
    }

    @Test
    public void whenSkillsAddedThroughAddAll_theyShouldBeinSkillSet() {
        Set<String> skillSet = new HashSet<String>();
        skillSet.add("skill1");
        skillSet.add("skill2");
        Skills skills = Skills.Builder.newInstance().addAllSkills(skillSet).build();
        assertTrue(skills.containsSkill("skill1"));
        assertTrue(skills.containsSkill("skill2"));
    }

    @Test
    public void whenSkillsAddedThroughAddAllCaseInsensitive_theyShouldBeinSkillSet() {
        Set<String> skillSet = new HashSet<String>();
        skillSet.add("skill1");
        skillSet.add("skill2");
        Skills skills = Skills.Builder.newInstance().addAllSkills(skillSet).build();
        assertTrue(skills.containsSkill("skilL1"));
        assertTrue(skills.containsSkill("skill2"));
    }

    @Test
    public void whenSkillsAddedThroughAddAllCaseInsensitive2_theyShouldBeinSkillSet() {
        Set<String> skillSet = new HashSet<String>();
        skillSet.add("skill1");
        skillSet.add("Skill2");
        Skills skills = Skills.Builder.newInstance().addAllSkills(skillSet).build();
        assertTrue(skills.containsSkill("skill1"));
        assertTrue(skills.containsSkill("skill2"));
    }

    @Test
    public void whenSkillsAddedPrecedingWhitespaceShouldNotMatter() {
        Set<String> skillSet = new HashSet<String>();
        skillSet.add(" skill1");
        skillSet.add("Skill2");
        Skills skills = Skills.Builder.newInstance().addAllSkills(skillSet).build();
        assertTrue(skills.containsSkill("skill1"));
        assertTrue(skills.containsSkill("skill2"));
    }

    @Test
    public void whenSkillsAddedTrailingWhitespaceShouldNotMatter() {
        Set<String> skillSet = new HashSet<String>();
        skillSet.add("skill1 ");
        skillSet.add("Skill2");
        Skills skills = Skills.Builder.newInstance().addAllSkills(skillSet).build();
        assertTrue(skills.containsSkill("skill1"));
        assertTrue(skills.containsSkill("skill2"));
    }

    @Test
    public void whenSkillsAddedTrailingWhitespaceShouldNotMatter2() {
        Skills skills = Skills.Builder.newInstance().addSkill("skill1 ").build();
        assertTrue(skills.containsSkill("skill1"));
    }

}
