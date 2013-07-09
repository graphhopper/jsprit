package util;

import basics.VehicleRoutingProblem;

public class BenchmarkInstance {
	public final String name;
	public final VehicleRoutingProblem vrp;
	public final Double bestKnownResult;
	public Double bestKnownVehicles;
	public BenchmarkInstance(String name, VehicleRoutingProblem vrp, Double bestKnownResult, Double bestKnowVehicles) {
		super();
		this.name = name;
		this.vrp = vrp;
		this.bestKnownResult = bestKnownResult;
		this.bestKnownVehicles = bestKnowVehicles;
	}
}