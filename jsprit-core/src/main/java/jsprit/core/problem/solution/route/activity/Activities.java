package jsprit.core.problem.solution.route.activity;

class Activities {
	
	public static String round(double time) {
		if (time == Double.MAX_VALUE) {
			return "oo";
		}
		return "" + Math.round(time);
	}

}
