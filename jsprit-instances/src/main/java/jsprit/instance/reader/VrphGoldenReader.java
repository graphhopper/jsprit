package jsprit.instance.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.VehicleRoutingProblem.Builder;
import jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import jsprit.core.problem.io.VrpXMLWriter;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.util.Coordinate;

/**
 * Reads modified files from Taillard's website 
 * http://mistic.heig-vd.ch/taillard/problemes.dir/vrp.dir/vrp.html. You can find the modified version here: 
 * jsprit-instances/instances/vrph.
 * 
 * <p>See {@link VrphType} what kind of problems can be generated
 * 
 * @author schroeder
 *
 */
public class VrphGoldenReader {
	
	/**
	 *
	 * <b>FSMD</b> - Fleet Size and Mix with Dependent costs
	 * <p><b>FSMF</b> - Fleet Size and Mix with Fixed costs
	 * <p><b>FSMFD</b> - Fleet Size and Mix with Fixed and Dependent costs
	 * <p><b>HVRPD</b> - Heterogeneous Vehicle Routing Problem with Dependent costs and finite (limited) fleet
	 * <p><b>HVRPFD</b> - Heterogeneous Vehicle Routing Problem with Fixed and Dependent costs and finite (limited) fleet
	 * 
	 * @author schroeder
	 *
	 */
	public enum VrphType {
		FSMD, 
		HVRPD, 
		FSMF, 
		FSMFD, 
		HVRPFD
	}
	
	private final VehicleRoutingProblem.Builder vrpBuilder;
	
	private final VrphType vrphType;
	
	public VrphGoldenReader(Builder vrpBuilder, VrphType vrphType) {
		super();
		this.vrpBuilder = vrpBuilder;
		this.vrphType = vrphType;
	}

	public void read(String filename){
		BufferedReader reader = getReader(filename);
		String line = null;
		boolean firstline = true;
		Coordinate depotCoord = null;
		int customerCount=0;
		Integer nuOfCustomer = 0;
		while((line=readLine(reader))!=null){
			String trimedLine = line.trim();
			if(trimedLine.startsWith("//")) continue;
			String[] tokens = trimedLine.split("\\s+");
			if(firstline){
				nuOfCustomer=Integer.parseInt(tokens[0]);
				customerCount=0;
				firstline=false;
			}
			else if(customerCount<=nuOfCustomer) {
				if(customerCount == 0){
					depotCoord = Coordinate.newInstance(Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2]));
				}
				else{
					Service.Builder serviceBuilder = Service.Builder.newInstance(tokens[0]).addSizeDimension(0, Integer.parseInt(tokens[3]));
					serviceBuilder.setCoord(Coordinate.newInstance(Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2])));
					vrpBuilder.addJob(serviceBuilder.build());
				}
				customerCount++;
			}
			else if(trimedLine.startsWith("v")){
				VehicleTypeImpl.Builder typeBuilder = VehicleTypeImpl.Builder.newInstance("type_"+tokens[1]).addCapacityDimension(0, Integer.parseInt(tokens[2]));
				int nuOfVehicles = 1;
				if(vrphType.equals(VrphType.FSMF)){
					typeBuilder.setFixedCost(Double.parseDouble(tokens[3]));
				}
				else if(vrphType.equals(VrphType.FSMFD)){
					typeBuilder.setFixedCost(Double.parseDouble(tokens[3]));
					if(tokens.length > 4){
						typeBuilder.setCostPerDistance(Double.parseDouble(tokens[4]));
					}
					else throw new IllegalStateException("option " + vrphType + " cannot be applied with this instance");
				}
				else if(vrphType.equals(VrphType.FSMD)){
					if(tokens.length > 4){
						typeBuilder.setCostPerDistance(Double.parseDouble(tokens[4]));
					}
					else throw new IllegalStateException("option " + vrphType + " cannot be applied with this instance");
				}
				else if(vrphType.equals(VrphType.HVRPD)){
					if(tokens.length > 4){
						typeBuilder.setCostPerDistance(Double.parseDouble(tokens[4]));
						nuOfVehicles = Integer.parseInt(tokens[5]);
						vrpBuilder.setFleetSize(FleetSize.FINITE);
						vrpBuilder.addPenaltyVehicles(5.0, 5000);
					}
					else throw new IllegalStateException("option " + vrphType + " cannot be applied with this instance");
				}
				else if (vrphType.equals(VrphType.HVRPFD)){
					if(tokens.length > 4){
						typeBuilder.setFixedCost(Double.parseDouble(tokens[3]));
						typeBuilder.setCostPerDistance(Double.parseDouble(tokens[4]));
						nuOfVehicles = Integer.parseInt(tokens[5]);
						vrpBuilder.setFleetSize(FleetSize.FINITE);
						vrpBuilder.addPenaltyVehicles(5.0, 5000);
					}
					else throw new IllegalStateException("option " + vrphType + " cannot be applied with this instance");
				}
				for(int i=0;i<nuOfVehicles;i++){
					VehicleTypeImpl type = typeBuilder.build();
					Vehicle vehicle = VehicleImpl.Builder.newInstance("vehicle_"+tokens[1]+"_"+i)
							.setStartLocationCoordinate(depotCoord).setType(type).build();
					vrpBuilder.addVehicle(vehicle);
				}
			}
		}
		closeReader(reader);
	}

	private void closeReader(BufferedReader reader) {
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private String readLine(BufferedReader reader) {
		String readLine = null;
		try {
			readLine = reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return readLine;
	}

	private BufferedReader getReader(String filename)  {
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new FileReader(new File(filename)));
			return bufferedReader;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return bufferedReader;
	}
	
	public static void main(String[] args) {
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		VrphGoldenReader goldenReader = new VrphGoldenReader(vrpBuilder, VrphType.FSMD);
		goldenReader.read("instances/vrph/orig/cn_13mix.txt");
		VehicleRoutingProblem vrp = vrpBuilder.build();
		new VrpXMLWriter(vrp).write("instances/vrph/cn_13mix_VRPH_INFINITE.xml");
	}

}
