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
import org.metacsp.throwables.NoSymbolsException;
import org.metacsp.utility.logging.MetaCSPLogging;

public class TestSymbolicVariableConstraintSolverNoReasoing {
	
	public static void main(String[] args) {
		Logger logger = MetaCSPLogging.getLogger(TestSymbolicVariableConstraintSolverNoReasoing.class);
		
		SymbolicVariableConstraintSolver solver = new SymbolicVariableConstraintSolver();
		Variable[] vars = solver.createVariables(2);
		
		((SymbolicVariable)vars[0]).setDomain(new String[] {"A","B","C","D"});

		((SymbolicVariable)vars[1]).setDomain(new String[] {"alpha","beta","gamma","delta"});

		ConstraintNetwork.draw(solver.getConstraintNetwork());
		
		SymbolicValueConstraint con = new SymbolicValueConstraint(Type.EQUALS);
		con.setFrom(vars[0]);
		con.setTo(vars[1]);
		
		try { solver.addConstraint(con); }
		catch (NoSymbolsException e) { logger.info(e.getMessage()); }

		
	}

}
