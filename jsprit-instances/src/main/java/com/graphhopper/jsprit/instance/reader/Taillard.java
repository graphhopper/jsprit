/*
 * Licensed to GraphHopper GmbH under one or more contributor
 * license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * GraphHopper GmbH licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.graphhopper.jsprit.instance.reader;
//package instances;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//
//import org.apache.log4j.Level;
//import org.apache.log4j.Logger;
//import org.matsim.contrib.freight.vrp.algorithms.rr.costCalculators.JobInsertionCalculator;
//import org.matsim.contrib.freight.vrp.algorithms.rr.costCalculators.RouteAgentFactory;
//import org.matsim.contrib.freight.vrp.algorithms.rr.listener.RuinAndRecreateReport;
//import org.matsim.contrib.freight.vrp.algorithms.rr.ruin.JobDistanceAvgCosts;
//import org.matsim.contrib.freight.vrp.algorithms.rr.ruin.RuinRadial;
//import org.matsim.contrib.freight.vrp.basics.Driver;
//import org.matsim.contrib.freight.vrp.basics.Job;
//import org.matsim.contrib.freight.vrp.basics.RouteAlgorithm;
//import org.matsim.contrib.freight.vrp.basics.RouteAlgorithm.VehicleSwitchedListener;
//import org.matsim.contrib.freight.vrp.basics.Service;
//import org.matsim.contrib.freight.vrp.basics.Tour;
//import org.matsim.contrib.freight.vrp.basics.TourActivity;
//import org.matsim.contrib.freight.vrp.basics.TourStateUpdater;
//import org.matsim.contrib.freight.vrp.basics.Vehicle;
//import org.matsim.contrib.freight.vrp.basics.VehicleFleetManager;
//import org.matsim.contrib.freight.vrp.basics.VehicleFleetManagerImpl;
//import org.matsim.contrib.freight.vrp.basics.VehicleImpl;
//import org.matsim.contrib.freight.vrp.basics.VehicleImpl.Type;
//import org.matsim.contrib.freight.vrp.basics.VehicleImpl.VehicleCostParams;
//import org.matsim.contrib.freight.vrp.basics.VehicleRoute;
//import org.matsim.contrib.freight.vrp.basics.VehicleRoute.VehicleRouteCostCalculator;
//import org.matsim.contrib.freight.vrp.basics.VehicleRouteCostFunction;
//import org.matsim.contrib.freight.vrp.basics.VehicleRouteCostFunctionFactory;
//import org.matsim.contrib.freight.vrp.basics.VehicleRoutingCosts;
//import org.matsim.contrib.freight.vrp.basics.VehicleRoutingProblem;
//import org.matsim.contrib.freight.vrp.basics.VehicleRoutingProblemSolution;
//import org.matsim.contrib.freight.vrp.basics.VrpBuilder;
//import org.matsim.contrib.freight.vrp.utils.Coordinate;
//import org.matsim.contrib.freight.vrp.utils.EuclideanDistanceCalculator;
//import org.matsim.contrib.freight.vrp.utils.Locations;
//import org.matsim.contrib.freight.vrp.utils.RouteUtils;
//import org.matsim.core.utils.io.IOUtils;
//
//import ruinFactories.RadialRuinFactory;
//import selectors.SelectBest;
//import selectors.SelectRandomly;
//import strategies.GendreauPostOpt;
//import strategies.RadialAndRandomRemoveBestInsert;
//import vrp.SearchStrategy;
//import vrp.SearchStrategyManager;
//import vrp.SearchStrategyModule;
//import vrp.VehicleRoutingMetaAlgorithm;
//import acceptors.AcceptNewRemoveWorst;
//import basics.VehicleRouteFactoryImpl;
//import basics.costcalculators.AuxilliaryCostCalculator;
//import basics.costcalculators.CalculatesActivityInsertion;
//import basics.costcalculators.CalculatesServiceInsertionConsideringFixCost;
//import basics.costcalculators.CalculatesServiceInsertionOnRouteLevel;
//import basics.costcalculators.CalculatesVehTypeDepServiceInsertion;
//import basics.inisolution.CreateInitialSolution;
//import basics.insertion.ConfigureFixCostCalculator;
//import basics.insertion.DepotDistance;
//import basics.insertion.ParRegretInsertion;
//import basics.insertion.RecreationBestInsertion;
//
///**
// * test instances for the capacitated vrp with time windows. instances are from solomon
// * and can be found at:
// * http://neo.lcc.uma.es/radi-aeb/WebVRP/
// * @author stefan schroeder
// *
// */
//
//
//
//public class Taillard {
//
//
//	static class MyLocations implements Locations{
//
//		private Map<String,Coordinate> locations = new HashMap<String, Coordinate>();
//
//		public void addLocation(String id, Coordinate coord){
//			locations.put(id, coord);
//		}
//
//		@Override
//		public Coordinate getCoord(String id) {
//			return locations.get(id);
//		}
//	}
//
//	public static final String VRPHE = "vrphe";
//
//	public static final String VFM = "vfm";
//
//	private static Logger logger = Logger.getLogger(Christophides.class);
//
//	private String fileNameOfInstance;
//
//	private String depotId;
//
//	private String instanceName;
//
//	private String vehicleFile;
//
//	private String vehicleCostScenario;
//
//	private String vrpType;
//
//	private ResultWriter resultWriter;
//
//	private RuinAndRecreateReport report;
//
//
//	public Taillard(String fileNameOfInstance, String instanceName, String vehicleFileName, String vehicleCostScenario, String vrpType) {
//		super();
//		this.fileNameOfInstance = fileNameOfInstance;
//		this.instanceName = instanceName;
//		this.vehicleFile = vehicleFileName;
//		this.vehicleCostScenario = vehicleCostScenario;
//		this.vrpType = vrpType;
//	}
//
//	public static void main(String[] args) throws IOException {
//		System.out.println("start " + System.currentTimeMillis());
//		Logger.getRootLogger().setLevel(Level.INFO);
//
//		int nOfProcessors = Runtime.getRuntime().availableProcessors();
//		logger.info("nOfProcessors: " + nOfProcessors);
//		ExecutorService executor = Executors.newFixedThreadPool(nOfProcessors+2);
//
//		ResultWriter resultWriter = new ResultWriter();
////		String vrpType = "VFM";
//		String vrpType = VRPHE;
//		String pblm_abbr = "R101";
//		String costScen = "R_19";
//
//		String problem = "100_" + pblm_abbr + "_LuiShen";
//		String problemFile = pblm_abbr + ".txt";
//		Taillard luiShen = new Taillard("/Users/stefan/Documents/Schroeder/Dissertation/vrpInstances/cvrptw_solomon/nOfCust100/"+problemFile,
//				problem, "/Users/stefan/Documents/Schroeder/Dissertation/vrpInstances/vrphe_taillard/"+costScen+".txt", costScen, vrpType);
//		luiShen.setResultWriter(resultWriter);
//		luiShen.run(executor);
//
//		System.out.println("finish " + System.currentTimeMillis());
//		resultWriter.write("output/taillard_"+ pblm_abbr + "_" + costScen + ".txt");
//		resultWriter.writeSolutions("output/taillard_solution_" + pblm_abbr + "_" + costScen + ".txt");
//
//		executor.shutdown();
//		try {
//			executor.awaitTermination(10, TimeUnit.SECONDS);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//	private static String getName(int i) {
//		if(i<10){
//			return "0" + i;
//		}
//		else{
//			return "" + i;
//		}
//	}
//
//	private void setResultWriter(ResultWriter resultWriter) {
//		this.resultWriter = resultWriter;
//
//	}
//
//	public void run(ExecutorService executor){
//
//		final MyLocations myLocations = new MyLocations();
//		Collection<Job> jobs = new ArrayList<Job>();
//		final Map<String,Service> jobMap = readLocationsAndJobs(myLocations,jobs);
//
//		VehicleRoutingCosts costs = new VehicleRoutingCosts() {
//
//			@Override
//			public double getBackwardTransportTime(String fromId, String toId, double arrivalTime, Driver driver, Vehicle vehicle) {
//				return getTransportTime(fromId, toId, arrivalTime, null, null);
//			}
//
//			@Override
//			public double getBackwardTransportCost(String fromId, String toId, double arrivalTime, Driver driver, Vehicle vehicle) {
//				return getTransportCost(fromId, toId, arrivalTime, null, null);
//			}
//
//			@Override
//			public double getTransportCost(String fromId, String toId, double departureTime, Driver driver, Vehicle vehicle) {
//				double variableCost;
//				if(vehicle == null){
//					variableCost = 1.0;
//				}
//				else{
//					variableCost = vehicle.getType().vehicleCostParams.perDistanceUnit;
//				}
//				return variableCost*EuclideanDistanceCalculator.calculateDistance(myLocations.getCoord(fromId), myLocations.getCoord(toId));
//			}
//
//			@Override
//			public double getTransportTime(String fromId, String toId, double departureTime, Driver driver, Vehicle vehicle) {
//				return getTransportCost(fromId, toId, departureTime, driver, vehicle);
//			}
//		};
//
//		VrpBuilder vrpBuilder = new VrpBuilder(costs);
//		for(Job j : jobs){
//			vrpBuilder.addJob(j);
//		}
//		createVehicles(vrpBuilder);
//		VehicleRoutingProblem vrp = vrpBuilder.build();
//
//		VehicleRoutingMetaAlgorithm metaAlgorithm = new VehicleRoutingMetaAlgorithm(vrp);
//		configure(metaAlgorithm,vrp,executor,myLocations);
//		metaAlgorithm.run();
//
//		printSolutions(vrp);
//
//		VehicleRoutingProblemSolution bestSolution = new SelectBest().selectSolution(vrp);
//
//		resultWriter.addResult(instanceName+"_"+vehicleCostScenario , instanceName, RouteUtils.getNuOfActiveRoutes(bestSolution.getRoutes()), bestSolution.getCost());
//		resultWriter.addSolution(bestSolution);
//
//
//	}
//
//	private void printSolutions(VehicleRoutingProblem vrp) {
//		for(VehicleRoutingProblemSolution s : vrp.getSolutions()){
//			System.out.println("total: " + s.getCost());
//			System.out.println("activeTours: " + RouteUtils.getNuOfActiveRoutes(s.getRoutes()));
//			System.out.println("");
//		}
//
//	}
//
//	private void configure(VehicleRoutingMetaAlgorithm metaAlgorithm, final VehicleRoutingProblem vrp, ExecutorService executor, MyLocations myLocations) {
//		VehicleRoute.VehicleRouteCostCalculator = new VehicleRouteCostCalculator() {
//
//			@Override
//			public double calculate(Tour tour, Vehicle vehicle, Driver driver) {
////				return vehicle.getType().vehicleCostParams.fix + tour.getCost();
//				return tour.getCost();
//			}
//
//		};
//
////		final VehicleFleetManager vehicleFleetManager = new InfiniteVehicles(vrp.getVehicles());
//		final VehicleFleetManager vehicleFleetManager = new VehicleFleetManagerImpl(vrp.getVehicles());
//
//		final VehicleRouteCostFunctionFactory costFuncFac = getFac();
//
//		AuxilliaryCostCalculator auxilliaryCostCalculator = new AuxilliaryCostCalculator(vrp.getCosts(), costFuncFac);
//		CalculatesActivityInsertion actInsertion = new CalculatesActivityInsertion(auxilliaryCostCalculator,0,5);
////		CalculatesServiceInsertion standardServiceInsertion = new CalculatesServiceInsertion(actInsertion);
//		CalculatesServiceInsertionOnRouteLevel standardServiceInsertion = new CalculatesServiceInsertionOnRouteLevel(actInsertion,vrp.getCosts(),costFuncFac);
//		CalculatesServiceInsertionOnRouteLevel.MEMORYSIZE_FORPROMISING_INSERTIONPOSITIONS = 2;
//		CalculatesServiceInsertionConsideringFixCost withFixCost = new CalculatesServiceInsertionConsideringFixCost(standardServiceInsertion);
//		withFixCost.setWeightOfFixCost(0.0);
//
//		final JobInsertionCalculator vehicleTypeDepInsertionCost = new CalculatesVehTypeDepServiceInsertion(vehicleFleetManager, standardServiceInsertion);
//
//		final TourStateUpdater tourStateCalculator = new TourStateUpdater(vrp.getCosts(),costFuncFac);
//		tourStateCalculator.setTimeWindowUpdate(false);
//
//		RouteAgentFactory routeAgentFactory = new RouteAgentFactory(){
//
//			@Override
//			public RouteAlgorithm createAgent(VehicleRoute route) {
//				VehicleSwitchedListener switched = new VehicleSwitchedListener() {
//
//					@Override
//					public void vehicleSwitched(Vehicle oldVehicle, Vehicle newVehicle) {
//						vehicleFleetManager.unlock(oldVehicle);
//						vehicleFleetManager.lock(newVehicle);
//					}
//
//				};
//				RouteAlgorithmImpl agent = new RouteAlgorithmImpl(vehicleTypeDepInsertionCost, tourStateCalculator);
//				agent.getListeners().add(switched);
//				return agent;
//			}
//
//		};
//
//		ParRegretInsertion regretInsertion = new ParRegretInsertion(executor, routeAgentFactory);
//		regretInsertion.getListener().add(new ConfigureFixCostCalculator(vrp, withFixCost));
//		regretInsertion.setJobDistance(new DepotDistance(myLocations, depotId));
//		regretInsertion.scoreParam_of_timeWindowLegth = 0.5;
//		regretInsertion.scoreParam_of_distance = 0.2;
//		regretInsertion.setVehicleRouteFactory(new VehicleRouteFactoryImpl(depotId));
//
//
//		RecreationBestInsertion bestInsertion = new RecreationBestInsertion(routeAgentFactory);
//		bestInsertion.getListener().add(new ConfigureFixCostCalculator(vrp, withFixCost));
//		bestInsertion.setVehicleRouteFactory(new VehicleRouteFactoryImpl(depotId));
//
////		for(int i=0;i<3;i++){
//		VehicleRoutingProblemSolution vrpSol = new CreateInitialSolution(bestInsertion).createInitialSolution(vrp);
//		vrp.getSolutions().add(vrpSol);
////		}
//
//
//		RadialAndRandomRemoveBestInsert smallNeighborHoodSearchModule = new RadialAndRandomRemoveBestInsert(vrp, vehicleFleetManager, routeAlgorithm);
//		smallNeighborHoodSearchModule.setCalcConsideringFix(withFixCost);
//		smallNeighborHoodSearchModule.setStateCalc(tourStateCalculator);
//		smallNeighborHoodSearchModule.setInsertionStrategy(bestInsertion);
//		smallNeighborHoodSearchModule.setAuxilliaryCostCalculator(auxilliaryCostCalculator);
//
//		SearchStrategy smallNeighborHoodSearch = new SearchStrategy(new SelectRandomly(), new AcceptNewRemoveWorst());
//
//		smallNeighborHoodSearch.addModule(smallNeighborHoodSearchModule);
//
//		GendreauPostOpt postOpt = new GendreauPostOpt(vrp, routeAgentFactory,
//				(RuinRadial) new RadialRuinFactory(0.2, new JobDistanceAvgCosts(vrp.getCosts()), routeAgentFactory).createStrategy(vrp),
//				bestInsertion);
//		postOpt.setFleetManager(vehicleFleetManager);
//		postOpt.setVehicleRouteFactory(new VehicleRouteFactoryImpl(depotId));
//		postOpt.setMaxIterations(2000);
//		postOpt.setShareOfJobsToRuin(0.18);
////		smallNeighborHoodSearch.addModule(postOpt);
//
//
////		SearchStrategy strat2 = new SearchStrategy(new SelectBest(), new AcceptNewRemoveWorst());
////		GendreauPostOpt postOpt2 = new GendreauPostOpt(vrp, routeAgentFactory,
////				(RuinRadial) new RadialRuinFactory(0.2, new JobDistanceAvgCosts(vrp.getCosts()), routeAgentFactory).createStrategy(vrp),
////				bestInsertion);
////		postOpt2.setFleetManager(vehicleFleetManager);
////		postOpt2.setVehicleRouteFactory(new VehicleRouteFactoryImpl(depotId));
////		postOpt2.setMaxIterations(2000);
////		postOpt2.setShareOfJobsToRuin(0.1);
////		strat2.addModule(postOpt2);
//
//		SearchStrategyModule solutionVerifier = new SearchStrategyModule() {
//
//			@Override
//			public VehicleRoutingProblemSolution runAndGetSolution(VehicleRoutingProblemSolution vrpSolution) {
//				logger.info("verify solution");
//				if(SolutionVerifier.resultOfSolutionEqualsSumOfIndividualRouteCost(vrpSolution, vrp, costFuncFac,false)) return vrpSolution;
//				throw new IllegalStateException("solution is not valid");
//			}
//		};
//		smallNeighborHoodSearch.addModule(solutionVerifier);
//
//		SearchStrategyManager strategyManager = new SearchStrategyManager();
//		strategyManager.addStrategy(smallNeighborHoodSearch, 1.0);
////		strategyManager.addStrategy(strat2, 0.3);
//
//		metaAlgorithm.setSearchStrategyManager(strategyManager);
//		metaAlgorithm.setMaxIterations(20);
//		VehicleRoutingProblem.SOLUTION_MEMORY = 4;
//
//
//	}
//
//	private VehicleRouteCostFunctionFactory getFac() {
//		VehicleRouteCostFunctionFactory fac = new VehicleRouteCostFunctionFactory() {
//
//			@Override
//			public VehicleRouteCostFunction createCostFunction(Vehicle vehicle, Driver driver) {
//				return new VehicleRouteCostFunction(){
//
//					double cost = 0.0;
//
//					@Override
//					public void handleActivity(TourActivity tourAct, double startTime, double endTime) {
//						if(startTime > tourAct.getLatestOperationStartTime()){
//							cost += Double.MAX_VALUE;
//						}
//					}
//
//					@Override
//					public void handleLeg(TourActivity fromAct, TourActivity toAct, double depTime, double tpCost) {
//						cost += tpCost;
//
//					}
//
//					@Override
//					public double getCost() {
//						return cost;
//					}
//
//					@Override
//					public void finish() {
//
//
//					}
//
//					@Override
//					public void reset() {
//
//					}
//
//				};
//			}
//		};
//		return fac;
//	}
//
//	private void createVehicles(VrpBuilder vrpBuilder) {
//		BufferedReader reader = IOUtils.getBufferedReader(vehicleFile);
//		String line = null;
//		int vehicleIdColumn = 0;
//		int capacityColumn = 1;
//		int fixColumn = 2;
//		int varColumn = 3;
//		int nOfVehiclesColumn = 4;
//		boolean firstLine = true;
//		try {
//			while((line = reader.readLine()) != null){
//				if(firstLine){
//					firstLine = false;
//					continue;
//				}
//				String[] tokens = line.split(";");
//				String vehicleId = tokens[vehicleIdColumn];
//				int capacity = Integer.parseInt(tokens[capacityColumn]);
//				int fixCost = Integer.parseInt(tokens[fixColumn]);
//				double var;
//				if(vrpType.equals(VRPHE)){
//					var = Double.parseDouble(tokens[varColumn]);
//				}
//				else {
//					var = 1.0;
//				}
//				int nOfVehicles = Integer.parseInt(tokens[nOfVehiclesColumn]);
//				for(int i=0;i<nOfVehicles;i++){
//					String vId = vehicleId + "_" + (i+1);
//					VehicleCostParams costparams = VehicleImpl.getFactory().createVehicleCostParams(fixCost, 0.0, var);
//					Type type = VehicleImpl.getFactory().createType(vehicleId, capacity, costparams);
//					VehicleImpl v = VehicleImpl.getFactory().createVehicle(vId, depotId, type);
//					vrpBuilder.addVehicle(v);
//				}
//			}
//			reader.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//	private Map<String,Service> readLocationsAndJobs(MyLocations locations, Collection<Job> jobs){
//		BufferedReader reader = IOUtils.getBufferedReader(fileNameOfInstance);
//		String line = null;
//		int counter = 0;
//		Map<String,Service> jobMap = new HashMap<String, Service>();
//		try {
//			while((line = reader.readLine()) != null){
//				line = line.replace("\r", "");
//				line = line.trim();
//				String[] tokens = line.split(" +");
//				counter++;
//				if(counter == 5){
//					int vehicleCap = Integer.parseInt(tokens[1]);
//					continue;
//				}
//
//				if(counter > 9){
//					Coordinate coord = makeCoord(tokens[1],tokens[2]);
//					double depotStart = 0.0;
//					double depotEnd = Double.MAX_VALUE;
//					String customerId = tokens[0];
//					locations.addLocation(customerId, coord);
//					int demand = Integer.parseInt(tokens[3]);
//					double start = Double.parseDouble(tokens[4]);
//					double end = Double.parseDouble(tokens[5]);
//					double serviceTime = Double.parseDouble(tokens[6]);
//					if(counter == 10){
//						depotStart = start;
//						depotEnd = end;
//						depotId = tokens[0];
//
//					}
//					else{
//						Service service = VrpUtils.createService("" + counter, customerId, demand, 0.0, 0.0, Double.MAX_VALUE);
////						Shipment shipment = VrpUtils.createShipment("" + counter, depotId, customerId, demand,
////								VrpUtils.createTimeWindow(depotStart, depotEnd), VrpUtils.createTimeWindow(start, end));
////						shipment.setDeliveryServiceTime(serviceTime);
//						jobs.add(service);
//						jobMap.put(customerId, service);
////						jobs.add(shipment);
////						j
////						Shipment shipment = VrpUtils.createShipment("" + counter, depotId, customerId, demand,
////								VrpUtils.createTimeWindow(depotStart, depotEnd), VrpUtils.createTimeWindow(start, end));
////						shipment.setDeliveryServiceTime(serviceTime);
////						jobs.add(shipment);
//					}
//				}
//			}
//			reader.close();
//		} catch (NumberFormatException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return jobMap;
//	}
//
//	private Coordinate makeCoord(String xString, String yString) {
//		double x = Double.parseDouble(xString);
//		double y = Double.parseDouble(yString);
//		return new Coordinate(x,y);
//	}
//
//}
