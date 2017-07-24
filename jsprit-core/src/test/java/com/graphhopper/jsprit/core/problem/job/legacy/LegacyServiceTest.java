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
package com.graphhopper.jsprit.core.problem.job.legacy;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.LegacyService;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;

public class LegacyServiceTest {

    @Test
    public void whenTwoServicesHaveTheSameId_theirReferencesShouldBeUnEqual() {
        LegacyService one = LegacyService.Builder.newInstance("service").addSizeDimension(0, 10).setLocation(Location.newInstance("foo")).build();
        LegacyService two = LegacyService.Builder.newInstance("service").addSizeDimension(0, 10).setLocation(Location.newInstance("fo")).build();

        assertTrue(one != two);
    }

    @Test
    public void whenTwoServicesHaveTheSameId_theyShouldBeEqual() {
        LegacyService one = LegacyService.Builder.newInstance("service").addSizeDimension(0, 10).setLocation(Location.newInstance("foo")).build();
        LegacyService two = LegacyService.Builder.newInstance("service").addSizeDimension(0, 10).setLocation(Location.newInstance("fo")).build();

        assertTrue(one.equals(two));
    }

    @Test
    public void noName() {
        Set<LegacyService> serviceSet = new HashSet<LegacyService>();
        LegacyService one = LegacyService.Builder.newInstance("service").addSizeDimension(0, 10).setLocation(Location.newInstance("foo")).build();
        LegacyService two = LegacyService.Builder.newInstance("service").addSizeDimension(0, 10).setLocation(Location.newInstance("fo")).build();
        serviceSet.add(one);
        // assertTrue(serviceSet.contains(two));
        serviceSet.remove(two);
        assertTrue(serviceSet.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenCapacityDimValueIsNegative_throwIllegalStateExpception() {
        @SuppressWarnings("unused")
        LegacyService s = LegacyService.Builder.newInstance("s").setLocation(Location.newInstance("foo")).addSizeDimension(0, -10).build();
    }

    @Test
    public void whenAddingTwoCapDimension_nuOfDimsShouldBeTwo() {
        LegacyService one = LegacyService.Builder.newInstance("s").setLocation(Location.newInstance("foofoo"))
                        .addSizeDimension(0, 2).addSizeDimension(1, 4).build();
        assertEquals(2, one.getSize().getNuOfDimensions());
    }

    @Test
    public void whenShipmentIsBuiltWithoutSpecifyingCapacity_itShouldHvCapWithOneDimAndDimValOfZero() {
        LegacyService one = LegacyService.Builder.newInstance("s").setLocation(Location.newInstance("foofoo"))
                        .build();
        assertEquals(1, one.getSize().getNuOfDimensions());
        assertEquals(0, one.getSize().get(0));
    }

    @Test
    public void whenShipmentIsBuiltWithConstructorWhereSizeIsSpecified_capacityShouldBeSetCorrectly() {
        LegacyService one = LegacyService.Builder.newInstance("s").addSizeDimension(0, 1).setLocation(Location.newInstance("foofoo"))
                        .build();
        assertEquals(1, one.getSize().getNuOfDimensions());
        assertEquals(1, one.getSize().get(0));
    }

    @Test
    public void whenCallingForNewInstanceOfBuilder_itShouldReturnBuilderCorrectly() {
        LegacyService.Builder builder = LegacyService.Builder.newInstance("s");
        assertNotNull(builder);
    }

    @Test
    public void whenSettingNoType_itShouldReturn_service() {
        LegacyService s = LegacyService.Builder.newInstance("s").setLocation(Location.newInstance("loc")).build();
        assertEquals("service", s.getType());
    }

    @Test
    public void whenSettingLocation_itShouldBeSetCorrectly() {
        LegacyService s = LegacyService.Builder.newInstance("s").setLocation(Location.newInstance("loc")).build();
        assertEquals("loc", s.getLocation().getId());
        assertEquals("loc", s.getLocation().getId());
    }

    @Test
    public void whenSettingLocation_itShouldWork() {
        LegacyService s = LegacyService.Builder.newInstance("s").setLocation(Location.Builder.newInstance().setId("loc").build()).build();
        assertEquals("loc", s.getLocation().getId());
        assertEquals("loc", s.getLocation().getId());
    }


    @Test
    public void whenSettingLocationCoord_itShouldBeSetCorrectly(){
        LegacyService s = LegacyService.Builder.newInstance("s").setLocation(Location.newInstance(1, 2)).build();
        assertEquals(1.0,s.getLocation().getCoordinate().getX(),0.01);
        assertEquals(2.0,s.getLocation().getCoordinate().getY(),0.01);
        assertEquals(1.0,s.getLocation().getCoordinate().getX(),0.01);
        assertEquals(2.0,s.getLocation().getCoordinate().getY(),0.01);
    }

    @Test(expected=IllegalArgumentException.class)
    public void whenSettingNeitherLocationIdNorCoord_throwsException(){
        @SuppressWarnings("unused")
        LegacyService s = LegacyService.Builder.newInstance("s").build();
    }

    @Test(expected=IllegalArgumentException.class)
    public void whenServiceTimeSmallerZero_throwIllegalStateException(){
        @SuppressWarnings("unused")
        LegacyService s = LegacyService.Builder.newInstance("s").setLocation(Location.newInstance("loc")).setServiceTime(-1).build();
    }

    @Test
    public void whenSettingServiceTime_itShouldBeSetCorrectly(){
        LegacyService s = LegacyService.Builder.newInstance("s").setLocation(Location.newInstance("loc")).setServiceTime(1).build();
        assertEquals(1.0,s.getServiceDuration(),0.01);
    }

    @Test(expected=IllegalArgumentException.class)
    public void whenTimeWindowIsNull_throwException(){
        @SuppressWarnings("unused")
        LegacyService s = LegacyService.Builder.newInstance("s").setLocation(Location.newInstance("loc")).setTimeWindow(null).build();
    }

    @Test
    public void whenSettingTimeWindow_itShouldBeSetCorrectly(){
        LegacyService s = LegacyService.Builder.newInstance("s").setLocation(Location.newInstance("loc")).setTimeWindow(TimeWindow.newInstance(1.0, 2.0)).build();
        assertEquals(1.0,s.getTimeWindow().getStart(),0.01);
        assertEquals(2.0,s.getTimeWindow().getEnd(),0.01);
    }

    @Test
    public void whenAddingSkills_theyShouldBeAddedCorrectly(){
        LegacyService s = LegacyService.Builder.newInstance("s").setLocation(Location.newInstance("loc"))
                        .addRequiredSkill("drill").addRequiredSkill("screwdriver").build();
        assertTrue(s.getRequiredSkills().containsSkill("drill"));
        assertTrue(s.getRequiredSkills().containsSkill("drill"));
        assertTrue(s.getRequiredSkills().containsSkill("ScrewDriver"));
    }

    @Test
    public void whenAddingSkillsCaseSens_theyShouldBeAddedCorrectly(){
        LegacyService s = LegacyService.Builder.newInstance("s").setLocation(Location.newInstance("loc"))
                        .addRequiredSkill("DriLl").addRequiredSkill("screwDriver").build();
        assertTrue(s.getRequiredSkills().containsSkill("drill"));
        assertTrue(s.getRequiredSkills().containsSkill("drilL"));
    }

    @Test
    public void whenAddingSeveralTimeWindows_itShouldBeSetCorrectly(){
        TimeWindow tw1 = TimeWindow.newInstance(1.0, 2.0);
        TimeWindow tw2 = TimeWindow.newInstance(3.0, 5.0);
        LegacyService s = LegacyService.Builder.newInstance("s").setLocation(Location.newInstance("loc"))
                        .addTimeWindow(tw1).addTimeWindow(tw2).build();
        assertEquals(2, s.getTimeWindows().size());
        assertThat(s.getTimeWindows(),hasItem(is(tw1)));
        assertThat(s.getTimeWindows(),hasItem(is(tw2)));
    }

    @Test
    public void whenAddingTimeWindow_itShouldBeSetCorrectly(){
        LegacyService s = LegacyService.Builder.newInstance("s").setLocation(Location.newInstance("loc"))
                        .addTimeWindow(TimeWindow.newInstance(1.0, 2.0)).build();
        assertEquals(1.0, s.getTimeWindow().getStart(), 0.01);
        assertEquals(2.0, s.getTimeWindow().getEnd(), 0.01);
    }




    @Test
    public void whenAddingSkillsCaseSensV2_theyShouldBeAddedCorrectly() {
        LegacyService s = LegacyService.Builder.newInstance("s").setLocation(Location.newInstance("loc"))
                        .addRequiredSkill("screwDriver").build();
        assertFalse(s.getRequiredSkills().containsSkill("drill"));
        assertFalse(s.getRequiredSkills().containsSkill("drilL"));
    }

    @Test
    public void nameShouldBeAssigned() {
        LegacyService s = LegacyService.Builder.newInstance("s").setLocation(Location.newInstance("loc"))
                        .setName("name").build();
        assertEquals("name", s.getName());
    }

    @Test
    public void shouldKnowMultipleTimeWindows(){
        LegacyService s = LegacyService.Builder.newInstance("s").setLocation(Location.newInstance("loc"))
                        .addTimeWindow(TimeWindow.newInstance(0., 10.))
                        .addTimeWindow(TimeWindow.newInstance(20., 30.)).setName("name").build();
        assertEquals(2,s.getTimeWindows().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenMultipleTWOverlap_throwEx(){
        LegacyService s = LegacyService.Builder.newInstance("s").setLocation(Location.newInstance("loc"))
                        .addTimeWindow(TimeWindow.newInstance(0., 10.))
                        .addTimeWindow(TimeWindow.newInstance(5., 30.)).setName("name").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenMultipleTWOverlap2_throwEx(){
        LegacyService s = LegacyService.Builder.newInstance("s").setLocation(Location.newInstance("loc"))
                        .addTimeWindow(TimeWindow.newInstance(20., 30.))
                        .addTimeWindow(TimeWindow.newInstance(0., 25.)).setName("name").build();
    }

    @Test
    public void whenSettingPriorities_itShouldBeSetCorrectly(){
        LegacyService s = LegacyService.Builder.newInstance("s").setLocation(Location.newInstance("loc"))
                        .setPriority(1).build();
        Assert.assertEquals(1, s.getPriority());
    }

    @Test
    public void whenSettingPriorities_itShouldBeSetCorrectly2(){
        LegacyService s = LegacyService.Builder.newInstance("s").setLocation(Location.newInstance("loc"))
                        .setPriority(3).build();
        Assert.assertEquals(3, s.getPriority());
    }

    @Test
    public void whenSettingPriorities_itShouldBeSetCorrectly3() {
        LegacyService s = LegacyService.Builder.newInstance("s").setLocation(Location.newInstance("loc"))
                        .setPriority(10).build();
        Assert.assertEquals(10, s.getPriority());
    }

    @Test
    public void whenNotSettingPriorities_defaultShouldBe2(){
        LegacyService s = LegacyService.Builder.newInstance("s").setLocation(Location.newInstance("loc"))
                        .build();
        Assert.assertEquals(2, s.getPriority());
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenSettingIncorrectPriorities_itShouldThrowException(){
        LegacyService s = LegacyService.Builder.newInstance("s").setLocation(Location.newInstance("loc"))
                        .setPriority(30).build();

    }

    @Test(expected = IllegalArgumentException.class)
    public void whenSettingIncorrectPriorities_itShouldThrowException2(){
        LegacyService s = LegacyService.Builder.newInstance("s").setLocation(Location.newInstance("loc"))
                        .setPriority(0).build();

    }


    @Test
    public void whenSettingUserData_itIsAssociatedWithTheJob() {
        LegacyService one = LegacyService.Builder.newInstance("s").setLocation(Location.newInstance("loc"))
                        .setUserData(new HashMap<String, Object>()).build();
        LegacyService two = LegacyService.Builder.newInstance("s2").setLocation(Location.newInstance("loc")).setUserData(42)
                        .build();
        LegacyService three = LegacyService.Builder.newInstance("s3").setLocation(Location.newInstance("loc")).build();

        assertTrue(one.getUserData() instanceof Map);
        assertEquals(42, two.getUserData());
        assertNull(three.getUserData());
    }
}
