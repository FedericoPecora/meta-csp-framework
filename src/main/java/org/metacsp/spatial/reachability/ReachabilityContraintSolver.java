package org.metacsp.spatial.reachability;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Variable;
import org.metacsp.framework.ConstraintSolver.OPTIONS;
import org.metacsp.spatial.RCC.Region;

public class ReachabilityContraintSolver extends ConstraintSolver{

	private int IDs = 0;
	public ReachabilityContraintSolver() {
		super(new Class[]{ReachabilityConstraint.class}, ConfigurationVariable.class);
		//this.setOptions(OPTIONS.AUTO_PROPAGATE);
	}

	@Override
	public boolean propagate() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean addConstraintsSub(Constraint[] c) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected void removeConstraintsSub(Constraint[] c) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Variable[] createVariablesSub(int num) {
		ConfigurationVariable[] ret = new ConfigurationVariable[num];
		for (int i = 0; i < num; i++) ret[i] = new ConfigurationVariable(this, IDs++);
			return ret;
	}

	@Override
	protected void removeVariablesSub(Variable[] v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void registerValueChoiceFunctions() {
		// TODO Auto-generated method stub
		
	}

}
