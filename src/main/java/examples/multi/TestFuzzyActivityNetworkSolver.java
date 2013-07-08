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
package examples.multi;

import multi.fuzzyActivity.FuzzyActivity;
import multi.fuzzyActivity.FuzzyActivityNetworkSolver;
import symbols.SymbolicValueConstraint;
import framework.Constraint;
import framework.ConstraintNetwork;
import fuzzyAllenInterval.FuzzyAllenIntervalConstraint;

public class TestFuzzyActivityNetworkSolver {
	
	
	public static void main(String[] args) {
		FuzzyActivityNetworkSolver solver = new FuzzyActivityNetworkSolver();

		FuzzyActivity act1 = (FuzzyActivity)solver.createVariable();
		act1.setDomain(new String[] {"A", "B", "C"}, new double[] {0.1,0.4,0.8});
		
		FuzzyActivity act2 = (FuzzyActivity)solver.createVariable();
		act2.setDomain(new String[] {"A", "B", "C"}, new double[] {0.8,0.2,0.7});

		ConstraintNetwork.draw(solver.getConstraintNetwork());
		
		SymbolicValueConstraint con1 = new SymbolicValueConstraint(SymbolicValueConstraint.Type.EQUALS);
		con1.setFrom(act1);
		con1.setTo(act2);
		//solver.addConstraint(con1);
		
		FuzzyAllenIntervalConstraint con2 = new FuzzyAllenIntervalConstraint(FuzzyAllenIntervalConstraint.Type.Before);
		con2.setFrom(act1);
		con2.setTo(act2);
		//solver.addConstraint(con2);
		
		Constraint[] cons = new Constraint[]{con1,con2};
		solver.addConstraints(cons);
				
		System.out.println(solver.getDescription());
		System.out.println(act1.getDescription());

		System.out.println("---------------------------------");
		System.out.println("Temporal Possibility:" + solver.getTemporalConsistency());
		System.out.println("Value Possibility:" + solver.getValueConsistency());
		System.out.println("---------------------------------");

		boolean add = false;
		
		while (true) {
			for (int i = 0; i < cons.length; i++) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (add) {
					solver.addConstraint(cons[i]);
					System.out.println("Added " + cons[i].getClass().getSimpleName() + " type");
				}
				else {
					solver.removeConstraint(cons[i]);
					System.out.println("Removed " + cons[i].getClass().getSimpleName() + " type");
				}
				System.out.println("Temporal Possibility:" + solver.getTemporalConsistency());
				System.out.println("Value Possibility:" + solver.getValueConsistency());
				System.out.println("---------------------------------");
			}
			add = !add;
		}
		

		
	}

}
