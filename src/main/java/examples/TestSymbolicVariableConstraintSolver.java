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
package examples;

import symbols.SymbolicValueConstraint;
import symbols.SymbolicVariable;
import symbols.SymbolicVariableConstraintSolver;
import symbols.SymbolicVariableNetwork;
import framework.Variable;

public class TestSymbolicVariableConstraintSolver {
	
	public static void main(String[] args) {
		SymbolicVariableConstraintSolver solver = new SymbolicVariableConstraintSolver();
		Variable[] vars = solver.createVariables(3);
		
		SymbolicVariable var0 = (SymbolicVariable)vars[0];
		var0.setDomain("A", "B", "C");
		
		SymbolicVariable var1 = (SymbolicVariable)vars[1];
		var1.setDomain("G", "B", "C", "D");

		SymbolicVariable var2 = (SymbolicVariable)vars[2];
		var2.setDomain("alpha", "beta", "gamma");

		SymbolicVariableNetwork.draw(solver.getConstraintNetwork());
				
		SymbolicValueConstraint con1 = new SymbolicValueConstraint(SymbolicValueConstraint.Type.EQUALS);
		con1.setFrom(var0);
		con1.setTo(var1);
		//solver.addConstraint(con1);
		
		SymbolicValueConstraint con2 = new SymbolicValueConstraint(SymbolicValueConstraint.Type.UNARYEQUALS);
		con2.setFrom(var1);
		con2.setTo(var1);
		con2.setUnaryValue("C");
		//solver.addConstraint(con2);
	
		SymbolicValueConstraint con3 = new SymbolicValueConstraint(SymbolicValueConstraint.Type.DIFFERENT);
		con3.setFrom(var1);
		con3.setTo(var2);
//		if (!solver.addConstraint(con3)) System.out.println("NO SOLUTION!");
		
		SymbolicValueConstraint[] cons = new SymbolicValueConstraint[] {con1, con2, con3};
		
		while(true) {
			for (SymbolicValueConstraint con : cons) {
				solver.addConstraint(con);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			for (SymbolicValueConstraint con : cons) {
				solver.removeConstraint(con);
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
