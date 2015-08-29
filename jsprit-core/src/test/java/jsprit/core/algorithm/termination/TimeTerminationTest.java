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

package jsprit.core.algorithm.termination;

import junit.framework.Assert;
import org.junit.Test;

/**
 * Created by schroeder on 16.12.14.
 */
public class TimeTerminationTest {

    @Test
    public void whenTimeThreshold2000msAndCurrentTime0_itShouldNotBreak() {
        long threshold = 2000;
        TimeTermination tt = new TimeTermination(threshold);
        tt.setTimeGetter(new TimeTermination.TimeGetter() {
            @Override
            public long getCurrentTime() {
                return 0;
            }
        });
        tt.start(0);
        Assert.assertFalse(tt.isPrematureBreak(null));
    }

    @Test
    public void whenTimeThreshold2000msAndCurrentTime2000ms_itShouldBreak() {
        long threshold = 2000;
        TimeTermination tt = new TimeTermination(threshold);
        tt.setTimeGetter(new TimeTermination.TimeGetter() {
            @Override
            public long getCurrentTime() {
                return 2001;
            }
        });
        tt.start(0);
        Assert.assertTrue(tt.isPrematureBreak(null));
    }
}
