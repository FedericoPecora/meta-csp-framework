package throwables;

import cern.colt.Arrays;
import framework.Constraint;
import framework.Variable;

public class IllegalVariableRemoval extends Error {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -547394980886475010L;

	public IllegalVariableRemoval(Variable v, Constraint[] c) {
		super("Cannot remove " + v + " as it is involved in " + Arrays.toString(c));
	}

}
