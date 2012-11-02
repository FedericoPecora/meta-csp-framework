package symbols;

import framework.BinaryConstraint;
import framework.Constraint;

public class SymbolicValueConstraint extends BinaryConstraint {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5383911027452257341L;

	public SymbolicValueConstraint(Type type) {
		this.type = type;
		// TODO Auto-generated constructor stub
	}

	public static enum Type {EQUALS, DIFFERENT, UNARYEQUALS, UNARYDIFFERENT, FORBIDDENTUPLE};
	
	private Type type;
	
	private String unaryValue = null;
	private String[] binaryValue = new String[2];
	
	public Type getType() {
		return type;
	}
	
	public void setUnaryValue(String s) {
		this.unaryValue = s;
	}
	
	public String getUnaryValue() {
		return unaryValue;
	}
	
	public String getBinaryValue(int i) {
		return binaryValue[i];
	}
	
	public void setBinaryValue(int i,String v) {
		binaryValue[i] = v;
	}
	
	public String toString() {
		if (this.type.equals(Type.UNARYDIFFERENT) || this.type.equals(Type.UNARYEQUALS))
			return "(" + this.getFrom() + ") " + this.type + " " + unaryValue;
		else return "(" + this.getFrom() + ") --" + this.type + "--> (" + this.getTo() + ")"; 
	}

	@Override
	public String getEdgeLabel() {
		if (this.type.equals(Type.UNARYDIFFERENT) || this.type.equals(Type.UNARYEQUALS))
			return "" + this.type + " " + unaryValue;
		else return "" + this.type;
	}

	@Override
	public Object clone() {
		return new SymbolicValueConstraint(this.type);
	}

	@Override
	public boolean isEquivalent(Constraint c) {
		SymbolicValueConstraint vc = (SymbolicValueConstraint)c;
		if (!(this.getType().equals(vc.getType()))) return false;
		if (!((this.getFrom().equals(vc.getFrom()) && this.getTo().equals(vc.getTo())) || (this.getFrom().equals(vc.getTo()) && this.getTo().equals(vc.getFrom()))) ) return false;
		if (!this.getUnaryValue().equals(vc.getUnaryValue())) return false;
		return true;
	}

}
