package throwables;

import meta.symbolsAndTime.StateVariable;

public class StateNotFound extends Error {
	
	private static final long serialVersionUID = 1L;

	public StateNotFound(String s, StateVariable fsa) {
		super("State " + s + " not found in FSA " + fsa);
	}	
}
