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

package jsprit.instance.reader;


import junit.framework.Assert;
import org.junit.Test;

import java.util.List;

public class FigliozziTest {

    @Test
    public void factoryShouldReturnCorrectSpeedDistribution(){
        List<Double> speedValues = Figliozzi.TimeDependentTransportCostsFactory.createSpeedValues(Figliozzi.TimeDependentTransportCostsFactory.SpeedDistribution.TD1a);
        Assert.assertEquals(speedValues.get(0),1.,0.01);
        Assert.assertEquals(speedValues.get(1),1.6,0.01);
        Assert.assertEquals(speedValues.get(2),1.05,0.01);
        Assert.assertEquals(5,speedValues.size());
    }

    @Test
    public void whenAskingForTD2a_factoryShouldReturnCorrectSpeedDistribution(){
        List<Double> speedValues = Figliozzi.TimeDependentTransportCostsFactory.createSpeedValues(Figliozzi.TimeDependentTransportCostsFactory.SpeedDistribution.TD2a);
        Assert.assertEquals(speedValues.get(0),1.,0.01);
        Assert.assertEquals(speedValues.get(1),2.,0.01);
        Assert.assertEquals(speedValues.get(2),1.5,0.01);
        Assert.assertEquals(5,speedValues.size());
    }

    @Test
    public void whenAskingForTD3a_factoryShouldReturnCorrectSpeedDistribution(){
        List<Double> speedValues = Figliozzi.TimeDependentTransportCostsFactory.createSpeedValues(Figliozzi.TimeDependentTransportCostsFactory.SpeedDistribution.TD3a);
        Assert.assertEquals(speedValues.get(0),1.,0.01);
        Assert.assertEquals(speedValues.get(1),2.5,0.01);
        Assert.assertEquals(speedValues.get(2),1.75,0.01);
        Assert.assertEquals(5,speedValues.size());
    }
}
