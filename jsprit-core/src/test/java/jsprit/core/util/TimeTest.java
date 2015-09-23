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
package jsprit.core.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TimeTest {

    @Test
    public void sixAM_shouldBeParsedCorrectly() {
        double sec = Time.parseTimeToSeconds("6AM");
        assertEquals(6. * 3600., sec, 0.01);
    }

    @Test
    public void sixAMWithWhiteSpace_shouldBeParsedCorrectly() {
        double sec = Time.parseTimeToSeconds("6 AM");
        assertEquals(6. * 3600., sec, 0.01);
    }

    @Test
    public void sixaMWithWhiteSpace_shouldBeParsedCorrectly() {
        double sec = Time.parseTimeToSeconds("6 aM");
        assertEquals(6. * 3600., sec, 0.01);
    }

    @Test
    public void sixamWithWhiteSpace_shouldBeParsedCorrectly() {
        double sec = Time.parseTimeToSeconds("6 am");
        assertEquals(6. * 3600., sec, 0.01);
    }

    @Test
    public void sixAmWithWhiteSpace_shouldBeParsedCorrectly() {
        double sec = Time.parseTimeToSeconds("6 Am");
        assertEquals(6. * 3600., sec, 0.01);
    }


    @Test
    public void sixPM_shouldBeParsedCorrectly() {
        double sec = Time.parseTimeToSeconds("6PM");
        assertEquals(6. * 3600. + 12. * 3600., sec, 0.01);
    }

    @Test
    public void sixPMWithWhiteSpace_shouldBeParsedCorrectly() {
        double sec = Time.parseTimeToSeconds("6 PM");
        assertEquals(6. * 3600. + 12. * 3600., sec, 0.01);
    }

    @Test
    public void sixpMWithWhiteSpace_shouldBeParsedCorrectly() {
        double sec = Time.parseTimeToSeconds("6 pM");
        assertEquals(6. * 3600. + 12. * 3600., sec, 0.01);
    }

    @Test
    public void sixpmWithWhiteSpace_shouldBeParsedCorrectly() {
        double sec = Time.parseTimeToSeconds("6 pm");
        assertEquals(6. * 3600. + 12. * 3600., sec, 0.01);
    }

    @Test
    public void sixPmWithWhiteSpace_shouldBeParsedCorrectly() {
        double sec = Time.parseTimeToSeconds("6 Pm");
        assertEquals(6. * 3600. + 12. * 3600, sec, 0.01);
    }

    @Test
    public void sixAMWithLeadingZero_shouldBeParsedCorrectly() {
        double sec = Time.parseTimeToSeconds("06AM");
        assertEquals(6. * 3600., sec, 0.01);
    }

    @Test
    public void sixHour_twelveMin_AM_shouldBeParsedCorrectly() {
        double sec = Time.parseTimeToSeconds("6:12AM");
        assertEquals(6. * 3600. + 12. * 60., sec, 0.01);
    }

    @Test
    public void sixHour_sixMin_AM_shouldBeParsedCorrectly() {
        double sec = Time.parseTimeToSeconds("6:6AM");
        assertEquals(6. * 3600. + 6. * 60., sec, 0.01);
    }

    @Test
    public void sixHour_sixMinWithLeadingZero_AM_shouldBeParsedCorrectly() {
        double sec = Time.parseTimeToSeconds("6:06AM");
        assertEquals(6. * 3600. + 6. * 60., sec, 0.01);
    }


    @Test
    public void sixHour_twelveMin_PM_shouldBeParsedCorrectly() {
        double sec = Time.parseTimeToSeconds("6:12PM");
        assertEquals(6. * 3600. + 12. * 60. + 12. * 3600., sec, 0.01);
    }

    @Test
    public void sixHour_sixMin_PM_shouldBeParsedCorrectly() {
        double sec = Time.parseTimeToSeconds("6:6PM");
        assertEquals(6. * 3600. + 6. * 60. + 12. * 3600., sec, 0.01);
    }

    @Test
    public void sixHour_sixMinWithLeadingZero_PM_shouldBeParsedCorrectly() {
        double sec = Time.parseTimeToSeconds("6:06PM");
        assertEquals(6. * 3600. + 6. * 60. + 12. * 3600., sec, 0.01);
    }

    @Test
    public void sixHour_sixMinWithLeadingZero_twelveSec_PM_shouldBeParsedCorrectly() {
        double sec = Time.parseTimeToSeconds("6:06:12PM");
        assertEquals(6. * 3600. + 6. * 60. + 12. * 3600. + 12., sec, 0.01);
    }

    @Test
    public void sixHour_sixMinWithLeadingZero_twelveSec_shouldBeParsedCorrectly() {
        double sec = Time.parseTimeToSeconds("6:06:12");
        assertEquals(6. * 3600. + 6. * 60. + 12., sec, 0.01);
    }

    @Test
    public void sixHour_sixMinWithLeadingZero_sixSecWithLeadingZero_shouldBeParsedCorrectly() {
        double sec = Time.parseTimeToSeconds("6:06:06");
        assertEquals(6. * 3600. + 6. * 60. + 6., sec, 0.01);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenTimeStringHasNoDigit_itThrowsException() {
        Time.parseTimeToSeconds("PM");
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenTimeStringHasMilliSeconds_itThrowsException() {
        Time.parseTimeToSeconds("01:00:12:01PM");
    }

    @Test
    public void zeroHour_zeroMinWithLeadingZero_oneSecWithLeadingZero_shouldBeParsedCorrectly() {
        double sec = Time.parseTimeToSeconds("0:00:01");
        assertEquals(1., sec, 0.01);
    }

    @Test
    public void whenSecIs3600_shouldReturnCorrectTimeString() {
        String time = Time.parseSecondsToTime(3600);
        System.out.println(time);
        assertEquals(3600., Time.parseTimeToSeconds(time), 0.01);
    }

    @Test
    public void whenSecIs4000_shouldReturnCorrectTimeString() {
        String time = Time.parseSecondsToTime(4000);
        System.out.println(time);
        assertEquals(4000., Time.parseTimeToSeconds(time), 0.01);
    }

    @Test
    public void whenSecIs86399_shouldReturnCorrectTimeString() {
        String time = Time.parseSecondsToTime(86399);
        System.out.println(time);
        assertEquals(86399., Time.parseTimeToSeconds(time), 0.01);
    }


}
