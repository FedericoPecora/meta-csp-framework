/*******************************************************************************
 * Copyright (c) 2010-2013 Federico Pecora <federico.pecora@oru.se>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
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
