package algorithms;

import java.util.Collection;

import basics.Job;
import basics.route.VehicleRoute;

abstract class AbstractRuinStrategy implements RuinStrategy{
	
	public void ruinStarts(Collection<VehicleRoute> routes){
		
	}
	
	public void ruinEnds(Collection<VehicleRoute> routes){
		
	}
	
	public void jobRemoved(Job job, VehicleRoute fromRoute){
		
	}

}
