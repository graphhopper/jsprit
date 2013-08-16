package algorithms;

import java.util.ArrayList;
import java.util.Collection;

import basics.Job;
import basics.algo.InsertionEndsListener;
import basics.algo.InsertionListener;
import basics.algo.InsertionStartsListener;
import basics.algo.JobInsertedListener;
import basics.route.VehicleRoute;

class InsertionListeners {
	
	private Collection<InsertionListener> listeners = new ArrayList<InsertionListener>();
	
	public void informJobInserted(int nOfJobs2Recreate, Job insertedJob, VehicleRoute insertedIn){
		for(InsertionListener l : listeners){
			if(l instanceof JobInsertedListener){
				((JobInsertedListener)l).informJobInserted(nOfJobs2Recreate, insertedJob, insertedIn);
			}
		}
	}
	
	public void informBeforeJobInsertion(Job job, InsertionData data, VehicleRoute route){
		for(InsertionListener l : listeners){
			if(l instanceof BeforeJobInsertionListener){
				((BeforeJobInsertionListener)l).informBeforeJobInsertion(job, data, route);
			}
		}
	}
	
	public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes, int nOfJobs2Recreate){
		for(InsertionListener l : listeners){
			if(l instanceof InsertionStartsListener){
				((InsertionStartsListener)l).informInsertionStarts(vehicleRoutes,nOfJobs2Recreate);
			}
		}
	}
	
	public void informInsertionEndsListeners(Collection<VehicleRoute> vehicleRoutes) {
		for(InsertionListener l : listeners){
			if(l instanceof InsertionEndsListener){
				((InsertionEndsListener)l).informInsertionEnds(vehicleRoutes);
			}
		}
	}
	
	public void addListener(InsertionListener insertionListener){
		listeners.add(insertionListener);
	}
	
	public void removeListener(InsertionListener insertionListener){
		listeners.remove(insertionListener);
	}

}
