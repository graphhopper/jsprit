package examples;

import readers.ChristofidesReader;
import basics.VehicleRoutingProblem;
import basics.io.VrpXMLWriter;

public class CVRPExample {
	
	public static void main(String[] args) {
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new ChristofidesReader(builder).read("input/vrpnc1.txt");
		VehicleRoutingProblem vrp = builder.build();
		new VrpXMLWriter(vrp).write("input/vrpnc1-jsprit.xml");
	}

}
