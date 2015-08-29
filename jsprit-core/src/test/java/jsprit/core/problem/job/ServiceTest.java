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
import jsprit.core.problem.solution.route.activity.TimeWindow;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class ServiceTest {

    @Test
    public void whenTwoServicesHaveTheSameId_theirReferencesShouldBeUnEqual() {
        Service one = Service.Builder.newInstance("service").addSizeDimension(0, 10).setLocation(Location.newInstance("foo")).build();
        Service two = Service.Builder.newInstance("service").addSizeDimension(0, 10).setLocation(Location.newInstance("fo")).build();

        assertTrue(one != two);
    }

    @Test
    public void whenTwoServicesHaveTheSameId_theyShouldBeEqual() {
        Service one = Service.Builder.newInstance("service").addSizeDimension(0, 10).setLocation(Location.newInstance("foo")).build();
        Service two = Service.Builder.newInstance("service").addSizeDimension(0, 10).setLocation(Location.newInstance("fo")).build();

        assertTrue(one.equals(two));
    }

    @Test
    public void noName() {
        Set<Service> serviceSet = new HashSet<Service>();
        Service one = Service.Builder.newInstance("service").addSizeDimension(0, 10).setLocation(Location.newInstance("foo")).build();
        Service two = Service.Builder.newInstance("service").addSizeDimension(0, 10).setLocation(Location.newInstance("fo")).build();
        serviceSet.add(one);
//		assertTrue(serviceSet.contains(two));
        serviceSet.remove(two);
        assertTrue(serviceSet.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenCapacityDimValueIsNegative_throwIllegalStateExpception() {
        @SuppressWarnings("unused")
        Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance("foo")).addSizeDimension(0, -10).build();
    }

    @Test
    public void whenAddingTwoCapDimension_nuOfDimsShouldBeTwo() {
        Service one = Service.Builder.newInstance("s").setLocation(Location.newInstance("foofoo"))
            .addSizeDimension(0, 2)
            .addSizeDimension(1, 4)
            .build();
        assertEquals(2, one.getSize().getNuOfDimensions());
    }

    @Test
    public void whenShipmentIsBuiltWithoutSpecifyingCapacity_itShouldHvCapWithOneDimAndDimValOfZero() {
        Service one = Service.Builder.newInstance("s").setLocation(Location.newInstance("foofoo"))
            .build();
        assertEquals(1, one.getSize().getNuOfDimensions());
        assertEquals(0, one.getSize().get(0));
    }

    @Test
    public void whenShipmentIsBuiltWithConstructorWhereSizeIsSpecified_capacityShouldBeSetCorrectly() {
        Service one = Service.Builder.newInstance("s").addSizeDimension(0, 1).setLocation(Location.newInstance("foofoo"))
            .build();
        assertEquals(1, one.getSize().getNuOfDimensions());
        assertEquals(1, one.getSize().get(0));
    }

    @Test
    public void whenCallingForNewInstanceOfBuilder_itShouldReturnBuilderCorrectly() {
        Service.Builder builder = Service.Builder.newInstance("s");
        assertNotNull(builder);
    }

    @Test
    public void whenSettingNoType_itShouldReturn_service() {
        Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance("loc")).build();
        assertEquals("service", s.getType());
    }

    @Test
    public void whenSettingLocation_itShouldBeSetCorrectly() {
        Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance("loc")).build();
        assertEquals("loc", s.getLocation().getId());
        assertEquals("loc", s.getLocation().getId());
    }

    @Test
    public void whenSettingLocation_itShouldWork() {
        Service s = Service.Builder.newInstance("s").setLocation(Location.Builder.newInstance().setId("loc").build()).build();
        assertEquals("loc", s.getLocation().getId());
        assertEquals("loc", s.getLocation().getId());
    }


    @Test
    public void whenSettingLocationCoord_itShouldBeSetCorrectly() {
        Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance(1, 2)).build();
        assertEquals(1.0, s.getLocation().getCoordinate().getX(), 0.01);
        assertEquals(2.0, s.getLocation().getCoordinate().getY(), 0.01);
        assertEquals(1.0, s.getLocation().getCoordinate().getX(), 0.01);
        assertEquals(2.0, s.getLocation().getCoordinate().getY(), 0.01);
    }

    @Test(expected = IllegalStateException.class)
    public void whenSettingNeitherLocationIdNorCoord_throwsException() {
        @SuppressWarnings("unused")
        Service s = Service.Builder.newInstance("s").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenServiceTimeSmallerZero_throwIllegalStateException() {
        @SuppressWarnings("unused")
        Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance("loc")).setServiceTime(-1).build();
    }

    @Test
    public void whenSettingServiceTime_itShouldBeSetCorrectly() {
        Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance("loc")).setServiceTime(1).build();
        assertEquals(1.0, s.getServiceDuration(), 0.01);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenTimeWindowIsNull_throwException() {
        @SuppressWarnings("unused")
        Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance("loc")).setTimeWindow(null).build();
    }

    @Test
    public void whenSettingTimeWindow_itShouldBeSetCorrectly() {
        Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance("loc")).setTimeWindow(TimeWindow.newInstance(1.0, 2.0)).build();
        assertEquals(1.0, s.getTimeWindow().getStart(), 0.01);
        assertEquals(2.0, s.getTimeWindow().getEnd(), 0.01);
    }

    @Test
    public void whenAddingSkills_theyShouldBeAddedCorrectly() {
        Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance("loc"))
            .addRequiredSkill("drill").addRequiredSkill("screwdriver").build();
        assertTrue(s.getRequiredSkills().containsSkill("drill"));
        assertTrue(s.getRequiredSkills().containsSkill("drill"));
        assertTrue(s.getRequiredSkills().containsSkill("ScrewDriver"));
    }

    @Test
    public void whenAddingSkillsCaseSens_theyShouldBeAddedCorrectly() {
        Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance("loc"))
            .addRequiredSkill("DriLl").addRequiredSkill("screwDriver").build();
        assertTrue(s.getRequiredSkills().containsSkill("drill"));
        assertTrue(s.getRequiredSkills().containsSkill("drilL"));
    }

    @Test
    public void whenAddingSkillsCaseSensV2_theyShouldBeAddedCorrectly() {
        Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance("loc"))
            .addRequiredSkill("screwDriver").build();
        assertFalse(s.getRequiredSkills().containsSkill("drill"));
        assertFalse(s.getRequiredSkills().containsSkill("drilL"));
    }

    @Test
    public void nameShouldBeAssigned() {
        Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance("loc"))
            .setName("name").build();
        assertEquals("name", s.getName());
    }

}
