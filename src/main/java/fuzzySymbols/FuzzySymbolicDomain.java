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
import java.util.Vector;

import throwables.PossibilityDegreeMismathcException;
import framework.Domain;

/**
 * Class for representing fuzzy sets (domains of {@link FuzzySymbolicVariable}s).
 * 
 * @author Masoumeh Mansouri
 *
 */
public class FuzzySymbolicDomain extends Domain implements Cloneable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4160885840955905514L;
	
	private HashMap<String, Double> theDomain = new HashMap<String, Double>();
	//private String[] symbols;
	//private double[] possibilities;
		
	public FuzzySymbolicDomain(FuzzySymbolicVariable v, String[] symbols, double[] possibilities) {
		super(v);
		if (possibilities.length != symbols.length) {
			try {
				throw new PossibilityDegreeMismathcException(this,possibilities);
			} catch (PossibilityDegreeMismathcException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		for (int i = 0; i < symbols.length; i++) {
			theDomain.put(symbols[i], possibilities[i]);
		}
		
		//this.symbols = symbols;
		//this.possibilities = possibilities;
	}
	
	public FuzzySymbolicDomain(FuzzySymbolicVariable v, String[] symbols) {
		super(v);
		
		for (int i = 0; i < symbols.length; i++) {
			theDomain.put(symbols[i], 0.0);
		}
		
		/*
		this.symbols = symbols;
		this.possibilities = new double[symbols.length];
		for (int i = 0; i < symbols.length; i++) possibilities[i] = 0.0;
		*/
	}
	
	public String[] getSymbols() {
		
		Vector<String> ret = new Vector<String>();
		for (String s : theDomain.keySet()) ret.add(s);
		return ret.toArray(new String[ret.size()]);
		
		//return this.symbols;
	}
	
	public double[] getPossibilityDegrees() {
		
		int size = theDomain.keySet().size();
		double[] ret = new double[size];
		int i = 0;
		for (Double d : theDomain.values()) ret[i++] = d;
		return ret;
		
		//return this.possibilities;
	}
	
	
	public HashMap<String, Double> getSymbolsAndPossibilities() {
		return theDomain;
	}
	
	
	public String toString() {
		return theDomain.keySet() + " Poss: " + theDomain.values();
		//return Arrays.toString(this.symbols) + " Poss: " + Arrays.toString(this.possibilities);
	}

	@Override
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof FuzzySymbolicDomain)) return false;
		FuzzySymbolicDomain od = (FuzzySymbolicDomain)o;
		String[] sA = this.getSymbols();
		String[] sB = od.getSymbols();
		double[] pA = this.getPossibilityDegrees();
		double[] pB = od.getPossibilityDegrees();
		if (sA.length != sB.length) return false;
		for (int i = 0; i < sA.length; i++) {
			if (!sA[i].equals(sB[i]) || Double.compare(pA[i], pB[i]) != 0) return false;
		}
		return true;
	}
	
	/*
	public double getPossibility(String symbol) throws SymbolNotFoundException {
		for (int i = 0; i < this.symbols.length; i++) {
			if (symbols[i].equals(symbol)) return possibilities[i];
		}
		throw new SymbolNotFoundException(this, symbol);
	}
	
	public void setPossibility(String symbol, double possibility) throws SymbolNotFoundException {
		boolean found = false;
		for (int i = 0; i < this.symbols.length; i++) {
			if (symbols[i].equals(symbol)) {
				found = true;
				possibilities[i] = possibility;
				break;
			}
		}
		if (!found) throw new SymbolNotFoundException(this, symbol);
	}
	*/
	
	public Object clone() {
		FuzzySymbolicDomain clone = new FuzzySymbolicDomain((FuzzySymbolicVariable) this.myVariable, this.getSymbols(), this.getPossibilityDegrees());
		return clone;
	}
	
}
