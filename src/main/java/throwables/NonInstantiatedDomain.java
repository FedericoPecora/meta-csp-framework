package throwables;

import framework.Variable;

public class NonInstantiatedDomain extends Error {
	
	private static final long serialVersionUID = 1L;

	public NonInstantiatedDomain(Variable v) {
		super("Domain of variable " + v.getID() + " is empty");
	}	
}
