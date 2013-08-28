package basics;


public final class Delivery extends Service{
	
	public static class Builder extends Service.Builder {

		public static Builder newInstance(String id, int size){
			return new Builder(id,size);
		}
		
		Builder(String id, int size) {
			super(id, size);
		}
		
		public Delivery build(){
			if(locationId == null) { 
				if(coord == null) throw new IllegalStateException("either locationId or a coordinate must be given. But is not.");
				locationId = coord.toString();
			}
			this.setType("delivery");
			return new Delivery(this);
		}
		
	}
	
	Delivery(Builder builder) {
		super(builder);
		
	}

}
