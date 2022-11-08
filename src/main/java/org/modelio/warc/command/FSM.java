package org.modelio.warc.command;

import java.util.ArrayList;

public class FSM {	
	ArrayList<State> states = new ArrayList<>();
	
	public void addState(State newState) {
		this.states.add(newState);
	}
	
	public State getStateByName(String name) {
		for(State state : this.states) {
			if(name.equals(state.name)) {
				return state;
			}
		}
		
		return null;
	}
}
