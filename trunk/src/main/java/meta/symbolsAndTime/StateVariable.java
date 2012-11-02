package meta.symbolsAndTime;

import java.util.Arrays;
import java.util.Vector;

import multi.activity.Activity;
import symbols.SymbolicDomain;
import time.Interval;
import framework.Constraint;
import framework.ConstraintNetwork;
import framework.ValueOrderingH;
import framework.VariableOrderingH;
import framework.meta.MetaConstraintSolver;

public class StateVariable extends Schedulable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8378253444408308230L;
	private Interval[][] reachability = null;
	private String[] states = null;
	
	public StateVariable(VariableOrderingH varOH, ValueOrderingH valOH,
			MetaConstraintSolver metaCS, SymbolicDomain allowedStates) {
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
		Vector<String> intersection = new Vector<String>(Arrays.asList(((SymbolicDomain)peak[0].getSymbolicVariable().getDomain()).getSymbols()));
		intersection.retainAll(Arrays.asList(((SymbolicDomain)peak[1].getSymbolicVariable().getDomain()).getSymbols()));
		return intersection.isEmpty();
	}

	@Override
	public void draw(ConstraintNetwork network) {
		// TODO Auto-generated method stub

	}
	
	private void setAllowedStates(SymbolicDomain st) {
		this.states = st.getSymbols();
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
	
	

}
