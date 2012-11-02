package meta.symbolsAndTime;

import multi.activity.Activity;
import symbols.SymbolicDomain;
import framework.Constraint;
import framework.ConstraintNetwork;
import framework.ValueOrderingH;
import framework.VariableOrderingH;

public class ReusableResource extends Schedulable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3747575248501554970L;
	private int capacity;
	
	public ReusableResource(VariableOrderingH varOH, ValueOrderingH valOH, int capacity) {
		super(varOH, valOH);
		this.capacity = capacity;
	}

	@Override
	public boolean isConflicting(Activity[] peak) {
		int sum = 0;
		for (Activity act : peak) {
			sum += Integer.parseInt(((SymbolicDomain)act.getSymbolicVariable().getDomain()).getSymbols()[0]);
			if (sum > capacity) return true;
		}
		return false;
	}

	@Override
	public void draw(ConstraintNetwork network) {
		// TODO Auto-generated method stub
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getEdgeLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object clone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEquivalent(Constraint c) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
