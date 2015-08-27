/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package jsprit.core.problem.solution.route;

import jsprit.core.problem.AbstractActivity;
import jsprit.core.problem.JobActivityFactory;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.driver.DriverImpl;
import jsprit.core.problem.job.*;
import jsprit.core.problem.solution.route.activity.*;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleImpl.NoVehicle;

import java.util.*;

/**
 * Contains the tour, i.e. a number of activities, a vehicle servicing the tour and a driver.
 *
 *
 * @author stefan
 *
 */
public class VehicleRoute {

    /**
     * Returns a deep copy of this vehicleRoute.
     *
     * @param route route to copy
     * @return copied route
     * @throws IllegalArgumentException if route is null
     */
    public static VehicleRoute copyOf(VehicleRoute route) {
        if (route == null) throw new IllegalArgumentException("route must not be null");
        return new VehicleRoute(route);
    }

    /**
     * Returns an empty route.
     *
     * <p>An empty route has an empty list of tour-activities, no driver (DriverImpl.noDriver()) and no vehicle (VehicleImpl.createNoVehicle()).
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
     *
     */
    public static class Builder {

        private Map<Shipment, TourActivity> openActivities = new HashMap<Shipment, TourActivity>();

        /**
         * Returns new instance of this builder.
         *
         * <p><b>Construction-settings of vehicleRoute:</b>
         * <p>startLocation == vehicle.getStartLocationId()
         * <p>endLocation == vehicle.getEndLocationId()
         * <p>departureTime == vehicle.getEarliestDepartureTime()
         * <p>latestStart == Double.MAX_VALUE
         * <p>earliestEnd == 0.0
         *
         * @param vehicle employed vehicle
         * @param driver employed driver
         * @return this builder
         */
        public static Builder newInstance(Vehicle vehicle, Driver driver) {
            if (vehicle == null || driver == null)
                throw new IllegalArgumentException("null arguments not accepted. ini emptyRoute with VehicleImpl.createNoVehicle() and DriverImpl.noDriver()");
            return new Builder(vehicle, driver);
        }

        /**
         * Returns new instance of this builder.
         *
         * <p><b>Construction-settings of vehicleRoute:</b>
         * <p>startLocation == vehicle.getStartLocationId()
         * <p>endLocation == vehicle.getEndLocationId()
         * <p>departureTime == vehicle.getEarliestDepartureTime()
         * <p>latestStart == Double.MAX_VALUE
         * <p>earliestEnd == 0.0
         *
         * @param vehicle employed vehicle
         * @return this builder
         */
        public static Builder newInstance(Vehicle vehicle) {
            if (vehicle == null)
                throw new IllegalArgumentException("null arguments not accepted. ini emptyRoute with VehicleImpl.createNoVehicle() and DriverImpl.noDriver()");
            return new Builder(vehicle, DriverImpl.noDriver());
        }

        private Vehicle vehicle;

        private Driver driver;

        private Start start;

        private End end;

        private TourActivities tourActivities = new TourActivities();

        private TourActivityFactory serviceActivityFactory = new DefaultTourActivityFactory();

        private TourShipmentActivityFactory shipmentActivityFactory = new DefaultShipmentActivityFactory();

        private Set<Shipment> openShipments = new HashSet<Shipment>();

        private JobActivityFactory jobActivityFactory = new JobActivityFactory() {

            @Override
            public List<AbstractActivity> createActivities(Job job) {
                List<AbstractActivity> acts = new ArrayList<AbstractActivity>();
                if (job instanceof Service) {
                    acts.add(serviceActivityFactory.createActivity((Service) job));
                } else if (job instanceof Shipment) {
                    acts.add(shipmentActivityFactory.createPickup((Shipment) job));
                    acts.add(shipmentActivityFactory.createDelivery((Shipment) job));
                }
                return acts;
            }

        };

        /**
         * Sets the serviceActivityFactory to create serviceActivities.
         *
         * <p>By default {@link DefaultTourActivityFactory} is used.
         *
         * @param serviceActivityFactory the factory to create serviceActivities
         */
        @Deprecated
        public Builder setServiceActivityFactory(TourActivityFactory serviceActivityFactory) {
            this.serviceActivityFactory = serviceActivityFactory;
            return this;
        }

        /**
         * Sets the shipmentActivityFactory to create shipmentActivities.
         *
         * <p>By default {@link DefaultShipmentActivityFactory} is used.
         *
         * @param shipmentActivityFactory the factory to create shipmentActivities
         */
        @Deprecated
        public Builder setShipmentActivityFactory(TourShipmentActivityFactory shipmentActivityFactory) {
            this.shipmentActivityFactory = shipmentActivityFactory;
            return this;
        }

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
         * Sets the departure-time of the route, i.e. which is the time the vehicle departs from start-location.
         *
         * <p><b>Note</b> that departureTime cannot be lower than earliestDepartureTime of vehicle.
         *
         * @param departureTime departure time of vehicle being employed for this route
         * @return builder
         * @throws IllegalArgumentException if departureTime < vehicle.getEarliestDeparture()
         */
        public Builder setDepartureTime(double departureTime) {
            if (departureTime < start.getEndTime())
                throw new IllegalArgumentException("departureTime < vehicle.getEarliestDepartureTime(). this must not be.");
            start.setEndTime(departureTime);
            return this;
        }

        /**
         * Sets the end-time of the route, i.e. which is the time the vehicle has to be at its end-location at latest.
         *
         * @param endTime endTime of route
         * @return this builder
         * @throws IllegalArgumentException if endTime > vehicle.getLatestArrival()
         */
        @Deprecated
        public Builder setRouteEndArrivalTime(double endTime) {
            if (endTime > vehicle.getLatestArrival())
                throw new IllegalArgumentException("endTime > vehicle.getLatestArrival(). this must not be.");
            end.setArrTime(endTime);
            return this;
        }

        /**
         * Adds a service to this route.
         *
         * <p>This implies that for this service a serviceActivity is created with {@link TourActivityFactory} and added to the sequence of tourActivities.
         *
         * <p>The resulting activity occurs in the activity-sequence in the order adding/inserting.
         *
         * @param service to be added
         * @return this builder
         * @throws IllegalArgumentException if service is null
         */
        public Builder addService(Service service) {
            if (service == null) throw new IllegalArgumentException("service must not be null");
            List<AbstractActivity> acts = jobActivityFactory.createActivities(service);
            TourActivity act = acts.get(0);
            tourActivities.addActivity(act);
            return this;
        }

        /**
         * Adds a pickup to this route.
         *
         * @param pickup pickup to be added
         * @return the builder
         */
        public Builder addPickup(Pickup pickup) {
            if (pickup == null) throw new IllegalArgumentException("pickup must not be null");
            addService(pickup);
            return this;
        }

        /**
         * Adds a delivery to this route.
         *
         * @param delivery delivery to be added
         * @return the builder
         */
        public Builder addDelivery(Delivery delivery) {
            if (delivery == null) throw new IllegalArgumentException("delivery must not be null");
            addService(delivery);
            return this;
        }

        /**
         * Adds a the pickup of the specified shipment.
         *
         * @param shipment to be picked up and added to this route
         * @throws IllegalStateException if method has already been called with the specified shipment.
         * @return the builder
         */
        public Builder addPickup(Shipment shipment) {
            if (openShipments.contains(shipment))
                throw new IllegalStateException("shipment has already been added. cannot add it twice.");
            List<AbstractActivity> acts = jobActivityFactory.createActivities(shipment);
            TourActivity act = acts.get(0);
            tourActivities.addActivity(act);
            openShipments.add(shipment);
            openActivities.put(shipment, acts.get(1));
            return this;
        }

        /**
         * Adds a the delivery of the specified shipment.
         *
         * @param shipment to be delivered and add to this vehicleRoute
         * @throws IllegalStateException if specified shipment has not been picked up yet (i.e. method addPickup(shipment) has not been called yet).
         * @return builder
         */
        public Builder addDelivery(Shipment shipment) {
            if (openShipments.contains(shipment)) {
                TourActivity act = openActivities.get(shipment);
                tourActivities.addActivity(act);
                openShipments.remove(shipment);
            } else {
                throw new IllegalStateException("cannot deliver shipment. shipment " + shipment + " needs to be picked up first.");
            }
            return this;
        }


        /**
         * Builds the route.
         *
         * @return {@link VehicleRoute}
         * @throws IllegalStateException if there are still shipments that have been picked up though but not delivery.
         */
        public VehicleRoute build() {
            if (!openShipments.isEmpty()) {
                throw new IllegalStateException("there are still shipments that have not been delivered yet.");
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

    /**
     * Copy constructor copying a route.
     *
     * @param route to copy
     */
    private VehicleRoute(VehicleRoute route) {
        this.start = Start.copyOf(route.getStart());
        this.end = End.copyOf(route.getEnd());
        this.tourActivities = TourActivities.copyOf(route.getTourActivities());
        this.vehicle = route.getVehicle();
        this.driver = route.getDriver();
    }

    /**
     * Constructs route.
     *
     * @param builder used to build route
     */
    private VehicleRoute(Builder builder) {
        this.tourActivities = builder.tourActivities;
        this.vehicle = builder.vehicle;
        this.driver = builder.driver;
        this.start = builder.start;
        this.end = builder.end;
    }

    /**
     * Returns an unmodifiable list of activities on this route (without start/end).
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
     * Sets the vehicle and its departureTime from <code>vehicle.getStartLocationId()</code>.
     *
     * <p>This implies the following:<br>
     * if start and end are null, new start and end activities are created.<br>
     * <p>startActivity is initialized with the start-location of the specified vehicle (<code>vehicle.getStartLocationId()</code>). the time-window of this activity is initialized
     * such that [<code>startActivity.getTheoreticalEarliestOperationStartTime()</code> = <code>vehicle.getEarliestDeparture()</code>][<code>startActivity.getTheoreticalLatestOperationStartTime()</code> = <code>vehicle.getLatestArrival()</code>]
     * <p>endActivity is initialized with the end-location of the specified vehicle (<code>vehicle.getEndLocationId()</code>). The time-window of the
     * endActivity is initialized such that [<code>endActivity.getTheoreticalEarliestOperationStartTime()</code> = <code>vehicle.getEarliestDeparture()</code>][<code>endActivity.getTheoreticalLatestOperationStartTime()</code> = <code>vehicle.getLatestArrival()</code>]
     * <p>startActivity.endTime (<code>startActivity.getEndTime()</code>) is set to max{<code>vehicle.getEarliestDeparture()</code>, <code>vehicleDepTime</code>}.
     * thus, <code>vehicle.getEarliestDeparture()</code> is a physical constraint that has to be met.
     *
     * @param vehicle to be employed
     * @param vehicleDepTime of employed vehicle
     */
    public void setVehicleAndDepartureTime(Vehicle vehicle, double vehicleDepTime) {
        this.vehicle = vehicle;
        setStartAndEnd(vehicle, vehicleDepTime);
    }

    private void setStartAndEnd(Vehicle vehicle, double vehicleDepTime) {
        if (!(vehicle instanceof NoVehicle)) {
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
     * @throws IllegalStateException if start is null
     */
    public double getDepartureTime() {
        if (start == null)
            throw new IllegalStateException("cannot get departureTime without having a vehicle on this route. use setVehicle(vehicle,departureTime) instead.");
        return start.getEndTime();
    }

    /**
     * Returns tour if tour-activity-sequence is empty, i.e. to activity on the tour yet.
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
        return "[start=" + start + "][end=" + end + "][departureTime=" + start.getEndTime() + "][vehicle=" + vehicle + "][driver=" + driver + "][nuOfActs=" + tourActivities.getActivities().size() + "]";
    }

}
