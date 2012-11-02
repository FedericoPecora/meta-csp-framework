package framework.multi;

import java.util.Arrays;
import java.util.logging.Logger;

import utility.logging.MetaCSPLogging;
import framework.Constraint;
import framework.Variable;

/**
 * This class is used to represent binay constraints among {@link MultiVariable}s.
 * 
 * @author Federico Pecora
 *
 */
public abstract class MultiBinaryConstraint extends MultiConstraint {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5679258233083583893L;
	private transient Logger logger = MetaCSPLogging.getLogger(this.getClass());

	/**
	 * Instantiates a new {@link MultiBinaryConstraint}, sets its scope to have size 2.
	 */
	public MultiBinaryConstraint() {
		this.scope = new Variable[2];
	}

	@Override
	protected Constraint[] createInternalConstraints(Variable[] variables) {
		Constraint[] ret = this.createInternalConstraints(variables[0], variables[1]);
		logger.finest("Created internal constraints for " + this + ": " + Arrays.toString(ret));
		return ret;
	}

	/**
	 * This method must be defined to create the internal constraints underlying 
	 * this {@link MultiBinaryConstraint}.
	 * @param from The source {@link Variable} of the {@link MultiConstraint}.
	 * @param to The destination {@link Variable} of the {@link MultiConstraint}.
	 * @return An array of lower-level constraints which "implement" this {@link MultiBinaryConstraint}.
	 */
	protected abstract Constraint[] createInternalConstraints(Variable from, Variable to);

	/**
	 * Get the source {@link Variable} of this {@link MultiBinaryConstraint}.
	 * @return The source {@link Variable} of this {@link MultiBinaryConstraint}.
	 */
	public Variable getFrom() {
		return this.scope[0];
	}

	/**
	 * Get the destination {@link Variable} of this {@link MultiBinaryConstraint}.
	 * @return The destination {@link Variable} of this {@link MultiBinaryConstraint}.
	 */
	public Variable getTo() {
		return this.scope[1];
	}

	/**
	 * Set the source {@link Variable} of this {@link MultiBinaryConstraint}.
	 * @param mv The source {@link Variable} of this {@link MultiBinaryConstraint}.
	 */
	public void setFrom(Variable mv) {
		this.scope[0] = mv;
	}

	/**
	 * Set the destination {@link Variable} of this {@link MultiBinaryConstraint}.
	 * @param mv The destination {@link Variable} of this {@link MultiBinaryConstraint}.
	 */
	public void setTo(Variable mv) {
		this.scope[1] = mv;
	}

	@Override
	public String toString() {
		return "(" + this.getFrom() + ") --" + this.getEdgeLabel() + "--> (" + this.getTo() + ")";
	}


}
