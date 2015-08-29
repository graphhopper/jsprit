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
package jsprit.core.problem.job;

import jsprit.core.problem.Location;
import org.junit.Test;

import static org.junit.Assert.*;

public class DeliveryTest {

    @Test(expected = IllegalStateException.class)
    public void whenNeitherLocationIdNorCoordIsSet_itThrowsException() {
        Delivery.Builder.newInstance("p").build();
    }

    @Test
    public void whenAddingTwoCapDimension_nuOfDimsShouldBeTwo() {
        Delivery one = (Delivery) Delivery.Builder.newInstance("s").setLocation(Location.newInstance("foofoo"))
            .addSizeDimension(0, 2)
            .addSizeDimension(1, 4)
            .build();
        assertEquals(2, one.getSize().getNuOfDimensions());
        assertEquals(2, one.getSize().get(0));
        assertEquals(4, one.getSize().get(1));

    }

    @Test
    public void whenPickupIsBuiltWithoutSpecifyingCapacity_itShouldHvCapWithOneDimAndDimValOfZero() {
        Delivery one = (Delivery) Delivery.Builder.newInstance("s").setLocation(Location.newInstance("foofoo"))
            .build();
        assertEquals(1, one.getSize().getNuOfDimensions());
        assertEquals(0, one.getSize().get(0));
    }

    @Test
    public void whenPickupIsBuiltWithConstructorWhereSizeIsSpecified_capacityShouldBeSetCorrectly() {
        Delivery one = (Delivery) Delivery.Builder.newInstance("s").addSizeDimension(0, 1).setLocation(Location.newInstance("foofoo"))
            .build();
        assertEquals(1, one.getSize().getNuOfDimensions());
        assertEquals(1, one.getSize().get(0));
    }

    @Test
    public void whenAddingSkills_theyShouldBeAddedCorrectly() {
        Delivery s = (Delivery) Delivery.Builder.newInstance("s").setLocation(Location.newInstance("loc"))
            .addRequiredSkill("drill").addRequiredSkill("screwdriver").build();
        assertTrue(s.getRequiredSkills().containsSkill("drill"));
        assertTrue(s.getRequiredSkills().containsSkill("ScrewDriver"));
    }

    @Test
    public void whenAddingSkillsCaseSens_theyShouldBeAddedCorrectly() {
        Delivery s = (Delivery) Delivery.Builder.newInstance("s").setLocation(Location.newInstance("loc"))
            .addRequiredSkill("DriLl").addRequiredSkill("screwDriver").build();
        assertTrue(s.getRequiredSkills().containsSkill("drill"));
        assertTrue(s.getRequiredSkills().containsSkill("drilL"));
    }

    @Test
    public void whenAddingSkillsCaseSensV2_theyShouldBeAddedCorrectly() {
        Delivery s = (Delivery) Delivery.Builder.newInstance("s").setLocation(Location.newInstance("loc"))
            .addRequiredSkill("screwDriver").build();
        assertFalse(s.getRequiredSkills().containsSkill("drill"));
        assertFalse(s.getRequiredSkills().containsSkill("drilL"));
    }

    @Test
    public void nameShouldBeAssigned() {
        Delivery s = (Delivery) Delivery.Builder.newInstance("s").setLocation(Location.newInstance("loc"))
            .setName("name").build();
        assertEquals("name", s.getName());
    }


}
