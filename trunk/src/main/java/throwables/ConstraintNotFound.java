package throwables;

import framework.Constraint;

public class ConstraintNotFound extends Error {
	
	private static final long serialVersionUID = 1L;
	
	public ConstraintNotFound(String message) {
		super("Constraint not found: " + message);
	}

	public ConstraintNotFound(Constraint c) {
		super("Constraint " + c.toString() + " not found");
	}	
}
