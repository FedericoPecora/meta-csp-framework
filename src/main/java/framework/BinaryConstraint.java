package framework;

/**
 * This is the basic abstract class for representing binary constraints
 * (i.e., constraints whose scope is of size 2). 
 */
public abstract class BinaryConstraint extends Constraint {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -303664629058197492L;

	/**
	 * Creates a new {@link BinaryConstraint}.
	 */
	public BinaryConstraint() {
		this.scope = new Variable[2];
	}
	
	/**
	 * Get the source {@link Variable} of this {@link BinaryConstraint}.
	 * @return The source {@link Variable} of this {@link BinaryConstraint}.
	 */
	public Variable getFrom() { return scope[0]; }

	/**
	 * Get the destination {@link Variable} of this {@link BinaryConstraint}.
	 * @return The destination {@link Variable} of this {@link BinaryConstraint}.
	 */
	public Variable getTo() { return scope[1]; }
	
	/**
	 * Set the source {@link Variable} of this {@link BinaryConstraint}.
	 * @param f The source {@link Variable} of this {@link BinaryConstraint}.
	 */
	public void setFrom(Variable f) {
			this.scope[0] = f;
	}

	/**
	 * Set the destination {@link Variable} of this {@link BinaryConstraint}.
	 * @param t The destination {@link Variable} of this {@link BinaryConstraint}.
	 */
	public void setTo(Variable t) { 
			this.scope[1] = t;
	}
	
	@Override
	public String toString() {
		return "(" + this.getFrom() + ") --" + this.getEdgeLabel() + "--> (" + this.getTo() + ")";
	}


}
