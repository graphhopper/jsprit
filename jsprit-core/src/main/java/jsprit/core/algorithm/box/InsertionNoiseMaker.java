package jsprit.core.algorithm.box;

import jsprit.core.algorithm.listener.IterationStartsListener;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.constraint.SoftActivityConstraint;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.util.RandomNumberGeneration;

import java.util.Collection;
import java.util.Random;

/**
 * Created by schroeder on 16/01/15.
 */
class InsertionNoiseMaker implements SoftActivityConstraint, IterationStartsListener {

    private final double noiseProbability;

    private boolean makeNoise = false;

    private double noiseLevel = 0.1;

    private Random random = RandomNumberGeneration.newInstance();

//    private Random[] randomArray;

    private double maxCosts;

    InsertionNoiseMaker(VehicleRoutingProblem vrp, double maxCosts, double noiseLevel, double noiseProbability) {
        this.noiseLevel = noiseLevel;
        this.noiseProbability = noiseProbability;
        this.maxCosts = maxCosts;
//        randomArray = new Random[vrp.getNuActivities() + 2];
//        for (int i = 0; i < randomArray.length; i++) {
//            Random r = new Random();
//            r.setSeed(random.nextLong());
//            randomArray[i] = r;
//        }
    }

    @Override
    public void informIterationStarts(int i, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
        if (random.nextDouble() < noiseProbability) {
            makeNoise = true;
        } else makeNoise = false;
    }

    @Override
    public double getCosts(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
        if (makeNoise) {
            return noiseLevel * maxCosts * random.nextDouble();
        }
        return 0;
    }

    public void setRandom(Random random) {
        this.random = random;
    }
}
