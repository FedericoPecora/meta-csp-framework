package framework.meta;

import framework.ConstraintNetwork;
import framework.ConstraintSolver;

/**
 * A constraint network for meta-CSPs.  This is used to maintain the search
 * space of the {@link MetaConstraintSolver}.
 * 
 * @author Federico Pecora
 *
 */
public class MetaVariableConstraintNetwork extends ConstraintNetwork {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1761603965354453570L;

	/**
	 * Instantiates a constraint network for use by a {@link MetaConstraintSolver}.
	 * @param sol The meta-CSP solver maintaining this network.
	 */
	public MetaVariableConstraintNetwork(ConstraintSolver sol) {
		super(sol);
		// TODO Auto-generated constructor stub
	}

}
