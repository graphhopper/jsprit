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

public class Time {

    /**
     * Parses seconds to this time format: hh:mm:ss {AM|PM}
     *
     * @param seconds seconds
     * @return time string
     */
    public static String parseSecondsToTime(double seconds) {
        int hours = (int) Math.floor(seconds / 3600.);
        int min = (int) Math.floor((seconds - hours * 3600.) / 60.);
        int secs = (int) (seconds - (hours * 3600. + min * 60.));
        String dayTime = "AM";
        if (hours > 12) {
            dayTime = "PM";
            hours -= 12;
        }
        String hourString = "0" + hours;
        if (hours > 9) hourString = "" + hours;
        String minString = "" + min;
        if (min < 10) minString = "0" + min;
        String secString = "" + secs;
        if (secs < 10) secString = "0" + secs;
        return hourString + ":" + minString + ":" + secString + " " + dayTime;
    }

    /**
     * Parse time to seconds.
     * <p>
     * <p>If you add PM or AM to timeString, it considers english-time, otherwise not.
     * <p>
     * <p>timeString can be 6AM, 06AM, 6 am, 6:01AM, 6:1 pM, 6:12:1 pm, 6:12:01 am, 06:12:01 Pm etc. - but not more precise than seconds.
     * <p>example: 12 AM returns 12*3600. sec
     * 6:30 AM --> 6*3600. + 30*60.
     * 0:30:20 AM --> 30*3600. + 20.
     * 6:00 PM --> 6*3600. + 12.*3600.
     * 6:00:12 --> 6*3600. + 12.
     *
     * @return seconds
     */
    public static double parseTimeToSeconds(String timeString) {
        if (timeString.substring(0, 1).matches("\\D"))
            throw new IllegalArgumentException("timeString must start with digit [0-9]");
        double dayTime = 0.;
        if (timeString.toLowerCase().contains("pm")) {
            dayTime = 12. * 3600.;
        }
        String[] tokens = timeString.split(":");

        if (tokens.length == 1) { //1 AM or 01 AM
            return getHourInSeconds(tokens[0]) + dayTime;
        } else if (tokens.length == 2) {
            return getHourInSeconds(tokens[0]) + getMinInSeconds(tokens[1]) + dayTime;
        } else if (tokens.length == 3) {
            return getHourInSeconds(tokens[0]) + getMinInSeconds(tokens[1]) + getSecondsInSeconds(tokens[2]) + dayTime;
        } else {
            throw new IllegalArgumentException("wrong timeString");
        }

    }

    private static double getSecondsInSeconds(String secString) {
        return getDigit(secString);
    }

    private static double getMinInSeconds(String minString) {
        return getDigit(minString) * 60.;
    }

    private static double getHourInSeconds(String hourString) {
        return getDigit(hourString) * 3600.;
    }

    private static double getDigit(String digitString) {
        if (digitString.length() == 1) {
            return Double.parseDouble(digitString);
        }
        if (digitString.substring(1, 2).matches("\\D")) {
            return Double.parseDouble(digitString.substring(0, 1));
        } else {
            if (digitString.startsWith("0")) {
                return Double.parseDouble(digitString.substring(1, 2));
            } else {
                return Double.parseDouble(digitString.substring(0, 2));
            }
        }
    }
}
