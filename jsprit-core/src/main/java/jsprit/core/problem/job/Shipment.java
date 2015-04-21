/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package jsprit.core.problem.job;

import jsprit.core.problem.AbstractJob;
import jsprit.core.problem.Capacity;
import jsprit.core.problem.Location;
import jsprit.core.problem.Skills;
import jsprit.core.problem.solution.route.activity.TimeWindow;


/**
 * Shipment is an implementation of Job and consists of a pickup and a delivery of something.
 * 
 * <p>It distinguishes itself from {@link Service} as two locations are involved a pickup where usually 
 * something is loaded to the transport unit and a delivery where something is unloaded.
 * 
 * <p>By default serviceTimes of both pickup and delivery is 0.0 and timeWindows of both is [0.0, Double.MAX_VALUE],
 * 
 * <p>A shipment can be built with a builder. You can get an instance of the builder by coding <code>Shipment.Builder.newInstance(...)</code>.
 * This way you can specify the shipment. Once you build the shipment, it is immutable, i.e. fields/attributes cannot be changed anymore and 
 * you can only 'get' the specified values.
 * 
 * <p>Note that two shipments are equal if they have the same id.
 * 
 * @author schroeder
 *
 */
public class Shipment extends AbstractJob{



    /**
	 * Builder that builds the shipment.
	 * 
	 * @author schroeder
	 *
	 */
	public static class Builder {
		
		private String id;

		private double pickupServiceTime = 0.0;

		private double deliveryServiceTime = 0.0;

		private TimeWindow deliveryTimeWindow = TimeWindow.newInstance(0.0, Double.MAX_VALUE);

		private TimeWindow pickupTimeWindow = TimeWindow.newInstance(0.0, Double.MAX_VALUE);
		
		private Capacity.Builder capacityBuilder = Capacity.Builder.newInstance();
		
		private Capacity capacity;

        private Skills.Builder skillBuilder = Skills.Builder.newInstance();

        private Skills skills;

        private String name = "no-name";

        private Location pickupLocation_;

        private Location deliveryLocation_;

        /**
		 * Returns new instance of this builder.
		 * 
		 * @param id the id of the shipment which must be a unique identifier among all jobs
		 * @return the builder
		 */
		public static Builder newInstance(String id){
			return new Builder(id);
		}
		
		Builder(String id){
			if(id == null) throw new IllegalArgumentException("id must not be null");
			this.id = id;
		}

		/**
         * Sets pickup location.
         *
         * @param pickupLocation pickup location
         * @return builder
         */
        public Builder setPickupLocation(Location pickupLocation){
            this.pickupLocation_ = pickupLocation;
            return this;
        }

		/**
		 * Sets pickupServiceTime.
		 * 
		 * <p>ServiceTime is intended to be the time the implied activity takes at the pickup-location.
		 * 
		 * @param serviceTime the service time / duration the pickup of the associated shipment takes
		 * @return builder
		 * @throws IllegalArgumentException if servicTime < 0.0
		 */
		public Builder setPickupServiceTime(double serviceTime){
			if(serviceTime < 0.0) throw new IllegalArgumentException("serviceTime must not be < 0.0");
			this.pickupServiceTime = serviceTime;
			return this;
		}
		
		/**
		 * Sets the timeWindow for the pickup, i.e. the time-period in which a pickup operation is
		 * allowed to START.
		 * 
		 * <p>By default timeWindow is [0.0, Double.MAX_VALUE}
		 * 
		 * @param timeWindow the time window within the pickup operation/activity can START
		 * @return builder
		 * @throws IllegalArgumentException if timeWindow is null
		 */
		public Builder setPickupTimeWindow(TimeWindow timeWindow){
			if(timeWindow == null) throw new IllegalArgumentException("timeWindow cannot be null");
			this.pickupTimeWindow = timeWindow;
			return this;
		}

        /**
         * Sets delivery location.
         *
         * @param deliveryLocation delivery location
         * @return builder
         */
        public Builder setDeliveryLocation(Location deliveryLocation){
            this.deliveryLocation_ = deliveryLocation;
            return this;
        }

		/**
		 * Sets the delivery service-time.
		 * 
		 * <p>ServiceTime is intended to be the time the implied activity takes at the delivery-location.
		 * 
		 * @param deliveryServiceTime the service time / duration of shipment's delivery
		 * @return builder
		 * @throws IllegalArgumentException if serviceTime < 0.0
		 */
		public Builder setDeliveryServiceTime(double deliveryServiceTime){
			if(deliveryServiceTime < 0.0) throw new IllegalArgumentException("deliveryServiceTime must not be < 0.0");
			this.deliveryServiceTime = deliveryServiceTime;
			return this;
		}
		
		/**
		 * Sets the timeWindow for the delivery, i.e. the time-period in which a delivery operation is
		 * allowed to start.
		 * 
		 * <p>By default timeWindow is [0.0, Double.MAX_VALUE}
		 * 
		 * @param timeWindow the time window within the associated delivery is allowed to START
		 * @return builder
		 * @throws IllegalArgumentException if timeWindow is null
		 */
		public Builder setDeliveryTimeWindow(TimeWindow timeWindow){
			if(timeWindow == null) throw new IllegalArgumentException("delivery time-window must not be null");
			this.deliveryTimeWindow = timeWindow;
			return this;
		}
		
		/**
		 * Adds capacity dimension.
		 * 
		 * @param dimensionIndex the dimension index of the corresponding capacity value
		 * @param dimensionValue the capacity value
		 * @return builder
		 * @throws IllegalArgumentException if dimVal < 0
		 */
		public Builder addSizeDimension(int dimensionIndex, int dimensionValue) {
			if(dimensionValue<0) throw new IllegalArgumentException("capacity value cannot be negative");
			capacityBuilder.addDimension(dimensionIndex, dimensionValue);
			return this;
		}
		

		/**
		 * Builds the shipment.
		 * 
		 * @return shipment
		 * @throws IllegalStateException if neither pickup-location nor pickup-coord is set or if neither delivery-location nor delivery-coord
		 * is set
		 */
		public Shipment build(){
			if(pickupLocation_ == null) throw new IllegalStateException("pickup location is missing");
			if(deliveryLocation_ == null) throw new IllegalStateException("delivery location is missing");
			capacity = capacityBuilder.build();
            skills = skillBuilder.build();
			return new Shipment(this);
		}


        public Builder addRequiredSkill(String skill) {
            skillBuilder.addSkill(skill);
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }
    }
	
	private final String id;

	private final double pickupServiceTime;

	private final double deliveryServiceTime;

	private final TimeWindow deliveryTimeWindow;

	private final TimeWindow pickupTimeWindow;
	
	private final Capacity capacity;

    private final Skills skills;

    private final String name;

    private final Location pickupLocation_;

    private final Location deliveryLocation_;

	Shipment(Builder builder){
		this.id = builder.id;
		this.pickupServiceTime = builder.pickupServiceTime;
		this.pickupTimeWindow = builder.pickupTimeWindow;
		this.deliveryServiceTime = builder.deliveryServiceTime;
		this.deliveryTimeWindow = builder.deliveryTimeWindow;
		this.capacity = builder.capacity;
        this.skills = builder.skills;
        this.name = builder.name;
        this.pickupLocation_ = builder.pickupLocation_;
        this.deliveryLocation_ = builder.deliveryLocation_;
	}
	
	@Override
	public String getId() {
		return id;
	}

    public Location getPickupLocation(){ return pickupLocation_; }

	/**
	 * Returns the pickup service-time.
	 * 
	 * <p>By default service-time is 0.0.
	 * 
	 * @return service-time
	 */
	public double getPickupServiceTime() {
		return pickupServiceTime;
	}

    public Location getDeliveryLocation() { return deliveryLocation_; }

	/**
	 * Returns service-time of delivery.
	 * 
	 * @return service-time of delivery
	 */
	public double getDeliveryServiceTime() {
		return deliveryServiceTime;
	}

	/**
	 * Returns the time-window of delivery.
	 * 
	 * @return time-window of delivery
	 */
	public TimeWindow getDeliveryTimeWindow() {
		return deliveryTimeWindow;
	}

	/**
	 * Returns the time-window of pickup.
	 * 
	 * @return time-window of pickup
	 */
	public TimeWindow getPickupTimeWindow() {
		return pickupTimeWindow;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/**
	 * Two shipments are equal if they have the same id.
	 * 
	 * @return true if shipments are equal (have the same id)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Shipment other = (Shipment) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public Capacity getSize() {
		return capacity;
	}

    @Override
    public Skills getRequiredSkills() {
        return skills;
    }

    @Override
    public String getName() {
        return name;
    }


}
