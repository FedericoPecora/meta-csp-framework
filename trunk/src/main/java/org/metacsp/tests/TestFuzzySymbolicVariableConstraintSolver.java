package org.metacsp.tests;

import java.util.logging.Level;

import junit.framework.TestCase;

import org.metacsp.framework.Variable;
import org.metacsp.fuzzySymbols.FuzzySymbolicVariable;
import org.metacsp.fuzzySymbols.FuzzySymbolicVariableConstraintSolver;
import org.metacsp.multi.symbols.SymbolicValueConstraint;
import org.metacsp.utility.logging.MetaCSPLogging;

public class TestFuzzySymbolicVariableConstraintSolver extends TestCase {
	
	@Override
	public void setUp() throws Exception {
		MetaCSPLogging.setLevel(Level.OFF);
	}

	@Override
	public void tearDown() throws Exception {
	}

	public void testValuePossibility() {
		FuzzySymbolicVariableConstraintSolver solver = new FuzzySymbolicVariableConstraintSolver();
		Variable[] vars = solver.createVariables(3);
		
		FuzzySymbolicVariable var0 = (FuzzySymbolicVariable)vars[0];
		var0.setDomain(new String[] {"A", "B", "C"}, new double[] {0.1,0.8,1.0});
		
		FuzzySymbolicVariable var1 = (FuzzySymbolicVariable)vars[1];
		var1.setDomain(new String[] {"A", "B", "C"}, new double[] {0.5,0.1,0.2});

		FuzzySymbolicVariable var2 = (FuzzySymbolicVariable)vars[2];
		var2.setDomain(new String[] {"A", "B", "C"}, new double[] {0.9,0.3,0.1});

		assertTrue(solver.getUpperBound() == 1.0);
		
		SymbolicValueConstraint con1 = new SymbolicValueConstraint(SymbolicValueConstraint.Type.EQUALS);
		con1.setFrom(var0);
		con1.setTo(var1);
		assertTrue(solver.addConstraint(con1));
		assertTrue(solver.getUpperBound() == 0.2);
		
		SymbolicValueConstraint con2 = new SymbolicValueConstraint(SymbolicValueConstraint.Type.EQUALS);
		con2.setFrom(var1);
		con2.setTo(var2);
		assertTrue(solver.addConstraint(con2));
		assertTrue(solver.getUpperBound() == 0.1);
		
		solver.removeConstraint(con1);
		assertTrue(solver.getUpperBound() == 0.5);

		solver.removeConstraint(con2);
		assertTrue(solver.getUpperBound() == 1.0);
	}
	
}