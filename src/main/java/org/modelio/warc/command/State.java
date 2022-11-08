package org.modelio.warc.command;

import java.util.ArrayList;
import java.util.Dictionary;

public class State {
	String name;
	ArrayList<Dictionary<String, String>> events;
	ArrayList<Dictionary<String, String>> transitions;
	
	public State(	String name, 
					ArrayList<Dictionary<String, String>> events, 
					ArrayList<Dictionary<String, String>> transitions) {
		this.name = name;
		this.events = events;
		this.transitions = transitions;
	}
}
