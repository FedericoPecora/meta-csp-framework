package org.metacsp.throwables;

import org.metacsp.framework.ConstraintSolver;

public class UnimplementedSubVariableException extends Error {

	private static final long serialVersionUID = 5970717539052777001L;

	public UnimplementedSubVariableException(ConstraintSolver cs) {
		super("Solver " + cs + " has empty implementation of createVariablesSub().");
	}

}
