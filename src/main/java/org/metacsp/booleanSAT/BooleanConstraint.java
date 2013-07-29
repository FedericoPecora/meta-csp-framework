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

package org.metacsp.booleanSAT;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.Map.Entry;

import orbital.logic.imp.Formula;
import orbital.logic.sign.ParseException;
import orbital.logic.sign.Signature;
import orbital.logic.sign.SymbolBase;
import orbital.moon.logic.ClassicalLogic;
import orbital.moon.logic.TokenMgrError;
import orbital.moon.logic.resolution.ClausalSetImpl;
import orbital.moon.logic.resolution.DefaultClausalFactory;
import orbital.moon.logic.resolution.IndexedClauseImpl;

import org.sat4j.core.VecInt;

import org.metacsp.utility.logging.MetaCSPLogging;

import org.metacsp.framework.Constraint;

/**
 * Class for representing disjunctive Boolean clauses, e.g., (x1 v x2 v ~x3).
 * Note that {@link BooleanConstraint}s can be instantiated with a factory method from non-CNF formulas
 * (see {@link #createBooleanConstraints(BooleanVariable[], String)}.
 * 
 * @author Federico Pecora
 *
 */
public class BooleanConstraint extends Constraint {
	
	private boolean[] positive;
	private static ClassicalLogic logic = new ClassicalLogic();
	
	/**
	 * A factory method for creating {@link BooleanConstraint}s from an arbitrary
	 * propositional logic formula (wff).  Allowed connectives are
	 * {'^' (and), 'v' (or), '~' (not), '->' (implies), '<->' (iff)}.
	 * Atoms in the formula should be named "xN" where
	 * x is in [a-z] and N is in {1..<code>scope.length</code>}. The signature
	 * of the formula must contain all and only the variables in the scope.
	 * 
	 * The conversion to CNF is provided by a clause translation algorithm
	 * implemented in the Orbital library (see <a href="http://symbolaris.com/orbital/">symbolaris.com/orbital</a>)
	 * ["David A. Plaisted & Steven Greenbaum. A structure-preserving clause form translation. J. Symb. Comput., Academic Press, Inc., 1986, 2, 293-304.", "Rolf Socher-Ambrosius. Boolean algebra admits no convergent term rewriting system, Springer Lecture Notes in Computer Science 488, RTA '91."].
	 * @param scope The {@link BooleanVariable}s referred to in the formula.
	 * @param wff An arbitrary propositional logic formula.
	 * @return One or more {@link BooleanConstraint}s representing the given formula in CNF.
	 */
	public static BooleanConstraint[] createBooleanConstraints(BooleanVariable[] scope, String wff) {
		try {
			wff = wff.replace('^', '&');
			wff = wff.replace('v', '|');
			MetaCSPLogging.getLogger(BooleanConstraint.class).finest("Converting WFF: " + wff);
			Formula f = (Formula)logic.createExpression(wff);
			if (f.getSignature().size() != scope.length) throw new Error("WFF must include all and only the variables in the scope");
			
			//get CNF from wff
			DefaultClausalFactory cf = new DefaultClausalFactory();
			ClausalSetImpl cs = (ClausalSetImpl)cf.asClausalSet(f);
			Formula newF = cs.toFormula();
			
			//get formula signature
			Signature newSig = newF.getSignature();
			@SuppressWarnings("unchecked")
			Iterator<SymbolBase> itSig = newSig.iterator();
			Vector<Formula> trueLits = new Vector<Formula>();
			Vector<Formula> falseLits = new Vector<Formula>();
			while (itSig.hasNext()) {
				SymbolBase var = itSig.next();
				Formula trueF = (Formula)logic.createExpression(""+var);
				Formula falseF = (Formula)logic.createExpression("~"+var);
				trueLits.add(trueF);
				falseLits.add(falseF);
			}
			
			//get literals in clauses
			@SuppressWarnings("unchecked")
			Iterator<IndexedClauseImpl> itClauses = cs.iterator();
			Vector<BooleanConstraint> cons = new Vector<BooleanConstraint>();
			while (itClauses.hasNext()) {
				HashMap<BooleanVariable,Boolean> newClause = new HashMap<BooleanVariable, Boolean>();
				IndexedClauseImpl cl = (IndexedClauseImpl)itClauses.next();
				try {
					for (Formula lit : trueLits) {
						if (!cl.getUnifiables(lit).isEmpty()) {
							BooleanVariable bv = scope[Integer.parseInt(lit.toString().substring(1))-1];
							newClause.put(bv, true);
						}
					}
					for (Formula lit : falseLits) {
						if (!cl.getUnifiables(lit).isEmpty()) {
							BooleanVariable bv = scope[Integer.parseInt(lit.toString().substring(2))-1];
							if (newClause.containsKey(bv)) newClause.remove(bv);
							else newClause.put(bv, false);
						}
					}
				}
				catch (ArrayIndexOutOfBoundsException e) { throw new Error("Variable numbering in WFF must be within scope"); }
				catch (NumberFormatException e) { throw new Error("Variables in WFF must be in the format [a-z][1-MAXINT]"); }			
				if (newClause.size() > 0) {
					BooleanVariable[] relevantVars = new BooleanVariable[newClause.size()];
					boolean[] positive = new boolean[newClause.size()];
					int counter = 0;
					for (Entry<BooleanVariable, Boolean> ent : newClause.entrySet()) {
						relevantVars[counter] = ent.getKey();
						positive[counter++] = ent.getValue();
					}
					BooleanConstraint bc = new BooleanConstraint(relevantVars, positive);
					MetaCSPLogging.getLogger(BooleanConstraint.class).finest("Created constraint " + bc);
					boolean subsumed = false;
					for (BooleanConstraint otherBc : cons) {
						if (bc.isEquivalent(otherBc)) {
							subsumed = true;
							break;
						}
					}
					if (!subsumed) cons.add(bc);
				}
			}
			MetaCSPLogging.getLogger(BooleanConstraint.class).finest("CNF(WFF): " + cons);
			return cons.toArray(new BooleanConstraint[cons.size()]);
		}
		catch (ParseException e) { throw new Error("Malformed BooleanConstraint - allowed logical connectives:\n\t^ : AND\n\tv : OR\n\t-> : implication\n\t<-> : iff\n\t~ : NOT"); }
		catch (TokenMgrError e) { throw new Error("Malformed BooleanConstraint - allowed logical connectives:\n\t^ : AND\n\tv : OR\n\t-> : implication\n\t<-> : iff\n\t~ : NOT"); }
	}

	/**
	 * Create a {@link BooleanConstraint} given its scope and a specification
	 * of the polarity of literals.
	 * @param scope The scope of the clause.
	 * @param positive Polarity of literals.
	 */
	public BooleanConstraint(BooleanVariable[] scope, boolean[] positive) {
		this.positive = positive;
		this.setScope(scope);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 8863340939973901776L;

	public VecInt getLiterals() {
		int[] literals = new int[this.getScope().length];
		for (int i = 0; i < this.getScope().length; i++) {
			if (positive[i]) literals[i] = this.getScope()[i].getID();
			else literals[i] = -this.getScope()[i].getID();
		}
		return new VecInt(literals);
	}
	
	@Override
	public String toString() {
		String ret = "(";
		for (int i = 0; i < this.getScope().length; i++) {
			BooleanVariable bv = (BooleanVariable)this.getScope()[i];
			if (!this.positive[i]) ret += "~x" + bv.getID();
			else ret += "x" + bv.getID();
			if (i == this.getScope().length-1) ret += ")";
			else ret += " v ";
		}
		return ret;
	}

	@Override
	public String getEdgeLabel() {
		return this.toString();
	}

	@Override
	public Object clone() {
		return new BooleanConstraint((BooleanVariable[])this.getScope(), this.positive);
	}

	@Override
	public boolean isEquivalent(Constraint c) {
		if (!(c instanceof BooleanConstraint)) return false;
		if (this.getScope().length != c.getScope().length) return false;
		for (int i = 0; i < this.getScope().length; i++) {
			if (!this.getScope()[i].equals(c.getScope()[i])) return false;
			if (this.positive[i] && !((BooleanConstraint)c).positive[i]) return false;
			if (!this.positive[i] && ((BooleanConstraint)c).positive[i]) return false;
		}
		return true;
	}

}
