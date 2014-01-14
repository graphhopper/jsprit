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
 * http://mistic.heig-vd.ch/taillard/problemes.dir/vrp.dir/vrp.html
 * 
 * <p>diff. options of VrphType yields to different problem types for
 * - vrphe with infinite fleet, i.e. different types with different variable costs and infinite number of vehicles of each type
 * - vrphe with finite fleet, i.e. different types with different variable costs and finite number of vehicles of each type
 * - vfm, different types with different fixed costs
 * - vfmvrc different types with different fixed costs and variable costs
 * 
 * <p>cxxx3-cxxx6 do not have variable costs and nuVehicle, thus they can only be used for vfm.
 * 
 * @author schroeder
 *
 */
public class VrphGoldenReader {
	
	/**
	 *
	 * VRPHE_INFINITE - different types with different variable costs and infinite number of vehicles of each type
	 * <p>VRPHE_FINITE - different types with different variable costs and finite number of vehicles of each type
	 * <p>VFM - different types with different fixed costs
	 * <p>VFMVRC - different types with different fixed costs and variable costs
	 *  
	 * @author schroeder
	 *
	 */
	public enum VrphType {
		VRPH_INFINITE, VRPH_FINITE, VFM, VFMVRC
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
					Service.Builder serviceBuilder = Service.Builder.newInstance(tokens[0], Integer.parseInt(tokens[0]));
					serviceBuilder.setCoord(Coordinate.newInstance(Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2])));
					vrpBuilder.addJob(serviceBuilder.build());
				}
				customerCount++;
			}
			else if(trimedLine.startsWith("v")){
				VehicleTypeImpl.Builder typeBuilder = VehicleTypeImpl.Builder.newInstance("type_"+tokens[1], Integer.parseInt(tokens[2]));
				int nuOfVehicles = 1;
				if(vrphType.equals(VrphType.VFM)){
					typeBuilder.setFixedCost(Double.parseDouble(tokens[3]));
				}
				else if(vrphType.equals(VrphType.VFMVRC)){
					typeBuilder.setFixedCost(Double.parseDouble(tokens[3]));
					if(tokens.length > 4){
						typeBuilder.setCostPerDistance(Double.parseDouble(tokens[4]));
					}
					else throw new IllegalStateException("option " + vrphType + " cannot be applied with this instance");
				}
				else if(vrphType.equals(VrphType.VRPH_INFINITE)){
					if(tokens.length > 4){
						typeBuilder.setCostPerDistance(Double.parseDouble(tokens[4]));
					}
					else throw new IllegalStateException("option " + vrphType + " cannot be applied with this instance");
				}
				else { //VrphType.VRPH_FINITE
					if(tokens.length > 4){
						typeBuilder.setCostPerDistance(Double.parseDouble(tokens[4]));
						nuOfVehicles = Integer.parseInt(tokens[5]);
						vrpBuilder.setFleetSize(FleetSize.FINITE);
					}
					else throw new IllegalStateException("option " + vrphType + " cannot be applied with this instance");
				}
				for(int i=0;i<nuOfVehicles;i++){
					VehicleTypeImpl type = typeBuilder.build();
					Vehicle vehicle = VehicleImpl.Builder.newInstance("vehicle_"+tokens[1]+"_"+i)
							.setLocationCoord(depotCoord).setType(type).build();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String readLine(BufferedReader reader) {
		String readLine = null;
		try {
			readLine = reader.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return readLine;
	}

	private BufferedReader getReader(String filename)  {
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new FileReader(new File(filename)));
			return bufferedReader;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bufferedReader;
	}
	
	public static void main(String[] args) {
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		VrphGoldenReader goldenReader = new VrphGoldenReader(vrpBuilder, VrphType.VRPH_INFINITE);
		goldenReader.read("instances/vrph/orig/cn_13mix.txt");
		VehicleRoutingProblem vrp = vrpBuilder.build();
		new VrpXMLWriter(vrp).write("instances/vrph/cn_13mix_VRPH_INFINITE.xml");
	}

}
