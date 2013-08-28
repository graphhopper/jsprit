package algorithms;

import basics.route.TourActivity;

class CalcUtils {
	
	/**
	 * 
	 * @param startTimeAtPrevAct
	 * @param tpTime_prevAct_nextAct
	 * @param nextAct
	 * @return
	 */
	static double getStartTimeAtAct(double startTimeAtPrevAct, double tpTime_prevAct_nextAct, TourActivity nextAct){
		return Math.max(startTimeAtPrevAct + tpTime_prevAct_nextAct, nextAct.getTheoreticalEarliestOperationStartTime()) + nextAct.getOperationTime();
	}

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
