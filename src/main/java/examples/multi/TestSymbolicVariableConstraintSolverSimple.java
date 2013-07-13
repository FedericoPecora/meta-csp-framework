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

import java.util.logging.Logger;

import booleanSAT.BooleanDomain;
import booleanSAT.BooleanVariable;

import cern.colt.Arrays;

import utility.logging.MetaCSPLogging;
import multi.symbols.SymbolicValueConstraint;
import multi.symbols.SymbolicVariable;
import multi.symbols.SymbolicVariableConstraintSolver;
import multi.symbols.SymbolicValueConstraint.Type;
import framework.ConstraintNetwork;
import framework.Variable;

public class TestSymbolicVariableConstraintSolverSimple {
	
	public static void main(String[] args) {
		Logger logger = MetaCSPLogging.getLogger(TestSymbolicVariableConstraintSolverSimple.class);
		
		String[] symbols = new String[] {"A","B","C","D","E","F","G"};
		SymbolicVariableConstraintSolver solver = new SymbolicVariableConstraintSolver(symbols, 100);
		Variable[] vars = solver.createVariables(2);
		
		((SymbolicVariable)vars[0]).setDomain(new String[] {"B", "D", "F"});
		BooleanVariable bv = (BooleanVariable)solver.getConstraintSolvers()[0].getConstraintNetwork().getVariable(1);

		ConstraintNetwork.draw(solver.getConstraintNetwork());
		System.out.println("x1: " + ((BooleanDomain)bv.getDomain()).canBeTrue());
		
		try { Thread.sleep(2000); }
		catch (InterruptedException e) { e.printStackTrace(); }
		
		SymbolicValueConstraint con1 = new SymbolicValueConstraint(Type.EQUALS);
		con1.setFrom(vars[0]);
		con1.setTo(vars[1]);

		logger.info("Added con1? " + solver.addConstraint(con1));
		System.out.println("x1: " + ((BooleanDomain)bv.getDomain()).canBeTrue());

	}

}
