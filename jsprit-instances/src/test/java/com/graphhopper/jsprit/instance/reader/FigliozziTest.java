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

package com.graphhopper.jsprit.instance.reader;


import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.Locations;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class FigliozziTest {

    Locations locations;

    @Before
    public void doBefore() {
        final Coordinate from = Coordinate.newInstance(0, 0);
        final Coordinate to = Coordinate.newInstance(100, 0);

        locations = new Locations() {

            @Override
            public Coordinate getCoord(String id) {
                if (id.equals("from")) return from;
                if (id.equals("to")) return to;
                return null;
            }
        };
    }

    @Test
    public void factoryShouldReturnCorrectSpeedDistribution() {
        List<Double> speedValues = Figliozzi.TimeDependentTransportCostsFactory.createSpeedValues(Figliozzi.TimeDependentTransportCostsFactory.SpeedDistribution.TD1a);
        Assert.assertEquals(speedValues.get(0), 1., 0.01);
        Assert.assertEquals(speedValues.get(1), 1.6, 0.01);
        Assert.assertEquals(speedValues.get(2), 1.05, 0.01);
        Assert.assertEquals(5, speedValues.size());
    }

    @Test
    public void whenAskingForTD2a_factoryShouldReturnCorrectSpeedDistribution() {
        List<Double> speedValues = Figliozzi.TimeDependentTransportCostsFactory.createSpeedValues(Figliozzi.TimeDependentTransportCostsFactory.SpeedDistribution.TD2a);
        Assert.assertEquals(speedValues.get(0), 1., 0.01);
        Assert.assertEquals(speedValues.get(1), 2., 0.01);
        Assert.assertEquals(speedValues.get(2), 1.5, 0.01);
        Assert.assertEquals(5, speedValues.size());
    }

    @Test
    public void whenAskingForTD3a_factoryShouldReturnCorrectSpeedDistribution() {
        List<Double> speedValues = Figliozzi.TimeDependentTransportCostsFactory.createSpeedValues(Figliozzi.TimeDependentTransportCostsFactory.SpeedDistribution.TD3a);
        Assert.assertEquals(speedValues.get(0), 1., 0.01);
        Assert.assertEquals(speedValues.get(1), 2.5, 0.01);
        Assert.assertEquals(speedValues.get(2), 1.75, 0.01);
        Assert.assertEquals(5, speedValues.size());
    }

    @Test
    public void whenAskingForTD1b_factoryShouldReturnCorrectSpeedDistribution() {
        List<Double> speedValues = Figliozzi.TimeDependentTransportCostsFactory.createSpeedValues(Figliozzi.TimeDependentTransportCostsFactory.SpeedDistribution.TD1b);
        Assert.assertEquals(speedValues.get(0), 1.6, 0.01);
        Assert.assertEquals(speedValues.get(1), 1., 0.01);
        Assert.assertEquals(speedValues.get(2), 1.05, 0.01);
        Assert.assertEquals(speedValues.get(3), 1., 0.01);
        Assert.assertEquals(speedValues.get(4), 1.6, 0.01);
        Assert.assertEquals(5, speedValues.size());
    }

    @Test
    public void whenAskingForTD2b_factoryShouldReturnCorrectSpeedDistribution() {
        List<Double> speedValues = Figliozzi.TimeDependentTransportCostsFactory.createSpeedValues(Figliozzi.TimeDependentTransportCostsFactory.SpeedDistribution.TD2b);
        Assert.assertEquals(speedValues.get(0), 2., 0.01);
        Assert.assertEquals(speedValues.get(1), 1., 0.01);
        Assert.assertEquals(speedValues.get(2), 1.5, 0.01);
        Assert.assertEquals(speedValues.get(3), 1., 0.01);
        Assert.assertEquals(speedValues.get(4), 2., 0.01);
        Assert.assertEquals(5, speedValues.size());
    }

    @Test
    public void whenAskingForTD3b_factoryShouldReturnCorrectSpeedDistribution() {
        List<Double> speedValues = Figliozzi.TimeDependentTransportCostsFactory.createSpeedValues(Figliozzi.TimeDependentTransportCostsFactory.SpeedDistribution.TD3b);
        Assert.assertEquals(speedValues.get(0), 2.5, 0.01);
        Assert.assertEquals(speedValues.get(1), 1., 0.01);
        Assert.assertEquals(speedValues.get(2), 1.75, 0.01);
        Assert.assertEquals(speedValues.get(3), 1., 0.01);
        Assert.assertEquals(speedValues.get(4), 2.5, 0.01);
        Assert.assertEquals(5, speedValues.size());
    }

    @Test
    public void whenAskingForTD1c_factoryShouldReturnCorrectSpeedDistribution() {
        List<Double> speedValues = Figliozzi.TimeDependentTransportCostsFactory.createSpeedValues(Figliozzi.TimeDependentTransportCostsFactory.SpeedDistribution.TD1c);
        Assert.assertEquals(speedValues.get(0), 1.6, 0.01);
        Assert.assertEquals(speedValues.get(1), 1.6, 0.01);
        Assert.assertEquals(speedValues.get(2), 1.05, 0.01);
        Assert.assertEquals(speedValues.get(3), 1., 0.01);
        Assert.assertEquals(speedValues.get(4), 1., 0.01);
        Assert.assertEquals(5, speedValues.size());
    }

    @Test
    public void whenAskingForTD2c_factoryShouldReturnCorrectSpeedDistribution() {
        List<Double> speedValues = Figliozzi.TimeDependentTransportCostsFactory.createSpeedValues(Figliozzi.TimeDependentTransportCostsFactory.SpeedDistribution.TD2c);
        Assert.assertEquals(speedValues.get(0), 2., 0.01);
        Assert.assertEquals(speedValues.get(1), 2., 0.01);
        Assert.assertEquals(speedValues.get(2), 1.5, 0.01);
        Assert.assertEquals(speedValues.get(3), 1., 0.01);
        Assert.assertEquals(speedValues.get(4), 1., 0.01);
        Assert.assertEquals(5, speedValues.size());
    }

    @Test
    public void whenAskingForTD3c_factoryShouldReturnCorrectSpeedDistribution() {
        List<Double> speedValues = Figliozzi.TimeDependentTransportCostsFactory.createSpeedValues(Figliozzi.TimeDependentTransportCostsFactory.SpeedDistribution.TD3c);
        Assert.assertEquals(speedValues.get(0), 2.5, 0.01);
        Assert.assertEquals(speedValues.get(1), 2.5, 0.01);
        Assert.assertEquals(speedValues.get(2), 1.75, 0.01);
        Assert.assertEquals(speedValues.get(3), 1., 0.01);
        Assert.assertEquals(speedValues.get(4), 1., 0.01);
        Assert.assertEquals(5, speedValues.size());
    }

    @Test
    public void whenAskingForTD1d_factoryShouldReturnCorrectSpeedDistribution() {
        List<Double> speedValues = Figliozzi.TimeDependentTransportCostsFactory.createSpeedValues(Figliozzi.TimeDependentTransportCostsFactory.SpeedDistribution.TD1d);
        Assert.assertEquals(speedValues.get(0), 1., 0.01);
        Assert.assertEquals(speedValues.get(1), 1., 0.01);
        Assert.assertEquals(speedValues.get(2), 1.05, 0.01);
        Assert.assertEquals(speedValues.get(3), 1.6, 0.01);
        Assert.assertEquals(speedValues.get(4), 1.6, 0.01);
        Assert.assertEquals(5, speedValues.size());
    }

    @Test
    public void whenAskingForTD2d_factoryShouldReturnCorrectSpeedDistribution() {
        List<Double> speedValues = Figliozzi.TimeDependentTransportCostsFactory.createSpeedValues(Figliozzi.TimeDependentTransportCostsFactory.SpeedDistribution.TD2d);
        Assert.assertEquals(speedValues.get(0), 1., 0.01);
        Assert.assertEquals(speedValues.get(1), 1., 0.01);
        Assert.assertEquals(speedValues.get(2), 1.5, 0.01);
        Assert.assertEquals(speedValues.get(3), 2., 0.01);
        Assert.assertEquals(speedValues.get(4), 2., 0.01);
        Assert.assertEquals(5, speedValues.size());
    }

    @Test
    public void whenAskingForTD3d_factoryShouldReturnCorrectSpeedDistribution() {
        List<Double> speedValues = Figliozzi.TimeDependentTransportCostsFactory.createSpeedValues(Figliozzi.TimeDependentTransportCostsFactory.SpeedDistribution.TD3d);
        Assert.assertEquals(speedValues.get(0), 1., 0.01);
        Assert.assertEquals(speedValues.get(1), 1., 0.01);
        Assert.assertEquals(speedValues.get(2), 1.75, 0.01);
        Assert.assertEquals(speedValues.get(3), 2.5, 0.01);
        Assert.assertEquals(speedValues.get(4), 2.5, 0.01);
        Assert.assertEquals(5, speedValues.size());
    }

    @Test
    public void whenConstantTimeDistribution_forwardTimeShouldBeCalculate100() {
        Figliozzi.TDCosts tdCosts = Figliozzi.TimeDependentTransportCostsFactory.createCosts(locations, Figliozzi.TimeDependentTransportCostsFactory.SpeedDistribution.CLASSIC, 100);
        Assert.assertEquals(100., tdCosts.getTransportTime(loc("from"), loc("to"), 0., null, null), 0.01);
    }

    private Location loc(String from) {
        return Location.Builder.newInstance().setId(from).build();
    }

    @Test
    public void whenTimeDistributionTD1a_forwardTimeShouldBeCalculate100() {
        Figliozzi.TDCosts tdCosts = Figliozzi.TimeDependentTransportCostsFactory.createCosts(locations, Figliozzi.TimeDependentTransportCostsFactory.SpeedDistribution.TD1a, 100);
        /*
        100
        (0,20) - 1. --> 20
        (20,40) 1.6 = s/t --> t = s / 1.6 = 20 * 1.6 = 32 : 52 --> 40
        (40,60) 1.05 = 21 : 73 --> 60
        (60,80) 1.6 = 20 * 1.6 = 32 --> 27 / 1.6 = 16.875 + 73 = -- 16.875

         20

         */
        Assert.assertEquals(76.875, tdCosts.getTransportTime(loc("from"), loc("to"), 0., null, null), 0.01);
    }

    @Test
    public void whenTimeDistributionTD2a_forwardTimeShouldBeCalculate100() {
        //(1.,2.,1.5,2.,1.)
        Figliozzi.TDCosts tdCosts = Figliozzi.TimeDependentTransportCostsFactory.createCosts(locations, Figliozzi.TimeDependentTransportCostsFactory.SpeedDistribution.TD2a, 100);
        /*
        100
        (0,20) - 1. --> 20 dist, 20 time
        (20,40) 2. = 20 --> 40 dist, 20 time : 60 dist, 40 time
        (40,60) 1.5 = 30 dist, 20 time : 90 dist, 60 time
        (60,80) 2. = 10 dist, 5 time : 100 dist, 65 time

         20

         */
        Assert.assertEquals(65., tdCosts.getTransportTime(loc("from"), loc("to"), 0., null, null), 0.01);
    }

    @Test
    public void whenTimeDistributionTD3a_forwardTimeShouldBeCalculate100() {
        //(1.,2.5,1.75,2.5,1.)
        Figliozzi.TDCosts tdCosts = Figliozzi.TimeDependentTransportCostsFactory.createCosts(locations, Figliozzi.TimeDependentTransportCostsFactory.SpeedDistribution.TD3a, 100);
        /*
        100
        (0,20) - 1. --> 20 dist, 20 time
        (20,40) 2.5 = 20 --> 50 dist, 20 time : 70 dist, 40 time
        (40,60) 1.75 = 30 dist, 17.1428571429 time : 100 dist, 57.1428571429 time
        */
        Assert.assertEquals(57.1428571429, tdCosts.getTransportTime(loc("from"), loc("to"), 0., null, null), 0.01);
    }

    @Test
    public void whenTimeDistributionTD2a_backwardTimeShouldBeCalculate100() {
        //(1.,2.,1.5,2.,1.)
        Figliozzi.TDCosts tdCosts = Figliozzi.TimeDependentTransportCostsFactory.createCosts(locations, Figliozzi.TimeDependentTransportCostsFactory.SpeedDistribution.TD2a, 100);
        /*
        100
        (0,20) - 1. --> 20 dist, 20 time
        (20,40) 2. = 20 --> 40 dist, 20 time : 60 dist, 40 time
        (40,60) 1.5 = 30 dist, 20 time : 90 dist, 60 time
        (60,80) 2. = 10 dist, 5 time : 100 dist, 65 time

         20

         */
        Assert.assertEquals(65., tdCosts.getBackwardTransportTime(loc("from"), loc("to"), 100., null, null), 0.01);
    }

    @Test
    public void whenTimeDistributionTD1a_backwardTimeShouldBeCalculate100() {
        Figliozzi.TDCosts tdCosts = Figliozzi.TimeDependentTransportCostsFactory.createCosts(locations, Figliozzi.TimeDependentTransportCostsFactory.SpeedDistribution.TD1a, 100);
        /*
        100
        (0,20) - 1. --> 20
        (20,40) 1.6 = s/t --> t = s / 1.6 = 20 * 1.6 = 32 : 52 --> 40
        (40,60) 1.05 = 21 : 73 --> 60
        (60,80) 1.6 = 20 * 1.6 = 32 --> 27 / 1.6 = 16.875 + 73 = -- 16.875

         20

         */
        Assert.assertEquals(76.875, tdCosts.getBackwardTransportTime(loc("from"), loc("to"), 100., null, null), 0.01);
    }

    @Test
    public void backwardTimeShouldBeCalculatedCorrectly() {
        Figliozzi.TDCosts tdCosts = Figliozzi.TimeDependentTransportCostsFactory.createCosts(locations, Figliozzi.TimeDependentTransportCostsFactory.SpeedDistribution.CLASSIC, 100);
        Assert.assertEquals(100., tdCosts.getBackwardTransportTime(loc("from"), loc("to"), 100., null, null), 0.01);
    }

    @Test
    public void whenTD1a_distanceShouldBe25PercentMore() {
        Locations locations = new Locations() {

            @Override
            public Coordinate getCoord(String id) {
                if (id.equals("from")) return Coordinate.newInstance(0, 0);
                if (id.equals("to")) return Coordinate.newInstance(125., 0);
                return null;
            }

        };
        Figliozzi.TDCosts tdCosts = Figliozzi.TimeDependentTransportCostsFactory.createCosts(locations, Figliozzi.TimeDependentTransportCostsFactory.SpeedDistribution.TD1a, 100);
        double time = tdCosts.getTransportTime(loc("from"), loc("to"), 0., null, null);
        Assert.assertEquals(100., time, 0.01);
    }

    @Test
    public void whenTD1b_distanceShouldBe25PercentMore() {
        Locations locations = new Locations() {

            @Override
            public Coordinate getCoord(String id) {
                if (id.equals("from")) return Coordinate.newInstance(0, 0);
                if (id.equals("to")) return Coordinate.newInstance(125., 0);
                return null;
            }

        };
        Figliozzi.TDCosts tdCosts = Figliozzi.TimeDependentTransportCostsFactory.createCosts(locations, Figliozzi.TimeDependentTransportCostsFactory.SpeedDistribution.TD1b, 100);
        double time = tdCosts.getTransportTime(loc("from"), loc("to"), 0., null, null);
        Assert.assertEquals(100., time, 0.01);
    }

    @Test
    public void whenTD1c_distanceShouldBe25PercentMore() {
        Locations locations = new Locations() {

            @Override
            public Coordinate getCoord(String id) {
                if (id.equals("from")) return Coordinate.newInstance(0, 0);
                if (id.equals("to")) return Coordinate.newInstance(125., 0);
                return null;
            }

        };
        Figliozzi.TDCosts tdCosts = Figliozzi.TimeDependentTransportCostsFactory.createCosts(locations, Figliozzi.TimeDependentTransportCostsFactory.SpeedDistribution.TD1c, 100);
        double time = tdCosts.getTransportTime(loc("from"), loc("to"), 0., null, null);
        Assert.assertEquals(100., time, 0.01);
    }

    @Test
    public void whenTD1d_distanceShouldBe25PercentMore() {
        Locations locations = new Locations() {

            @Override
            public Coordinate getCoord(String id) {
                if (id.equals("from")) return Coordinate.newInstance(0, 0);
                if (id.equals("to")) return Coordinate.newInstance(125., 0);
                return null;
            }

        };
        Figliozzi.TDCosts tdCosts = Figliozzi.TimeDependentTransportCostsFactory.createCosts(locations, Figliozzi.TimeDependentTransportCostsFactory.SpeedDistribution.TD1d, 100);
        double time = tdCosts.getTransportTime(loc("from"), loc("to"), 0., null, null);
        Assert.assertEquals(100., time, 0.01);
    }

    @Test
    public void whenTD2a_distanceShouldBe50PercentMore() {
        Locations locations = new Locations() {

            @Override
            public Coordinate getCoord(String id) {
                if (id.equals("from")) return Coordinate.newInstance(0, 0);
                if (id.equals("to")) return Coordinate.newInstance(150., 0);
                return null;
            }

        };
        Figliozzi.TDCosts tdCosts = Figliozzi.TimeDependentTransportCostsFactory.createCosts(locations, Figliozzi.TimeDependentTransportCostsFactory.SpeedDistribution.TD2a, 100);
        double time = tdCosts.getTransportTime(loc("from"), loc("to"), 0., null, null);
        Assert.assertEquals(100., time, 0.01);
    }

    @Test
    public void whenTD2b_distanceShouldBe50PercentMore() {
        Locations locations = new Locations() {

            @Override
            public Coordinate getCoord(String id) {
                if (id.equals("from")) return Coordinate.newInstance(0, 0);
                if (id.equals("to")) return Coordinate.newInstance(150., 0);
                return null;
            }

        };
        Figliozzi.TDCosts tdCosts = Figliozzi.TimeDependentTransportCostsFactory.createCosts(locations, Figliozzi.TimeDependentTransportCostsFactory.SpeedDistribution.TD2b, 100);
        double time = tdCosts.getTransportTime(loc("from"), loc("to"), 0., null, null);
        Assert.assertEquals(100., time, 0.01);
    }

    @Test
    public void whenTD2c_distanceShouldBe50PercentMore() {
        Locations locations = new Locations() {

            @Override
            public Coordinate getCoord(String id) {
                if (id.equals("from")) return Coordinate.newInstance(0, 0);
                if (id.equals("to")) return Coordinate.newInstance(150., 0);
                return null;
            }

        };
        Figliozzi.TDCosts tdCosts = Figliozzi.TimeDependentTransportCostsFactory.createCosts(locations, Figliozzi.TimeDependentTransportCostsFactory.SpeedDistribution.TD2c, 100);
        double time = tdCosts.getTransportTime(loc("from"), loc("to"), 0., null, null);
        Assert.assertEquals(100., time, 0.01);
    }

    @Test
    public void whenTD2d_distanceShouldBe50PercentMore() {
        Locations locations = new Locations() {

            @Override
            public Coordinate getCoord(String id) {
                if (id.equals("from")) return Coordinate.newInstance(0, 0);
                if (id.equals("to")) return Coordinate.newInstance(150., 0);
                return null;
            }

        };
        Figliozzi.TDCosts tdCosts = Figliozzi.TimeDependentTransportCostsFactory.createCosts(locations, Figliozzi.TimeDependentTransportCostsFactory.SpeedDistribution.TD2d, 100);
        double time = tdCosts.getTransportTime(loc("from"), loc("to"), 0., null, null);
        Assert.assertEquals(100., time, 0.01);
    }

    @Test
    public void whenTD3a_distanceShouldBe75PercentMore() {
        Locations locations = new Locations() {

            @Override
            public Coordinate getCoord(String id) {
                if (id.equals("from")) return Coordinate.newInstance(0, 0);
                if (id.equals("to")) return Coordinate.newInstance(175., 0);
                return null;
            }

        };
        Figliozzi.TDCosts tdCosts = Figliozzi.TimeDependentTransportCostsFactory.createCosts(locations, Figliozzi.TimeDependentTransportCostsFactory.SpeedDistribution.TD3a, 100);
        double time = tdCosts.getTransportTime(loc("from"), loc("to"), 0., null, null);
        Assert.assertEquals(100., time, 0.01);
    }

    @Test
    public void whenTD3b_distanceShouldBe75PercentMore() {
        Locations locations = new Locations() {

            @Override
            public Coordinate getCoord(String id) {
                if (id.equals("from")) return Coordinate.newInstance(0, 0);
                if (id.equals("to")) return Coordinate.newInstance(175., 0);
                return null;
            }

        };
        Figliozzi.TDCosts tdCosts = Figliozzi.TimeDependentTransportCostsFactory.createCosts(locations, Figliozzi.TimeDependentTransportCostsFactory.SpeedDistribution.TD3b, 100);
        double time = tdCosts.getTransportTime(loc("from"), loc("to"), 0., null, null);
        Assert.assertEquals(100., time, 0.01);
    }

    @Test
    public void whenTD3c_distanceShouldBe75PercentMore() {
        Locations locations = new Locations() {

            @Override
            public Coordinate getCoord(String id) {
                if (id.equals("from")) return Coordinate.newInstance(0, 0);
                if (id.equals("to")) return Coordinate.newInstance(175., 0);
                return null;
            }

        };
        Figliozzi.TDCosts tdCosts = Figliozzi.TimeDependentTransportCostsFactory.createCosts(locations, Figliozzi.TimeDependentTransportCostsFactory.SpeedDistribution.TD3c, 100);
        double time = tdCosts.getTransportTime(loc("from"), loc("to"), 0., null, null);
        Assert.assertEquals(100., time, 0.01);
    }

    @Test
    public void whenTD3d_distanceShouldBe75PercentMore() {
        Locations locations = new Locations() {

            @Override
            public Coordinate getCoord(String id) {
                if (id.equals("from")) return Coordinate.newInstance(0, 0);
                if (id.equals("to")) return Coordinate.newInstance(175., 0);
                return null;
            }

        };
        Figliozzi.TDCosts tdCosts = Figliozzi.TimeDependentTransportCostsFactory.createCosts(locations, Figliozzi.TimeDependentTransportCostsFactory.SpeedDistribution.TD3d, 100);
        double time = tdCosts.getTransportTime(loc("from"), loc("to"), 0., null, null);
        Assert.assertEquals(100., time, 0.01);
    }
}



