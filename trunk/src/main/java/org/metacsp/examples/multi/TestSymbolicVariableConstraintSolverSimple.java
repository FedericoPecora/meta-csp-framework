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

import java.util.logging.Logger;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.multi.symbols.SymbolicValueConstraint;
import org.metacsp.multi.symbols.SymbolicValueConstraint.Type;
import org.metacsp.multi.symbols.SymbolicVariable;
import org.metacsp.multi.symbols.SymbolicVariableConstraintSolver;
import org.metacsp.utility.logging.MetaCSPLogging;

import cern.colt.Arrays;

public class TestSymbolicVariableConstraintSolverSimple {
	
	public static void main(String[] args) {
		Logger logger = MetaCSPLogging.getLogger(TestSymbolicVariableConstraintSolverSimple.class);
		
		String[] symbols = new String[] {"A","B","C","D","E","F","G"};
		SymbolicVariableConstraintSolver solver = new SymbolicVariableConstraintSolver(symbols, 100);
		Variable[] vars = solver.createVariables(3);

		ConstraintNetwork.draw(solver.getConstraintNetwork());

		logger.info("Created variables " + Arrays.toString(vars));

		
		((SymbolicVariable)vars[0]).setDomain(new String[] {"B", "D", "F"});

//		BooleanVariable bv = (BooleanVariable)solver.getConstraintSolvers()[0].getConstraintNetwork().getVariable(1);
//
//		ConstraintNetwork.draw(solver.getConstraintNetwork());
//		System.out.println("x1: " + ((BooleanDomain)bv.getDomain()).canBeTrue());
		
//		try { Thread.sleep(2000); }
//		catch (InterruptedException e) { e.printStackTrace(); }
		
		SymbolicValueConstraint con1 = new SymbolicValueConstraint(Type.EQUALS);
		con1.setFrom(vars[0]);
		con1.setTo(vars[1]);
		logger.info("Added con1? " + solver.addConstraint(con1));
		
//		System.out.println("x1: " + ((BooleanDomain)bv.getDomain()).canBeTrue());
//		
//		logger.info("Removing con1");
//		solver.removeConstraint(con1);
		
		System.out.println("Pausing...");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		logger.info("Removing variable " + vars[2]);
		solver.removeVariable(vars[2]);
		logger.info("Done!");

		System.out.println("Pausing...");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		logger.info("Removing constraint " + con1);
		solver.removeConstraint(con1);
		logger.info("Done!");

	}

}
