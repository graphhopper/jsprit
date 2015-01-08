package jsprit.core.algorithm.ruin;

import jsprit.core.algorithm.ruin.distance.JobDistance;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Job;
import jsprit.core.util.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
* Created by schroeder on 07/01/15.
*/
class JobNeighborhoodsImplWithCapRestriction implements JobNeighborhoods {

    private static Logger logger = LogManager.getLogger(JobNeighborhoodsImpl.class);

    private VehicleRoutingProblem vrp;

    private Map<String, TreeSet<ReferencedJob>> distanceNodeTree = new HashMap<String, TreeSet<ReferencedJob>>();

    private JobDistance jobDistance;

    private int capacity;

    public JobNeighborhoodsImplWithCapRestriction(VehicleRoutingProblem vrp, JobDistance jobDistance, int capacity) {
        super();
        this.vrp = vrp;
        this.jobDistance = jobDistance;
        this.capacity = capacity;
        logger.info("intialise " + this);
    }

    public Iterator<Job> getNearestNeighborsIterator(int nNeighbors, Job neighborTo){
        TreeSet<ReferencedJob> tree = distanceNodeTree.get(neighborTo.getId());
        Iterator<ReferencedJob> descendingIterator = tree.iterator();
        return new NearestNeighborhoodIterator(descendingIterator, nNeighbors);
    }

    public void initialise(){
        logger.info("calculates distances from EACH job to EACH job --> n^2="+Math.pow(vrp.getJobs().values().size(), 2) + " calculations, but 'only' "+(vrp.getJobs().values().size()*capacity)+ " are cached.");
        if(capacity==0) return;
        calculateDistancesFromJob2Job();
    }

    private void calculateDistancesFromJob2Job() {
        logger.info("preprocess distances between locations ...");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        int nuOfDistancesStored = 0;
        for (Job i : vrp.getJobs().values()) {
            TreeSet<ReferencedJob> treeSet = new TreeSet<ReferencedJob>(
                    new Comparator<ReferencedJob>() {
                        @Override
                        public int compare(ReferencedJob o1, ReferencedJob o2) {
                            if (o1.getDistance() <= o2.getDistance()) {
                                return -1;
                            } else {
                                return 1;
                            }
                        }
                    });
            distanceNodeTree.put(i.getId(), treeSet);
            for (Job j : vrp.getJobs().values()) {
                if(i==j) continue;
                double distance = jobDistance.getDistance(i, j);
                ReferencedJob refNode = new ReferencedJob(j, distance);
                if(treeSet.size() < capacity){
                    treeSet.add(refNode);
                    nuOfDistancesStored++;
                }
                else{
                    if(treeSet.last().getDistance() > distance){
                        treeSet.pollLast();
                        treeSet.add(refNode);
                    }
                }
            }
            assert treeSet.size() <= capacity : "treeSet.size() is bigger than specified capacity";

        }
        stopWatch.stop();
        logger.info("preprocessing comp-time: " + stopWatch + "; nuOfDistances stored: " + nuOfDistancesStored + "; estimated memory: " +
                (distanceNodeTree.keySet().size()*64+nuOfDistancesStored*92) + " bytes");
    }

    @Override
    public String toString() {
        return "[name=neighborhoodWithCapRestriction][capacity="+capacity+"]";
    }

}
