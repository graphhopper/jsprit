package algorithms;

import java.util.HashMap;
import java.util.Map;

import basics.Job;
import basics.route.Driver;
import basics.route.Vehicle;
import basics.route.VehicleRoute;

public class JobCalculatorSwitcher implements JobInsertionCalculator{

	private Map<Class<? extends Job>,JobInsertionCalculator> calcMap = new HashMap<Class<? extends Job>, JobInsertionCalculator>();
	
	void put(Class<? extends Job> jobClass, JobInsertionCalculator jic){
		calcMap.put(jobClass, jic);
	}
	
	public InsertionData calculate(VehicleRoute currentRoute, Job jobToInsert, Vehicle newVehicle, double newVehicleDepartureTime, Driver newDriver, double bestKnownScore){
		JobInsertionCalculator jic = calcMap.get(jobToInsert.getClass());
		if(jic==null) throw new IllegalStateException("cannot find calculator for " + jobToInsert.getClass());
		return jic.calculate(currentRoute, jobToInsert, newVehicle, newVehicleDepartureTime, newDriver, bestKnownScore);
	}
	
}
