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

package com.graphhopper.jsprit.core.problem.solution.spec;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.*;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Materializes a {@link SolutionSpec} into a {@link VehicleRoutingProblemSolution}.
 */
public class SolutionSpecMaterializer {

    /**
     * Result of validating a spec against a VRP.
     */
    public record ValidationResult(
            boolean valid,
            List<String> missingVehicles,
            List<String> missingJobs,
            List<String> errors
    ) {
        public static ValidationResult success() {
            return new ValidationResult(true, List.of(), List.of(), List.of());
        }

        public static ValidationResult failure(List<String> missingVehicles, List<String> missingJobs, List<String> errors) {
            return new ValidationResult(false, missingVehicles, missingJobs, errors);
        }
    }

    private final VehicleRoutingProblem vrp;
    private final Map<String, Vehicle> vehicleMap;
    private final Map<String, Job> jobMap;

    public SolutionSpecMaterializer(VehicleRoutingProblem vrp) {
        this.vrp = vrp;
        this.vehicleMap = vrp.getVehicles().stream()
                .collect(Collectors.toMap(Vehicle::getId, v -> v));
        this.jobMap = vrp.getJobs();
    }

    /**
     * Validates a spec against the VRP.
     *
     * @param spec the solution spec to validate
     * @return validation result with details about any missing vehicles or jobs
     */
    public ValidationResult validate(SolutionSpec spec) {
        List<String> missingVehicles = new ArrayList<>();
        List<String> missingJobs = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (RouteSpec routeSpec : spec.routes()) {
            if (!vehicleMap.containsKey(routeSpec.vehicleId())) {
                missingVehicles.add(routeSpec.vehicleId());
            }

            for (ActivitySpec actSpec : routeSpec.activities()) {
                if (!jobMap.containsKey(actSpec.jobId())) {
                    missingJobs.add(actSpec.jobId());
                    continue;
                }

                Job job = jobMap.get(actSpec.jobId());
                String error = validateActivitySpec(actSpec, job);
                if (error != null) {
                    errors.add(error);
                }
            }
        }

        if (missingVehicles.isEmpty() && missingJobs.isEmpty() && errors.isEmpty()) {
            return ValidationResult.success();
        }
        return ValidationResult.failure(missingVehicles, missingJobs, errors);
    }

    private String validateActivitySpec(ActivitySpec actSpec, Job job) {
        // Validate activity type matches job type
        if (job instanceof Shipment) {
            if (actSpec.type() == ActivityType.VISIT) {
                return "Job '%s' is a Shipment but activity type is VISIT. Use PICKUP or DELIVERY.".formatted(actSpec.jobId());
            }
        } else {
            if (actSpec.type() == ActivityType.PICKUP || actSpec.type() == ActivityType.DELIVERY) {
                return "Job '%s' is not a Shipment but activity type is %s. Use VISIT.".formatted(actSpec.jobId(), actSpec.type());
            }
        }

        // Validate time window index if specified
        if (actSpec.options() != null && actSpec.options().timeWindowIndex() != null) {
            Activity activity = getActivity(job, actSpec.type());
            int twIndex = actSpec.options().timeWindowIndex();
            if (twIndex < 0 || twIndex >= activity.getTimeWindows().size()) {
                return "Job '%s' activity has %d time windows but index %d was specified.".formatted(
                        actSpec.jobId(), activity.getTimeWindows().size(), twIndex);
            }
        }

        return null;
    }

    /**
     * Materializes a spec into a solution.
     * <p>
     * Call {@link #validate(SolutionSpec)} first to check for errors.
     *
     * @param spec the solution spec to materialize
     * @return the materialized solution
     * @throws IllegalArgumentException if the spec references missing vehicles or jobs
     */
    public VehicleRoutingProblemSolution materialize(SolutionSpec spec) {
        ValidationResult validation = validate(spec);
        if (!validation.valid()) {
            throw new IllegalArgumentException("Invalid spec: missing vehicles=%s, missing jobs=%s, errors=%s".formatted(
                    validation.missingVehicles(), validation.missingJobs(), validation.errors()));
        }

        Set<String> assignedJobIds = new HashSet<>();
        List<VehicleRoute> routes = new ArrayList<>();

        for (RouteSpec routeSpec : spec.routes()) {
            VehicleRoute route = materializeRoute(routeSpec, assignedJobIds);
            if (route != null) {
                routes.add(route);
            }
        }

        // Derive unassigned jobs
        List<Job> unassigned = jobMap.values().stream()
                .filter(j -> !assignedJobIds.contains(j.getId()))
                .toList();

        return new VehicleRoutingProblemSolution(routes, unassigned, 0.0);
    }

    private VehicleRoute materializeRoute(RouteSpec routeSpec, Set<String> assignedJobIds) {
        Vehicle vehicle = vehicleMap.get(routeSpec.vehicleId());
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle '%s' not found in VRP".formatted(routeSpec.vehicleId()));
        }

        if (routeSpec.activities().isEmpty()) {
            return null;
        }

        VehicleRoute.Builder builder = VehicleRoute.Builder.newInstance(vehicle)
                .setJobActivityFactory(vrp.getJobActivityFactory());

        for (ActivitySpec actSpec : routeSpec.activities()) {
            Job job = jobMap.get(actSpec.jobId());
            if (job == null) {
                throw new IllegalArgumentException("Job '%s' not found in VRP".formatted(actSpec.jobId()));
            }

            addActivityToRoute(builder, job, actSpec);
            assignedJobIds.add(actSpec.jobId());
        }

        return builder.build();
    }

    private void addActivityToRoute(VehicleRoute.Builder builder, Job job, ActivitySpec actSpec) {
        TimeWindow timeWindow = getTimeWindow(job, actSpec);

        if (job instanceof Shipment shipment) {
            if (actSpec.type() == ActivityType.PICKUP) {
                builder.addPickup(shipment, timeWindow);
            } else if (actSpec.type() == ActivityType.DELIVERY) {
                builder.addDelivery(shipment, timeWindow);
            }
        } else if (job instanceof Pickup pickup) {
            builder.addPickup(pickup, timeWindow);
        } else if (job instanceof Delivery delivery) {
            builder.addDelivery(delivery, timeWindow);
        } else if (job instanceof Service service) {
            builder.addService(service, timeWindow);
        }
    }

    private TimeWindow getTimeWindow(Job job, ActivitySpec actSpec) {
        Activity activity = getActivity(job, actSpec.type());
        Collection<TimeWindow> timeWindows = activity.getTimeWindows();

        if (actSpec.options() != null && actSpec.options().timeWindowIndex() != null) {
            int index = actSpec.options().timeWindowIndex();
            return timeWindows.stream()
                    .skip(index)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Time window index %d out of bounds for job '%s'".formatted(index, job.getId())));
        }

        // Default: use first time window
        return timeWindows.iterator().next();
    }

    private Activity getActivity(Job job, ActivityType type) {
        List<Activity> activities = job.getActivities();
        return switch (type) {
            case VISIT -> activities.get(0);
            case PICKUP -> activities.get(0);
            case DELIVERY -> activities.get(1);
        };
    }
}
