package algorithms;

import basics.route.TourActivity;

class CalcUtils {
	

	/**
	 * Calculates actEndTime assuming that activity can at earliest start at act.getTheoreticalEarliestOperationStartTime().
	 * 
	 * @param actArrTime
	 * @param act
	 * @return
	 */
	static double getActivityEndTime(double actArrTime, TourActivity act){
		return Math.max(actArrTime, act.getTheoreticalEarliestOperationStartTime()) + act.getOperationTime();
	}
}
