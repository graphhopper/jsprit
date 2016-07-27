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

package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

public interface ActivityInsertionCostsCalculator {

    public class ActivityInsertionCosts {

        private double additionalCosts;
        private double additionalTime;

        public ActivityInsertionCosts(double additionalCosts, double additionalTime) {
            super();
            this.additionalCosts = additionalCosts;
            this.additionalTime = additionalTime;
        }

        /**
         * @return the additionalCosts
         */
        public double getAdditionalCosts() {
            return additionalCosts;
        }

        /**
         * @return the additionalTime
         */
        public double getAdditionalTime() {
            return additionalTime;
        }


    }

    public double getCosts(JobInsertionContext iContext, TourActivity prevAct, TourActivity nextAct, TourActivity newAct, double depTimeAtPrevAct);

}
