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
package org.metacsp.examples.multi;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.multi.fuzzySetActivity.FuzzySetActivity;
import org.metacsp.multi.fuzzySetActivity.FuzzySetActivityNetworkSolver;
import org.metacsp.multi.symbols.SymbolicValueConstraint;

public class TestFuzzySetActivityNetworkSolver {
	
	
	public static void main(String[] args) {
		FuzzySetActivityNetworkSolver solver = new FuzzySetActivityNetworkSolver(0,10000);

		FuzzySetActivity act1 = (FuzzySetActivity)solver.createVariable();
		act1.setDomain(new String[] {"A"}, new double[] {1.0});
		
		FuzzySetActivity act2 = (FuzzySetActivity)solver.createVariable();
		act2.setDomain(new String[] {"A"}, new double[] {1.0});

		ConstraintNetwork.draw(solver.getConstraintNetwork());
		
		SymbolicValueConstraint con1 = new SymbolicValueConstraint(SymbolicValueConstraint.Type.DIFFERENT);
		con1.setFrom(act1);
		con1.setTo(act2);
		
		Constraint[] cons = new Constraint[]{con1};
		solver.addConstraints(cons);
				
//		System.out.println(solver.getDescription());
//		System.out.println(act1.getDescription());

		System.out.println("---------------------------------");
		System.out.println("Value Possibility: " + solver.getValueConsistency());
		System.out.println("---------------------------------");		

		
	}

}
