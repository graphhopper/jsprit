package algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import algorithms.RuinStrategy.RuinListener;
import basics.Job;
import basics.route.VehicleRoute;

class RuinListeners {
	
	private Collection<RuinListener> ruinListeners = new ArrayList<RuinListener>();

	void ruinStarts(Collection<VehicleRoute> routes){
		for(RuinListener l : ruinListeners) l.ruinStarts(routes);
	}
	
	void ruinEnds(Collection<VehicleRoute> routes, Collection<Job> unassignedJobs){
		for(RuinListener l : ruinListeners) l.ruinEnds(routes, unassignedJobs);
	}
	
	void removed(Job job, VehicleRoute fromRoute){
		for(RuinListener l : ruinListeners) l.removed(job, fromRoute);
	}
	
	void addListener(RuinListener ruinListener){
		ruinListeners.add(ruinListener);
	}
	
	void removeListener(RuinListener ruinListener){
		ruinListeners.remove(ruinListener);
	}
	
	Collection<RuinListener> getListeners(){
		return Collections.unmodifiableCollection(ruinListeners);
	}
}
