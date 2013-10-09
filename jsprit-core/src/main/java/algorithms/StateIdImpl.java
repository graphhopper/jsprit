package algorithms;

import algorithms.StateManager.StateId;

class StateIdImpl implements StateId {
	
	private String name;

	public StateIdImpl(String name) {
		super();
		this.name = name;
	}
	
	public String toString(){
		return name;
	}

	
}