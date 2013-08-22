package algorithms;

class Marginals {
	
	private double additionalCosts;
	private double additionalTime;
	public Marginals(double additionalCosts, double additionalTime) {
		super();
		this.additionalCosts = additionalCosts;
		this.additionalTime = additionalTime;
	}
	/**
	 * @return the additionalCosts
	 */
	public double getAdditionalCosts() {
		return additionalCosts;
	}
	/**
	 * @return the additionalTime
	 */
	public double getAdditionalTime() {
		return additionalTime;
	}
	
	

}
