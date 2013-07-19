package basics;


public class Delivery extends Service{
	
	public static class Builder extends Service.Builder {

		public static Builder newInstance(String id, int size){
			return new Builder(id,size);
		}
		
		Builder(String id, int size) {
			super(id, -1*size);
		}
		
		public Delivery build(){
			return new Delivery(this);
		}
		
	}
	
	Delivery(Builder builder) {
		super(builder);
		
	}
	
	public static void main(String[] args) {
		Delivery.Builder deliveryBuilder = Delivery.Builder.newInstance("myDelivery", 10);
		Delivery delivery = (Delivery) deliveryBuilder.setLocationId("foo").build();
		 
		System.out.println("loc="+delivery.getLocationId());
		System.out.println("capDemand="+delivery.getCapacityDemand());
	}





}
