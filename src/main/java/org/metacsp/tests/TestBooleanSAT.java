package org.metacsp.tests;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;
import org.metacsp.meta.symbolsAndTime.Scheduler;
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenInterval;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.allenInterval.AllenIntervalNetworkSolver;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;
import org.metacsp.booleanSAT.BooleanConstraint;
import org.metacsp.booleanSAT.BooleanDomain;
import org.metacsp.booleanSAT.BooleanSatisfiabilitySolver;
import org.metacsp.booleanSAT.BooleanVariable;
import org.metacsp.examples.TestBooleanSatisfiabilitySolverSATAlternative1;
import org.metacsp.examples.TestBooleanSatisfiabilitySolverSATNonCNF;
import org.metacsp.examples.TestBooleanSatisfiabilitySolverUNSAT;
import org.metacsp.examples.TestBooleanSatisfiabilitySolverUNSATNonCNF;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;

public class TestBooleanSAT extends TestCase {
	
	@Override
	public void setUp() throws Exception {
		MetaCSPLogging.setLevel(Level.OFF);
	}

	@Override
	public void tearDown() throws Exception {
	}

	public void testBooleanSATResult() {
		BooleanSatisfiabilitySolver solver = new BooleanSatisfiabilitySolver(10, 10);
		
		//(~x1 v x2 v x4) ^ (x1 v ~x2 v x3) ^ (x1 v x2 v ~x4) ^ (x2 v x3) ^
		//(x1 v x2 v ~x3 v x4) ^ (x1 v ~x2 v ~x3 v x4) ^ (x1 v ~x2 v ~x3 v ~x4) ^
		//(~x1 v ~x2 v ~x3 v x4) ^ (~x1 v x2 v ~x3 v ~x4) ^ (~x1 v ~x2 v ~x3 v ~x4)
		BooleanVariable[] vars = (BooleanVariable[])solver.createVariables(4);
		BooleanConstraint clause1 = new BooleanConstraint(new BooleanVariable[] {vars[0],vars[1],vars[3]}, new boolean[] {false,true,true});
		BooleanConstraint clause2 = new BooleanConstraint(new BooleanVariable[] {vars[0],vars[1],vars[2]}, new boolean[] {true,false,true});
		BooleanConstraint clause3 = new BooleanConstraint(new BooleanVariable[] {vars[0],vars[1],vars[3]}, new boolean[] {true,true,false});
		BooleanConstraint clause4 = new BooleanConstraint(new BooleanVariable[] {vars[1],vars[2]}, new boolean[] {true,true});

		BooleanConstraint clause5 = new BooleanConstraint(new BooleanVariable[] {vars[0],vars[1],vars[2],vars[3]}, new boolean[] {true,true,false,true});
		BooleanConstraint clause6 = new BooleanConstraint(new BooleanVariable[] {vars[0],vars[1],vars[2],vars[3]}, new boolean[] {true,false,false,true});
		BooleanConstraint clause7 = new BooleanConstraint(new BooleanVariable[] {vars[0],vars[1],vars[2],vars[3]}, new boolean[] {true,false,false,false});

		BooleanConstraint clause8 = new BooleanConstraint(new BooleanVariable[] {vars[0],vars[1],vars[2],vars[3]}, new boolean[] {false,false,false,true});
		BooleanConstraint clause9 = new BooleanConstraint(new BooleanVariable[] {vars[0],vars[1],vars[2],vars[3]}, new boolean[] {false,true,false,false});
		BooleanConstraint clause10 = new BooleanConstraint(new BooleanVariable[] {vars[0],vars[1],vars[2],vars[3]}, new boolean[] {false,false,false,false});

		assertTrue(solver.addConstraints(new BooleanConstraint[] {clause1,clause2,clause3,clause4,clause5,clause6,clause7,clause8,clause9,clause10}));
		assertTrue(((BooleanDomain)vars[0].getDomain()).canBeTrue() && !((BooleanDomain)vars[0].getDomain()).canBeFalse());
		assertTrue(((BooleanDomain)vars[1].getDomain()).canBeTrue() && !((BooleanDomain)vars[1].getDomain()).canBeFalse());
		assertTrue(!((BooleanDomain)vars[2].getDomain()).canBeTrue() && ((BooleanDomain)vars[2].getDomain()).canBeFalse());
		assertTrue(((BooleanDomain)vars[3].getDomain()).canBeTrue() && ((BooleanDomain)vars[3].getDomain()).canBeFalse());
	}
	
	public void testBooleanSATResultWithWFF() {
		BooleanSatisfiabilitySolver solver = new BooleanSatisfiabilitySolver(10, 10);
		BooleanVariable[] vars = (BooleanVariable[])solver.createVariables(4);
		String wff = "((x1 <-> ((x2 v ~x3) ^ ~(x2 v ~x3))) ^ (x4 v ~x4))";
		BooleanConstraint[] cons = BooleanConstraint.createBooleanConstraints(vars, wff);

		assertTrue(solver.addConstraints(cons));
		assertTrue(!((BooleanDomain)vars[0].getDomain()).canBeTrue() && ((BooleanDomain)vars[0].getDomain()).canBeFalse());
		assertTrue(((BooleanDomain)vars[1].getDomain()).canBeTrue() && !((BooleanDomain)vars[1].getDomain()).canBeFalse());
		assertTrue(!((BooleanDomain)vars[2].getDomain()).canBeTrue() && ((BooleanDomain)vars[2].getDomain()).canBeFalse());
		assertTrue(((BooleanDomain)vars[3].getDomain()).canBeTrue() && ((BooleanDomain)vars[3].getDomain()).canBeFalse());		
	}
	
	public void testBooleanSATAndUNSATResult() {
		BooleanSatisfiabilitySolver solver = new BooleanSatisfiabilitySolver(10, 10);
		
		//(~x1 v x2) ^ (x1 v x2) ^ (x1 v ~x2) ^ (~x1 v ~x2)
		BooleanVariable[] vars = (BooleanVariable[])solver.createVariables(2);
		BooleanConstraint clause1 = new BooleanConstraint(new BooleanVariable[] {vars[0],vars[1]}, new boolean[] {false,true});
		BooleanConstraint clause2 = new BooleanConstraint(new BooleanVariable[] {vars[0],vars[1]}, new boolean[] {true,true});
		BooleanConstraint clause3 = new BooleanConstraint(new BooleanVariable[] {vars[0],vars[1]}, new boolean[] {true,false});
		BooleanConstraint clause4 = new BooleanConstraint(new BooleanVariable[] {vars[0],vars[1]}, new boolean[] {false,false});
		
		//This will succeed
		assertTrue(solver.addConstraints(new BooleanConstraint[] {clause1,clause2,clause3}));
		//This will fail
		assertFalse(solver.addConstraint(clause4));
		solver.removeConstraint(clause3);
		//(~x1 v x3) ^ (x2 v ~x3)
		BooleanVariable[] newVars = (BooleanVariable[])solver.createVariables(1);
		BooleanConstraint clause5 = new BooleanConstraint(new BooleanVariable[] {vars[0],newVars[0]}, new boolean[] {false,false});
		BooleanConstraint clause6 = new BooleanConstraint(new BooleanVariable[] {vars[1],newVars[0]}, new boolean[] {true,false});
		//This will succeed
		assertTrue(solver.addConstraints(new BooleanConstraint[] {clause5,clause6}));
		solver.removeConstraints(new BooleanConstraint[] {clause5,clause6});
		solver.removeVariable(newVars[0]);
		assertTrue(((BooleanDomain)vars[0].getDomain()).canBeTrue() && ((BooleanDomain)vars[0].getDomain()).canBeFalse());
		assertTrue(((BooleanDomain)vars[1].getDomain()).canBeTrue() && !((BooleanDomain)vars[1].getDomain()).canBeFalse());
	}
	
	public void testBooleanUNSATResultWithWFF() {
		BooleanSatisfiabilitySolver solver = new BooleanSatisfiabilitySolver(10, 10);
		BooleanVariable[] vars = (BooleanVariable[])solver.createVariables(3);
		String wff = "((x1 <-> ((x2 v ~x3) ^ ~(x2 v ~x3))) ^ (x1))";
		BooleanConstraint[] cons = BooleanConstraint.createBooleanConstraints(vars, wff);
		assertFalse(solver.addConstraints(cons));
	}
	
}