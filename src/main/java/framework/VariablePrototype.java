package framework;

import cern.colt.Arrays;

/**
 * Objects of this type can be used to represent prototype variables.  This is useful since the only way to create a {@link Variable} is to
 * invoke a {@link ConstraintSolver}'s factory methods, which in turn perform other operations to keep track of the created variable(s).
 * If a variable is to be used only as a prototype, then all of these operations can be skipped, and this class used.  Note that variable prototypes are
 * not usable for reasoning, and real {@link Variable}s should be instantiated through the factory methods of instantiated {@link ConstraintSolver} on the
 * basis of the information contained in variable prototypes.  
 * @author Federico Pecora
 *
 */
public class VariablePrototype extends Variable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6552888283766914530L;

	private Object[] parameters;
	
	/**
	 * Static ID counter for {@link VariablePrototype}s. 
	 */
	public static int id = 0;
	
	/**
	 * Create a new {@link VariablePrototype} with a given {@link ConstraintSolver} and given parameters. 
	 * @param cs The {@link ConstraintSolver} to which concrete {@link Variable}s created on the basis of this prototype
	 * should refer to.
	 * @param parameters Parameters useful for the creation of a concrete {@link Variable}.
	 */
	public VariablePrototype(ConstraintSolver cs, Object ... parameters) {
		super(cs, id++);
		this.parameters = parameters;
	}
	
	/**
	 * Get the parameters of this {@link VariablePrototype}.
	 * @return The parameters of this {@link VariablePrototype}.
	 */
	public Object[] getParameters() { return this.parameters; } 

	@Override
	public int compareTo(Variable arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Domain getDomain() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDomain(Domain d) {
		// TODO Auto-generated method stub

	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return Arrays.toString(parameters);
	}

}
