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
import org.metacsp.throwables.NoSymbolsException;
import org.metacsp.utility.logging.MetaCSPLogging;

public class SymbolicValueConstraint extends MultiBinaryConstraint {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4010342193923812891L;

	private transient Logger logger = MetaCSPLogging.getLogger(this.getClass());
	
	public static enum Type {EQUALS, DIFFERENT, UNARYEQUALS, UNARYDIFFERENT};

	private Type type;
	private boolean[] unaryValue;
	
	public SymbolicValueConstraint(Type type) {
		this.type = type;
	}
	
	public void setUnaryValue(boolean[] unaryValue) {
		this.unaryValue = unaryValue;
	}
		
	@Override
	protected Constraint[] createInternalConstraints(Variable f, Variable t) {
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
		
		if (this.type.equals(Type.DIFFERENT)) {
			BooleanVariable[] scope = new BooleanVariable[internalVarsFrom.length*2];
			String wff = "";
			for (int i = 0; i < internalVarsFrom.length*2; i+=2) {
				scope[i] = ((BooleanVariable)internalVarsFrom[i/2]);
				scope[i+1] = ((BooleanVariable)internalVarsTo[i/2]);
				//wff += ("(w" + (i+1) + " <-> ~w" + (i+2) + ")");
				if (i != 0) wff = "(" + wff + (" ^ (w" + (i+1) + " <-> ~w" + (i+2) + ")") + ")";
				else wff = ("(w" + (i+1) + " <-> ~w" + (i+2) + ")");
			}
			logger.finest("Generated WFF for DIFFERENT constraint: " + wff);
			logger.finest("\tscope: " + Arrays.toString(scope));
			BooleanConstraint[] cons = BooleanConstraint.createBooleanConstraints(scope, wff);
			return cons;
		}
		
		if (this.type.equals(Type.UNARYEQUALS)) {
			Vector<BooleanVariable> scope = new Vector<BooleanVariable>();
			String wff = "";
			int counter = 0;
			for (int i = 0; i < internalVarsFrom.length; i++) {
				if (!unaryValue[i]) {
					scope.add(((BooleanVariable)internalVarsFrom[i]));
					//if (counter != 0) wff += " ^ ";
					//wff += ("(~w" + (++counter) + ")");
					if (counter != 0) wff = "(" + wff + (" ^ (~w" + (++counter) + ")") + ")";
					else wff = ("(~w" + (++counter) + ")");
				}
			}
			logger.finest("Generated WFF for UNARYEQUALS constraint: " + wff);
			logger.finest("\tscope: " + scope);
			BooleanConstraint[] cons = BooleanConstraint.createBooleanConstraints(scope.toArray(new BooleanVariable[scope.size()]), wff);
			return cons;
		}
		
		if (this.type.equals(Type.UNARYDIFFERENT)) {
			Vector<BooleanVariable> scope = new Vector<BooleanVariable>();
			String wff = "";
			int counter = 0;
			for (int i = 0; i < internalVarsFrom.length; i++) {
				if (unaryValue[i]) {
					scope.add(((BooleanVariable)internalVarsFrom[i]));
					//if (counter != 0) wff += " ^ ";
					//wff += ("(~w" + (++counter) + ")");
					if (counter != 0) wff = "(" + wff + (" ^ (~w" + (++counter) + ")") + ")";
					else wff = ("(~w" + (++counter) + ")");
				}
			}
			logger.finest("Generated WFF for UNARYDIFFERENT constraint: " + wff);
			logger.finest("\tscope: " + scope);
			BooleanConstraint[] cons = BooleanConstraint.createBooleanConstraints(scope.toArray(new BooleanVariable[scope.size()]), wff);
			return cons;
		}
		
		return null;
	}

	@Override
	public String getEdgeLabel() {
		if (this.type.equals(Type.UNARYDIFFERENT) || this.type.equals(Type.UNARYEQUALS))
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
	
}
