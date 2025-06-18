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
package com.graphhopper.jsprit.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Assertions;

@DisplayName("Time Test")
class TimeTest {

    @Test
    @DisplayName("Six AM _ should Be Parsed Correctly")
    void sixAM_shouldBeParsedCorrectly() {
        double sec = Time.parseTimeToSeconds("6AM");
        Assertions.assertEquals(6. * 3600., sec, 0.01);
    }

    @Test
    @DisplayName("Six AM With White Space _ should Be Parsed Correctly")
    void sixAMWithWhiteSpace_shouldBeParsedCorrectly() {
        double sec = Time.parseTimeToSeconds("6 AM");
        Assertions.assertEquals(6. * 3600., sec, 0.01);
    }

    @Test
    @DisplayName("Sixa M With White Space _ should Be Parsed Correctly")
    void sixaMWithWhiteSpace_shouldBeParsedCorrectly() {
        double sec = Time.parseTimeToSeconds("6 aM");
        Assertions.assertEquals(6. * 3600., sec, 0.01);
    }

    @Test
    @DisplayName("Sixam With White Space _ should Be Parsed Correctly")
    void sixamWithWhiteSpace_shouldBeParsedCorrectly() {
        double sec = Time.parseTimeToSeconds("6 am");
        Assertions.assertEquals(6. * 3600., sec, 0.01);
    }

    @Test
    @DisplayName("Six Am With White Space _ should Be Parsed Correctly")
    void sixAmWithWhiteSpace_shouldBeParsedCorrectly() {
        double sec = Time.parseTimeToSeconds("6 Am");
        Assertions.assertEquals(6. * 3600., sec, 0.01);
    }

    @Test
    @DisplayName("Six PM _ should Be Parsed Correctly")
    void sixPM_shouldBeParsedCorrectly() {
        double sec = Time.parseTimeToSeconds("6PM");
        Assertions.assertEquals(6. * 3600. + 12. * 3600., sec, 0.01);
    }

    @Test
    @DisplayName("Six PM With White Space _ should Be Parsed Correctly")
    void sixPMWithWhiteSpace_shouldBeParsedCorrectly() {
        double sec = Time.parseTimeToSeconds("6 PM");
        Assertions.assertEquals(6. * 3600. + 12. * 3600., sec, 0.01);
    }

    @Test
    @DisplayName("Sixp M With White Space _ should Be Parsed Correctly")
    void sixpMWithWhiteSpace_shouldBeParsedCorrectly() {
        double sec = Time.parseTimeToSeconds("6 pM");
        Assertions.assertEquals(6. * 3600. + 12. * 3600., sec, 0.01);
    }

    @Test
    @DisplayName("Sixpm With White Space _ should Be Parsed Correctly")
    void sixpmWithWhiteSpace_shouldBeParsedCorrectly() {
        double sec = Time.parseTimeToSeconds("6 pm");
        Assertions.assertEquals(6. * 3600. + 12. * 3600., sec, 0.01);
    }

    @Test
    @DisplayName("Six Pm With White Space _ should Be Parsed Correctly")
    void sixPmWithWhiteSpace_shouldBeParsedCorrectly() {
        double sec = Time.parseTimeToSeconds("6 Pm");
        Assertions.assertEquals(6. * 3600. + 12. * 3600, sec, 0.01);
    }

    @Test
    @DisplayName("Six AM With Leading Zero _ should Be Parsed Correctly")
    void sixAMWithLeadingZero_shouldBeParsedCorrectly() {
        double sec = Time.parseTimeToSeconds("06AM");
        Assertions.assertEquals(6. * 3600., sec, 0.01);
    }

    @Test
    @DisplayName("Six Hour _ twelve Min _ AM _ should Be Parsed Correctly")
    void sixHour_twelveMin_AM_shouldBeParsedCorrectly() {
        double sec = Time.parseTimeToSeconds("6:12AM");
        Assertions.assertEquals(6. * 3600. + 12. * 60., sec, 0.01);
    }

    @Test
    @DisplayName("Six Hour _ six Min _ AM _ should Be Parsed Correctly")
    void sixHour_sixMin_AM_shouldBeParsedCorrectly() {
        double sec = Time.parseTimeToSeconds("6:6AM");
        Assertions.assertEquals(6. * 3600. + 6. * 60., sec, 0.01);
    }

    @Test
    @DisplayName("Six Hour _ six Min With Leading Zero _ AM _ should Be Parsed Correctly")
    void sixHour_sixMinWithLeadingZero_AM_shouldBeParsedCorrectly() {
        double sec = Time.parseTimeToSeconds("6:06AM");
        Assertions.assertEquals(6. * 3600. + 6. * 60., sec, 0.01);
    }

    @Test
    @DisplayName("Six Hour _ twelve Min _ PM _ should Be Parsed Correctly")
    void sixHour_twelveMin_PM_shouldBeParsedCorrectly() {
        double sec = Time.parseTimeToSeconds("6:12PM");
        Assertions.assertEquals(6. * 3600. + 12. * 60. + 12. * 3600., sec, 0.01);
    }

    @Test
    @DisplayName("Six Hour _ six Min _ PM _ should Be Parsed Correctly")
    void sixHour_sixMin_PM_shouldBeParsedCorrectly() {
        double sec = Time.parseTimeToSeconds("6:6PM");
        Assertions.assertEquals(6. * 3600. + 6. * 60. + 12. * 3600., sec, 0.01);
    }

    @Test
    @DisplayName("Six Hour _ six Min With Leading Zero _ PM _ should Be Parsed Correctly")
    void sixHour_sixMinWithLeadingZero_PM_shouldBeParsedCorrectly() {
        double sec = Time.parseTimeToSeconds("6:06PM");
        Assertions.assertEquals(6. * 3600. + 6. * 60. + 12. * 3600., sec, 0.01);
    }

    @Test
    @DisplayName("Six Hour _ six Min With Leading Zero _ twelve Sec _ PM _ should Be Parsed Correctly")
    void sixHour_sixMinWithLeadingZero_twelveSec_PM_shouldBeParsedCorrectly() {
        double sec = Time.parseTimeToSeconds("6:06:12PM");
        Assertions.assertEquals(6. * 3600. + 6. * 60. + 12. * 3600. + 12., sec, 0.01);
    }

    @Test
    @DisplayName("Six Hour _ six Min With Leading Zero _ twelve Sec _ should Be Parsed Correctly")
    void sixHour_sixMinWithLeadingZero_twelveSec_shouldBeParsedCorrectly() {
        double sec = Time.parseTimeToSeconds("6:06:12");
        Assertions.assertEquals(6. * 3600. + 6. * 60. + 12., sec, 0.01);
    }

    @Test
    @DisplayName("Six Hour _ six Min With Leading Zero _ six Sec With Leading Zero _ should Be Parsed Correctly")
    void sixHour_sixMinWithLeadingZero_sixSecWithLeadingZero_shouldBeParsedCorrectly() {
        double sec = Time.parseTimeToSeconds("6:06:06");
        Assertions.assertEquals(6. * 3600. + 6. * 60. + 6., sec, 0.01);
    }

    @Test
    @DisplayName("When Time String Has No Digit _ it Throws Exception")
    void whenTimeStringHasNoDigit_itThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            Time.parseTimeToSeconds("PM");
        });
    }

    @Test
    @DisplayName("When Time String Has Milli Seconds _ it Throws Exception")
    void whenTimeStringHasMilliSeconds_itThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            Time.parseTimeToSeconds("01:00:12:01PM");
        });
    }

    @Test
    @DisplayName("Zero Hour _ zero Min With Leading Zero _ one Sec With Leading Zero _ should Be Parsed Correctly")
    void zeroHour_zeroMinWithLeadingZero_oneSecWithLeadingZero_shouldBeParsedCorrectly() {
        double sec = Time.parseTimeToSeconds("0:00:01");
        Assertions.assertEquals(1., sec, 0.01);
    }

    @Test
    @DisplayName("When Sec Is 3600 _ should Return Correct Time String")
    void whenSecIs3600_shouldReturnCorrectTimeString() {
        String time = Time.parseSecondsToTime(3600);
        Assertions.assertEquals(3600., Time.parseTimeToSeconds(time), 0.01);
    }

    @Test
    @DisplayName("When Sec Is 4000 _ should Return Correct Time String")
    void whenSecIs4000_shouldReturnCorrectTimeString() {
        String time = Time.parseSecondsToTime(4000);
        Assertions.assertEquals(4000., Time.parseTimeToSeconds(time), 0.01);
    }

    @Test
    @DisplayName("When Sec Is 86399 _ should Return Correct Time String")
    void whenSecIs86399_shouldReturnCorrectTimeString() {
        String time = Time.parseSecondsToTime(86399);
        Assertions.assertEquals(86399., Time.parseTimeToSeconds(time), 0.01);
    }
}
