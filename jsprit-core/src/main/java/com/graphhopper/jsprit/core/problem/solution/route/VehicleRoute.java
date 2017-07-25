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
import java.util.Set;

import com.graphhopper.jsprit.core.problem.JobActivityFactory;
import com.graphhopper.jsprit.core.problem.SimpleJobActivityFactory;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.driver.DriverImpl;
import com.graphhopper.jsprit.core.problem.job.AbstractSingleActivityJob;
import com.graphhopper.jsprit.core.problem.job.Break;
import com.graphhopper.jsprit.core.problem.job.DeliveryJob;
import com.graphhopper.jsprit.core.problem.job.PickupJob;
import com.graphhopper.jsprit.core.problem.job.ShipmentJob;
import com.graphhopper.jsprit.core.problem.solution.route.activity.End;
import com.graphhopper.jsprit.core.problem.solution.route.activity.JobActivity;
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
        if (route == null) {
            throw new IllegalArgumentException("route must not be null");
        }
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
     */
    public static class Builder {

        private Map<ShipmentJob, TourActivity> openActivities = new HashMap<>();

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
            if (vehicle == null || driver == null) {
                throw new IllegalArgumentException(
                                "null arguments not accepted. ini emptyRoute with VehicleImpl.createNoVehicle() and DriverImpl.noDriver()");
            }
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
            if (vehicle == null) {
                throw new IllegalArgumentException(
                                "null arguments not accepted. ini emptyRoute with VehicleImpl.createNoVehicle() and DriverImpl.noDriver()");
            }
            return new Builder(vehicle, DriverImpl.noDriver());
        }

        private Vehicle vehicle;

        private Driver driver;

        private Start start;

        private End end;

        private TourActivities tourActivities = new TourActivities();

        // private TourActivityFactory serviceActivityFactory = new
        // DefaultTourActivityFactory();
        //
        // private TourShipmentActivityFactory shipmentActivityFactory = new
        // DefaultShipmentActivityFactory();

        private Set<ShipmentJob> openShipments = new HashSet<>();

        private JobActivityFactory jobActivityFactory = new SimpleJobActivityFactory();

        public Builder setJobActivityFactory(JobActivityFactory jobActivityFactory) {
            this.jobActivityFactory = jobActivityFactory;
            return this;
        }

        private Builder(Vehicle vehicle, Driver driver) {
            super();
            this.vehicle = vehicle;
            this.driver = driver;
            start = new Start(vehicle.getStartLocation(), vehicle.getEarliestDeparture(), Double.MAX_VALUE);
            start.setEndTime(vehicle.getEarliestDeparture());
            end = new End(vehicle.getEndLocation(), 0.0, vehicle.getLatestArrival());
        }

        /**
         * Sets the departure-time of the route, i.e. which is the time the
         * vehicle departs from start-location.
         * <p>
         * <p>
         * <b>Note</b> that departureTime cannot be lower than
         * earliestDepartureTime of vehicle.
         *
         * @param departureTime
         *            departure time of vehicle being employed for this route
         * @return builder
         * @throws IllegalArgumentException
         *             if departureTime < vehicle.getEarliestDeparture()
         */
        public Builder setDepartureTime(double departureTime) {
            if (departureTime < start.getEndTime()) {
                throw new IllegalArgumentException("departureTime < vehicle.getEarliestDepartureTime(). this must not be.");
            }
            start.setEndTime(departureTime);
            return this;
        }

        /**
         * Adds a service to this route. Activity is initialized with
         * .getSingleTimeWindow(). If you want to explicitly set another time
         * window use {@linkplain #addService(Service TimeWindow)}
         * <p>
         * <p>
         * This implies that for this service a serviceActivity is created with
         * {@link TourActivityFactory} and added to the sequence of
         * tourActivities.
         * <p>
         * <p>
         * The resulting activity occurs in the activity-sequence in the order
         * adding/inserting.
         *
         * <p>
         * <i><b>Note: Using this method is not recommended. Use the
         * {@linkplain #addService(AbstractSingleActivityJob, TimeWindow)}
         * instead.</b></i>
         * </p>
         *
         * @param service
         *            to be added
         * @return this builder
         * @throws IllegalArgumentException
         *             if service is null
         */
        public Builder addService(AbstractSingleActivityJob<?> service) {
            if (service == null) {
                throw new IllegalArgumentException("service must not be null");
            }
            return addSingleActivityJob(service);
        }

        public Builder addService(AbstractSingleActivityJob<?> service,
                        TimeWindow timeWindow) {
            if (service == null) {
                throw new IllegalArgumentException("service must not be null");
            }
            return addSingleActivityJob(service, timeWindow);
        }

        private Builder addSingleActivityJob(AbstractSingleActivityJob<?> service) {
            return addSingleActivityJob(service, service.getActivity().getSingleTimeWindow());
        }

        private Builder addSingleActivityJob(AbstractSingleActivityJob<?> service,
                        TimeWindow timeWindow) {
            if (service == null) {
                throw new IllegalArgumentException("service must not be null");
            }
            List<JobActivity> acts = jobActivityFactory.createActivities(service);
            TourActivity act = acts.get(0);
            act.setTheoreticalEarliestOperationStartTime(timeWindow.getStart());
            act.setTheoreticalLatestOperationStartTime(timeWindow.getEnd());
            tourActivities.addActivity(act);
            return this;
        }

        public Builder addBreak(Break currentbreak) {
            if (currentbreak == null) {
                throw new IllegalArgumentException("break must not be null");
            }
            return addSingleActivityJob(currentbreak);
        }

        public Builder addBreak(Break currentbreak, TimeWindow timeWindow) {
            if (currentbreak == null) {
                throw new IllegalArgumentException("break must not be null");
            }
            return addSingleActivityJob(currentbreak, timeWindow);
        }

        /**
         * Adds a pickup to this route.
         *
         * <p>
         * <i><b>Note: Using this method is not recommended. Use the
         * {@linkplain #addPickup(PickupJob, TimeWindow)} instead.</b></i>
         * </p>
         *
         * @param pickup
         *            pickup to be added
         * @return the builder
         */
        public Builder addPickup(PickupJob pickup) {
            if (pickup == null) {
                throw new IllegalArgumentException("pickup must not be null");
            }
            return addService(pickup);
        }

        public Builder addPickup(PickupJob pickup, TimeWindow timeWindow) {
            if (pickup == null) {
                throw new IllegalArgumentException("pickup must not be null");
            }
            return addSingleActivityJob(pickup, timeWindow);
        }

        /**
         * Adds a delivery to this route.
         *
         * <p>
         * <i><b>Note: Using this method is not recommended. Use the
         * {@linkplain #addDelivery(DeliveryJob, TimeWindow)} instead.</b></i>
         * </p>
         *
         *
         * @param delivery
         *            delivery to be added
         * @return the builder
         */
        public Builder addDelivery(DeliveryJob delivery) {
            if (delivery == null) {
                throw new IllegalArgumentException("delivery must not be null");
            }
            return addService(delivery);
        }

        public Builder addDelivery(DeliveryJob delivery, TimeWindow timeWindow) {
            if (delivery == null) {
                throw new IllegalArgumentException("delivery must not be null");
            }
            return addSingleActivityJob(delivery, timeWindow);
        }

        /**
         * Adds a the pickup of the specified shipment.
         *
         * <p>
         * <i><b>Note: Using this method is not recommended. Use the
         * {@linkplain #addPickup(ShipmentJob, TimeWindow)} instead.</b></i>
         * </p>
         *
         * @param shipment
         *            to be picked up and added to this route
         * @return the builder
         * @throws IllegalArgumentException
         *             if method has already been called with the specified
         *             shipment.
         */
        public Builder addPickup(ShipmentJob shipment) {
            return addPickup(shipment,
                            shipment.getPickupActivity().getSingleTimeWindow());
        }

        public Builder addPickup(ShipmentJob shipment, TimeWindow pickupTimeWindow) {
            if (openShipments.contains(shipment)) {
                throw new IllegalArgumentException("shipment has already been added. cannot add it twice.");
            }
            List<JobActivity> acts = jobActivityFactory.createActivities(shipment);
            TourActivity act = acts.get(0);
            act.setTheoreticalEarliestOperationStartTime(pickupTimeWindow.getStart());
            act.setTheoreticalLatestOperationStartTime(pickupTimeWindow.getEnd());
            tourActivities.addActivity(act);
            openShipments.add(shipment);
            openActivities.put(shipment, acts.get(1));
            return this;
        }

        /**
         * Adds a the delivery of the specified shipment. The shipment could
         * have only one time window.
         *
         * <p>
         * <i><b>Note: Using this method is not recommended. Use the
         * {@linkplain #addDelivery(ShipmentJob, TimeWindow)} instead.</b></i>
         * </p>
         *
         * @param shipment
         *            to be delivered and add to this vehicleRoute
         * @return builder
         * @throws IllegalArgumentException
         *             if specified shipment has not been picked up yet (i.e.
         *             method addPickup(shipment) has not been called yet).
         */
        public Builder addDelivery(ShipmentJob shipment) {
            return addDelivery(shipment, shipment.getDeliveryActivity().getSingleTimeWindow());
        }

        public Builder addDelivery(ShipmentJob shipment, TimeWindow deliveryTimeWindow) {
            if (openShipments.contains(shipment)) {
                TourActivity act = openActivities.get(shipment);
                act.setTheoreticalEarliestOperationStartTime(deliveryTimeWindow.getStart());
                act.setTheoreticalLatestOperationStartTime(deliveryTimeWindow.getEnd());
                tourActivities.addActivity(act);
                openShipments.remove(shipment);
            } else {
                throw new IllegalArgumentException(
                                "cannot deliver shipment. shipment " + shipment + " needs to be picked up first.");
            }
            return this;
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
            if (!openShipments.isEmpty()) {
                throw new IllegalArgumentException("there are still shipments that have not been delivered yet.");
            }
            if (!vehicle.isReturnToDepot()) {
                if (!tourActivities.isEmpty()) {
                    end.setLocation(tourActivities.getActivities().get(tourActivities.getActivities().size() - 1).getLocation());
                }
            }
            return new VehicleRoute(this);
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
        if (start == null) {
            throw new IllegalArgumentException(
                            "cannot get departureTime without having a vehicle on this route. use setVehicle(vehicle,departureTime) instead.");
        }
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
