package jsprit.core.algorithm.box;

import jsprit.core.algorithm.listener.IterationStartsListener;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.constraint.SoftActivityConstraint;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.util.RandomNumberGeneration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
* Created by schroeder on 16/01/15.
*/
class InsertionNoiseMaker implements SoftActivityConstraint, IterationStartsListener {

    private final double noiseProbability;

    private boolean makeNoise = false;

    private VehicleRoutingProblem vrp;

    double maxCosts = 0.;

    private double noiseLevel = 0.1;

    private Random random = RandomNumberGeneration.getRandom();

    public InsertionNoiseMaker(VehicleRoutingProblem vrp, double noiseLevel, double noiseProbability) {
        this.vrp = vrp;
        this.noiseLevel = noiseLevel;
        this.noiseProbability = noiseProbability;
        determineMaxCosts(vrp);
    }

    //@ToDo refactor determining max costs to allow skipping this
    private void determineMaxCosts(VehicleRoutingProblem vrp) {
        double max = 0.;
        for(Job i : vrp.getJobs().values()){
            List<Location> fromLocations = getLocations(i);
            for(Job j : vrp.getJobs().values()){
                List<Location> toLocations = getLocations(j);
                for(Location iLoc : fromLocations){
                    for(Location jLoc : toLocations) {
                        max = Math.max(max, vrp.getTransportCosts().getTransportCost(iLoc, jLoc, 0, null, vrp.getVehicles().iterator().next()));
                    }
                }
            }
        }
        maxCosts = max;
    }

    private List<Location> getLocations(Job j) {
        List<Location> locs = new ArrayList<Location>();
        if(j instanceof Service) {
            locs.add(((Service) j).getLocation());
        }
        else if(j instanceof Shipment){
            locs.add(((Shipment) j).getPickupLocation());
            locs.add(((Shipment) j).getDeliveryLocation());
        }
        return locs;
    }

    @Override
    public void informIterationStarts(int i, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
        if(random.nextDouble() < noiseProbability){
            makeNoise = true;
        }
        else makeNoise = false;
    }

    @Override
    public double getCosts(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
        if(makeNoise) {
            return noiseLevel * maxCosts * random.nextDouble();
        }
        return 0;
    }


    public void setRandom(Random random) {
        this.random = random;
    }
}
