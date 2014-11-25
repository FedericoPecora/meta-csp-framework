package org.metacsp.framework.meta;

import java.util.Arrays;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.Variable;

public class FocusConstraint extends Constraint {

	private static final long serialVersionUID = 1065072281439135501L;

	@Override
	public String toString() {
		return Arrays.toString(this.getScope());
	}

	@Override
	public String getEdgeLabel() {
		return Arrays.toString(this.getScope());
	}

	@Override
	public Object clone() {
		FocusConstraint ret = new FocusConstraint();
		ret.setScope(this.getScope());
		return ret;
	}

	@Override
	public boolean isEquivalent(Constraint c) {
		if (!(c instanceof FocusConstraint)) return false;
		for (Variable var : this.getScope()) {
			boolean found = false;
			for (Variable var1 : c.getScope()) {
				if (var1.equals(var)) {
					found = true;
					break;
				}
			}
			if (!found) return false;
		}
		return true;
	}

}
