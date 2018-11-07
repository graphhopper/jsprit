package com.graphhopper.jsprit.core.problem.job;

import com.graphhopper.jsprit.core.problem.Capacity;

public class BreakForMultipleTimeWindows extends Service {

    public static class Builder extends Service.Builder<BreakForMultipleTimeWindows> {

        /**
         * Returns a new instance of builder that builds a pickup.
         *
         * @param id the id of the pickup
         * @return the builder
         */
        public static BreakForMultipleTimeWindows.Builder newInstance(String id) {
            return new BreakForMultipleTimeWindows.Builder(id);
        }

        private boolean variableLocation = true;

        Builder(String id) {
            super(id);
        }

        /**
         * Builds Pickup.
         * <p>
         * <p>Pickup type is "pickup"
         *
         * @return pickup
         * @throws IllegalStateException if neither locationId nor coordinate has been set
         */
        public BreakForMultipleTimeWindows build() {
            if (location != null) {
                variableLocation = false;
            }
            this.setType("break");
            super.capacity = Capacity.Builder.newInstance().build();
            super.skills = skillBuilder.build();
            return new BreakForMultipleTimeWindows(this);
        }

    }

    private boolean variableLocation = true;

    BreakForMultipleTimeWindows(BreakForMultipleTimeWindows.Builder builder) {
        super(builder);
        this.variableLocation = builder.variableLocation;
    }

    public boolean hasVariableLocation() {
        return variableLocation;
    }


}
