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
package org.metacsp.onLineMonitoring;

import org.metacsp.multi.fuzzyActivity.FuzzyActivity;
import org.metacsp.multi.fuzzyActivity.FuzzyActivityNetworkSolver;
import org.metacsp.fuzzyAllenInterval.FuzzyAllenIntervalConstraint;


public abstract class Sensor {
	
	private String name;
	private String[] states;
	private FuzzyActivityNetworkSolver solver;
	private double[] currentPossibilities;
	private FuzzyActivity currentAct = null;
	
	/**
	 * @return the currentAct
	 */
	public FuzzyActivity getCurrentAct() {
		return currentAct;
	}

	/**
	 * @param currentAct the currentAct to set
	 */
	public void setCurrentAct(FuzzyActivity currentAct) {
		this.currentAct = currentAct;
	}

	public Sensor(String name, String... states) {
		this.setName(name);
		this.setStates(states);
		currentPossibilities = new double[states.length];
		for (int i = 0; i < currentPossibilities.length; i++) currentPossibilities[i] = 0.0;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setStates(String[] states) {
		this.states = states;
	}

	public String[] getStates() {
		return states;
	}

	public void setSolver(FuzzyActivityNetworkSolver solver) {
		this.solver = solver;
	}

	public FuzzyActivityNetworkSolver getSolver() {
		return solver;
	}

	public FuzzyAllenIntervalConstraint setCurrentPossibilities(double[] possibilities) {
		
		FuzzyAllenIntervalConstraint tcon = null;
		
		boolean diff = false;
		for (int i = 0; i < currentPossibilities.length; i++) {
			if (currentPossibilities[i] != possibilities[i]) {
				diff = true;
				break;
			}
		}

		//Case: currentPossibilities have changed
		if (diff) {
			FuzzyActivity act = (FuzzyActivity)solver.createVariable(this.getName());
			act.setDomain(this.states, possibilities);
			this.currentPossibilities = possibilities;
			if (currentAct != null) {
				//tcon = new FuzzyAllenIntervalConstraint(solver, Type.Before, Type.Meets);
				tcon = new FuzzyAllenIntervalConstraint(FuzzyAllenIntervalConstraint.Type.Meets);
				tcon.setFrom(currentAct);
				tcon.setTo(act);
				
			}
			currentAct = act;
		 }
		
		return tcon;
	}

	public double[] getCurrentPossibilities() {
		return currentPossibilities;
	}
	

}
