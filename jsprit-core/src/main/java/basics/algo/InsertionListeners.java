package basics.algo;

import java.util.ArrayList;
import java.util.Collection;

import basics.Job;
import basics.route.VehicleRoute;

public class InsertionListeners {
	
	private Collection<InsertionStartsListener> startListeners = new ArrayList<InsertionStartsListener>();
	
	private Collection<JobInsertedListener> jobInsertedListeners = new ArrayList<JobInsertedListener>();
	
	private Collection<InsertionEndsListener> endListeners = new ArrayList<InsertionEndsListener>();
	
	public void addListener(InsertionListener insertionListener){
		if(insertionListener instanceof InsertionStartsListener) startListeners.add((InsertionStartsListener) insertionListener);
		if(insertionListener instanceof JobInsertedListener) jobInsertedListeners.add((JobInsertedListener) insertionListener);
		if(insertionListener instanceof InsertionEndsListener) endListeners.add((InsertionEndsListener) insertionListener);
//		else throw new IllegalStateException("cannot add this type of insertionListener");
	}
	
	public void removeListener(InsertionListener insertionListener){
		if(insertionListener instanceof InsertionStartsListener) startListeners.remove((InsertionStartsListener) insertionListener);
		if(insertionListener instanceof JobInsertedListener) jobInsertedListeners.remove((JobInsertedListener) insertionListener);
		if(insertionListener instanceof InsertionEndsListener) endListeners.remove((InsertionEndsListener) insertionListener);
//		else throw new IllegalStateException("cannot remove this type of insertionListener");
	}
	
	public void insertionStarts(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs){
		for(InsertionStartsListener l : startListeners) l.informInsertionStarts(vehicleRoutes, unassignedJobs);
	}
	
	public void jobInserted(Job job2insert, VehicleRoute inRoute, double additionalCosts, double additionalTime){
		for(JobInsertedListener l : jobInsertedListeners) l.informJobInserted(job2insert, inRoute, additionalCosts, additionalTime);
	}

	public void insertionEnds(Collection<VehicleRoute> vehicleRoutes){
		for(InsertionEndsListener l : endListeners){ l.informInsertionEnds(vehicleRoutes); }
	}
}
