package jsprit.core.problem.vehicle;

/**
 * Key to identify different vehicles
 * 
 * <p>Two vehicles are equal if they share the same type and location.
 * <p>Note that earliestStart and latestArrival are ignored by this key (this might change in future)
 * 
 * @author stefan
 *
 */
class VehicleTypeKey {
	
	public final String type;
	public final String startLocationId;
	public final String endLocationId;
	
	VehicleTypeKey(String typeId, String startLocationId, String endLocationId) {
		super();
		this.type = typeId;
		this.startLocationId = startLocationId;
		this.endLocationId = endLocationId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((endLocationId == null) ? 0 : endLocationId.hashCode());
		result = prime * result
				+ ((startLocationId == null) ? 0 : startLocationId.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VehicleTypeKey other = (VehicleTypeKey) obj;
		if (endLocationId == null) {
			if (other.endLocationId != null)
				return false;
		} else if (!endLocationId.equals(other.endLocationId))
			return false;
		if (startLocationId == null) {
			if (other.startLocationId != null)
				return false;
		} else if (!startLocationId.equals(other.startLocationId))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	
	
}