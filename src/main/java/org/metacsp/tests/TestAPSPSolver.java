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
import org.metacsp.time.SimpleDistanceConstraint;
import org.metacsp.time.TimePoint;
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
import org.metacsp.framework.Variable;

public class TestAPSPSolver extends TestCase {
	
	@Override
	public void setUp() throws Exception {
		MetaCSPLogging.setLevel(Level.OFF);
	}

	@Override
	public void tearDown() throws Exception {
	}

	public void testBoundsAfterPropagation() {
		APSPSolver solver = new APSPSolver(100, 500);
		Variable[] vars = solver.createVariables(3);
		Variable one = vars[0];
		Variable two = vars[1];
		Variable three = vars[2];

		SimpleDistanceConstraint con1 = new SimpleDistanceConstraint();
		con1.setFrom(solver.getVariable(0));
		con1.setTo(one);
		con1.setMinimum(60);
		con1.setMaximum(75);
		
		SimpleDistanceConstraint con2 = new SimpleDistanceConstraint();
		con2.setFrom(one);
		con2.setTo(two);
		con2.setMinimum(7);
		con2.setMaximum(9);
		
		SimpleDistanceConstraint con3 = new SimpleDistanceConstraint();
		con3.setFrom(solver.getVariable(0));
		con3.setTo(two);
		con3.setMinimum(68);
		con3.setMaximum(70);

		assertTrue(solver.addConstraints(new SimpleDistanceConstraint[] {con1,con2,con3}));
				
		SimpleDistanceConstraint con4 = new SimpleDistanceConstraint();
		con4.setFrom(two);
		con4.setTo(three);
		con4.setMinimum(56);
		con4.setMaximum(100);

		assertTrue(solver.addConstraint(con4));

		SimpleDistanceConstraint con5 = new SimpleDistanceConstraint();
		con5.setFrom(one);
		con5.setTo(three);
		con5.setMinimum(70);
		con5.setMaximum(100);
		
		assertTrue(solver.addConstraint(con5));
		
		assertTrue((Long)((TimePoint)one).getDomain().chooseValue("ET") == 160);
		assertTrue((Long)((TimePoint)one).getDomain().chooseValue("LT") == 163);
		assertTrue((Long)((TimePoint)two).getDomain().chooseValue("ET") == 168);
		assertTrue((Long)((TimePoint)two).getDomain().chooseValue("LT") == 170);
		assertTrue((Long)((TimePoint)three).getDomain().chooseValue("ET") == 230);
		assertTrue((Long)((TimePoint)three).getDomain().chooseValue("LT") == 263);
		
	}
	
	public void testInconsistency() {
		APSPSolver solver = new APSPSolver(100, 500);
		Variable[] vars = solver.createVariables(3);
		Variable one = vars[0];
		Variable two = vars[1];
		Variable three = vars[2];

		SimpleDistanceConstraint con1 = new SimpleDistanceConstraint();
		con1.setFrom(one);
		con1.setTo(two);
		con1.setMinimum(5);
		con1.setMaximum(100);

		SimpleDistanceConstraint con2 = new SimpleDistanceConstraint();
		con2.setFrom(two);
		con2.setTo(three);
		con2.setMinimum(5);
		con2.setMaximum(100);

		assertTrue(solver.addConstraints(con1,con2));
		
		SimpleDistanceConstraint con3 = new SimpleDistanceConstraint();
		con3.setFrom(three);
		con3.setTo(one);
		con3.setMinimum(5);
		con3.setMaximum(100);

		assertFalse(solver.addConstraints(con3));
		
		SimpleDistanceConstraint con4 = new SimpleDistanceConstraint();
		con4.setFrom(one);
		con4.setTo(three);
		con4.setMinimum(5);
		con4.setMaximum(100);

		assertTrue(solver.addConstraints(con4));
	}
	
}