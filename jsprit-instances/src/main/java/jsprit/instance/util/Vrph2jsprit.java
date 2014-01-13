package jsprit.instance.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.util.Coordinate;

public class Vrph2jsprit {
	

	public static void main(String[] args) throws IOException {
		
		for(int i=3;i<7;i++){
			VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
			String instance = "vrph/orig/c20_"+i+"mix.txt";
			BufferedReader reader = new BufferedReader(new FileReader(new File(instance)));
			String line = null;
			boolean firstline = true;
			Coordinate depotCoord = null;
			int customerCount=0;
			while((line=reader.readLine())!=null){
				String trimedLine = line.trim();
				if(trimedLine.startsWith("//")) continue;
				String[] tokens = trimedLine.split("\\s+");
				Integer nuOfCustomer = 0;
				if(firstline){
					nuOfCustomer=Integer.parseInt(line);
					customerCount=1;
					firstline=false;
				}
				else if(customerCount<=nuOfCustomer) {
					if(customerCount == 1){
						depotCoord = Coordinate.newInstance(Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2]));
					}
					else{
						Service.Builder serviceBuilder = Service.Builder.newInstance(tokens[0], Integer.parseInt(tokens[0]));
						serviceBuilder.setCoord(Coordinate.newInstance(Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2])));
						vrpBuilder.addJob(serviceBuilder.build());
						customerCount++;
					}
				}
				else if(trimedLine.startsWith("v")){
					VehicleTypeImpl.Builder typeBuilder = VehicleTypeImpl.Builder.newInstance("type_"+tokens[1], Integer.parseInt(tokens[2]));
					typeBuilder.setFixedCost(Double.parseDouble(tokens[3]));
					typeBuilder.setCostPerDistance(1.0);
					VehicleTypeImpl type = typeBuilder.build();
					Vehicle vehicle = VehicleImpl.Builder.newInstance("vehicle_"+tokens[1]).setType(type).build();
				}
				
			}
			reader.close();
		}
		
	}

}
