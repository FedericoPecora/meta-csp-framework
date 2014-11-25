package org.metacsp.throwables;

import java.util.Arrays;

import org.metacsp.framework.Variable;

public class NoFocusDefinedException extends RuntimeException {
	
	private static final long serialVersionUID = -720305819659302093L;

	public NoFocusDefinedException(Variable ... vars) {
		super(Arrays.toString(vars));
	}

}
