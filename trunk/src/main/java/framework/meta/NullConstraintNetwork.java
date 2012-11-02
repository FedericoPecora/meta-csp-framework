package framework.meta;

import framework.ConstraintNetwork;
import framework.ConstraintSolver;

/**
 * A special constraint network which is used to represent terminal nodes
 * in the search space of the {@link MetaConstraintSolver}.
 * 
 * @author Federico Pecora
 *
 */
public class NullConstraintNetwork extends ConstraintNetwork {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4572644031938139796L;

	/**
	 * Instantiates a new terminal node for the meta-CSP search space.
	 * @param sol The meta-CSP solver which has reached a terminal node in its search space.
	 */
	public NullConstraintNetwork(ConstraintSolver sol) {
		super(sol);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Get a {@link String} representation of the terminal node.
	 */
	public String toString() {
		return "conflicting";
	}

}
