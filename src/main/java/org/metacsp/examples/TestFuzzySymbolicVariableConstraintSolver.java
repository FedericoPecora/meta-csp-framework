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
import org.metacsp.framework.Variable;
import org.metacsp.fuzzySymbols.FuzzySymbolicVariable;
import org.metacsp.fuzzySymbols.FuzzySymbolicVariableConstraintSolver;
import org.metacsp.multi.symbols.SymbolicValueConstraint;

public class TestFuzzySymbolicVariableConstraintSolver {
	
	public static void main(String[] args) {
		FuzzySymbolicVariableConstraintSolver solver = new FuzzySymbolicVariableConstraintSolver();
		Variable[] vars = solver.createVariables(3);
		
		FuzzySymbolicVariable var0 = (FuzzySymbolicVariable)vars[0];
		var0.setDomain(new String[] {"A", "B", "C"}, new double[] {0.1,0.8,1.0});
		
		FuzzySymbolicVariable var1 = (FuzzySymbolicVariable)vars[1];
		var1.setDomain(new String[] {"A", "B", "C"}, new double[] {0.5,0.1,0.2});

		FuzzySymbolicVariable var2 = (FuzzySymbolicVariable)vars[2];
		var2.setDomain(new String[] {"A", "B", "C"}, new double[] {0.9,0.3,0.1});

		ConstraintNetwork.draw(solver.getConstraintNetwork());
		
		SymbolicValueConstraint con1 = new SymbolicValueConstraint(SymbolicValueConstraint.Type.EQUALS);
		con1.setFrom(var0);
		con1.setTo(var1);
//		solver.addConstraint(con1);
		
		SymbolicValueConstraint con2 = new SymbolicValueConstraint(SymbolicValueConstraint.Type.EQUALS);
		con2.setFrom(var1);
		con2.setTo(var2);
//		solver.addConstraint(con2);

		SymbolicValueConstraint[] cons = new SymbolicValueConstraint[] {con1,con2};
		
		/*
Value Possibility: 0.2
Value Possibility: 0.1
Value Possibility: 0.5
Value Possibility: 1.0
		 */
		while (true) {
			for (SymbolicValueConstraint con : cons) {
				solver.addConstraint(con);
				System.out.println("Value Possibility: " + solver.getUpperBound());
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			for (SymbolicValueConstraint con : cons) {
				solver.removeConstraint(con);
				System.out.println("Value Possibility: " + solver.getUpperBound());
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

}
