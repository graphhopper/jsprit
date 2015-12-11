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

package jsprit.core.algorithm.recreate;

import jsprit.core.algorithm.recreate.listener.BeforeJobInsertionListener;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.vehicle.FiniteFleetManagerFactory;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleFleetManager;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.util.Coordinate;
import junit.framework.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

public class RegretInsertionTest {

    @Test
    public void noRoutesShouldBeCorrect() {
        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(0, 10)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(0, 5)).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s1).addJob(s2).addVehicle(v).build();

        VehicleFleetManager fm = new FiniteFleetManagerFactory(vrp.getVehicles()).createFleetManager();
        JobInsertionCostsCalculator calculator = getCalculator(vrp);
        RegretInsertionFast regretInsertion = new RegretInsertionFast(calculator, vrp, fm);
        Collection<VehicleRoute> routes = new ArrayList<VehicleRoute>();

        regretInsertion.insertJobs(routes, vrp.getJobs().values());
        Assert.assertEquals(1, routes.size());
    }

    @Test
    public void noJobsInRouteShouldBeCorrect() {
        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(0, 10)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(0, 5)).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s1).addJob(s2).addVehicle(v).build();

        VehicleFleetManager fm = new FiniteFleetManagerFactory(vrp.getVehicles()).createFleetManager();
        JobInsertionCostsCalculator calculator = getCalculator(vrp);
        RegretInsertionFast regretInsertion = new RegretInsertionFast(calculator, vrp, fm);
        Collection<VehicleRoute> routes = new ArrayList<VehicleRoute>();

        regretInsertion.insertJobs(routes, vrp.getJobs().values());
        Assert.assertEquals(2, routes.iterator().next().getActivities().size());
    }

    @Test
    public void s1ShouldBeAddedFirst() {
        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(0, 10)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(0, 5)).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        final VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s1).addJob(s2).addVehicle(v).build();

        VehicleFleetManager fm = new FiniteFleetManagerFactory(vrp.getVehicles()).createFleetManager();
        JobInsertionCostsCalculator calculator = getCalculator(vrp);
        RegretInsertionFast regretInsertion = new RegretInsertionFast(calculator, vrp, fm);
        Collection<VehicleRoute> routes = new ArrayList<VehicleRoute>();

        CkeckJobSequence position = new CkeckJobSequence(2, s1);
        regretInsertion.addListener(position);
        regretInsertion.insertJobs(routes, vrp.getJobs().values());
        Assert.assertTrue(position.isCorrect());
    }

    @Test
    public void shipment1ShouldBeAddedFirst() {
        Shipment s1 = Shipment.Builder.newInstance("s1")
            .setPickupLocation(Location.Builder.newInstance().setId("pick1").setCoordinate(Coordinate.newInstance(-1, 10)).build())
            .setDeliveryLocation(Location.Builder.newInstance().setId("del1").setCoordinate(Coordinate.newInstance(1, 10)).build())
            .build();

        Shipment s2 = Shipment.Builder.newInstance("s2")
            .setPickupLocation(Location.Builder.newInstance().setId("pick2").setCoordinate(Coordinate.newInstance(-1, 20)).build())
            .setDeliveryLocation(Location.Builder.newInstance().setId("del2").setCoordinate(Coordinate.newInstance(1, 20)).build())
            .build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(0, 0)).build()).build();
        final VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s1).addJob(s2).addVehicle(v).build();

        VehicleFleetManager fm = new FiniteFleetManagerFactory(vrp.getVehicles()).createFleetManager();
        JobInsertionCostsCalculator calculator = getShipmentCalculator(vrp);
        RegretInsertionFast regretInsertion = new RegretInsertionFast(calculator, vrp, fm);
        Collection<VehicleRoute> routes = new ArrayList<VehicleRoute>();

        CkeckJobSequence position = new CkeckJobSequence(2, s2);
        regretInsertion.addListener(position);
        regretInsertion.insertJobs(routes, vrp.getJobs().values());
        Assert.assertTrue(position.isCorrect());
    }

    private JobInsertionCostsCalculator getShipmentCalculator(final VehicleRoutingProblem vrp) {
        return new JobInsertionCostsCalculator() {

            @Override
            public InsertionData getInsertionData(VehicleRoute currentRoute, Job newJob, Vehicle newVehicle, double newVehicleDepartureTime, Driver newDriver, double bestKnownCosts) {
                Vehicle vehicle = vrp.getVehicles().iterator().next();
                if (newJob.getId().equals("s1")) {
                    return new InsertionData(10, 0, 0, vehicle, newDriver);
                } else {
                    return new InsertionData(20, 0, 0, vehicle, newDriver);
                }
            }
        };
    }


    static class CkeckJobSequence implements BeforeJobInsertionListener {

        int atPosition;

        Job job;

        int positionCounter = 1;

        boolean correct = false;

        CkeckJobSequence(int atPosition, Job job) {
            this.atPosition = atPosition;
            this.job = job;
        }

        @Override
        public void informBeforeJobInsertion(Job job, InsertionData data, VehicleRoute route) {
            if (job == this.job && atPosition == positionCounter) {
                correct = true;
            }
            positionCounter++;
        }

        public boolean isCorrect() {
            return correct;
        }
    }

    private JobInsertionCostsCalculator getCalculator(final VehicleRoutingProblem vrp) {
        return new JobInsertionCostsCalculator() {

            @Override
            public InsertionData getInsertionData(VehicleRoute currentRoute, Job newJob, Vehicle newVehicle, double newVehicleDepartureTime, Driver newDriver, double bestKnownCosts) {
                Service service = (Service) newJob;
                Vehicle vehicle = vrp.getVehicles().iterator().next();
                InsertionData iData;
                if (currentRoute.isEmpty()) {
                    double mc = getCost(service.getLocation(), vehicle.getStartLocation());
                    iData = new InsertionData(2 * mc, -1, 0, vehicle, newDriver);
                    iData.getEvents().add(new InsertActivity(currentRoute, vehicle, vrp.copyAndGetActivities(newJob).get(0), 0));
                    iData.getEvents().add(new SwitchVehicle(currentRoute, vehicle, newVehicleDepartureTime));
                } else {
                    double best = Double.MAX_VALUE;
                    int bestIndex = 0;
                    int index = 0;
                    TourActivity prevAct = currentRoute.getStart();
                    for (TourActivity act : currentRoute.getActivities()) {
                        double mc = getMarginalCost(service, prevAct, act);
                        if (mc < best) {
                            best = mc;
                            bestIndex = index;
                        }
                        index++;
                        prevAct = act;
                    }
                    double mc = getMarginalCost(service, prevAct, currentRoute.getEnd());
                    if (mc < best) {
                        best = mc;
                        bestIndex = index;
                    }
                    iData = new InsertionData(best, -1, bestIndex, vehicle, newDriver);
                    iData.getEvents().add(new InsertActivity(currentRoute, vehicle, vrp.copyAndGetActivities(newJob).get(0), bestIndex));
                    iData.getEvents().add(new SwitchVehicle(currentRoute, vehicle, newVehicleDepartureTime));
                }
                return iData;
            }

            private double getMarginalCost(Service service, TourActivity prevAct, TourActivity act) {
                double prev_new = getCost(prevAct.getLocation(), service.getLocation());
                double new_act = getCost(service.getLocation(), act.getLocation());
                double prev_act = getCost(prevAct.getLocation(), act.getLocation());
                return prev_new + new_act - prev_act;
            }

            private double getCost(Location loc1, Location loc2) {
                return vrp.getTransportCosts().getTransportCost(loc1, loc2, 0., null, null);
            }
        };

//        LocalActivityInsertionCostsCalculator local = new LocalActivityInsertionCostsCalculator(vrp.getTransportCosts(),vrp.getActivityCosts());
//        StateManager stateManager = new StateManager(vrp);
//        ConstraintManager manager = new ConstraintManager(vrp,stateManager);
//        ServiceInsertionCalculator calculator = new ServiceInsertionCalculator(vrp.getTransportCosts(), local, manager);
//        calculator.setJobActivityFactory(vrp.getJobActivityFactory());
//        return calculator;
    }

}
