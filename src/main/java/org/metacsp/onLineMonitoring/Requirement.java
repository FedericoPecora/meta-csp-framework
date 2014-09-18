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

import org.metacsp.fuzzyAllenInterval.FuzzyAllenIntervalConstraint;
import org.metacsp.multi.symbols.SymbolicValueConstraint;

public class Requirement {
	
	private Sensor sensor;
	private SymbolicValueConstraint.Type vCons;
	private FuzzyAllenIntervalConstraint.Type tCons;
	private double[] possibilities;
	
	public Requirement(Sensor sensor, double[] possibilities, SymbolicValueConstraint.Type vCons, 
			FuzzyAllenIntervalConstraint.Type tCons) {
		this.setSensor(sensor);
		this.possibilities = possibilities;
		this.vCons = vCons;
		this.tCons = tCons;
	}
	
	public Requirement() {
		// TODO Auto-generated constructor stub
	}

	public double[] getPossibilities() { return possibilities; }
	/**
	 * @return the vCons
	 */
	public SymbolicValueConstraint.Type getvCons() {
		return vCons;
	}

	/**
	 * @param vCons the vCons to set
	 */
	public void setvCons(SymbolicValueConstraint.Type vCons) {
		this.vCons = vCons;
	}

	/**
	 * @return the tCons
	 */
	public FuzzyAllenIntervalConstraint.Type gettCons() {
		return tCons;
	}

	/**
	 * @param tCons the tCons to set
	 */
	public void settCons(FuzzyAllenIntervalConstraint.Type tCons) {
		this.tCons = tCons;
	}

	public void setSensor(Sensor sensor) {
		this.sensor = sensor;
	}

	public Sensor getSensor() {
		return sensor;
	}
	
	public String toString() {
		String state = "";
		for (int i = 0; i < this.getPossibilities().length; i++) {
			if (this.getPossibilities()[i] == 1.0) {
				state = this.getSensor().getStates()[i];
				break;
			}
		}
		return "" + this.getSensor().getName() + " {" + this.getvCons() + "," + this.gettCons() + "} " + state;
	}
	
}
