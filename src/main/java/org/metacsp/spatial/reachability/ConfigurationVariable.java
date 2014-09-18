package org.metacsp.spatial.reachability;

import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Domain;
import org.metacsp.framework.Variable;

public class ConfigurationVariable extends Variable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3333917390676953990L;
	private Domain dom;
	protected ConfigurationVariable(ConstraintSolver cs, int id) {
		super(cs, id);
		setDomain(new ConfigurationDomain(this));
	}

	@Override
	public int compareTo(Variable arg0) {
		
		return this.getID() - arg0.getID();
	}

	@Override
	public Domain getDomain() {
		// TODO Auto-generated method stub
		return dom;
	}

	@Override
	public void setDomain(Domain d) {
		this.dom = d;
		
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.getClass().getSimpleName() + " " + this.id + " " + this.getDomain();
	}

}
