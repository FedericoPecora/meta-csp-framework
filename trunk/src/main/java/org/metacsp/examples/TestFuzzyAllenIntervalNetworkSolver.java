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
package org.metacsp.examples;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.fuzzyAllenInterval.FuzzyAllenIntervalConstraint;
import org.metacsp.fuzzyAllenInterval.FuzzyAllenIntervalNetworkSolver;
import org.metacsp.time.qualitative.SimpleAllenInterval;

public class TestFuzzyAllenIntervalNetworkSolver {
	
	public static void main(String[] args) {
		FuzzyAllenIntervalNetworkSolver solver = new FuzzyAllenIntervalNetworkSolver();
		SimpleAllenInterval act0 = (SimpleAllenInterval)solver.createVariable();
		SimpleAllenInterval act1 = (SimpleAllenInterval)solver.createVariable();
		SimpleAllenInterval act2 = (SimpleAllenInterval)solver.createVariable();

		ConstraintNetwork.draw(solver.getConstraintNetwork());
				
		FuzzyAllenIntervalConstraint con0 = new FuzzyAllenIntervalConstraint(FuzzyAllenIntervalConstraint.Type.After);
		con0.setFrom(act0);
		con0.setTo(act1);
		//if (!solver.addConstraint(con0)) System.out.println("Failed to add constraint " + con0);
		
		FuzzyAllenIntervalConstraint con1 = new FuzzyAllenIntervalConstraint(FuzzyAllenIntervalConstraint.Type.Contains);
		con1.setFrom(act1);
		con1.setTo(act2);
		//if (!solver.addConstraint(con1)) System.out.println("Failed to add constraint " + con1);

		FuzzyAllenIntervalConstraint con3 = new FuzzyAllenIntervalConstraint(FuzzyAllenIntervalConstraint.Type.Meets);
		con3.setFrom(act2);
		con3.setTo(act0);
		//if (!solver.addConstraint(con3)) System.out.println("Failed to add constraint " + con2);

		
		FuzzyAllenIntervalConstraint[] allConstraints = {con0,con1,con3};
		if (!solver.addConstraints(allConstraints)) {
			System.out.println("Failed to add constraints!");
			System.exit(0);
		}
		
		System.out.println(solver.getPosibilityDegree());
		
		
		
	}
	

}
