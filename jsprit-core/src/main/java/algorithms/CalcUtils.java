package algorithms;

import basics.route.TourActivity;

class CalcUtils {
	
	static double getStartTimeAtAct(double startTimeAtPrevAct, double tpTime_prevAct_nextAct, TourActivity nextAct){
		return Math.max(startTimeAtPrevAct + tpTime_prevAct_nextAct, nextAct.getTheoreticalEarliestOperationStartTime()) + nextAct.getOperationTime();
	}

	static double getStartTimeAtAct(double nextActArrTime, TourActivity nextAct){
		return Math.max(nextActArrTime, nextAct.getTheoreticalEarliestOperationStartTime()) + nextAct.getOperationTime();
	}
}
