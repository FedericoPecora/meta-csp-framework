package org.metacsp.framework;

public class DummyConstraint extends Constraint {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4104395475066915919L;

	private String edgeLabel;
	
	private DummyConstraint() {}
	
	public DummyConstraint(String edgeLabel) {
		this.edgeLabel = edgeLabel;
	}
	
	@Override
	public String toString() {
		return "Dummy constraint \"" + this.edgeLabel + "\"";
	}

	@Override
	public String getEdgeLabel() {
		return this.edgeLabel;
	}

	@Override
	public Object clone() {
		DummyConstraint dc = new DummyConstraint(this.edgeLabel);
		dc.setScope(this.scope);
		dc.setAutoRemovable(this.isAutoRemovable());
		return dc;
	}

	@Override
	public boolean isEquivalent(Constraint c) {
		if (!(c instanceof DummyConstraint)) return false;
		return this.edgeLabel.equals(((DummyConstraint)c).getEdgeLabel());
	}
	
	public DummyVariable getDummyVariable() { 
		for (Variable v : this.scope)
			if (v instanceof DummyVariable) return (DummyVariable)v;
		return null;
	}

}
