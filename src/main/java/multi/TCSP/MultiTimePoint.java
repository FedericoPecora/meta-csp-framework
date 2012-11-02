package multi.TCSP;

import framework.Constraint;
import framework.ConstraintSolver;
import framework.Domain;
import framework.Variable;
import framework.multi.MultiVariable;

public class MultiTimePoint extends MultiVariable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1769651361635250174L;

	protected MultiTimePoint(ConstraintSolver cs, int id, ConstraintSolver[] internalSolvers) {
		super(cs, id, internalSolvers);
		// TODO Auto-generated constructor stub
	}

	protected MultiTimePoint(ConstraintSolver cs, int id, ConstraintSolver internalSolver, time.TimePoint tp) {
		super(cs, id, new ConstraintSolver[] {internalSolver}, new Variable[] {tp}, null);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int compareTo(Variable arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected Variable[] createInternalVariables() {
		Variable tp = internalSolvers[0].createVariable();
		return new Variable[] {tp};
	}

	@Override
	protected Constraint[] createInternalConstraints(Variable[] variables) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDomain(Domain d) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String toString() {
		return this.getInternalVariables()[0].toString();
	}
	
	public void setTimePoint(time.TimePoint tp) {
		this.getInternalVariables()[0] = tp;
	}

}
