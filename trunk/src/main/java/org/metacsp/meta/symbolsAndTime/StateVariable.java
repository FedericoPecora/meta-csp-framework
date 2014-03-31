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
package org.metacsp.meta.symbolsAndTime;

import java.util.Arrays;
import java.util.Vector;

import org.metacsp.multi.activity.Activity;
import org.metacsp.time.Interval;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.VariableOrderingH;
import org.metacsp.framework.meta.MetaConstraintSolver;

public class StateVariable extends Schedulable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8378253444408308230L;
	private Interval[][] reachability = null;
	private String[] states = null;
	
	public StateVariable(VariableOrderingH varOH, ValueOrderingH valOH,
			MetaConstraintSolver metaCS, String[] allowedStates) {
		super(varOH, valOH);
		this.setPeakCollectionStrategy(PEAKCOLLECTION.BINARY);
		//this.setPeakCollectionStrategy(PEAKCOLLECTION.SAMPLING);
		setAllowedStates(allowedStates);
		
	}

//	@Override
//	public boolean isConflicting(Activity[] peak) {
//		Vector<String> intersection = new Vector<String>(Arrays.asList(((SymbolicDomain)peak[0].getSymbolicVariable().getDomain()).getSymbols()));
//		for (int i = 1; i < peak.length; i++) {
//			intersection.retainAll(Arrays.asList(((SymbolicDomain)peak[i].getSymbolicVariable().getDomain()).getSymbols()));	
//		}		
//		return intersection.isEmpty();
//	}

	@Override
	public boolean isConflicting(Activity[] peak) {		
		if (peak.length != 2) return false;
		peak[0].getSymbolicVariable().getDomain();
//		Vector<String> intersection = new Vector<String>(Arrays.asList(((SymbolicDomain)peak[0].getSymbolicVariable().getDomain()).getSymbols()));
//		intersection.retainAll(Arrays.asList(((SymbolicDomain)peak[1].getSymbolicVariable().getDomain()).getSymbols()));
		Vector<String> intersection = new Vector<String>(Arrays.asList(peak[0].getSymbolicVariable().getSymbols()));
		intersection.retainAll(Arrays.asList(peak[1].getSymbolicVariable().getSymbols()));
		return intersection.isEmpty(); // if the intersection is null these variables do not share symbols and therefore can not co-exist
	}

	@Override
	public void draw(ConstraintNetwork network) {
		// TODO Auto-generated method stub

	}
	
	private void setAllowedStates(String[] st) {
		this.states = st;
		Arrays.sort(states);
		reachability = new Interval[states.length][states.length];
		for (int i = 0; i < states.length; i++) {
			for (int j = 0; j < states.length; j++) {
				reachability[i][j] = null;
			}
		}
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getEdgeLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object clone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEquivalent(Constraint c) {
		// TODO Auto-generated method stub
		return false;
	}

	public String[] getStates() {
		return states;
	}

	public void setStates(String[] states) {
		this.states = states;
	}

	@Override
	public ConstraintSolver getGroundSolver() {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
