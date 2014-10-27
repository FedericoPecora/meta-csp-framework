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
package org.metacsp.multi.symbols;

import java.util.Arrays;
import java.util.Vector;
import java.util.logging.Logger;

import org.metacsp.booleanSAT.BooleanConstraint;
import org.metacsp.booleanSAT.BooleanVariable;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiBinaryConstraint;
import org.metacsp.framework.multi.MultiConstraint;
import org.metacsp.throwables.NoSymbolsException;
import org.metacsp.throwables.WrongSymbolListException;
import org.metacsp.utility.logging.MetaCSPLogging;

public class SymbolicValueConstraint extends MultiConstraint {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4010342193923812891L;

	private transient Logger logger = MetaCSPLogging.getLogger(this.getClass());
	
	public static enum Type {EQUALS, DIFFERENT, VALUEEQUALS, VALUEDIFFERENT, CONTAINS};

	private Type type;
	private boolean[] unaryValue;
	
	public SymbolicValueConstraint(Type type) {
		this.type = type;
	}
	
	public void setUnaryValue(boolean[] unaryValue) {
		this.unaryValue = unaryValue;
	}

	
	private Constraint[] createInternalBinaryConstraints(Variable f, Variable t) {
		if (!(f instanceof SymbolicVariable) || !(t instanceof SymbolicVariable)) return null;
		SymbolicVariable svFrom = ((SymbolicVariable)f);
		Variable[] internalVarsFrom = svFrom.getInternalVariables();
		if (((SymbolicVariableConstraintSolver)svFrom.getConstraintSolver()).getSymbols().length == 0) 
			throw new NoSymbolsException(svFrom);
		SymbolicVariable svTo = ((SymbolicVariable)t);
		Variable[] internalVarsTo = svTo.getInternalVariables();
		
		if (this.type.equals(Type.EQUALS)) {
			BooleanVariable[] scope = new BooleanVariable[internalVarsFrom.length*2];
			String wff = "";
			for (int i = 0; i < internalVarsFrom.length*2; i+=2) {
				scope[i] = ((BooleanVariable)internalVarsFrom[i/2]);
				scope[i+1] = ((BooleanVariable)internalVarsTo[i/2]);
				//wff += ("(w" + (i+1) + " <-> w" + (i+2) + ")");
				if (i != 0) wff = "(" + wff + (" ^ (w" + (i+1) + " <-> w" + (i+2) + ")") + ")";
				else wff = ("(w" + (i+1) + " <-> w" + (i+2) + ")");
			}
			logger.finest("Generated WFF for EQUALS constraint: " + wff);
			logger.finest("\tscope: " + Arrays.toString(scope));
			BooleanConstraint[] cons = BooleanConstraint.createBooleanConstraints(scope, wff);
			return cons;
		}
		
		else if (this.type.equals(Type.CONTAINS)) {
			BooleanVariable[] scope = new BooleanVariable[internalVarsFrom.length*2];
			String wff = "";
			// f1 f2 f3
			// t1 t2 t3
			// t1 -> f1, t2 -> f2, t3 -> f3
			for (int i = 0; i < internalVarsFrom.length*2; i+=2) {
				scope[i] = ((BooleanVariable)internalVarsTo[i/2]);
				scope[i+1] = ((BooleanVariable)internalVarsFrom[i/2]);
				//wff += ("(~w" + (i+1) + " v ~w" + (i+2) + ")");
				if (i == 0) wff = ("(w" + (i+1) + " -> w" + (i+2) + ")");
				else wff = "(" + wff + (" ^ (w" + (i+1) + " -> w" + (i+2) + ")") + ")";
			}
			logger.finest("Generated WFF for CONTAINS constraint: " + wff);
			logger.finest("\tscope: " + Arrays.toString(scope));
			BooleanConstraint[] cons = BooleanConstraint.createBooleanConstraints(scope, wff);
			return cons;
		}

		
//		if (this.type.equals(Type.DIFFERENT)) {
//			BooleanVariable[] scope = new BooleanVariable[internalVarsFrom.length*2];
//			String wff = "";
//			for (int i = 0; i < internalVarsFrom.length*2; i+=2) {
//				scope[i] = ((BooleanVariable)internalVarsFrom[i/2]);
//				scope[i+1] = ((BooleanVariable)internalVarsTo[i/2]);
//				//wff += ("(w" + (i+1) + " <-> ~w" + (i+2) + ")");
//				if (i != 0) wff = "(" + wff + (" ^ (w" + (i+1) + " <-> ~w" + (i+2) + ")") + ")";
//				else wff = ("(w" + (i+1) + " <-> ~w" + (i+2) + ")");
//			}
//			logger.finest("Generated WFF for DIFFERENT constraint: " + wff);
//			logger.finest("\tscope: " + Arrays.toString(scope));
//			BooleanConstraint[] cons = BooleanConstraint.createBooleanConstraints(scope, wff);
//			return cons;
//		}
		
		if (this.type.equals(Type.DIFFERENT)) {
			BooleanVariable[] scope = new BooleanVariable[internalVarsFrom.length*2];
			String wff = "";
			for (int i = 0; i < internalVarsFrom.length*2; i+=2) {
				scope[i] = ((BooleanVariable)internalVarsFrom[i/2]);
				scope[i+1] = ((BooleanVariable)internalVarsTo[i/2]);
				//wff += ("(~w" + (i+1) + " v ~w" + (i+2) + ")");
				if (i != 0) wff = "(" + wff + (" ^ (~w" + (i+1) + " v ~w" + (i+2) + ")") + ")";
				else wff = ("(~w" + (i+1) + " v ~w" + (i+2) + ")");
			}
			logger.finest("Generated WFF for DIFFERENT constraint: " + wff);
			logger.finest("\tscope: " + Arrays.toString(scope));
			BooleanConstraint[] cons = BooleanConstraint.createBooleanConstraints(scope, wff);
			return cons;
		}

		
		if (this.type.equals(Type.VALUEEQUALS)) {
			Vector<BooleanVariable> scope = new Vector<BooleanVariable>();
			String wff = "";
			int counter = 0;
			boolean allTrue = true;
			for (int i = 0; i < internalVarsFrom.length; i++) {
				try {
					if (!unaryValue[i]) {
						allTrue = false;
						scope.add(((BooleanVariable)internalVarsFrom[i]));
						//if (counter != 0) wff += " ^ ";
						//wff += ("(~w" + (++counter) + ")");
						if (counter != 0) wff = "(" + wff + (" ^ (~w" + (++counter) + ")") + ")";
						else wff = ("(~w" + (++counter) + ")");
					}
				}
				catch (java.lang.ArrayIndexOutOfBoundsException e) {
					throw new WrongSymbolListException(unaryValue.length,internalVarsFrom.length);
				}

			}
			if (allTrue) {
				logger.finest("Ignored trivial VALUEEQUALS constraint (all values true)");
				return new BooleanConstraint[0];
			}
			logger.finest("Generated WFF for VALUEEQUALS constraint: " + wff);
			logger.finest("\tscope: " + scope);
			BooleanConstraint[] cons = BooleanConstraint.createBooleanConstraints(scope.toArray(new BooleanVariable[scope.size()]), wff);
			return cons;
		}
		
		if (this.type.equals(Type.VALUEDIFFERENT)) {
			Vector<BooleanVariable> scope = new Vector<BooleanVariable>();
			String wff = "";
			int counter = 0;
			boolean allFalse = true;
			for (int i = 0; i < internalVarsFrom.length; i++) {
				try {
					if (unaryValue[i]) {
						allFalse = false;
						scope.add(((BooleanVariable)internalVarsFrom[i]));
						//if (counter != 0) wff += " ^ ";
						//wff += ("(~w" + (++counter) + ")");
						if (counter != 0) wff = "(" + wff + (" ^ (~w" + (++counter) + ")") + ")";
						else wff = ("(~w" + (++counter) + ")");
					}
				}
				catch (java.lang.ArrayIndexOutOfBoundsException e) {
					throw new WrongSymbolListException(unaryValue.length,internalVarsFrom.length);
				}
			}
			if (allFalse) {
				logger.finest("Ignored trivial VALUEDIFFERENT constraint (all values false)");
				return new BooleanConstraint[0];
			}
			logger.finest("Generated WFF for VALUEDIFFERENT constraint: " + wff);
			logger.finest("\tscope: " + scope);
			BooleanConstraint[] cons = BooleanConstraint.createBooleanConstraints(scope.toArray(new BooleanVariable[scope.size()]), wff);
			return cons;
		}
		
		return null;
	}


	@Override
	public String getEdgeLabel() {
		if (this.type.equals(Type.VALUEDIFFERENT) || this.type.equals(Type.VALUEEQUALS))
			return "" + this.type + " " + Arrays.toString(unaryValue);
		return "" + this.type;
	}

	@Override
	public Object clone() {
		SymbolicValueConstraint res = new SymbolicValueConstraint(this.type);
		if(this.unaryValue != null)
			res.setUnaryValue(this.unaryValue);
		return res;
	}


	@Override
	public boolean isEquivalent(Constraint c) {
		return false;
	}

	public Object getType() {
		return this.type;
	}

	@Override
	protected Constraint[] createInternalConstraints(Variable[] variables) {
		if (variables == null || variables.length == 0) return null;
		for (Variable var : variables) if (!(var instanceof SymbolicVariable)) return null;
		Vector<Constraint> cons = new Vector<Constraint>();
		if (this.type.equals(Type.EQUALS) || this.type.equals(Type.DIFFERENT) || this.type.equals(Type.CONTAINS)) {
			for (int i = 0; i < variables.length; i++) {
				for (int j = i+1; j < variables.length; j++) {
					for (Constraint con : this.createInternalBinaryConstraints(variables[i], variables[j])) cons.add(con);
				}			
			}
		}
		else {
			for (int i = 0; i < variables.length; i++) {
				for (Constraint con : this.createInternalBinaryConstraints(variables[i], variables[i])) cons.add(con);
			}
		}
		return cons.toArray(new Constraint[cons.size()]);
	}

	@Override
	public String toString() {
		return this.getEdgeLabel() + " (" + (Arrays.toString(this.getScope()) + ")");
	}
	
	public void setFrom(Variable f) {
		if (this.scope == null) this.scope = new Variable[2];
		this.scope[0] = f;
	}

	public void setTo(Variable t) {
		if (this.scope == null) this.scope = new Variable[2];
		this.scope[1] = t;
	}

}
