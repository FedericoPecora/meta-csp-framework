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

import org.metacsp.meta.symbolsAndTime.Schedulable;
import org.metacsp.multi.activity.Activity;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.VariableOrderingH;



// For the moment just look at that like a capacity with associated a set of activities:
// this class comes from Schedulable that implements sophisticated methods to 
// detect peaks in resource consumption
public class SimpleReusableResource extends Schedulable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7860488618227112837L;
	private int capacity;
	private SimpleDomain rd;
	private String name;
	
	public SimpleReusableResource(VariableOrderingH varOH, ValueOrderingH valOH, int capacity, SimpleDomain rd, String name) {
		super(varOH, valOH);
		this.capacity = capacity;
		this.rd = rd;
		this.name = name;
	}
	

	@Override
	public boolean isConflicting(Activity[] peak) {
		int sum = 0;
		for (Activity act : peak) {
			sum += rd.getResourceUsageLevel(this, act);
			if (sum > capacity) return true;
		}
		return false;
	}

	@Override
	public void draw(ConstraintNetwork network) {
		// TODO Auto-generated method stub
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "SimpleReusableResource " + name + ", capacity = " + capacity;
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


	@Override
	public ConstraintSolver getGroundSolver() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public int getCapacity(){
		return capacity;
	}
	
}
