package algorithms;

import basics.Job;
import basics.route.Driver;
import basics.route.Vehicle;
import basics.route.VehicleRoute;

class InsertionContext {
	
	private VehicleRoute route;
	private Job job;
	private Vehicle newVehicle;
	private Driver newDriver;
	private double newDepTime;
	
	/**
	 * @return the route
	 */
	public VehicleRoute getRoute() {
		return route;
	}

	/**
	 * @return the job
	 */
	public Job getJob() {
		return job;
	}

	/**
	 * @return the newVehicle
	 */
	public Vehicle getNewVehicle() {
		return newVehicle;
	}

	/**
	 * @return the newDriver
	 */
	public Driver getNewDriver() {
		return newDriver;
	}

	/**
	 * @return the newDepTime
	 */
	public double getNewDepTime() {
		return newDepTime;
	}

	public InsertionContext(VehicleRoute route, Job job, Vehicle newVehicle,
			Driver newDriver, double newDepTime) {
		super();
		this.route = route;
		this.job = job;
		this.newVehicle = newVehicle;
		this.newDriver = newDriver;
		this.newDepTime = newDepTime;
	}
	
	

}
