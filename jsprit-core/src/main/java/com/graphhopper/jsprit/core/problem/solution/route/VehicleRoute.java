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
package com.graphhopper.jsprit.core.problem.solution.route;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.driver.DriverImpl;
import com.graphhopper.jsprit.core.problem.job.AbstractJob;
import com.graphhopper.jsprit.core.problem.job.AbstractSingleActivityJob;
import com.graphhopper.jsprit.core.problem.job.Break;
import com.graphhopper.jsprit.core.problem.job.DeliveryJob;
import com.graphhopper.jsprit.core.problem.job.PickupJob;
import com.graphhopper.jsprit.core.problem.job.ShipmentJob;
import com.graphhopper.jsprit.core.problem.solution.route.activity.DeliveryActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.End;
import com.graphhopper.jsprit.core.problem.solution.route.activity.JobActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.Start;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivities;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;

/**
 * Contains the tour, i.e. a number of activities, a vehicle servicing the tour
 * and a driver.
 *
 * @author stefan
 */
public class VehicleRoute {

    /**
     * Returns a deep copy of this vehicleRoute.
     *
     * @param route
     *            route to copy
     * @return copied route
     * @throws IllegalArgumentException
     *             if route is null
     */
    public static VehicleRoute copyOf(VehicleRoute route) {
        if (route == null)
            throw new IllegalArgumentException("route must not be null");
        return new VehicleRoute(route);
    }

    /**
     * Returns an empty route.
     * <p>
     * <p>
     * An empty route has an empty list of tour-activities, no driver
     * (DriverImpl.noDriver()) and no vehicle (VehicleImpl.createNoVehicle()).
     *
     * @return empty route
     */
    public static VehicleRoute emptyRoute() {
        return Builder.newInstance(VehicleImpl.createNoVehicle(), DriverImpl.noDriver()).build();
    }

    /**
     * Builder that builds the vehicle route.
     *
     * @author stefan
     * @author Balage
     */
    public static class Builder {

        /**
         * Returns new instance of this builder.
         * <p>
         * <p>
         * <b>Construction-settings of vehicleRoute:</b>
         * <p>
         * startLocation == vehicle.getStartLocationId()
         * <p>
         * endLocation == vehicle.getEndLocationId()
         * <p>
         * departureTime == vehicle.getEarliestDepartureTime()
         * <p>
         * latestStart == Double.MAX_VALUE
         * <p>
         * earliestEnd == 0.0
         *
         * @param vehicle
         *            employed vehicle
         * @param driver
         *            employed driver
         * @return this builder
         */
        public static Builder newInstance(Vehicle vehicle, Driver driver) {
            if (vehicle == null || driver == null)
                throw new IllegalArgumentException(
                        "null arguments not accepted. ini emptyRoute with VehicleImpl.createNoVehicle() and DriverImpl.noDriver()");
            return new Builder(vehicle, driver);
        }

        /**
         * Returns new instance of this builder.
         * <p>
         * <p>
         * <b>Construction-settings of vehicleRoute:</b>
         * <p>
         * startLocation == vehicle.getStartLocationId()
         * <p>
         * endLocation == vehicle.getEndLocationId()
         * <p>
         * departureTime == vehicle.getEarliestDepartureTime()
         * <p>
         * latestStart == Double.MAX_VALUE
         * <p>
         * earliestEnd == 0.0
         *
         * @param vehicle
         *            employed vehicle
         * @return this builder
         */
        public static Builder newInstance(Vehicle vehicle) {
            if (vehicle == null)
                throw new IllegalArgumentException(
                        "null arguments not accepted. ini emptyRoute with VehicleImpl.createNoVehicle() and DriverImpl.noDriver()");
            return new Builder(vehicle, DriverImpl.noDriver());
        }

        private Vehicle vehicle;

        private Driver driver;

        private Start start;

        private End end;

        private TourActivities tourActivities = new TourActivities();

        private Builder(Vehicle vehicle, Driver driver) {
            super();
            this.vehicle = vehicle;
            this.driver = driver;
            start = new Start(vehicle.getStartLocation(), vehicle.getEarliestDeparture(), Double.MAX_VALUE);
            start.setEndTime(vehicle.getEarliestDeparture());
            end = new End(vehicle.getEndLocation(), 0.0, vehicle.getLatestArrival());
        }

        public Builder setDepartureTime(double departureTime) {
            if (departureTime < start.getEndTime())
                throw new IllegalArgumentException(
                        "departureTime < vehicle.getEarliestDepartureTime(). this must not be.");
            start.setEndTime(departureTime);
            return this;
        }


        /**
         * Adds the first activity of a job with its first time window.
         *
         * @param job
         *            The job to get the activity from.
         * @return The builder.
         */
        public Builder addActivity(AbstractJob job) {
            return addActivity(job, 0);
        }

        /**
         * Adds all activities of a job with their first time window.
         *
         * @param job
         *            The job to get the activities from.
         * @return The builder.
         */
        public Builder addAllActivities(AbstractJob job) {
            job.getActivityList().getAll().forEach(a -> addActivity(a));
            return this;
        }

        /**
         * Adds the selected activity of a job with its first time window.
         *
         * @param job
         *            The job to get the activity from.
         * @param activityIndex
         *            The (0-based) index of the activity to add.
         * @return The builder.
         * @throws IndexOutOfBoundsException
         *             When the index is invalid.
         */
        public Builder addActivity(AbstractJob job, int activityIndex) {
            if (job == null)
                throw new IllegalArgumentException("job must not be null");
            JobActivity activity = job.getActivityList().getAll().get(activityIndex);
            return addActivity(activity);
        }

        /**
         * Adds the first activity with the selected type of a job with its
         * first time window.
         *
         * <p>
         * Note: If your job has more than one activity of the same type, you
         * have to add them manually:
         * </p>
         *
         * <pre>
         * job.getActivityList().getAll().stream()
         *     .filter(a -> a instanceof ExchangeActivity))
         *     .forEach(a -> addActivity(a));
         * </pre>
         *
         * @param job
         *            The job to get the activity from.
         * @param activityClass
         *            The class of the activity.
         * @return The builder.
         * @throws IllegalArgumentException
         *             When there is no activity with the given type in the job.
         */
        public Builder addActivity(AbstractJob job, Class<? extends JobActivity> activityClass) {
            if (job == null)
                throw new IllegalArgumentException("job must not be null");
            Optional<JobActivity> activity = job.getActivityList().getAll().stream()
                    .filter(a -> activityClass.isAssignableFrom(a.getClass()))
                    .findFirst();
            if (activity.isPresent())
                return addActivity(activity.get());
            else
                throw new IllegalArgumentException("Job has no " + activityClass.getSimpleName() + " activity");
        }

        /**
         * Adds the first activity of a job with the given time window.
         *
         * @param job
         *            The job to get the activity from.
         * @param timeWindow
         *            The time window to use.
         * @return The builder.
         */
        public Builder addActivity(AbstractJob job, TimeWindow timeWindow) {
            return addActivity(job, 0, timeWindow);
        }

        /**
         * Adds the first activity with the selected type of a job with the
         * selected time window.
         *
         * @param job
         *            The job to get the activity from.
         * @param activityClass
         *            The class of the activity.
         * @param timeWindow
         *            The time window to use.
         * @return The builder.
         * @throws IllegalArgumentException
         *             When there is no activity with the given type in the job.
         */
        public Builder addActivity(AbstractJob job, Class<? extends JobActivity> activityClass, TimeWindow timeWindow) {
            if (job == null)
                throw new IllegalArgumentException("job must not be null");
            Optional<JobActivity> activity = job.getActivityList().getAll().stream()
                    .filter(a -> activityClass.isAssignableFrom(a.getClass()))
                    .findFirst();
            if (activity.isPresent())
                return addActivity(activity.get(), timeWindow);
            else
                throw new IllegalArgumentException("Job has no " + activityClass.getSimpleName() + " activity");
        }

        /**
         * Adds the selected activity of a job with the specified time window.
         *
         * @param job
         *            The job to get the activity from.
         * @param activityIndex
         *            The (0-based) index of the activity to add.
         * @param timeWindow
         *            The time window to use.
         * @return The builder.
         * @throws IndexOutOfBoundsException
         *             When the index is invalid.
         */
        public Builder addActivity(AbstractJob job, int activityIndex, TimeWindow timeWindow) {
            if (job == null)
                throw new IllegalArgumentException("job must not be null");
            TourActivity act = job.getActivityList().getAll().get(activityIndex);
            return addActivity(act, timeWindow);
        }

        /**
         * Adds the activity to the route with its first time window.
         *
         * @param activity
         *            The activity to add.
         * @return The builder.
         */
        public Builder addActivity(JobActivity activity) {
            return addActivity(activity, activity.getTimeWindows().iterator().next());
        }

        /**
         * Adds the activity to the route with the selected time window.
         *
         * @param activity
         *            The activity to add.
         * @param timeWindow
         *            The time window to use.
         * @return The builder.
         */
        public Builder addActivity(TourActivity activity, TimeWindow timeWindow) {
            if (activity == null)
                throw new IllegalArgumentException("activity must not be null");
            activity.setTheoreticalEarliestOperationStartTime(timeWindow.getStart());
            activity.setTheoreticalLatestOperationStartTime(timeWindow.getEnd());
            tourActivities.addActivity(activity);
            return this;
        }

        @Deprecated
        public Builder addService(AbstractSingleActivityJob<?> service) {
            return addActivity(service);
        }

        @Deprecated
        public Builder addService(AbstractSingleActivityJob<?> service, TimeWindow timeWindow) {
            return addActivity(service, timeWindow);
        }

        @Deprecated
        public Builder addBreak(Break currentbreak) {
            return addActivity(currentbreak);
        }

        @Deprecated
        public Builder addBreak(Break currentbreak, TimeWindow timeWindow) {
            return addActivity(currentbreak, timeWindow);
        }

        @Deprecated
        public Builder addPickup(PickupJob pickup) {
            return addActivity(pickup);
        }

        @Deprecated
        public Builder addPickup(PickupJob pickup, TimeWindow timeWindow) {
            return addActivity(pickup, timeWindow);
        }

        @Deprecated
        public Builder addDelivery(DeliveryJob delivery) {
            return addActivity(delivery);
        }

        @Deprecated
        public Builder addDelivery(DeliveryJob delivery, TimeWindow timeWindow) {
            return addActivity(delivery, timeWindow);
        }

        @Deprecated
        public Builder addPickup(ShipmentJob shipment) {
            return addActivity(shipment, PickupActivity.class);
        }

        @Deprecated
        public Builder addPickup(ShipmentJob shipment, TimeWindow pickupTimeWindow) {
            return addActivity(shipment, PickupActivity.class, pickupTimeWindow);
        }

        @Deprecated
        public Builder addDelivery(ShipmentJob shipment) {
            return addActivity(shipment, DeliveryActivity.class);
        }

        @Deprecated
        public Builder addDelivery(ShipmentJob shipment, TimeWindow deliveryTimeWindow) {
            return addActivity(shipment, PickupActivity.class, deliveryTimeWindow);

        }

        /**
         * Builds the route.
         *
         * @return {@link VehicleRoute}
         * @throws IllegalArgumentException
         *             if there are still shipments that have been picked up
         *             though but not delivery.
         */
        public VehicleRoute build() {
            validateActivities();
            if (!vehicle.isReturnToDepot()) {
                if (!tourActivities.isEmpty()) {
                    end.setLocation(tourActivities.getActivities().get(tourActivities.getActivities().size() - 1).getLocation());
                }
            }
            return new VehicleRoute(this);
        }

        private void validateActivities() {
            Map<AbstractJob, Set<JobActivity>> activityCounter = new HashMap<>();
            Set<JobActivity> activities = new HashSet<>();
            Set<JobActivity> duplicatedActivities = new HashSet<>();
            tourActivities.getActivities().stream()
            .filter(a -> a instanceof JobActivity)
            .map(a -> (JobActivity) a)
            .forEach(ja -> {
                // Checks duplicated activities
                if (activities.contains(ja)) {
                    duplicatedActivities.add(ja);
                    return;
                }
                activities.add(ja);

                AbstractJob job = ja.getJob();
                // New job, add all activities to missing list
                if (!activityCounter.containsKey(job)) {
                    activityCounter.put(job, new HashSet<>(job.getActivityList().getAll()));
                }
                Set<JobActivity> missingActivities = activityCounter.get(job);

                // Remove activity from missing
                missingActivities.remove(ja);

                // All activity has been found
                if (missingActivities.isEmpty()) {
                    activityCounter.remove(job);
                }
            });

            if (!duplicatedActivities.isEmpty()) {
                System.err.println("Duplicated activities: \n" + duplicatedActivities.stream()
                .map(a -> a.toString())
                .collect(Collectors.joining("\n   ", "   ", "")));
            }
            if (!activityCounter.isEmpty()) {
                System.err.println("Missing activities:");
                for (Entry<AbstractJob, Set<JobActivity>> missing : activityCounter.entrySet()) {
                    System.err.println("   "+missing.getKey());
                    for (JobActivity act : missing.getValue()) {
                        System.err.println("      "+act);
                    }
                }
            }

            if (!duplicatedActivities.isEmpty() || !activityCounter.isEmpty())
                throw new IllegalArgumentException("Invalid route. See details above.");
        }
    }

    private TourActivities tourActivities;

    private Vehicle vehicle;

    private Driver driver;

    private Start start;

    private End end;

    private int id = 0;

    /**
     * Copy constructor copying a route.
     *
     * @param route
     *            to copy
     */
    private VehicleRoute(VehicleRoute route) {
        start = Start.copyOf(route.getStart());
        end = End.copyOf(route.getEnd());
        tourActivities = TourActivities.copyOf(route.getTourActivities());
        vehicle = route.getVehicle();
        driver = route.getDriver();
    }

    /**
     * Constructs route.
     *
     * @param builder
     *            used to build route
     */
    private VehicleRoute(Builder builder) {
        tourActivities = builder.tourActivities;
        vehicle = builder.vehicle;
        driver = builder.driver;
        start = builder.start;
        end = builder.end;
    }

    /**
     * Returns an unmodifiable list of activities on this route (without
     * start/end).
     *
     * @return list of tourActivities
     */
    public List<TourActivity> getActivities() {
        return Collections.unmodifiableList(tourActivities.getActivities());
    }

    /**
     * Returns TourActivities.
     *
     * @return {@link TourActivities}
     */
    public TourActivities getTourActivities() {
        return tourActivities;
    }

    /**
     * Returns the vehicle operating this route.
     *
     * @return Vehicle
     */
    public Vehicle getVehicle() {
        return vehicle;
    }

    /**
     * Returns the driver operating this route.
     *
     * @return Driver
     */
    public Driver getDriver() {
        return driver;
    }

    /**
     * Sets the vehicle and its departureTime from
     * <code>vehicle.getStartLocationId()</code>.
     * <p>
     * <p>
     * This implies the following:<br>
     * if start and end are null, new start and end activities are created.<br>
     * <p>
     * startActivity is initialized with the start-location of the specified
     * vehicle (<code>vehicle.getStartLocationId()</code>). the time-window of
     * this activity is initialized such that
     * [<code>startActivity.getTheoreticalEarliestOperationStartTime()</code> =
     * <code>vehicle.getEarliestDeparture()</code>][<code>startActivity.getTheoreticalLatestOperationStartTime()</code>
     * = <code>vehicle.getLatestArrival()</code>]
     * <p>
     * endActivity is initialized with the end-location of the specified vehicle
     * (<code>vehicle.getEndLocationId()</code>). The time-window of the
     * endActivity is initialized such that
     * [<code>endActivity.getTheoreticalEarliestOperationStartTime()</code> =
     * <code>vehicle.getEarliestDeparture()</code>][<code>endActivity.getTheoreticalLatestOperationStartTime()</code>
     * = <code>vehicle.getLatestArrival()</code>]
     * <p>
     * startActivity.endTime (<code>startActivity.getEndTime()</code>) is set to
     * max{<code>vehicle.getEarliestDeparture()</code>,
     * <code>vehicleDepTime</code>}. thus,
     * <code>vehicle.getEarliestDeparture()</code> is a physical constraint that
     * has to be met.
     *
     * @param vehicle
     *            to be employed
     * @param vehicleDepTime
     *            of employed vehicle
     */
    public void setVehicleAndDepartureTime(Vehicle vehicle, double vehicleDepTime) {
        this.vehicle = vehicle;
        setStartAndEnd(vehicle, vehicleDepTime);
    }

    private void setStartAndEnd(Vehicle vehicle, double vehicleDepTime) {
        if (!(vehicle instanceof VehicleImpl.NoVehicle)) {
            if (start == null && end == null) {
                start = new Start(vehicle.getStartLocation(), vehicle.getEarliestDeparture(), vehicle.getLatestArrival());
                end = new End(vehicle.getEndLocation(), vehicle.getEarliestDeparture(), vehicle.getLatestArrival());
            }
            start.setEndTime(Math.max(vehicleDepTime, vehicle.getEarliestDeparture()));
            start.setTheoreticalEarliestOperationStartTime(vehicle.getEarliestDeparture());
            start.setTheoreticalLatestOperationStartTime(vehicle.getLatestArrival());
            start.setLocation(vehicle.getStartLocation());
            end.setLocation(vehicle.getEndLocation());
            end.setTheoreticalEarliestOperationStartTime(vehicle.getEarliestDeparture());
            end.setTheoreticalLatestOperationStartTime(vehicle.getLatestArrival());
        }

    }

    /**
     * Returns the departureTime of this vehicle in this route.
     *
     * @return departureTime
     * @throws IllegalArgumentException
     *             if start is null
     */
    public double getDepartureTime() {
        if (start == null)
            throw new IllegalArgumentException(
                    "cannot get departureTime without having a vehicle on this route. use setVehicle(vehicle,departureTime) instead.");
        return start.getEndTime();
    }

    /**
     * Returns tour if tour-activity-sequence is empty, i.e. to activity on the
     * tour yet.
     *
     * @return true if route is empty
     */
    public boolean isEmpty() {
        return tourActivities.isEmpty();
    }

    /**
     * Returns start-activity of this route.
     *
     * @return start
     */
    public Start getStart() {
        return start;
    }

    /**
     * Returns end-activity of this route.
     *
     * @return end
     */
    public End getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return "[id=" + id + "][start=" + start + "][end=" + end + "][departureTime=" + start.getEndTime() + "][vehicle=" + vehicle
                + "][driver="
                + driver + "][nuOfActs=" + tourActivities.getActivities().size() + "]";
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

}
