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
package org.metacsp.meta.simplePlanner;

import java.util.Arrays;

import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.throwables.InvalidActivityException;

public class Axiom {

	protected String[] requirementActivities;
	protected AllenIntervalConstraint[][] extraConstraints;
	
	public Axiom(String[] requirementActivities) {
		this.requirementActivities = requirementActivities;
		this.extraConstraints = new AllenIntervalConstraint[requirementActivities.length+1][requirementActivities.length+1];
	}
	
	public void addConstraint(AllenIntervalConstraint c, int from, int to) {
		extraConstraints[from][to] = c;
	}
	
	public AllenIntervalConstraint[][] getExtraConstraints() {
		return this.extraConstraints;
	}
	
	public String[] getRequirementActivities() {
		return requirementActivities;
	}

	public String toString() {
		String ret = "";
		
		String extraCons = "";
		if (extraConstraints != null) {
			extraCons += "";
			for (int i = 1; i < extraConstraints.length; i++) {
				for (int j = 1; j < extraConstraints[i].length; j++) {
					if (extraConstraints[i][j] != null) {
						extraCons += "\n" + requirementActivities[i-1];
						extraCons += " --" + Arrays.toString(extraConstraints[i][j].getTypes()) + " " + Arrays.toString(extraConstraints[i][j].getBounds()) + "--> ";
						extraCons += requirementActivities[j-1];
					}
				}
			}
		}
		if (!extraCons.trim().equals("")) ret += extraCons;
		
		return ret;
	}
	
	
}
