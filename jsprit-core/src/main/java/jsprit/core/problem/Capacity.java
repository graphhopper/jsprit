package jsprit.core.problem;


public class Capacity {
	
	public static Capacity copyOf(Capacity capacity){
		if(capacity == null) return null;
		return new Capacity(capacity);
	}
	
	public static class Builder {
		
		/**
		 * default is 1 dimension with size of zero
		 */
		private int[] dimensions = new int[1];
		
		public static Builder newInstance(){
			return new Builder();
		}
		
		Builder(){}
		
		/**
		 * add capacity dimension
		 * 
		 * <p>if automatically resizes dimensions according to index, i.e. if index=7 there are 8 dimensions.
		 * 
		 * @throw IllegalStateException if dimValue < 0
		 * @param index
		 * @param dimValue
		 * @return
		 */
		public Builder addDimension(int index, int dimValue){
			if(dimValue<0) throw new IllegalStateException("dimValue can never be negative");
			if(index < dimensions.length){
				dimensions[index] = dimValue;
			}
			else{
				int requiredSize = index + 1;
				int[] newDimensions = new int[requiredSize]; 
				copy(dimensions,newDimensions);
				newDimensions[index]=dimValue;
				this.dimensions=newDimensions;
			}
			return this;
		}
		
		private void copy(int[] from, int[] to) {
			for(int i=0;i<dimensions.length;i++){
				to[i]=from[i];
			}
		}

		public Capacity build() {
			return new Capacity(this);
		}

		
	}
	
	private int[] dimensions;
	
	/**
	 * copy constructor
	 * 
	 * @param capacity
	 */
	Capacity(Capacity capacity){
		this.dimensions = new int[capacity.getNuOfDimensions()];
		for(int i=0;i<capacity.getNuOfDimensions();i++){
			this.dimensions[i]=capacity.get(i);
		}
	}
	
	Capacity(Builder builder) {
		dimensions = builder.dimensions;
	}

	public int getNuOfDimensions(){
		return dimensions.length;
	}
	
	public int get(int index){
		return dimensions[index];
	}
	
}
