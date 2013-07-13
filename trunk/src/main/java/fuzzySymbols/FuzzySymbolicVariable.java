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
package fuzzySymbols;

import java.util.HashMap;

import framework.ConstraintSolver;
import framework.Domain;
import framework.Variable;

public class FuzzySymbolicVariable extends Variable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6872340540095116373L;
	private FuzzySymbolicDomain dom;
	private FuzzySymbolicDomain backupDom;
	
	protected FuzzySymbolicVariable(ConstraintSolver cs, int id) {
		super(cs, id);
	}

	@Override
	public Domain getDomain() {
		return this.dom;
	}
	
	@Override
	public void setDomain(Domain d) {
		if (d instanceof FuzzySymbolicDomain) {
			this.dom = (FuzzySymbolicDomain)d;
			this.backupDom = (FuzzySymbolicDomain)((FuzzySymbolicDomain)d).clone();
		}
	}
	
	public void setDomain(String[] symbols, double[] possibilities) {
		this.dom = new FuzzySymbolicDomain(this,symbols,possibilities);
		this.backupDom = (FuzzySymbolicDomain)dom.clone();
	}

	public void setDomain(FuzzySymbolicDomain s) {
		this.dom = s;
		this.backupDom = (FuzzySymbolicDomain)dom.clone();
	}
	
	
	public HashMap<String, Double> getSymbolsAndPossibilities() {
		return dom.getSymbolsAndPossibilities();
	}
	

	public String toString() {
		//return "Variable " + this.getID() + ": " + this.dom.toString();
		return this.getClass().getSimpleName() + " " + this.id + " " + this.getDomain();
	}

	@Override
	public int compareTo(Variable o) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void resetDomain() {
		if (this.backupDom != null) this.dom = (FuzzySymbolicDomain)backupDom.clone();
	}


}
