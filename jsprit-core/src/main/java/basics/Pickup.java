package basics;

public class Pickup extends Service {

	public static class Builder extends Service.Builder {

		public static Builder newInstance(String id, int size){
			return new Builder(id,size);
		}
		
		Builder(String id, int size) {
			super(id, size);
		}
		
		public Pickup build(){
			this.setType("pickup");
			return new Pickup(this);
		}
		
	}
	
	Pickup(Builder builder) {
		super(builder);
	}
	
	public static void main(String[] args) {
		Pickup.Builder pickupBuilder = Pickup.Builder.newInstance("myPick", 10);
		Pickup pickup = (Pickup) pickupBuilder.setLocationId("foo").build();
		 
		System.out.println("loc="+pickup.getLocationId());
		System.out.println("capDemand="+pickup.getCapacityDemand());
	}

	
	

}
