package throwables;

import framework.Variable;

public class VariableNotFound extends Error {
	
	private static final long serialVersionUID = 1L;

	public VariableNotFound(Variable v) {
		super("Variable " + v.toString() + " not found");
	}	
}
