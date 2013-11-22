package jsprit.core.algorithm.recreate;

import java.util.HashMap;
import java.util.Map;

import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.vehicle.Vehicle;


class JobCalculatorSwitcher implements JobInsertionCostsCalculator{

	private Map<Class<? extends Job>,JobInsertionCostsCalculator> calcMap = new HashMap<Class<? extends Job>, JobInsertionCostsCalculator>();
	
	void put(Class<? extends Job> jobClass, JobInsertionCostsCalculator jic){
		calcMap.put(jobClass, jic);
	}
	
	public InsertionData getInsertionData(VehicleRoute currentRoute, Job jobToInsert, Vehicle newVehicle, double newVehicleDepartureTime, Driver newDriver, double bestKnownScore){
		JobInsertionCostsCalculator jic = calcMap.get(jobToInsert.getClass());
		if(jic==null) throw new IllegalStateException("cannot find calculator for " + jobToInsert.getClass());
		return jic.getInsertionData(currentRoute, jobToInsert, newVehicle, newVehicleDepartureTime, newDriver, bestKnownScore);
	}
	
}
