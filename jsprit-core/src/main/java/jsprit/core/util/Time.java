package jsprit.core.util;

public class Time {

	/**
	 * Parse time to seconds.
	 * 
	 * <p>If you add PM or AM to timeString, it considers english-time, otherwise not.
	 * 
	 * <p>timeString must be hour:min:sec
	 * <p>example: 12 AM returns 12*3600. sec
	 * 6:30 AM --> 6*3600. + 30*60.
	 * 0:30:20 AM --> 30*3600. + 20. 
	 * 6:00 PM --> 6*3600. + 12.*3600. 
	 * 6:00:12 --> 6*3600. + 12.
	 *  
	 * @return
	 */
	public static double parseTimeToSeconds(String timeString){
		if(timeString.substring(0, 1).matches("\\D")) throw new IllegalArgumentException("timeString must start with digit [0-9]");
		double dayTime = 0.;
		if(timeString.toLowerCase().contains("pm")){
			dayTime = 12.*3600.;
		}
		String[] tokens = timeString.split(":");
		
		if(tokens.length == 1){ //1 AM or 01 AM	
			return getHourInSeconds(tokens[0]) + dayTime;
		}
		else if(tokens.length == 2){
			return getHourInSeconds(tokens[0]) + getMinInSeconds(tokens[1]) + dayTime;
		}
		else if(tokens.length == 3){
			return getHourInSeconds(tokens[0]) + getMinInSeconds(tokens[1]) + getSecondsInSeconds(tokens[2]) + dayTime;
		}
		else {
			throw new IllegalArgumentException("wrong timeString");
		}
		
	}

	private static double getSecondsInSeconds(String secString) {
		return getDigit(secString);
	}

	private static double getMinInSeconds(String minString) {
		return getDigit(minString)*60.;
	}

	private static double getHourInSeconds(String hourString) {
		return getDigit(hourString)*3600.;
	}

	private static double getDigit(String digitString) {
		if(digitString.length() == 1){
			return Double.parseDouble(digitString);
		}
		if(digitString.substring(1, 2).matches("\\D")){
			return Double.parseDouble(digitString.substring(0, 1));
		}
		else{
			if(digitString.startsWith("0")){
				return Double.parseDouble(digitString.substring(1, 2));
			}
			else{
				return Double.parseDouble(digitString.substring(0, 2));
			}
		}
	}
}
