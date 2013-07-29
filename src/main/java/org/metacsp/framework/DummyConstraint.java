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
		return new DummyConstraint(this.edgeLabel);
	}

	@Override
	public boolean isEquivalent(Constraint c) {
		if (!(c instanceof DummyConstraint)) return false;
		return this.edgeLabel.equals(((DummyConstraint)c).getEdgeLabel());
	}

}
