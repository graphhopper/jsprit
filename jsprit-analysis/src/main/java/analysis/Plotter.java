package analysis;

import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblemSolution;

public class Plotter {
	
	public static enum Label {
		ID, SIZE, NO_LABEL
	}
	
	private boolean showFirstActivity = true;
	
	private Label label = Label.SIZE;
	
	private VehicleRoutingProblem vrp;
	
	private VehicleRoutingProblemSolution solution;
	
	public void setShowFirstActivity(boolean show){
		showFirstActivity = show;
	}
	
	public void setLabel(Label label){
		this.label = label;
	}

	public Plotter(VehicleRoutingProblem vrp) {
		super();
		this.vrp = vrp;
	}

	public Plotter(VehicleRoutingProblem vrp, VehicleRoutingProblemSolution solution) {
		super();
		this.vrp = vrp;
		this.solution = solution;
	}
	
	public void plot(String pngFileName, String plotTitle){
		
	}

}
