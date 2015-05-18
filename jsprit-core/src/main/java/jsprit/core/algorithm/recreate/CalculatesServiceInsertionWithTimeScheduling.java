package jsprit.core.algorithm.recreate;

import jsprit.core.algorithm.recreate.listener.InsertionStartsListener;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.util.RandomNumberGeneration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;


class CalculatesServiceInsertionWithTimeScheduling implements JobInsertionCostsCalculator{


    public static class KnowledgeInjection implements InsertionStartsListener {
        private CalculatesServiceInsertionWithTimeScheduling c;
        public KnowledgeInjection(CalculatesServiceInsertionWithTimeScheduling c) {
            super();
            this.c = c;
        }
        @Override
        public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes,Collection<Job> unassignedJobs) {
            List<Double> knowledge = new ArrayList<Double>();
            if(vehicleRoutes.isEmpty()){
//                System.out.println("hmm");
            }
            for(VehicleRoute route : vehicleRoutes){
//                if(route.getDepartureTime() == 21600.){
//                    System.out.println("hu");
//                }
                knowledge.add(route.getDepartureTime());
            }
            c.setDepartureTimeKnowledge(knowledge);
        }
    }

    private static Logger log = LogManager.getLogger(CalculatesServiceInsertionWithTimeScheduling.class);

    private JobInsertionCostsCalculator jic;

    private List<Double> departureTimeKnowledge = new ArrayList<Double>();

    public void setRandom(Random random) {
        this.random = random;
    }

    private Random random = RandomNumberGeneration.getRandom();

    CalculatesServiceInsertionWithTimeScheduling(JobInsertionCostsCalculator jic, double t, double f) {
        super();
        this.jic = jic;
        log.debug("initialise " + this);
    }

    @Override
    public String toString() {
        return "[name="+this.getClass().toString()+"]";
    }

    @Override
    public InsertionData getInsertionData(VehicleRoute currentRoute, Job jobToInsert, Vehicle newVehicle, double newVehicleDepartureTime, Driver newDriver, double bestKnownScore) {
        double departureTime = newVehicleDepartureTime;
        if(currentRoute.isEmpty()){
            if(!departureTimeKnowledge.isEmpty()){
                departureTime = departureTimeKnowledge.get(random.nextInt(departureTimeKnowledge.size()));
            }
        }
        else if(!currentRoute.getVehicle().getId().equals(newVehicle.getId())){
            departureTime = currentRoute.getDepartureTime();
        }

        InsertionData insertionData = jic.getInsertionData(currentRoute, jobToInsert, newVehicle, departureTime, newDriver, bestKnownScore);
//        if(!(insertionData instanceof NoInsertionFound) && insertionData.getVehicleDepartureTime() < 28000){
//            System.out.println("hmm");
//        }
        return insertionData;
    }

    public void setDepartureTimeKnowledge(List<Double> departureTimes){
        departureTimeKnowledge=departureTimes;
    }
}
